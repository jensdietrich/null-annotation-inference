package nz.ac.wgtn.nullannoinference.merger;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.scoper.LogSystem;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility to merge issue sets.
 * @author jens dietrich
 */
public class IssueSetMerger {

    public static final Logger LOGGER = LogSystem.getLogger("issue-merger");

    private static final Type ISSUE_SET_TYPE = new TypeToken<Set<Issue>>() {}.getType();

    public static void main (String[] args) throws Exception {

        Options options = new Options();
        options.addRequiredOption("i", "input", true, "the Json files to be merged, comma separated, if those a folders, all JSON files within those folders (non-recursive!) will be merged (required)");
        options.addRequiredOption("o", "output", true, "the new merged file to be generated (required)");

        CommandLineParser parser = new DefaultParser() {
            @Override
            protected void checkRequiredOptions() throws MissingOptionException {
                try {
                    super.checkRequiredOptions();
                } catch (MissingOptionException x) {
                    LOGGER.error("arguments missing", x);
                    // print help instructions
                    String header = "Merge issue sets\n\n";
                    String footer = "\nPlease report issues at https://github.com/jensdietrich/null-annotation-inference/issues";
                    HelpFormatter formatter = new HelpFormatter();
                    formatter.printHelp(IssueSetMerger.class.getName(), header, options, footer, true);
                    System.exit(1);
                }
            }
        };
        CommandLine cmd = parser.parse(options, args);
        String inputNames = cmd.getOptionValue("input");
        File output = new File(cmd.getOptionValue("output"));

        String[] inputNames2 = inputNames.split(",");
        Set<Issue> issues = new HashSet<>();
        for (String issueName:inputNames2) {
            File input = new File(issueName);
            Preconditions.checkState(input.exists(),"File does not exist: " + input.getAbsolutePath());
            if (input.isDirectory()) {
                for (File f:input.listFiles(n -> n.getName().toLowerCase().endsWith(".json"))) {
                    parseIssues(f,issues);
                }
            }
            else {
                parseIssues(input,issues);
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter out = new FileWriter(output)) {
            gson.toJson(issues,out);
        }
        catch (Exception x) {
            LOGGER.error("Error writing issues to file " + output.getAbsolutePath(),x);
        }
        LOGGER.info(issues.size() + " issues merged and exported to " + output.getAbsolutePath());
    }

    private static void parseIssues(File input, Set<Issue> issues) {
        Gson gson = new Gson();
        try (FileReader in = new FileReader(input)) {
            Set<Issue> issues2 = gson.fromJson(in,ISSUE_SET_TYPE);
            issues.addAll(issues2);
            LOGGER.info(issues2.size() + " issues read from " + input.getAbsolutePath() + " and merged");
        }
        catch (Exception x) {
            LOGGER.error("Error reading issues from file " + input.getAbsolutePath(),x);
        }
    }

}
