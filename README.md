# tpp_scala

This repository includes the spark program for computing _sparse_ and _semi-sparse_ descriptors using [CDK](https://cdk.github.io/) and _jCompoundMapper_.

The program has two presets: _sparse_ and _semi-sparse_ which include a set of fingerprints / descriptors as used in the _LSC_ paper.

## Features

### Sparse

The features are defined in [SparseConfig.scala](./src/main/scala/at/jku/ml/SparseConfig.scala)

```yaml
ECFC4:
  - algorithm: ECFC
  - atomtype: DAYLIGHT_INVARIANT_RING
  - searchdepth: 2
  - library: jCompoundMapper

DFS8:
  - algoritm: DFS
  - atomtype: ELEMENT_SYMBOL
  - searchdepth: 8
  - library: jCompoundMapper

ECFC6:
  - algorithm: ECFC
  - atomtype: ELEMENT_SYMBOL
  - searchdepth: 3
  - library: jCompoundMapper
```

Original configuration in _LSC_ paper can be found here [link](https://github.com/ml-jku/lsc/blob/master/callChemblScript1.sh)

### Semi-Sparse

The features are defined in [SemiSparseConfig.scala](./src/main/scala/at/jku/ml/SparseConfig.scala).

#### Notes

In contrast to the _LSC_ paper this code utilizes _CDK_ for computing the _PubChem_ fingerprints.

```yaml
PubChem:
  - library: CDK

CATS2D:
  - algorithm: CATS2D
  - atomtype: ELEMENT_SYMBOL
  - searchdepth: 9
  - library: jCompoundMapper

SHED:
  - algoritm: SHED
  - atomtype: ELEMENT_SYMBOL
  - searchdepth: <unused>
  - library: jCompoundMapper
```

Original configuration in _LSC_ paper can be found here [link](https://github.com/ml-jku/lsc/blob/master/callChemblScript2.sh) and [link](https://github.com/ml-jku/lsc/blob/master/callChemblScript3.sh)

## Installation

### Dependencies

Download [SDKMan!](https://sdkman.io/install) and install Java 8, Scala and SBT for to build the `jar`

```bash
# Install dependencies via SDKMan!
sdk install java 8.0.202-amzn   # optionally update to latest version
sdk install scala 2.11.12
sdk install sbt 1.2.8
```

#### jCompoundMapper

This program also depends on the code from the _jCompoundMapper_ project which has to be installed first.

Clone the _jCompoundMapper_ repo from the BioInf GitLab install it using maven:

```bash
# Install dependencies via SDKMan!
sdk install maven 3.6.0

# Navigate to JCompoundMapper directory
mvn install
```

### Building the jar

After you have installed all the dependencies navigate to the project directory and run these commands:

```bash
sbt update
sbt assembly
```

This will create a new `jar` in the folder `target/scala-2.11` which you then can copy to the server.

### Errors

It is possible that `sbt` will error because it cannot find _jCompoundMapper_ despite it being installed.
This is due to caching issues and can easily be resolved by deleting the `.ivy2` folder in your `HOME` directory and running the build commands again.

## Usage

**Important: Always run the program with `--executor-cores=1` as not all classes are thread-safe**

The output `jar` built can be submitted as a spark job via `spark-submit`. The documentation for Spark can be found [here](https://spark.apache.org/docs/latest/).

```text
Compute Features via JCMapper and CDK
Usage: SparseFeatureCalculator [options]

  -i, --input <file>    input parquet file (required)
  -o, --output <file>   output parquet file (required)
  --overwrite           Overwrite output parquet file
  --features <feature>  feature type to compute [sparse, semisparse]
  --npartitions <int>   number of partitions
```

### Example Call

```bash
spark-submit --master <master_url> --executor-cores=1 tpp_scala-assembly-0.1.jar -i <path_input_parquet> -o <path_output_parquet> --features sparse --npartitions 200
```

It is recommended to only interact with this program by calling the corresponding workflows via Apache Airflow WebUI.
