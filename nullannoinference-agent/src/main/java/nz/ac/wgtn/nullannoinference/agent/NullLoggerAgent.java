package nz.ac.wgtn.nullannoinference.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.stream.Stream;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

/**
 * The actual agent.
 * @author jens dietrich
 */
public class NullLoggerAgent {

    public static final String PACKAGE_PREFIX_SYS_PROPERTY = "nz.ac.wgtn.nullannoinference.includes";

    static void log(String msg) {
        // disable as this creates issues with forked JVMs
        // System.out.println(NullLoggerAgent.class.getName() + ": " + msg);
    }


    public static void premain(String arg, Instrumentation inst) {

        String prefix = System.getProperty(PACKAGE_PREFIX_SYS_PROPERTY);
        if (prefix==null) {
            log("No class name prefix set, instrumenting all classes possible");
        }
        else {
            log("class name prefix set, instrumenting only classes with names starting with any of: " + prefix);
        }

        ElementMatcher.Junction<? super TypeDescription> matcher = (prefix==null) ?
            ElementMatchers.any() :
            Stream.of(prefix.split(","))
                .map(pf -> ElementMatchers.nameStartsWith(pf))
                .reduce(ElementMatchers.none(), (aggregation,next) ->  (aggregation.equals(ElementMatchers.none()))? next:aggregation.or(next));

        log("Instrumenting classes matching condition: " + matcher);

        new AgentBuilder.Default()
            .with(new DebugListener())
            // .with(AgentBuilder.Listener.StreamWriting.toSystemError())
            .ignore(nameStartsWith("net.bytebuddy.").or(nameStartsWith("java.").or(nameStartsWith("javax.").or(nameStartsWith("sun.")).or(nameStartsWith("com.sun.")))))
            .type(matcher)
            .transform(
                new AgentBuilder.Transformer() {
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
                        return builder.visit(NullChecks.MethodWithoutReturnValues.VISITOR);
                    }
                }
            )
            .transform(
                new AgentBuilder.Transformer() {
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
                        return builder.visit(NullChecks.MethodWithReturnValues.VISITOR);
                    }
                }
            )
            .transform(
                new AgentBuilder.Transformer() {
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
                        return builder.visit(NullChecks.Constructors.VISITOR);
                    }
                }
            )
            .transform(
                new AgentBuilder.Transformer() {
                    public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder, TypeDescription typeDescription, ClassLoader classLoader, JavaModule javaModule) {
                        return builder.visit(NullChecks.StaticBlocks.VISITOR);
                    }
                }
            )

//            .transform((builder, td, cl, m) -> builder.visit(NullChecks.MethodWithoutReturnValues.VISITOR))
//            .transform((builder, td, cl, m) -> builder.visit(NullChecks.MethodWithReturnValues.VISITOR))
//            .transform((builder, td, cl, m) -> builder.visit(NullChecks.Constructors.VISITOR))
//            .transform((builder, td, cl, m) -> builder.visit(NullChecks.StaticBlocks.VISITOR))
            .installOn(inst);
    }

}
