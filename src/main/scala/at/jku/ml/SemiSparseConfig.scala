package at.jku.ml

import org.apache.spark.sql.types._
import at.jku.ml.features.{Feature, BitFeature, FrequencyFeature}
import org.openscience.cdk.fingerprint.IFingerprinter
import de.zbit.jcmapper.fingerprinters.EncodingFingerprint
import de.zbit.jcmapper.fingerprinters.topological.Encoding2D
import de.zbit.jcmapper.tools.moltyping.enumerations.EnumerationsAtomTypes.AtomLabelType

object SemiSparseConfig {

  object PubChemFP extends BitFeature {
    override val featureName: String = "PubChem"

    // PubChemFingerprinter does not take any arguments
    val fpType: String = "PubChemFP"
    override lazy val fp: IFingerprinter = initializeFeature(fpType)
  }

  object CATS2D extends FrequencyFeature {
    override val featureName: String = "CATS2D"

    // Fingerprinting Algorithm -- (flag: -c)
    val fpType: String = "CATS2D"
    override lazy val fp: EncodingFingerprint = initializeFeature(fpType)

    // Atom Type -- (flag: -a)
    val atomType: String = "ELEMENT_SYMBOL"
    val atomLabelType: AtomLabelType = AtomLabelType.valueOf(atomType)
    fp.setAtomLabelType(atomLabelType)

    // Distance Cutoff / Search Depth -- (flag: -d)
    val searchDepth: Int = 9
    fp.asInstanceOf[Encoding2D].setSearchDepth(searchDepth)
  }

  object SHED extends FrequencyFeature {
    override val featureName: String = "SHED"

    // Fingerprinting Algorithm -- (flag: -c)
    val fpType: String = "SHED"
    override lazy val fp: EncodingFingerprint = initializeFeature(fpType)

    // Atom Type -- (flag: -a)
    val atomType: String = "ELEMENT_SYMBOL"
    val atomLabelType: AtomLabelType = AtomLabelType.valueOf(atomType)
    fp.setAtomLabelType(atomLabelType)

    // Distance Cutoff / Search Depth -- (flag: -d)
    // SHED does not utilize search depth argument
  }

  def getFeatures(): Seq[Feature] = {
    getBitFeatures() ++ getFrequencyFeatures()
  }

  def getBitFeatures(): Seq[BitFeature] = {
    PubChemFP :: Nil
  }

  def getFrequencyFeatures(): Seq[FrequencyFeature] = {
    CATS2D :: SHED :: Nil
  }

  def getSchema(): StructType = {
    val fields: Seq[StructField] = getFeatures().flatMap { feature =>
      feature.getSchema()
    }
    val schema = StructType(fields)
    schema
  }
}
