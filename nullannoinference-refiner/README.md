# The Refiner Module

The refiner uses static analysis to improve the precision and recall of issues. Note that issues are expected to have a scope set (see scoper module), and only issues are processed 
that have a main scope. In Maven projects, that corresponds to classes defined in `src/main/java` with binaries
generated in `target/classes`. 

Issues that are observed in *negative tests*, i.e. tests triggering abnormal behaviour, are removed, while additional issues
and inferred for sub / superclasses and added by applying Liskov's Substitution Principle. This is detected based on the test
oracle -- tests that check for expected exceptions or errors are considered negative. 

usage: `java -cp <classpath> nz.ac.wgtn.nullannoinference.refiner.Main <args>`

If the project is build with `mvn package`, a jar containing all dependencies will be produced in `target/`, and the application can simply be started by executing:

`java -jar target/nullannoinference-refiner.jar <args>`

## Arguments: 

| short | long                      | description                                                                                                                                                                 | 
|-----------|---------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `-a`      | `--propagate4args <arg>`  | whether to propagate nullability for arguments to subtypes (optional, default is `false`)                                                                                   |
| `-i`      | `--input <arg>`           | a folder containing jsonfiles with null issues reported by a test run instrumented with the nullannoinference agent, the folder will be checked recursively for files (required) |
| `-n`      | `--negativetests <arg>`   | a csv file where information about negative tests detected will be save in CSV format (optional, default is `negative-tests.csv`)                                           |
| `-p`      | `--project <arg>`         | the folder containing the Maven project (i.e. containing pom.xml) to be analysed, the project must have been built with `mvn test` as the test binaries are needed (required) |
| `-s`      | `--sanitisedissues <arg>` | a folder where the issues not removed by the negative test sanitisation will be saved (optional, default is `sanitised_nullability_issues`)                                 |
| `-x`      | `--packagePrefix <arg>`   | the prefix of packages for which the hierachy will be analysed, such as "org.apache.commons" (required) |
| `-o`      | `--summary <arg>`         | a summary csv file with some stats about the inferences performed (optional, default is "summary.csv") |






