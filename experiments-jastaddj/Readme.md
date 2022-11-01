# Readme

## Building Projects

- projects are build using Java 7 as this is supported by `jastaddj` (accourding to communication with authors)
- the versions used correspond to folder names, but can also be queried with `git describe --tags`
- a simplified pom (`pom-simple.xml`) has been created for each project that removes plugins and in some cases modifies dependency versions to fix issues with artifacts not being available in the Maven repository, for details, diff `pom.xml` and `pom-simple.xml`
- before building, verify the Java version with `java -version` (must be 1.7)
- compile classes with `mvn clean compile -f pom-simple.xml`
- run tests with `mvn clean compile -f pom-simple.xml -DskipTests`, this will ignore (a very small number of) failing tests


## Static Nonnull Analysis with *jastaddj*


### Tools

*tools* has scripts to collect compilation units, and translate annotations in Java files. The background is that `jastaddj` creates virtual annotations which are not actually defined. The tool just translates them to JSR305 annotations (`@javax.annotation.Nonnull`).

The `tools/` project must be build first by running `mvn clean package` in `tools/`. Java 1.7 is sufficient to compile and run tools.


### Collecting Java Files

This is the file list used in the project-specific sh scripts for the jastaddj analysis.

Example:
`java -cp tools/target/tools.jar CollectJavaFiles commons-lang-3.0/src/main/java src-main-commons-lang-3.0.txt`

This will create a list of Java files in the mains scope in `commons-lang-3.0` and write them to `src-main-commons-lang-3.0.txt`. 

### Running The Actual Analysis

- run project-specific sh scripts to run the analysis, example: `jastaddj-commons-lang-3.0.sh` , this will also use *tools* to translate the generated annotations
- make a fresh copy of the project `<project>-inferred`
- copy annotated `*.java` files into `<project>-inferred` folder
- confirm build by CCing into `<project>-inferred` folder, the run `mvn clean test-compile -f pom-simple.xml` (this will also build test classes)


*Note*: we run the analysis with the `-disableraw`  option as otherwise some of the modified classes cannot be parsed and build fails, example: `org.apache.commons.lang3.Range`, the issue is caused by `@Raw` annotations being inserted. Accourding to the documentation: *" .. this has the effect that all fields are considered possibly-null"* . 

## Dynamic Nullable Analysis with *null-annotation-inference*

This is the dynamic analysis to compare jastaddj with.


### Capture

Run the instrumented build in the `-inferred` folders, they must contain the two agent files `nullannoinference-agent.jar` and `nullannoinference-agent2.jar`. Switch to Java 11 or better to build. 

`mvn test -f pom-simple.xml -Dmaven.test.failure.ignore=true`

Note that the nullannoinference capture does not read the annotations inserted by *jastaddj* , so this can also be done in the original folder. The annotation is configured in the surefire plugins in  `pom-simple.xml`.

### Issues During Capture

#### commons-lang-3.0

```
[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.1:testCompile (default-testCompile) on project commons-lang3: Compilation failure
[ERROR] /Users/jens/Development/null-annotation-inference/experiments-jastaddj/commons-lang-3.0-inferred/src/test/java/org/apache/commons/lang3/reflect/TypeUtilsTest.java:[508,41] incompatible types: inferred type does not conform to upper bound(s)
[ERROR]     inferred: G
[ERROR]     upper bound(s): java.lang.Comparable<G>,java.lang.Integer
```

Solution: remove `org/apache/commons/lang3/reflect/TypeUtilsTest` 

#### commons-maths-3.0

Then instrumented run of `org.apache.commons.math3.optimization.direct.BOBYQAOptimizerTest` hangs. Solution: remove this test.


### Inference

Run `capture-and-infer.sh`, this will perform the following for each project:

1. merge the issues captured into a single file in `results/captured/null-issues-<project>.json`
2. apply sanitisation, and save the sanitised issues in `results/sanitised/null-issues-<project>.json`
3. apply propagation, and save the sanitised and propagated issues in `results/propagated/null-issues-<project>.json`

This script used the binaries from the *null-annotation-inference* pipeline. Copies can be found in `analyses/`, the respective binaries can be build by building *[null-annotation-inference](https://github.com/jensdietrich/null-annotation-inference/)*  with `mvn package`.

This script requires Java 11 or better ! 






	