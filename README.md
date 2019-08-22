# Installation

## Dependencies

Download [SDKMan!](https://sdkman.io/install) and install Java 8, Scala and SBT for to build the `jar`

```bash
# Install dependencies via SDKMan!
sdk install java 8.0.202-amzn   # optionally update to latest version
sdk install scala 2.11.12
sdk install sbt 1.2.8
```

### jCompoundMapper

This program also depends on the code from the _jCompoundMapper_ project which has to be installed first.

Clone the _jCompoundMapper_ repo from the BioInf GitLab install it using maven:

```bash
# Install dependencies via SDKMan!
sdk install maven 3.6.0

# Navigate to JCompoundMapper directory
mvn install
```

## Building the jar

After you have installed all the dependencies navigate to the project directory and run these commands:

```bash
sbt update
sbt assembly
```

This will create a new `jar` in the folder `target/scala-2.11` which you then can copy to the server.

## Errors

It is possible that `sbt` will error because it cannot find _jCompoundMapper_ despite it being installed.
This is due to caching issues and can easily be resolved by deleting the `.ivy2` folder in your `HOME` directory and running the build commands again.
