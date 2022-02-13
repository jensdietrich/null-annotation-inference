# null-annotation-inference

This is an engine to infer `@Nullable` annotations using a combination of dynamic and static analysis. Adding those annotations is 
 useful as static analysers like [infer-eradicate](https://fbinfer.com/docs/next/checker-eradicate/) use the *nonnull-by-default* assumption, but take advantage of those annotations. 
The presence of annotations in code significantly increases the power of those analysers, but it is a very expensive task to add them.

This is the problem this tool addresses. The idea is that most tests represent intended program behaviour, and if null is observed when tests execute, a
```@Nullable``` annotation is added to the code.

The tool is designed for Maven projects, and contains three modules: 

1. An __agent__  that can be used to instrument tests (e.g. can be used in the sure-fire plugin in Maven projects) , this will produce json-encoded files listing nulls that have been observed in method returns or arguments (*nullability issues*) during test execution
2. A __refiner__ module that performs a static bytecode analysis on the program and refines the issues collected by both removing and adding nullability issues
3. An __annotator__ module that inserts nullable annotations into the project, and also adds the dependency to the artifact containing the annotations to the projects pom (such as [JSR305](https://mvnrepository.com/artifact/com.google.code.findbugs/jsr305), particular annotation mechanisms to be used are pluggable). 


## Build and Usage Instructions

All modules can be built by running `mvn clean package` in the project root folder. After succesful builds, the binaries for the respective modules can be found in 
the `target/` folders of the modules. See readmes in the module folders for module-specific instructions. 