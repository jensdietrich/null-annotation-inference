# The Sanitizer Module

The sanitizer uses static analysis to improve the precision of issues. Note that issues are expected to have a scope set (see scoper module), and only issues are processed 
that have a main scope. In Maven projects, this corresponds to classes defined in `src/main/java` with binaries
generated in `target/classes`. 

Issues that are observed in *negative tests*, i.e. tests triggering abnormal behaviour, are removed. Negative tests are detected based on the test
oracle -- tests that check for expected exceptions or errors are considered negative. 

usage: `java -cp <classpath> nz.ac.wgtn.nullannoinference.sanitizer.Main <args>`

If the project is build with `mvn package`, a jar containing all dependencies will be produced in `target/`, and the application can simply be started by executing:

`java -jar target/sanitizer.jar <args>`

## Arguments: 

| short | long                      | description                                                                                                                               | 
|-----------|---------------------------|-------------------------------------------------------------------------------------------------------------------------------------------|
| `-i`      | `--input <arg>`           | a json file with null issues (required)                                                                                                   |
| `-n`      | `--negativetests <arg>`   | a csv file where information about negative tests detected will be save in CSV format (optional, default is `negative-tests.csv`)         |
| `-p`      | `--project <arg>`         | the folder containing the project to be analysed, the project must have been built (required)                                             |
| `-s`      | `--sanitisedissues <arg>` | a file where the sanitized issues will be saved (required) |
| `-t`  | `--projecttype <arg>`     | the project type, can be set to any of `mvn`, `gradle` and `gradle_multilang` (optional, default is `mvn`)                |







