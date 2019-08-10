package at.jku.ml

import at.jku.ml.features.SparseFeature

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Row
import org.apache.spark.sql.functions._

import at.jku.ml.features.{Feature, BitFeature, FrequencyFeature, SparseFeature}
import at.jku.ml.features.BitFeature
import java.io.BufferedInputStream
import java.io.FileInputStream

case class Config(
    input: String = null,
    output: String = null
)

object SparseFeatureCalculator {

  /*
  val calculateSparse = udf(
    (molFile: String) => {
      val molecule = MolFileReader.parseMolFile(molFile)
      val sparseFeatures = SparseConfig.getSparseFeatures()
      val features: Seq[Seq[String]] = sparseFeatures
        .map { sparseFeature: SparseFeature =>
          sparseFeature.computeFeature(molecule)
        }

      val row = Row(features: _*)
      row
    },
    SparseConfig.getSparseSchema()
  )
   */

  def main(args: Array[String]): Unit = {

    val source = scala.io.Source
      .fromFile("/Users/gfrogat/Downloads/Structure2D_CID_18636.sdf")
    val molFile = source.getLines mkString "\n"
    println(molFile)
    /*
      """
        |CWRITER308281317412D
        |Created with ChemWriter - http://chemwriter.com
        |  2  1  0  0  0  0  0  0  0  0999 V2000
        |   67.4074  -47.2503    0.0000 C   0  0  0  0  0  0  0  0  0  0  0  0
        |   76.0677  -42.2503    0.0000 N   0  0  0  0  0  0  0  0  0  0  0  0
        |  1  2  1  0  0  0
        |M  END
      """.stripMargin
     */

    val molecule = MolFileReader.parseMolFile(molFile)

    val sparseFeatures = SparseConfig.getFeatures()
    val sparseSchema = SparseConfig.getSchema()
    val features1: Seq[Seq[String]] = sparseFeatures
      .map { feature: SparseFeature =>
        feature.computeFeature(molecule)
      }

    println(features1)
    println(sparseSchema)

    val semiSparseSchema =
      SemiSparseConfig.getSchema()
    val frequencyFeatures: Seq[FrequencyFeature] =
      SemiSparseConfig.getFrequencyFeatures()
    val bitFeatures: Seq[BitFeature] = SemiSparseConfig.getBitFeatures()

    val features2: Seq[Seq[Int]] =
      bitFeatures.map { feature: BitFeature =>
        feature.computeFeature(molecule)
      }

    val features3: Seq[Seq[Any]] =
      frequencyFeatures
        .map { feature: FrequencyFeature =>
          feature.computeFeature(molecule)
        }
        .flatMap { tuple =>
          List(tuple._1, tuple._2)
        }

    val row1: Row = Row(features1: _*)
    val row2: Row = Row(features2: _*)
    val row3: Row = Row(features3: _*)

    /*
    val parser = new scopt.OptionParser[Config]("SparseFeatureCalculator") {
      head(
        "Compute sparse features (ECFP, DFS, ECFC) via JCompoundMapper and CDK"
      )
      opt[String]('i', "input")
        .required()
        .action((x, c) => c.copy(input = x))
        .text("input path to parquet file")

      opt[String]('o', "output")
        .required()
        .action((x, c) => c.copy(output = x))
        .text("output path to parquet file")
    }

    parser.parse(args, Config()) match {
      case Some(config) =>
        val inputFile = config.input
        val outputFile = config.output

        println(inputFile)
        println(outputFile)

      case None =>
        println("No arguments given")
    }
    val name = "SparseFeatureCalculator"

    println(name)
    val spark = SparkSession.builder
      .master(master)
      .appName(name)
      .config("spark.app.id", name)
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .getOrCreate()

    val sc = spark.sparkContext

    try {} finally {
      spark.stop()
    }
   */
  }
}
