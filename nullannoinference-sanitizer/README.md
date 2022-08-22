# The Sanitizer Module

The sanitizer uses static analysis to improve the precision of issues. Sanitization is implemented using *Sanitizers* , basically issues. 

Implementation is based on bytecode analysis. There are four sanitizers implemented in this module (package names omitted):

1. `DeprecatedElementsSanitizer` - only accept issues in non-deprecated classes, methods or fields.
2. `MainScopeSanitizer` - only accept issues in classes in the main scope (in the sense of Maven or Gradle projects). In particular, this excludes test classes.
3. `ShadingSanitizer` - only accept issues in non-shaded classes. This is justified by the fact that developers are unliklely to add annotations to shaded classes that get replaced during builds. 
4. `NegativeTestSanitizer` - only accept issues observed by test executions of non-negative tests. This excludes issues observed in tests for non-normal behaviour, recognised by the use of exception oracles. 


usage: `java -cp <classpath> Main <args>`

If the project is build with `mvn package`, a jar `sanitizer.jar` containing all dependencies will be produced in `target/`, and the application can simply be started by executing:

`java -jar target/sanitizer.jar <args>`

## Arguments: 

| short | long | description  | 
|-------|----- |-------------|
| `-m` | `--removeissuesnotinmain` | if set, issues in classes not in main scope are removed |
| `-n` | `--removeissuesfromnegativetests` | if set, perform an analysis to remove issues observed while executing negative tests |
| `-s` | `--removeissuesinshadedclasses` | if set, perform an analysis to remove issues in shaded classes |
| `-pr`| `--removeissuesinprivatemethods` | if set, perform an analysis to remove issues in private and packageprivate methods |
| `-d` | `--removeissuesindeprecatedelements` | if set, perform an analysis to remove issues in deprecated elements |
| `-i`      | `--input <arg>`           |  a json file with null issues (required) |
| `-p`      | `--project <arg>`         | the folder containing the project to be analysed, the project must have been built (required)|
| `-o`   | `--sanitisedissues <arg>` | a file where the sanitized issues will be saved (required) |
| `-t`   | `--projecttype <arg>`     | the project type, can be set to any of `mvn`, `gradle` and `gradle_multilang` (optional, default is `mvn`)  |
| `-nt`  | `--negativetests <arg>`   | a csv file where information about negative tests detected will be save in CSV format (optional, default is `negative-tests.csv`)  |          
| `-sh`  | `--shadingspecs <arg>`    | the json file with definitions of shaded packages, required for shading analysis |
| `-de`  | `--deprecatedelements <arg>` | the text file where information about deprecated elements found will be saved (optional) |
| `-pm`  | `--privatemethods <arg>` | the text file where private methods detected be written (optional) |






