package nz.ac.wgtn.nullannoinference.agent;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import nz.ac.wgtn.nullannoinference.agent.shaded.org.json.*;


/**
 * Simple in memory store for issues collected during a program run.
 * Includes an export/save function that can be used in a shutdown hook.
 * @author jens dietrich
 */

public class IssueStore {

    private static final String FILE_NAME = "null-issues-observed-while-testing";
    private static final Set<Issue> issues = Collections.newSetFromMap(new ConcurrentHashMap<>());

    enum StackTracePruningStrategy {KEEP_FULL,MODERATE, AGGRESSIVE};

    // @TODO make configurable with system variable
    static StackTracePruningStrategy PRUNING_STRATEGY = StackTracePruningStrategy.AGGRESSIVE;

    public static void add (Issue issue) {

        List<String> stacktrace = Stream.of(Thread.currentThread().getStackTrace())
            .map(element -> element.getClassName() + "::" + element.getMethodName() + ':' + element.getLineNumber())
            .collect(Collectors.toList());

        // sanitise stack trace
        if (!stacktrace.isEmpty()) {
            if (stacktrace.get(0).startsWith("java.lang.Thread::getStackTrace")) {
                stacktrace.remove(0);
            }

            // from instrumentation
            if (PRUNING_STRATEGY!= StackTracePruningStrategy.KEEP_FULL) {
                while (stacktrace.get(0).startsWith(IssueStore.class.getPackageName())) {
                    stacktrace.remove(0);
                }
            }
            if (PRUNING_STRATEGY== StackTracePruningStrategy.AGGRESSIVE) {
                removeFromEndOfStacktrace(stacktrace,IS_SUREFIRE_INVOCATION);
                int firstTestFrameworkCallPosition = findFirstInvocationSuchThat(stacktrace,IS_TEST_FRAMEWORK_INVOCATION);
                if (firstTestFrameworkCallPosition>-1) {
                    stacktrace = stacktrace.subList(0,firstTestFrameworkCallPosition);
                }
                if (1==removeFromEndOfStacktrace(stacktrace,IS_REFLECTION_INVOCATION)) {
                    removeFromEndOfStacktrace(stacktrace,IS_JDK_INTERNAL_INVOCATION);
                }
            }


        }
        issue.setStacktrace(stacktrace);
        // System.out.println("null-related issue found: " + issue);

        issues.add(issue);
    }

    private static int removeFromEndOfStacktrace(List<String> stacktrace, Predicate<String> condition) {
        int counter = 0;
        while (stacktrace.size()>0 && condition.test(stacktrace.get(stacktrace.size()-1))) {
            stacktrace.remove(stacktrace.size()-1);
            counter = counter+1;
        }
        return counter;
    }

    private static int findFirstInvocationSuchThat(List<String> stacktrace, Predicate<String> condition) {
        for (int i=0;i<stacktrace.size();i++) {
            if (condition.test(stacktrace.get(i))) {
                return i;
            }
        }
        return -1;
    }

    private static Predicate<String> IS_REFLECTION_INVOCATION =  stackTraceElement -> stackTraceElement.startsWith("java.lang.reflect.Method::invoke");

    private static Predicate<String> IS_SUREFIRE_INVOCATION =  stackTraceElement -> stackTraceElement.startsWith("org.apache.maven.surefire.");

    private static Predicate<String> IS_TEST_FRAMEWORK_INVOCATION =  stackTraceElement -> stackTraceElement.startsWith("org.junit.") || stackTraceElement.startsWith("junit.") || stackTraceElement.startsWith("org.testng.");

    private static Predicate<String> IS_JDK_INTERNAL_INVOCATION = stackTraceElement -> stackTraceElement.startsWith("jdk.internal.");

    // for testing
    public static void clear () {
        issues.clear();
    }

    // for testing
    public static Set<Issue> getIssues() {
        return Collections.unmodifiableSet(issues);
    }

    // persistence
    static void save () {
        JSONArray array = new JSONArray();
        for (Issue issue:issues) {
            JSONObject jobj = new JSONObject();
            jobj.put("class",issue.getClassName());
            jobj.put("method",issue.getMethodName());
            jobj.put("descriptor",issue.getDescriptor());
            jobj.put("kind",issue.getKind().name());
            jobj.put("index",issue.getArgsIndex());
            jobj.put("context",issue.getContext());
            if (issue.getStacktrace()!=null) {
                for (String element : issue.getStacktrace()) {
                    jobj.append("stacktrace",element);
                }
            }
            array.put(jobj);
        }
        NullLoggerAgent.log("null issues discovered: " + array.length());
        String json = array.toString(3);
        try (PrintWriter out = new PrintWriter(new FileWriter(FILE_NAME + '-' + System.currentTimeMillis() + ".json"))) {
            out.println(json);
            NullLoggerAgent.log("null issues observed written to " + new File(FILE_NAME).getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
