# Readme

## Building Projects

- projects are build using Java 7 as this is supported by `jastaddj` (accourding to communication with authors)
- the versions used correspond to folder names, but can also be queried with `git describe --tags`
- a simplified pom (`pom-simple.xml`) has been created for each project that removes plugins and in some cases modifies dependency versions to fix issues with artifacts not being available in the Maven repository, for details, diff `pom.xml` and `pom-simple.xml`
- before building, verify the Java version with `java -version` (must be 1.7)
- compile classes with `mvn clean compile -f pom-simple.xml`
- run tests with `mvn clean compile -f pom-simple.xml -DskipTests`, this will ignore (a very small number of) failing tests


## Running Nonnull Analysis with `jastaddj`


### Tools

*tools* has scripts to collect compilation units, and translate annotations in Java files. The background is that `jastaddj` creates virtual annotations which are not actually defined. The tool just translates them to JSR305 annotations (`@javax.annotation.Nonnull`).

The `tools/` project must be build first by running `mvn clean package` in `tools/`. Java 1.7 is sufficient to compile and run tools.


### Collecting Java Files

This is the file list used in the project-specific sh scripts for the jastaddj analysis.

Example:
`java -cp tools/target/tools.jar CollectJavaFiles commons-lang-3.0/src/main/java src-main-commons-lang-3.0.txt`

This will create a list of Java files in the mains scope in `commons-lang-3.0` and write them to `src-main-commons-lang-3.0.txt`. 

## `jastaddj` Analysis

- run project-specific sh scripts to run the analysis, example: `jastaddj-commons-lang-3.0.sh` , this will also use *tools* to translate the generated annotations
- make a fresh copy of the project `<project>-inferred`
- copy annotated `*.java` files into `<project>-inferred` folder


*Note*: we run the analysis with the `-disableraw`  option as otherwise some of the modified classes cannot be parsed and build fails, example: `org.apache.commons.lang3.Range`, the issue is caused by `@Raw` annotations being inserted. Accourding to the documentation: *" .. this has the effect that all fields are considered possibly-null"* . 

## Running Projects with Nullable Capture


### Project-Specific Changes

###  