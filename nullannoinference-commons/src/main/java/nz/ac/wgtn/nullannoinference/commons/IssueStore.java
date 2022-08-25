package nz.ac.wgtn.nullannoinference.commons;

import nz.ac.wgtn.nullannoinference.commons.json.JSONArray;
import nz.ac.wgtn.nullannoinference.commons.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple in memory store for issues collected during a program run.
 * Includes an export/save function that can be used in a shutdown hook.
 * @author jens dietrich
 */

public class IssueStore {

    private static final String INSTRUMENTATION_PACKAGE1 = "nz.ac.wgtn.nullannoinference.agent";
    private static final String INSTRUMENTATION_PACKAGE2 = "nz.ac.wgtn.nullannoinference.agent2";
    static {
        TimerTask task = new TimerTask() {
            public void run() {
                save();
            }
        };
        Timer timer = new Timer("issue store timer");

        long delay = 1000L * 60; // 1 min
        timer.schedule(task, delay, delay);

        Thread saveResult = new Thread(() -> IssueStore.save());
        Runtime.getRuntime().addShutdownHook(saveResult);
    }

    private static final String FILE_NAME = "null-issues-observed-while-testing";
    private static final Set<Issue> issues = Collections.newSetFromMap(new ConcurrentHashMap<>());

    enum StackTracePruningStrategy {KEEP_FULL,MODERATE, AGGRESSIVE};

    // @TODO make configurable with system variable
    static StackTracePruningStrategy PRUNING_STRATEGY = StackTracePruningStrategy.AGGRESSIVE;

    public static void add (Issue issue) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();
        List<String> stacktrace = Stream.of(stack)
            .map(element -> element.getClassName() + "::" + element.getMethodName() + ':' + element.getLineNumber())
            .collect(Collectors.toList());

        // sanitise stack trace
        if (!stacktrace.isEmpty()) {
            if (stacktrace.get(0).startsWith("java.lang.Thread::getStackTrace")) {
                stacktrace.remove(0);
            }

            // from instrumentation
            if (PRUNING_STRATEGY!= StackTracePruningStrategy.KEEP_FULL) {
                while (stacktrace.get(0).startsWith(IssueStore.class.getPackage().getName())) {
                    stacktrace.remove(0);
                }
                while (stacktrace.get(0).startsWith(INSTRUMENTATION_PACKAGE1) || stacktrace.get(0).startsWith(INSTRUMENTATION_PACKAGE2)) {
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
    public static void save () {

        if (issues.isEmpty()) {
            System.out.println("no issues recorded");
        }
        String fileName = FILE_NAME + '-' + System.currentTimeMillis() + ".json";
        File workingDir = new File(System.getProperty("user.dir"));
        File file = new File(workingDir,fileName);
        System.out.println("issues will be save to " + file.getAbsolutePath());

        Set<Issue> issuesToBeSaved = new HashSet<>();
        issuesToBeSaved.addAll(issues);
        System.out.println("saving " + issuesToBeSaved.size() + " issues");
        issues.removeAll(issuesToBeSaved);

        IssuePersistency.save (issuesToBeSaved, file);
    }

}
