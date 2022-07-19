package nz.ac.wgtn.nullannoinference.sanitizer;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Copy / filter issues. Remove issues observed running negative tests.
 * @author jens dietrich
 */
public class SantitiseObservedIssues {

    public static final Logger LOGGER = LogSystem.getLogger("negative-test-analysis");

    public static void run (File originalIssuesFile, File sanitisedIssuesFile, File negativeTestList, Predicate<Issue> issueFilter) throws Exception {

        Preconditions.checkArgument(originalIssuesFile.exists(), originalIssuesFile.getAbsolutePath() + " must exist");
        Preconditions.checkArgument(negativeTestList.exists());

        LOGGER.info("reading issues from " + originalIssuesFile.getAbsolutePath());
        LOGGER.info("sanitised issues will be saved in  " + sanitisedIssuesFile.getAbsolutePath());

        LOGGER.info("importing negative tests from " + negativeTestList.getAbsolutePath());
        List<String> methods = null; // representation to match representation is stacktraces
        try (BufferedReader in = new BufferedReader(new FileReader(negativeTestList))) {
            methods = in.lines()
                .map(line -> line.split("\t"))
                .map(tokens -> tokens[0] + "::" + tokens[1] + ":") // in stacktraces this is followed by a line number
                .collect(Collectors.toList());
        }
        List<String> methods2 = methods;

        Set<Issue> sanitisedIssues = new HashSet<>();
        Set<Issue> rejectedIssues = new HashSet<>();
        Set<Issue> allIssues = new HashSet<>();

        Set<Issue> sanitisedIssues2 = new HashSet<>();
        Set<Issue> rejectedIssues2 = new HashSet<>();
        Set<Issue> allIssues2 = new HashSet<>();
        Gson gson = new Gson();
        try (Reader in = new FileReader(originalIssuesFile)){
            Issue[] issues = gson.fromJson(in,Issue[].class);
            LOGGER.info("\t" + issues.length + " issues found");
            allIssues2 = Stream.of(issues).filter(issueFilter).collect(Collectors.toSet());
            rejectedIssues2 = allIssues2.stream().filter(i -> isCausedByNegativeTest(i,methods2)).collect(Collectors.toSet());
            sanitisedIssues2 = allIssues2.stream().filter(i -> !isCausedByNegativeTest(i,methods2)).collect(Collectors.toSet());
        }
        catch (Exception x) {
            LOGGER.error("Error reading issues from " + originalIssuesFile.getAbsolutePath());
        }

        allIssues.addAll(allIssues2);
        rejectedIssues.addAll(rejectedIssues2);
        sanitisedIssues.addAll(sanitisedIssues2);

        // todo write results
        if (sanitisedIssues2.size()>0) {
            LOGGER.info("\twriting sanitised set to " + sanitisedIssuesFile.getAbsolutePath());
            if (!sanitisedIssuesFile.getParentFile().exists()) {
                sanitisedIssuesFile.getParentFile().mkdirs();
            }

            try (Writer out = new FileWriter(sanitisedIssuesFile)) {
                gson.toJson(sanitisedIssues2, out);
            }
            catch (Exception x) {
                LOGGER.error("Error writing issues to " + sanitisedIssuesFile.getAbsolutePath());
            }
        }


    }

    private static boolean isCausedByNegativeTest(Issue issue, List<String> methods) {
        if (issue.getStacktrace()==null || issue.getStacktrace().isEmpty()) {
            LOGGER.warn("cannot analyse issue for negative test cause, no stacktrace found, assume that it is not caused by negative test (issue provenance type is " + issue.getProvenanceType() + ")");
            return false;
        }
        else {
            for (String ste : issue.getStacktrace()) {
                for (String m : methods) {
                    if (ste.startsWith(m)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }


}
