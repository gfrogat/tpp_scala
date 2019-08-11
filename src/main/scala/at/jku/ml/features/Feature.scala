package at.jku.ml.features

import org.apache.spark.sql.types._

trait Feature {
  def getSchema: Seq[StructField]
}
