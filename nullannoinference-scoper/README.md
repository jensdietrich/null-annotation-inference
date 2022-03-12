# The Scoper Module

Utility to add the `scope` attribute to issues with possible values `MAIN`, `TEST`, `OTHER` by cross-referencing them with the project using a lightweight static analysis, to see whether an issue occurs in a core project class (i.e. defined in `src/main/java`) , 
in a test class (i.e. defined in `src/test/java`), or is defined somewhere else (usually in a dependency). 
This information is useful to filter issues later, for instance, in order to add annotation only to main project classes (but not tests).

The tool also produces a useful summary about the nature of the issues discovered. 

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





