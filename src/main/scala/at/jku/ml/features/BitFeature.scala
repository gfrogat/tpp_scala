package at.jku.ml.features

import org.apache.spark.sql.types._
import org.openscience.cdk.DefaultChemObjectBuilder
import org.openscience.cdk.fingerprint.{IFingerprinter, PubchemFingerprinter}
import org.openscience.cdk.interfaces.{IAtomContainer, IChemObjectBuilder}

import scala.util.{Success, Try}

trait BitFeature extends Feature {
  val featureName: String
  val fpType: String
  val fp: IFingerprinter

  override def getSchema: Seq[StructField] = {
    val schema: Seq[StructField] = Seq(
      StructField(
        featureName,
        ArrayType(IntegerType, containsNull = true),
        nullable = true
      )
    )
    schema
  }

  def initializePubchemfingerprinter(): IFingerprinter = {
    val builder: IChemObjectBuilder = DefaultChemObjectBuilder.getInstance
    new PubchemFingerprinter(builder)
  }

  def initializeFeature(): IFingerprinter = {
    fpType match {
      case "PubChemFP" => initializePubchemfingerprinter()
      case _           => throw new IllegalArgumentException("fpType is not supported")
    }
  }

  def computeBitFeature(molecule: IAtomContainer): Try[Seq[Int]] = Try {
    val features: Seq[Int] =
      fp.getBitFingerprint(molecule).getSetbits
    features
  }

  def computeFeature(molecule: IAtomContainer): Seq[Int] = {
    computeBitFeature(molecule) match {
      case Success(features) => features
      case _                 => null
    }
  }
}
