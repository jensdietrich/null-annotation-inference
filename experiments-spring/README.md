## Spring Framework Experiments

1. `fetch.sh` -- fetches projects from github, and switches to the version to be analysed, repo,  version and target folder settings are defined in `spring.env`. 
2. Then build the pulled springframework by CDing into the local folder and running `./gradlew build`. According to the spring documentation, this requires Java version 17. The build may fail due to some compiler warnings interpreted as errors. This can be prevented by removing `-Werror` from being added to `COMPILER_ARGS` in `/spring-framework/buildSrc/src/main/java/org/springframework/build/compile/CompilerConventionsPlugin.java`. To do this, comment out the respective code before building. 
3. Also remove `-Werror` by commenting out lines in gradle files using it in `compileGroovy` -- notably in `spring-beans/spring-beans.gradle`
3. The build is still brittle, it experiments it succeeded using Java 11.0.11, and using the `--continue` parameter to ignore a very small number of failing tests. 

