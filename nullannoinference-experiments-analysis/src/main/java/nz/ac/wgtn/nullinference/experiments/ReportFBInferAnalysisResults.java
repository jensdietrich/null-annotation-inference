package nz.ac.wgtn.nullinference.experiments;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nz.ac.wgtn.nullinference.experiments.Utils.*;

/**
 * Report fbinfer results for annotated and original versions.
 * @author jens dietrich
 */
public class ReportFBInferAnalysisResults {

    // simple latex report, reporting issues found
    public static final File SIMPLE_LATEX_REPORT_NAME = new File("infer-issues.tex");

    // more advanced latex report, reporting issues found and diff to baseline
    public static final File ADVANCED_LATEX_REPORT_NAME = new File("infer-issues-diff.tex");

    public static final String CAPTION = "Null dereference issues issues reported by fbinfer before and after annotations";
    public static final String LABEL = "tab:fbinfer";

    public static final Set<String> ISSUE_TYPES = Set.of("NULL_DEREFERENCE");

    public static void main (String[] args) throws IOException {


        Preconditions.checkArgument(args.length==2,"two arguments required -- a folder containing issues reported by infer for original and annotated projects (json), and output file (.tex)");
        File inferIssuesFolder = new File(args[0]);
        Preconditions.checkState(inferIssuesFolder.exists(),"folder does not exits: " + inferIssuesFolder.getAbsolutePath());
        Preconditions.checkState(inferIssuesFolder.isDirectory(),"folder must be folder: " + inferIssuesFolder.getAbsolutePath());

        File orginalProjectFolder = new File(inferIssuesFolder,"original");
        Preconditions.checkState(orginalProjectFolder.exists(),"folder does not exits: " + orginalProjectFolder.getAbsolutePath());
        Preconditions.checkState(orginalProjectFolder.isDirectory(),"folder must be folder: " + orginalProjectFolder.getAbsolutePath());

        File annotatedProjectFolder = new File(inferIssuesFolder,"annotated");
        Preconditions.checkState(annotatedProjectFolder.exists(),"folder does not exits: " + annotatedProjectFolder.getAbsolutePath());
        Preconditions.checkState(annotatedProjectFolder.isDirectory(),"folder must be folder: " + annotatedProjectFolder.getAbsolutePath());

        List<String> projects = Stream.of(orginalProjectFolder.listFiles())
            .filter(f -> f.isDirectory() && !f.isHidden())
            .map(f -> f.getName())
            .sorted()
            .collect(Collectors.toList());

        List<String> projects2 = Stream.of(annotatedProjectFolder.listFiles())
            .filter(f -> f.isDirectory() && !f.isHidden())
            .map(f -> f.getName())
            .sorted()
            .collect(Collectors.toList());

        Preconditions.checkState(projects.equals(projects2),"original and annotated project list does not match");


        System.out.println("Results for the following projects will be analysed: " + projects.stream().collect(Collectors.joining(", ")));

        File outputFile = new File(args[1]);
        try (PrintWriter out = new PrintWriter(outputFile)) {

            addProvenanceToLatexOutput(out, ReportDeduplicationResults.class);

            out.println("\\begin{table}[h!]");
            out.println("\\begin{tabular}{|l|r|r|}");
            out.println(" \\hline");
            out.println("project & original & annotated \\\\ ");
            out.println(" \\hline");

            // start latex generation
            for (String project:projects) {
                Set<FBInferIssue> issuesInOriginal = loadInferIssues(orginalProjectFolder,project);
                System.out.println("issues loaded for " + project + " (original): " + issuesInOriginal.size());
                Set<FBInferIssue> nullderefIssuesInOriginal = issuesInOriginal.stream()
                        .filter(issue -> ISSUE_TYPES.contains(issue.getBug_type()))
                        .collect(Collectors.toSet());
                System.out.println("\tnullderef issues: " + nullderefIssuesInOriginal.size());

                Set<FBInferIssue> issuesInAnnotated= loadInferIssues(annotatedProjectFolder,project);
                System.out.println("issues loaded for " + project + " (annotated): " + issuesInAnnotated.size());
                Set<FBInferIssue> nullderefIssuesInAnnotated = issuesInAnnotated.stream()
                        .filter(issue -> ISSUE_TYPES.contains(issue.getBug_type()))
                        .collect(Collectors.toSet());
                System.out.println("\tnullderef issues: " + nullderefIssuesInAnnotated.size());

                out.print(project);
                out.print(" & ");
                out.print(format(nullderefIssuesInOriginal.size()));
                out.print(" & ");
                out.print(format(nullderefIssuesInAnnotated.size()));

                out.println("\\\\");
            }
            out.println("\\hline");
            out.println("\\end{tabular}");
            out.println("\\caption{" + CAPTION + "}");
            out.println("\\label{" + LABEL + "}");
            out.println("\\end{table}");

        }

    }


    private static void checkForMultipleIssuesInTheSameProcedure(List<String> datasetNames,List<String> programNames, Map<String, Multimap<String, FBInferIssue>> issuesByRunAndProgram) {
        System.out.println();
        System.out.println("Reporting methods with multiple issues ----------------------");
        for (String programName:programNames) {
            for (String datasetName:datasetNames) {
                Set<FBInferIssue> issues = issuesByRunAndProgram.get(datasetName).get(programName).stream().collect(Collectors.toSet());
                Map<String,Set<FBInferIssue>> issuesByProcedure = new HashMap<>();
                for (FBInferIssue issue:issues) {
                    issuesByProcedure.compute(issue.getProcedure(), (k,v) -> {
                        if (v==null) {
                            Set<FBInferIssue> set = new HashSet<>();
                            set.add(issue);
                            return set;
                        }
                        else {
                            v.add(issue);
                            return v;
                        }
                    });
                }

                for (String method:issuesByProcedure.keySet()) {
                    Set<FBInferIssue> relevantIssues = issuesByProcedure.get(method);
                    if (relevantIssues.size()>1) {
                        System.out.println("\t" + programName + " (" + datasetName + "): " + method + " -> " + relevantIssues.size() + " issues");
                    }
                }
            }
        }
    }

    private static Set<FBInferIssue> loadInferIssues (File issueFolder,String project) {

        Set<FBInferIssue> issues = new HashSet<>();
        File inferResults = new File(issueFolder, project+"/infer-out/report.json");
        try (Reader in = new FileReader(inferResults)) {
            FBInferIssue[] loaded = new Gson().fromJson(in, FBInferIssue[].class);
            // System.out.println("\t" + loaded.length + " imported");
            for (FBInferIssue issue : loaded) {
                    issues.add(issue);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return issues;

    }




}
