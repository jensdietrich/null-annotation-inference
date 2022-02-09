package nz.ac.wgtn.nullannoinference.negtests;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Copy / filter issues. Remove issues observed running negative tests.
 * @author jens dietrich
 */
public class SantitiseObservedIssues {

    public static void main (String[] args) throws Exception {

        Preconditions.checkArgument(args.length == 3, "three arguments required -- a folder containing issues (json-encoded, will be checked recursively), a folder to copy the issues not removed by filtering to, and a file with a list of negative tests");
        File inputFolder = new File(args[0]);
        Preconditions.checkArgument(inputFolder.exists(), inputFolder.getAbsolutePath() + " must exist");
        Preconditions.checkArgument(inputFolder.isDirectory(), inputFolder.getAbsolutePath() + " must be a folder");
        File outputFolder = new File(args[1]);
        if (!outputFolder.exists()) {
            outputFolder.mkdirs();
        }

        File negativeTestListFile = new File(args[2]);
        Preconditions.checkArgument(negativeTestListFile.exists());

        System.out.println("reading issues from " + inputFolder.getAbsolutePath());
        System.out.println("results will be copied to  " + outputFolder.getAbsolutePath());

        System.out.println("importing negative tests from " + negativeTestListFile.getAbsolutePath());
        List<String> methods = null; // representation to match representation is stacktraces
        try (BufferedReader in = new BufferedReader(new FileReader(negativeTestListFile))) {
            methods = in.lines()
                .map(line -> line.split("\t"))
                .map(tokens -> tokens[0] + "::" + tokens[1] + ":") // in stacktraces this is followed by a line number
                .collect(Collectors.toList());
        }

        Map<String,Integer> issueCountByProgram = new TreeMap<>();
        Map<String,Integer> rejectedIssueCountByProgram = new HashMap<>();
        for (File file:FileUtils.listFiles(inputFolder,new String[]{"json"},true)) {
            System.out.println("Processing " + file.getAbsolutePath());
            String program = getProgramName(file);
            Gson gson = new Gson();
            try (Reader in = new FileReader(file)){
                Issue[] issues = gson.fromJson(in,Issue[].class);
                System.out.println("\t" + issues.length + " issues found");
                issueCountByProgram.compute(program,(k,v) -> v==null?issues.length:v+issues.length);

                List<Issue> sanitisedIssues = new ArrayList<>();
                // check for any method in stacktrace -- note that this is O(n^2)
                for (Issue issue:issues) {
                    if (isCausedByNegativeTest(issue,methods)) {
                        rejectedIssueCountByProgram.compute(program,(k,v) -> v==null?1:v+1);
                    }
                    else {
                        sanitisedIssues.add(issue);
                    }
                }
                // todo write results
                if (sanitisedIssues.size()>0) {
                    Path rel = inputFolder.toPath().relativize(file.toPath());
                    File sanitised = new File(outputFolder,rel.toString());
                    System.out.println("\twriting sanitised set to " + sanitised.getAbsolutePath());
                    if (!sanitised.getParentFile().exists()) {
                        sanitised.getParentFile().mkdirs();
                    }

                    try (Writer out = new FileWriter(sanitised)) {
                        gson.toJson(sanitisedIssues, out);
                    }
                }
            }
        }

        System.out.println("Printing summary (program, all issues, rejected issues)");

        for (String program:issueCountByProgram.keySet()) {
            int rejectedCount = rejectedIssueCountByProgram.computeIfAbsent(program,pr->0);
            System.out.println(program + "\t" + issueCountByProgram.get(program) + "\t" + rejectedCount);
        }

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

    private static String getProgramName(File file) {
        String name = file.getName();
        if (file.getName().startsWith("commons-")) {
            String[] tokens = name.split("-");
            if (tokens.length>2) {
                return tokens[0]+"-"+tokens[1];
            }
        }
        return "<other>";
    }
}
