## The Agent Module

This module provides an agent that instruments executing code (such as tests), looks for `null` values in arguments and return values, and logs the results to files `null-issues-observed-while-testing*.json`.
It also intercepts object creating by instrumenting constructors, and checks for nullable fields (static and non-static) by analysing fields after object construction
using reflection. However, it does not check for nullability of fields that are written later.
For this purpose, another agent is required, and there is a separate module provided for this purpose. 


Example:

    [
        {
            "methodName": "m3",
            "kind": "ARGUMENT",
            "index": 0,
            "descriptor": "(Ljava/lang/Object;)V",
            "class": "Foo"
        },
        {
            "methodName": "m1",
            "kind": "RETURN_VALUE",
            "index": -1,
            "descriptor": "()Ljava/lang/Object;",
            "class": "Foo"
        }
    ]


The system variable `nz.ac.wgtn.nullannoinference.includes` can be used to set a list of prefixes (comma-separated) for classes to be instrumented (typically this is some top-level package name). 

The system variable `nz.ac.wgtn.nullannoinference.context` can be used to include a context, this is used to track the system for which the tests
are executed. This is useful as API issues might be reported in another systems the system under analysis depends on. Usually this
information can also be inferred from stack traces captured. 

The system variable `nz.ac.wgtn.nullannoinference.issues.dir` (defined in the commons module) can be used to set the output folder where 
issues encountered during execution will be stored (in JSON format). If not set, the current working directory will be used.

Example usage: `java -Dnz.ac.wgtn.nullannoinference.includes=com.example,org.example -javaagent:nullannoinference-agent.jar .. `

``



