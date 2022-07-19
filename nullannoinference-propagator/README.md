# The Propagator Module

The propagator uses static analysis to improve the recall of issues. Note that issues are expected to have a scope set (see scoper module), and only issues are processed 
that have a main scope. In Maven projects, this corresponds to classes defined in `src/main/java` with binaries
generated in `target/classes`. 

Additional issues are inferred for sub / superclasses and added by applying Liskov's Substitution Principle. 

usage: `java -cp <classpath> nz.ac.wgtn.nullannoinference.propagator.Main <args>`

If the project is build with `mvn package`, a jar containing all dependencies will be produced in `target/`, and the application can simply be started by executing:

`java -jar target/propagator.jar <args>`

## Arguments: 

| short | long                     | description                                                                                                | 
|-----------|--------------------------|------------------------------------------------------------------------------------------------------------|
| `-a`      | `--propagate4args <arg>` | whether to propagate nullability for arguments to subtypes (optional, default is `true`)                   |
| `-i`      | `--input <arg>`          | a json file with null issues (required)                                                                    |
| `-p`      | `--project <arg>`        | the folder containing the project to be analysed, the project must have been built (required)              |
| `-x`      | `--packagePrefix <arg>`  | the prefix of packages for which the hierachy will be analysed, such as "org.apache.commons" (required)    |
| `-o`      | `--output <arg>`         | the json file where both input and inferred issues will be stored (required)                               |
| `-t`  | `--projecttype <arg>`    | the project type, can be set to any of `mvn`, `gradle` and `gradle_multilang` (optional, default is `mvn`) |







