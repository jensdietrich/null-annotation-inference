package nz.ac.wgtn.nullannoinference.agent;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.matcher.ElementMatchers;
import nz.ac.wgtn.nullannoinference.commons.IssueStore;

import java.lang.instrument.Instrumentation;
import java.util.stream.Stream;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

/**
 * The actual agent.
 * @author jens dietrich
 */
public class NullLoggerAgent {

    static {
        Thread saveResult = new Thread(() -> IssueStore.save());
        Runtime.getRuntime().addShutdownHook(saveResult);
    }

    public static final String PACKAGE_PREFIX_SYS_PROPERTY = "nz.ac.wgtn.nullannoinference.includes";

    static void log(String msg) {
        System.out.println(NullLoggerAgent.class.getSimpleName() + ": " + msg);
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
            .transform((builder, td, cl, m) -> builder.visit(NullChecks.MethodWithoutReturnValues.VISITOR))
            .transform((builder, td, cl, m) -> builder.visit(NullChecks.MethodWithReturnValues.VISITOR))
            .transform((builder, td, cl, m) -> builder.visit(NullChecks.Constructors.VISITOR))
            .installOn(inst);
    }



}
