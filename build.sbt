name := "tpp_scala"
organization := "at.jku.ml"
description := "Target Prediction Pipeline Scala components"
version := "0.1"
scalaVersion := "2.11.12"
scalacOptions := Seq(
  "-deprecation",
  "-unchecked",
  "-encoding",
  "utf8",
  "-Xlint"
)

val sparkVersion = "2.4.3"

/* Exclude xml-apis to avoid dependency erros when building fat jar */
libraryDependencies += "org.openscience.cdk" % "cdk-bundle" % "2.1.1" exclude ("xml-apis", "xml-apis")
libraryDependencies += "de.zbit.jcmapper" % "jcompoundmapper" % "1.1" exclude ("xml-apis", "xml-apis")
libraryDependencies += "org.apache.spark" %% "spark-core" % sparkVersion % "provided"
libraryDependencies += "org.apache.spark" %% "spark-sql" % sparkVersion % "provided"

assemblyMergeStrategy in assembly := {
  //case PathList("xml-apis", xs @ _*) =>
  //  MergeStrategy.first
  //case PathList("org", "w3c", "dom", "UserDataHandler.class") =>
  // MergeStrategy.first
  case "header.txt"       => MergeStrategy.discard
  case "log4j.properties" => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

/* including scala bloats your assembly jar unnecessarily, and may interfere with
   spark runtime */
assemblyOption in assembly := (assemblyOption in assembly).value
  .copy(includeScala = false)

assemblyShadeRules in assembly := Seq(
  ShadeRule
    .rename("com.google.guava.**" -> "repackaged.com.google.guava.@1")
    .inAll,
  ShadeRule
    .rename("com.google.code.**" -> "repackaged.com.google.code.@1")
    .inAll,
  ShadeRule
    .rename("joda-time.**" -> "repackaged.joda-time.@1")
    .inAll,
  /*ShadeRule
    .rename("xml-apis.**" -> "repackaged.xml-apis.@1")
    .inAll,*/
  ShadeRule
    .rename("io.netty.**" -> "repackaged.io.netty.@1")
    .inAll
)

logLevel in assembly := Level.Debug
