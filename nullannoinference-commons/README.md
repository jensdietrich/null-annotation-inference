## The Commons Module

Common code used across projects. This is not in the central Maven repository, so to satisfy the dependency from other modules this must be build and installed. 

The easiest way to do this is to build the parent project. Alternatively, this module can be installed locally by running 
`mvn clean install`