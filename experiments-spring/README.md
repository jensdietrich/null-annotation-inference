## Spring Framework Experiments

1. `fetch.sh` -- fetches projects from github, and switches to the version to be analysed, repo,  version and target folder settings are defined in `spring.env`. 
2. Then build the pulled springframework by CDing into the local folder and running `./gradlew build`. According to the spring documentation, this requires Java version 17. The build may fail due to some compiler warnings interpreted as errors. This can be prevented by removing `-Werror` from being added to `COMPILER_ARGS` in `/spring-framework/buildSrc/src/main/java/org/springframework/build/compile/CompilerConventionsPlugin.java`. To do this, comment out the respective code before building. 
3. Also remove `-Werror` by commenting out lines in gradle files using it in `compileGroovy` -- notably in `spring-beans/spring-beans.gradle`
4. If the build is short and doesn't produce the expected issue files, rerun with the `--no-build-cache` option
3. The build is still brittle, in experiments it succeeded using Java 11.0.11, and using the `--continue` parameter to ignore a very small number of failing tests. 

## Running Automated Scripts

Several sh scripts can be used to run the processing pipeline. Thiose script consume data from `/projects` and `/results`, and create new data, organised by module

1. `build.sh` builds the tool and copies executables corresponding to each processing step into `/analyses`. Each executable is a jar file that can be invoked with `java -jar <jar-file> <parameters>`. The various `.sh` scripts illustrate how to supply parameters.
2. `capture.sh` runs the tests in the instrumented modules, and records the nullability issues found
3. `extract.sh` extracts the existing nullable annotations found in spring, to be used to assess the quality of the captures nullability issues
4. `sanitizeD.sh` applies the deprecation sanitiser to the captured issues
5. `sanitizeM.sh` applies the main scope sanitiser to the captured issues
6. `sanitizeN.sh` applies the negative test sanitiser to the captured issues
7. `sanitizeS.sh` applies the shading sanitiser to the captured issues
8. `sanitize-all.sh` applies all of the previous sanitisers to the captured issues
9. `propagate-observed.sh` applies propagation to the captured and sanitised issues
10. `sanitize-observed-and-propagated.sh` applies sanitisation to already sanitised and then propagated issues, assessing fix-point like properties

## Testing the Annotator

This is a semi-manual process, to be performed as follows:

1. copy `projects/original` into `projects/nullable-removed`
2. open `projects/nullable-removed` in IDE (tested with IntelliJ-2022.2 (Ultimate), other versions or Eclipse *may* also work)
3. in the module `spring-core`, locate the class `org.springframework.lang.Nullable`, and use the IDE refactoring to rename it (e.g., to `Foo`). This will also rename all references within the project, and effectively remove `@Nullable`
4. run `reannotate.sh` -- this will use the extracted issues, and reinsert the `@Nullable` annotations in `projects/reannotated`
5. because `projects/reannotated` does not have `org.springframework.lang.Nullable` , it needs to be brought back, the easiest way to do this is to open the project in an IDE, and copy the copy made in step 2 (`Foo`) back into `Nullable`
6. confirm that the project can be compiled by running `gradle compileJava` in `projects/reannotated` -- note that a full build may still fail, for instance, the code produced by the annotator does not satisfy some of the spring-specific checkstyle rules