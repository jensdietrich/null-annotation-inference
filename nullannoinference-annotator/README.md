# The Annotator Module

The annotator module adds `nullable` annotations to a project based on a set of issues. The `ClassAnnotator` processes single source code files (`*.java`), 
while the `MvnProjectAnnotator` processes entire Maven projects (using the standard project layout, modules are not yet supported)
and also adds a dependency to a Maven artifact if necessary. 

Which particular annotation mechanism is to be used can be set via the simple `NullableAnnotationProvider` that can be registered as a service. 
There is a default implementation `JSR305NullableAnnotationProvider` that is pre-registered (see `src/main/resources/services`).

usage: `java -cp <classpath> nz.ac.wgtn.nullannoinference.annotator.MvnProjectAnnotator <args>`

If the project is build with `mvn package`, a jar containing all dependencies will be produced in `target/`, and the application can simply be started by executing:

`java -jar target/nullannoinference-annotator.jar <args>`

## Arguments:

| short | long                         | description                                                                                                                                                                   | 
|-------|------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `-i`  | `--issues <arg>`             | a folder containing json files with null issues collected and refined, the folder will be checked recursively for files (required)                                            |
| `-p`  | `--input <arg>`              | the folder containing the Maven project (i.e. containing pom.xml) to be analysed, the project must have been built with `mvn test` as the test binaries are needed (required) |
| `-o`  | `--output <arg>`             | the output mvn project folder (files will be override / emptied) (required))                                                                                                  |
| `-a`  | `--annotationprovider <arg>` | the name of an annotation provider (optional, default is to use a provider that scans for Nullable in annotation name)                                                        |
