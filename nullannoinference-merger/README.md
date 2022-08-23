# The Merger Utility

This is a simple utility that merges issues files found in a folder into a single file.

usage: `java -cp <classpath> nz.ac.wgtn.nullannoinference.scoper.Scoper <args>`

If the project is build with `mvn package`, a jar containing all dependencies will be produced in `target/`, and the application can simply be started by executing:

`java -jar target/nullannoinference-scoper.jar <args>`

## Arguments:

| short | long                      | description                                                                                                                                                                      | 
|-------|---------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `-i`  | `--input <arg>`           | a folder containing jsonfiles with null issues reported by a test run instrumented with the nullannoinference agent, the folder will be checked recursively for files (required) |
| `-p`  | `--project <arg>`         | the folder containing the Maven project (i.e. containing pom.xml) to be analysed, the project must have been built with `mvn test` as the test binaries are needed (required)    |
| `-s`  | `--summary <arg>`         | a summary csv file with some stats about the project bytecode analysed (optional, default is "byte-code-summary.csv")                                                            |
| `-t`  | `--projecttype <arg>`     | the project type, default is mvn (Maven), can be set to any of mvn, gradle and gradle_multilang (optional, default is "mvn")                                                     |





