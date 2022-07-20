# The Scoper Module

Utility to add the `scope` attribute to issues with possible values `MAIN`, `TEST`, `OTHER` by cross-referencing them with the project using a lightweight static analysis, to see whether an issue occurs in a core project class (i.e. defined in `src/main/java`) , 
in a test class (i.e. defined in `src/test/java`), or is defined somewhere else (usually in a dependency). 
This information is useful to filter issues later, for instance, in order to add annotation only to main project classes (but not tests).

The tool also produces a useful summary about the nature of the issues discovered. 

usage: `java -cp <classpath> nz.ac.wgtn.nullannoinference.scoper.Scoper <args>`

If the project is build with `mvn package`, a jar containing all dependencies will be produced in `target/`, and the application can simply be started by executing:

`java -jar target/scoper.jar <args>`

## Arguments: 

| short | long                  | description                                                                                                                                                                      | 
|-------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `-i`  | `--input <arg>`       | a json file containing null issues (required) |
| `-p`  | `--project <arg>`     | the folder containing the Maven project (i.e. containing pom.xml) to be analysed, the project must have been built with `mvn test` as the test binaries are needed (required)    |
| `-o`  | `--output <arg>`      | a json file where the issues withthe scope attribute set will be saved (required)                                                                                                |
| `-t`  | `--projecttype <arg>` | the project type, default is mvn (Maven), can be set to any of mvn, gradle and gradle_multilang (optional, default is "mvn")                                                     |





