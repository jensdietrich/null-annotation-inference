# The Annotation Collector Module

This is a simple static (bytecode, ASM-based) analysis to extract existing null annotations from projects, and to save them to a file in JSON-format for further analysis. 

usage: `java -cp <classpath> nz.ac.wgtn.nullinference.extractor.Main <args>`

If the project is build with `mvn package`, a jar containing all dependencies will be produced in `target/`, and the application can simply be started by executing:

`java -jar target/annotation-collector.jar <args>`

## Arguments:

| short | long                         | description                                                                        | 
|-------|------------------------------|------------------------------------------------------------------------------------|
| `-i`  | `--project <arg>`            | the project (folder) to be analysed, the project must have been built   (required) |
| `-p`  | `--issues <arg>`             | the name of the issue file to be created   (required)                              |
| `-o`  | `--projecttype <arg>`        | the type of the project -- mvn or gradle (required))                               |

