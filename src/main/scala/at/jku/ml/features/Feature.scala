package at.jku.ml.features

import org.apache.spark.sql.types._
import org.openscience.cdk.interfaces.IAtomContainer

trait Feature {
  def getSchema(): Seq[StructField]
}
