# The Refiner Module


usage: `java -cp <classpath> nz.ac.wgtn.nullannoinference.Main [-a <arg>] -i <arg> [-n <arg>] -p
<arg> [-s <arg>]`

## Arguments: 

| short | long                | description                                                                                                                                                                      | 
|-----------|-------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `-a`      | `--propagate4args <arg>` | whether to propagate nullability for arguments to subtypes (optional, default is `false`)                                                                                        |
| `-i`      | `--input <arg>`         | a folder containing jsonfiles with null issues reported by a test run instrumented with the nullannoinference agent, the folder will be checked recursively for files (required) |
| `-n`      | `--negativetests <arg>` | a csv file where information about negative tests detected will be save in CSV format (optional, default is `negative-tests.csv`)                                                |
| `-p`      | `--project <arg>`       | the folder containing the Maven project (i.e. containing pom.xml) to be analysed, the project must have been built with `mvn test` as the test binaries are needed (required)    |
| `-s`      | `--sanitisedissues <arg>` | a folder where the issues not removed by the negative test sanitisation will be saved (optional, default is `sanitised_nullability_issues`)                                      |

If the project is build with `mvn package`, a jar containing all dependencies will be produced in `target/`, and the application can simply be started by executing:

`java -jar target/nullannoinference-refiner.jar <args>`



