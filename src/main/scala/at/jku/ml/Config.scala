package at.jku.ml

import at.jku.ml.features.SparseFeature
import org.apache.spark.sql.types._
import de.zbit.jcmapper.fingerprinters.EncodingFingerprint
import de.zbit.jcmapper.fingerprinters.topological.Encoding2D
import de.zbit.jcmapper.tools.moltyping.enumerations.EnumerationsAtomTypes.AtomLabelType

object Config {
  object ECFC4 extends SparseFeature {
    override val featureName: String = "ECFC4"

    // Fingerprinting Algorithm -- (flag: -c)
    val fpType: String = "ECFC"
    override lazy val fp: EncodingFingerprint = initializeFeature(fpType)

    // Atom Type -- (flag: -a)
    val atomType: String = "DAYLIGHT_INVARIANT_RING"
    val atomLabelType: AtomLabelType = AtomLabelType.valueOf(atomType)
    fp.setAtomLabelType(atomLabelType)

    // Distance Cutoff / Search Depth -- (flag: -d)
    val searchDepth: Int = 2
    fp.asInstanceOf[Encoding2D].setSearchDepth(searchDepth)
  }

  object DFS8 extends SparseFeature {
    override val featureName: String = "DFS8"

    // Fingerprinting Algorithm -- (flag: -c)
    val fpType: String = "DFS"
    override lazy val fp: EncodingFingerprint = initializeFeature(fpType)

    // Atom Type -- (flag: -a)
    val atomType: String = "ELEMENT_SYMBOL"
    val atomLabelType: AtomLabelType = AtomLabelType.valueOf(atomType)
    fp.setAtomLabelType(atomLabelType)

    // Distance Cutoff / Search Depth -- (flag: -d)
    val searchDepth: Int = 8
    fp.asInstanceOf[Encoding2D].setSearchDepth(searchDepth)
  }

  object ECFC6 extends SparseFeature {
    override val featureName: String = "ECFC6"

    // Fingerprinting Algorithm -- (flag: -c)
    val fpType: String = "ECFC"
    override lazy val fp: EncodingFingerprint = initializeFeature(fpType)

    // Atom Type -- (flag: -a)
    val atomType: String = "ELEMENT_SYMBOL"
    val atomLabelType: AtomLabelType = AtomLabelType.valueOf(atomType)
    fp.setAtomLabelType(atomLabelType)

    // Distance Cutoff / Search Depth -- (flag: -d)
    val searchDepth: Int = 3
    fp.asInstanceOf[Encoding2D].setSearchDepth(searchDepth)
  }

  def getSparseFeatures(): Seq[SparseFeature] = {
    Config.ECFC4 :: Config.DFS8 :: Config.ECFC6 :: Nil
  }

  def getSparseSchema(): StructType = {
    val fields: Seq[StructField] = getSparseFeatures().flatMap {
      sparseFeature =>
        sparseFeature.getSchema()
    }
    val schema = StructType(fields)
    schema
  }
}
