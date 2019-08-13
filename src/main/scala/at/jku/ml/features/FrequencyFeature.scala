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
  val fpType: String
  val fp: EncodingFingerprint

  override def getSchema: Seq[StructField] = {
    val schema: Seq[StructField] = Seq(
      StructField(
        featureName,
        MapType(StringType, DoubleType, valueContainsNull = false),
        nullable = true
      )
    )
    schema
  }

  def initializeFingerprinter(fpType: String): EncodingFingerprint = {
    val fingerprintType: FingerprintType = FingerprintType.valueOf(fpType)
    FingerPrinterFactory.getFingerprinter(fingerprintType)
  }

  def initializeFeature(): EncodingFingerprint = {
    fpType match {
      case "ECFC"   => initializeFingerprinter("ECFC")
      case "DFS"    => initializeFingerprinter("DFS")
      case "ECFP"   => initializeFingerprinter("ECFP")
      case "CATS2D" => initializeFingerprinter("CATS2D")
      case "SHED"   => initializeFingerprinter("SHED")
      case _        => throw new IllegalArgumentException("fpType is not supported")
    }
  }

  def computeFrequencyFeature(
      molecule: IAtomContainer
  ): Try[Map[String, Double]] =
    Try {
      val features: mutable.Buffer[IFeature] =
        fp.getFingerprint(molecule).asScala

      fpType match {
        case "ECFC" | "DFS" | "ECFP" => {
          features
            .map { feature: IFeature =>
              feature.featureToString(true)
            }
            .groupBy(identity)
            .mapValues(_.size.toDouble)
        }
        case "SHED" | "CATS2D" => {
          features
            .map { feature: IFeature =>
              (feature.featureToString(true), feature.getValue)
            }
            .filter {
              case (_, num) => num != 0.0
            }
            .toMap
        }
      }
    }

  def computeFeature(molecule: IAtomContainer): Map[String, Double] = {
    computeFrequencyFeature(molecule) match {
      case Success(features) => features
      case _                 => null
    }
  }
}
