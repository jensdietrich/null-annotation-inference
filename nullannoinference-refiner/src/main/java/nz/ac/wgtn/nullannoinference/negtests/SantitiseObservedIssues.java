package nz.ac.wgtn.nullannoinference.negtests;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import nz.ac.wgtn.nullannoinference.commons.IssueAggregator;
import nz.ac.wgtn.nullannoinference.LogSystem;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Copy / filter issues. Remove issues observed running negative tests.
 * @author jens dietrich
 */
public class SantitiseObservedIssues {

    public static final Logger LOGGER = LogSystem.getLogger("negative-test-analysis");
    public static final String COUNT_ALL_ISSUES = "all issues collected";
    public static final String COUNT_ALL_ISSUES_AGGREGATED = "all issues collected (aggregated)";
    public static final String COUNT_SANITISED_ISSUES = "sanitised issues";
    public static final String COUNT_SANITISED_ISSUES_AGGREGATED = "sanitised issues (aggregated)";

    public static void run (File originalIssueFolder, File sanitisedIssueFolder,File negativeTestList,Map<String,Integer> counts) throws Exception {

        Preconditions.checkArgument(originalIssueFolder.exists(), originalIssueFolder.getAbsolutePath() + " must exist");
        Preconditions.checkArgument(originalIssueFolder.isDirectory(), originalIssueFolder.getAbsolutePath() + " must be a folder");
        Preconditions.checkArgument(negativeTestList.exists());

        LOGGER.info("reading issues from " + originalIssueFolder.getAbsolutePath());
        LOGGER.info("sanitised issues will be saved in  " + sanitisedIssueFolder.getAbsolutePath());

        LOGGER.info("importing negative tests from " + negativeTestList.getAbsolutePath());
        List<String> methods = null; // representation to match representation is stacktraces
        try (BufferedReader in = new BufferedReader(new FileReader(negativeTestList))) {
            methods = in.lines()
                .map(line -> line.split("\t"))
                .map(tokens -> tokens[0] + "::" + tokens[1] + ":") // in stacktraces this is followed by a line number
                .collect(Collectors.toList());
        }

        Set<Issue> sanitisedIssues = new HashSet<>();
        Set<Issue> rejectedIssues = new HashSet<>();
        Set<Issue> allIssues = new HashSet<>();

        for (File file:FileUtils.listFiles(originalIssueFolder,new String[]{"json"},true)) {
            LOGGER.info("\t reading issues from " + file.getAbsolutePath());
            Gson gson = new Gson();
            try (Reader in = new FileReader(file)){
                Issue[] issues = gson.fromJson(in,Issue[].class);
                LOGGER.info("\t" + issues.length + " issues found");
                for (Issue issue:issues) {
                    allIssues.add(issue);
                }

                // check for any method in stacktrace -- note that this is O(n^2)
                for (Issue issue:issues) {
                    if (isCausedByNegativeTest(issue,methods)) {
                        rejectedIssues.add(issue);
                    }
                    else {
                        sanitisedIssues.add(issue);
                    }
                }
                // todo write results
                if (sanitisedIssues.size()>0) {
                    Path rel = originalIssueFolder.toPath().relativize(file.toPath());
                    File sanitised = new File(sanitisedIssueFolder,rel.toString());
                    LOGGER.info("\twriting sanitised set to " + sanitised.getAbsolutePath());
                    if (!sanitised.getParentFile().exists()) {
                        sanitised.getParentFile().mkdirs();
                    }

                    try (Writer out = new FileWriter(sanitised)) {
                        gson.toJson(sanitisedIssues, out);
                    }
                }
            }
        }

        counts.put(COUNT_ALL_ISSUES,allIssues.size());
        counts.put(COUNT_ALL_ISSUES_AGGREGATED, IssueAggregator.aggregate(allIssues).size());

        counts.put(COUNT_SANITISED_ISSUES,sanitisedIssues.size());
        counts.put(COUNT_SANITISED_ISSUES_AGGREGATED, IssueAggregator.aggregate(sanitisedIssues).size());

    }

    private static boolean isCausedByNegativeTest(Issue issue, List<String> methods) {
        for (String ste:issue.getStacktrace()) {
            for (String m:methods) {
                if (ste.startsWith(m)) {
                    return true;
                }
            }
        }
        return false;
    }


}
