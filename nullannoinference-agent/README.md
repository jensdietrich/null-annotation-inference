## README

This module provides an agent that instruments executing code (such as tests), looks for `null` values in arguments and return values, and logs the results to files `null-issues-observed-while-testing*.json`.

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

Example usage: `java -Dnz.ac.wgtn.nullannoinference.includes=com.example,org.example -javaagent:nullannoinference-agent.jar .. `

``



