## The Commons Module

Common code used across projects. This is not in the central Maven repository, so to satisfy the dependency from other modules this must be build and installed. 

The module contains an easy-to-serialise representation of issues that are being created and processed by different modules. The moduke
also comntains a shaed version of [json.org](https://mvnrepository.com/artifact/org.json/json) (in order to prevent dependency conflicts when deploying other modules as java agents).

The easiest way to do this is to build the parent project. Alternatively, this module can be installed locally by running 
`mvn clean install`