package nz.ac.wgtn.nullannoinference.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.utility.JavaModule;

public class DebugListener implements AgentBuilder.Listener {

    @Override
    public void onDiscovery(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
    }

    @Override
    public void onTransformation(TypeDescription typeDescription,
                         ClassLoader classLoader,
                         JavaModule module,
                         boolean loaded,
                         DynamicType dynamicType) {
        System.out.println("instrumenting: " + typeDescription);
    }

    @Override
    public void onIgnored(TypeDescription typeDescription,
                          ClassLoader classLoader,
                          JavaModule module,
                          boolean loaded) {
    }

    @Override
    public void onError(String typeName,
                        ClassLoader classLoader,
                        JavaModule module,
                        boolean loaded,
                        Throwable throwable) {
        System.out.println("Agent Error: " + throwable);
    }

    @Override
    public void onComplete(String typeName, ClassLoader classLoader, JavaModule module, boolean loaded) {
    }
}