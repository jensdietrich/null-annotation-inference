
# A Tool Suite for Inferring Nullability Annotations for Java Programs

Multiple static null checkers have become in recent years to address what is probably the most prevelant source of bugs in Java -- null dereferences. This include the [checkerframework](https://checkerframework.org/), [facebook infer eradicate](https://fbinfer.com/docs/next/checker-eradicate/), and [uber NullAway](https://github.com/uber/NullAway).  Null checkers use exiting `@Nullable` and `@NonNull` annotations to achieve more accurate results, however, most programs in the wild do not have such annotations. In this case checkers often resort to the *[nonnull-by-default](https://www.researchgate.net/profile/Perry-James/publication/221496273_Non-null_References_by_Default_in_Java_Alleviating_the_Nullity_Annotation_Burden/links/54f84b390cf210398e956e3d/Non-null-References-by-Default-in-Java-Alleviating-the-Nullity-Annotation-Burden.pdf)* assumption, i.e. they treat all APIs as annotated with `@NonNull`. 

This tool suite infers `@Nullable` annotations to refine those assumptions using a combination of dynamic and static analyses. This will then lead to a more accurate analysis when using static checkers. In a nutshell, instrumented tests are executed and method invocations and field access is monitored for the use of `null`. Then several static analyses are used to refine the results, reducing both false negatives and false positives.


## Publication

[Dietrich, Jens, David J. Pearce, and Mahin Chandramohan. "On Leveraging Tests to Infer Nullable Annotations." 37th European Conference on Object-Oriented Programming (ECOOP 2023). Schloss Dagstuhl-Leibniz-Zentrum für Informatik, 2023.](https://drops.dagstuhl.de/opus/volltexte/2023/18203/pdf/LIPIcs-ECOOP-2023-10.pdf)

## Sightings


nullable annotations added based on this analysis:

1. [guava](https://github.com/google/guava/issues/6510)
2. [spring](https://github.com/spring-projects/spring-framework/pull/29150)


## Design

The tool is designed to work for Maven and Gradle (Gradle support is incomplete at the moment) projects, and contains the following modules: 

1. An __agent__  that can be used to instrument tests (e.g. can be used in the sure-fire plugin in Maven projects) , this will produce json-encoded files listing nulls that have been observed in method returns or arguments (*nullability issues*) or after object creation during test execution. 
   A second instrumentation module __agent2__ provides a second agent to be used to record field access and capture `null` writes. The reason to have two modules is purely technical as the first one uses [bytebuddy](https://bytebuddy.net/), while the second one is based on [ASM](https://asm.ow2.io/) as bytebuddy had some limitations instrumenting fields.
2. A __sanitizer__ module that removes issues  by applying several filters related to sources of false positives (improving precision), with filters implemented using bytecode analyses. The sanitizer takes a set of issues as input, and produces a subset of issues, removing likely false positives. Several sanitizer heuristics are supported, they are described in detail the readme of the sanitizer module.
3. A __propagator__ module that performs a static bytecode analysis on the program to infer additional nullability issues, therefore addressing potenial false mnegatives (improving recall). The strategy used here is based on [Liskov's Substitution Principle](https://en.wikipedia.org/wiki/Liskov_substitution_principle). 
4. An __annotator__ module that inserts nullable annotations into the project, and also adds the dependency to the artifact containing the annotations to the projects pom (such as [JSR305](https://mvnrepository.com/artifact/com.google.code.findbugs/jsr305), particular annotation mechanisms to be used are pluggable).

There are also __commons__  and __commons-io__ modules containing some code used by several other modules, and a __experiments-analysis__ module
with scripts to analyse several [spring modules](https://spring.io/). Spring is one of the few projects that uses `@Nullable` annotations, and is therefore a good scenario to evaluate this approach. This is done by running the analysis on spring, and comparing the inferred @Nullable annotations with the ones found in the Spring source code.

## Build and Usage Instructions

All modules can be built by running `mvn clean package` in the project root folder. After succesful builds, the binaries for the respective modules can be found in 
the `target/` folders of the modules. See readmes in the module folders for module-specific instructions. 
