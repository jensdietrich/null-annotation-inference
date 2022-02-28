# Tools for Inferring and Adding Nullability Annotations

This is an engine to infer `@Nullable` annotations using a combination of dynamic and static analysis. Adding those annotations is 
 useful as static analysers like [infer-eradicate](https://fbinfer.com/docs/next/checker-eradicate/) use the *nonnull-by-default* assumption, but take advantage of those annotations. 
The presence of annotations in code significantly increases the power of those analysers, but it is a very expensive task to add them.

This is the problem this tool addresses. The idea is that most tests represent intended program behaviour, and if null is observed when tests execute, a
```@Nullable``` annotation is added to the code.

The tool is designed for Maven projects, and contains the following modules: 

1. An __agent__  that can be used to instrument tests (e.g. can be used in the sure-fire plugin in Maven projects) , this will produce json-encoded files listing nulls that have been observed in method returns or arguments (*nullability issues*) or after object creation during test execution. 
   A second instrumentation module __agent2__ providing a second agent to be used to record field access and capture null writes. The reason to have two modules in mainly technical as the first one uses bytebuddy, while the second one is based on ASM.
2. A __scoper__ that adds the scope attribute to issues with possible values `MAIN`, `TEST`, `OTHER` by cross-referencing them with the project using a lightweight static analysis, to see whether an issue occurs in a core project class (i.e. defined in `src/main/java`) , in a test class (i.e. defined in `src/test/java`), or is defined somewhere else (usually in a dependency). This information is useful to filter issues later.
3. A __refiner__ module that performs a static bytecode analysis on the program and refines the issues collected by both removing and adding nullability issues
4. An __annotator__ module that inserts nullable annotations into the project, and also adds the dependency to the artifact containing the annotations to the projects pom (such as [JSR305](https://mvnrepository.com/artifact/com.google.code.findbugs/jsr305), particular annotation mechanisms to be used are pluggable).

There is also a __commons__ module containing some code used by several other modules, and a experiments-analysis module
with scripts to analyse and summarise the data obtained running evaluation experiments on `commons-` libraries, the respective
scripts can be found in `experiments`. 

## Build and Usage Instructions

All modules can be built by running `mvn clean package` in the project root folder. After succesful builds, the binaries for the respective modules can be found in 
the `target/` folders of the modules. See readmes in the module folders for module-specific instructions. 