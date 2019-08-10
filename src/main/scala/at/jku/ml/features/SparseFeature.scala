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

trait SparseFeature extends Feature {
  val featureName: String
  val fp: EncodingFingerprint

  override def getSchema(): Seq[StructField] = {
    val schema = Seq(
      StructField(featureName, ArrayType(StringType, true), true)
    )
    schema
  }

  def initializeFeature(fpName: String): EncodingFingerprint = {
    val fingerprintType: FingerprintType = FingerprintType.valueOf(fpName)
    FingerPrinterFactory.getFingerprinter(fingerprintType)
  }

  def computeStringFeature(molecule: IAtomContainer): Try[Seq[String]] = Try {
    val features: mutable.Buffer[IFeature] =
      fp.getFingerprint(molecule).asScala
    features.map { feature: IFeature =>
      feature.featureToString(true)
    }
  }

  def computeFeature(molecule: IAtomContainer): Seq[String] = {
    computeStringFeature(molecule) match {
      case Success(features) => features
      case _                 => null
    }
  }
}
