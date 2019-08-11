package at.jku.ml

import java.io.IOException
import java.nio.file.{Files, Path, Paths}

import at.jku.ml.features.{BitFeature, FrequencyFeature, SparseFeature}
import at.jku.ml.util.MolFileReader
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions._
import scopt.OParser

object FeatureCalculator {
  val calculateSparseFeatures: UserDefinedFunction = udf(
    (molFile: String) => {
      val molecule = MolFileReader.parseMolFile(molFile)
      val sparseFeatures: Seq[Seq[String]] = SparseConfig.getFeatures
        .map { feature: SparseFeature =>
          feature.computeFeature(molecule)
        }

      Row(sparseFeatures: _*)
    },
    SparseConfig.getSchema
  )

  val calculateSemiSparseFeatures: UserDefinedFunction = udf(
    (molFile: String) => {
      val molecule = MolFileReader.parseMolFile(molFile)
      val frequencyFeatures: Seq[Seq[Any]] =
        SemiSparseConfig.getFrequencyFeatures
          .map { feature: FrequencyFeature =>
            feature.computeFeature(molecule)
          }
          .flatMap { tuple =>
            List(tuple._1, tuple._2)
          }

      val bitFeatures: Seq[Seq[Int]] =
        SemiSparseConfig.getBitFeatures.map { feature: BitFeature =>
          feature.computeFeature(molecule)
        }

      Row.merge(
        Row(bitFeatures: _*),
        Row(frequencyFeatures: _*)
      )
    },
    SemiSparseConfig.getSchema
  )

  def main(args: Array[String]): Unit = {
    case class Config(
        inputPath: String = null,
        outputPath: String = null,
        overwrite: Boolean = false,
        featureType: String = "sparse"
    )

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
          .valueName("<feature>")
          .action((x, c) => c.copy(featureType = x))
          .validate(
            x =>
              x match {
                case "sparse" | "semisparse" => success
                case _ =>
                  failure("Only [sparse, semisparse] features are supported!")
              }
          )
          .text("feature type to compute [sparse, semisparse]"),
        opt[Unit]("overwrite")
          .action((_, c) => c.copy(overwrite = true))
          .text("Overwrite output parquet file")
      )
    }

    OParser.parse(parser, args, Config()) match {
      case Some(config) =>
        val inputPath: Path = Paths.get(config.inputPath)
        val outputPath: Path = Paths.get(config.outputPath)
        val featureType: String = config.featureType
        val writeMode: String = if (config.overwrite) "overwrite" else "error"

        val descriptorColumnName: String = featureType + "_descriptors"
        val descriptorSchema = featureType match {
          case "sparse" => SparseConfig.getSchema
          case "semisparse" => SemiSparseConfig.getSchema
        }

        val calculateFeatures = featureType match {
          case "sparse"     => calculateSparseFeatures
          case "semisparse" => calculateSemiSparseFeatures
        }

        try {
          if (Files.notExists(inputPath)) {
            throw new IOException("Input filepath does not exist!")
          }

          if (inputPath == outputPath) {
            throw new IOException("Input and Output filepath are identical!")
          }

          if (Files.exists(outputPath)) {
            if (writeMode != "overwrite") {
              throw new IOException(
                "Output filepath already exists! Run with option overwrite to `--overwrite` Output file!"
              )
            }
          }

          if (!inputPath.toString.endsWith(".parquet")) {
            throw new IllegalArgumentException(
              "Input filepath possibly not a `.parquet` file."
            )
          }

          if (!outputPath.toString.endsWith(".parquet")) {
            throw new IllegalArgumentException(
              "Output filepath possibly not a `.parquet` file."
            )
          }
        } catch {
          case iae: IllegalArgumentException =>
            System.err.println(iae.getMessage)
          case ioe: IOException => System.err.println(ioe.getMessage)
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
          val df = spark.read.parquet(inputPath.toString).limit(1000)
          //.repartition(200)

          val dfColumns = df.schema.fields.map{f => col(f.name)}
          val descriptorColumns = descriptorSchema.fields.map{f => col(descriptorColumnName + "." + f.name)}
          val queryColumns = dfColumns ++ descriptorColumns

          val dfResult = df.withColumn(descriptorColumnName, calculateFeatures(col("mol_file"))).select(queryColumns: _*)
          dfResult.write.mode(writeMode).parquet(outputPath.toString)
        } finally {
          spark.stop()
        }
      case _ =>
      // arguments are bad, error message will have been displayed
    }

  }
}
