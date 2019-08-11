package at.jku.ml

import at.jku.ml.features.SparseFeature
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.Row
import org.apache.spark.sql.functions._
import at.jku.ml.features.{BitFeature, Feature, FrequencyFeature, SparseFeature}
import at.jku.ml.features.BitFeature
import java.io.{BufferedInputStream, FileInputStream, IOException}
import java.nio.file.{Files, Path, Paths}

import scopt.OParser

case class Config(
    inputPath: String = null,
    outputPath: String = null,
    overwrite: Boolean = false,
    featureType: String = "sparse"
)

object FeatureCalculator {
  val calculateSparseFeatures = udf(
    (molFile: String) => Row {
      val molecule = MolFileReader.parseMolFile(molFile)
      val sparseFeatures: Seq[Seq[String]] = SparseConfig.getFeatures()
        .map { feature: SparseFeature =>
          feature.computeFeature(molecule)
        }

      Row(sparseFeatures: _*)
    },
    SparseConfig.getSchema()
  )

  val calculateSemiSparseFeatures = udf(
    (molFile: String) => Row {
      val molecule = MolFileReader.parseMolFile(molFile)
      val frequencyFeatures: Seq[Seq[Any]] =
        SemiSparseConfig.getFrequencyFeatures()
          .map { feature: FrequencyFeature =>
            feature.computeFeature(molecule)
          }
          .flatMap { tuple =>
            List(tuple._1, tuple._2)
          }

      val bitFeatures: Seq[Seq[Int]] =
        SemiSparseConfig.getBitFeatures().map { feature: BitFeature =>
          feature.computeFeature(molecule)
        }

      Row.merge(
        Row(bitFeatures: _*),
        Row(frequencyFeatures: _*)
      )
    },
    SemiSparseConfig.getSchema()
  )

  def main(args: Array[String]): Unit = {
    val builder = OParser.builder[Config]
    val parser = {
      import builder._
      OParser.sequence(
        programName("SparseFeatureCalculator"),
        head("Compute Features via JCMapper and CDK"),
        opt[String]('i', "input")
          .required()
          .valueName("<file>")
          .action((x, c) => c.copy(inputPath = x))
          .text("input parquet file (required)"),
        opt[String]('o', "output")
          .required()
          .valueName("<file>")
          .action((x, c) => c.copy(outputPath = x))
          .text("output parquet file (required)"),
        opt[String]("features")
          .valueName("(sparse|semisparse)")
          .action((x, c) => c.copy(featureType = x))
          .validate(x => x match {
            case "sparse" | "semisparse" => success
            case _ => failure("Only (sparse|semisparse) features are supported!")
          })
          .text("Feature type to compute"),
        opt[Unit]("overwrite")
          .action((_, c) => c.copy(overwrite = true))
      )
    }

    OParser.parse(parser, args, Config()) match {
      case Some(config) =>
        val inputPath: Path = Paths.get(config.inputPath)
        val outputPath: Path = Paths.get(config.outputPath)
        val writeMode: String = if (config.overwrite) "overwrite" else "error"

        val calculateFeatures = config.featureType match {
          case "sparse" => calculateSparseFeatures
          case "semisparse" => calculateSparseFeatures
        }

        if (Files.notExists(inputPath)) {
           throw new IOException("Input filepath does not exist!")
        }
        if (Files.exists(outputPath)) {
          throw new IOException("Output filepath already exists! Run with option overwrite to `--overwrite` Output file!")
        }
        if (inputPath == outputPath) {
          throw new IOException("Input and Output filepath are identical!")
        }


        val name = "SparseFeatureCalculator"
        val spark = SparkSession.builder
          .appName(name)
          .config("spark.app.id", name)
          .config(
            "spark.serializer",
            "org.apache.spark.serializer.KryoSerializer"
          )
          .getOrCreate()

        try {
          val df = spark.read.parquet(inputPath.toString).repartition(200)
          val dfFiltered = df.filter(size(col("molfile")).equalTo(1))

          val dfCleaned = dfFiltered.withColumn("mol_file_single", explode(col("mol_file"))).select(
            col("inchikey"), col("mol_file_single").alias("mol_file"), col("activity"), col("index")
          )

          val dfResult = dfCleaned.withColumn("descriptors", calculateFeatures(col("mol_file"))).select("inchikey", "mol_file", "activity", "index", "descriptors.*")
          dfResult.write.mode(writeMode).parquet(outputPath.toString)
        } finally {
          spark.stop()
        }
      case _ =>
      // arguments are bad, error message will have been displayed
    }

  }
}
