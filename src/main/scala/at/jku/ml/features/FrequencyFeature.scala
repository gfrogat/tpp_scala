package at.jku.ml.features

import de.zbit.jcmapper.fingerprinters.FingerPrinterFactory.FingerprintType
import de.zbit.jcmapper.fingerprinters.features.IFeature
import de.zbit.jcmapper.fingerprinters.{
  EncodingFingerprint,
  FingerPrinterFactory
}
import org.apache.spark.sql.types._
import org.openscience.cdk.interfaces.IAtomContainer

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.util.{Success, Try}

trait FrequencyFeature extends Feature {
  val featureName: String
  val fp: EncodingFingerprint

  override def getSchema(): Seq[StructField] = {
    val schema = Seq(
      StructField(
        featureName.concat("_index"),
        ArrayType(StringType, true),
        true
      ),
      StructField(
        featureName
          .concat("_freq"),
        ArrayType(DoubleType, true),
        true
      )
    )
    schema
  }

  def initializeFingerprinter(fpType: String): EncodingFingerprint = {
    val fingerprintType: FingerprintType = FingerprintType.valueOf(fpType)
    FingerPrinterFactory.getFingerprinter(fingerprintType)
  }

  def initializeFeature(fpType: String): EncodingFingerprint = {
    fpType match {
      case "CATS2D" => initializeFingerprinter("CATS2D")
      case "SHED"   => initializeFingerprinter("SHED")
      case _        => throw new IllegalArgumentException("fpType is not supported")
    }
  }

  def computeFrequencyFeature(
      molecule: IAtomContainer
  ): Try[(Seq[String], Seq[Double])] =
    Try {
      val features: mutable.Buffer[IFeature] =
        fp.getFingerprint(molecule).asScala
      val combinedFeatures: Seq[(String, Double)] = features
        .map { feature: IFeature =>
          (feature.featureToString(true), feature.getValue())
        }
        .filter {
          case (_, num) => num != 0.0
        }
      combinedFeatures.unzip
    }

  def computeFeature(molecule: IAtomContainer): (Seq[String], Seq[Double]) = {
    computeFrequencyFeature(molecule) match {
      case Success(features) => features
      case _                 => (null, null)
    }
  }
}
