package at.jku.ml

import at.jku.ml.features.SparseFeature

import org.apache.spark.sql.Row
import org.apache.spark.sql.functions._

object FeatureCalculator {

  val calculateSparse = udf(
    (molFile: String) => {
      val molecule = MolFileReader.parseMolFile(molFile)
      val sparseFeatures = Config.getSparseFeatures()
      val features: Seq[Seq[String]] = sparseFeatures
        .map { sparseFeature: SparseFeature =>
          sparseFeature.computeFeature(molecule)
        }

      val row = Row(features: _*)
      row
    },
    Config.getSparseSchema()
  )
}
