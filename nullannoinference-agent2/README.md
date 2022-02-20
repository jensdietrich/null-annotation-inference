## The Agent2 Module

This is an agent to instrument field access. The __agent__ module only checks fields by inspecting the state after constructor invocations. This agent intercepts field write access (for both static and non-static fields)
and logs an issue if `null` is set.

The same system variables used by the agent module are supported to set the package name prefix for classes to be instrumented and context, check the respective module README for details.

The reason to implement this in a different module is not conceptual but technical -- [intercepting fields with bytebuddy was not possible 
](https://stackoverflow.com/questions/70965761/intercept-field-access-to-log-with-bytebuddy) and after changes were made in the library is at least still difficult due
to a lack of documentation, so we opted for implementing a separate agent with the more low-level [ASM](https://asm.ow2.io/).

Note that multiple agents can be used -- by just adding multiple `-javaagent` arguments to the JVM.  




