
# Tools for Inferring and Adding Nullability Annotations

This is a set of tools to infer `@Nullable` annotations using a combination of dynamic and static analysis. Adding those annotations is 
 useful as many static null checkers like [infer-eradicate](https://fbinfer.com/docs/next/checker-eradicate/) use the *nonnull-by-default* assumption, but take advantage of those annotations. 
The presence of annotations in code significantly increases the power of those analysers, but it is a very expensive task to add them.

This is the problem this project addresses. The idea is that most tests represent intended program behaviour, and if null is observed when tests execute, a
```@Nullable``` annotation is added to the code.

The tool is designed for Maven projects, and contains the following modules: 

1. An __agent__  that can be used to instrument tests (e.g. can be used in the sure-fire plugin in Maven projects) , this will produce json-encoded files listing nulls that have been observed in method returns or arguments (*nullability issues*) or after object creation during test execution. 
   A second instrumentation module __agent2__ provides a second agent to be used to record field access and capture `null` writes. The reason to have two modules is purely technical as the first one uses [bytebuddy](https://bytebuddy.net/), while the second one is based on [ASM](https://asm.ow2.io/) as bytebuddy had some limitations instrumenting fields.
2. A __sanitizer__ module that removes issues  by applying several filters related to sources of false positives (improving precision), with filters implemented using bytecode analyses. The sanitizer takes a set of issues as input, and produces a subset of issues, removing likely false positives. Several sanitizer heuristics are supported, see module readme for details.
3. A __propagator__ module that performs a static bytecode analysis on the program to infer additional nullability issues, therefore addressing potenial false mnegatives (improving recall). The strategy used here is based on [Liskov's Substitution Principle](https://en.wikipedia.org/wiki/Liskov_substitution_principle). 
4. An __annotator__ module that inserts nullable annotations into the project, and also adds the dependency to the artifact containing the annotations to the projects pom (such as [JSR305](https://mvnrepository.com/artifact/com.google.code.findbugs/jsr305), particular annotation mechanisms to be used are pluggable).

There are also __commons__  and __commons-io__ modules containing some code used by several other modules, and a __experiments-analysis__ module
with scripts to analyse several [spring modules](https://spring.io/). Spring is one of the few projects that uses `@Nullable` annotations, and is therefore a good scenario to evaluate this approach. 

## Build and Usage Instructions

All modules can be built by running `mvn clean package` in the project root folder. After succesful builds, the binaries for the respective modules can be found in 
the `target/` folders of the modules. See readmes in the module folders for module-specific instructions. 
