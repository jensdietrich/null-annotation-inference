package nz.ac.wgtn.nullinference.experiments;

import com.google.common.base.Preconditions;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;

import java.io.*;
import java.util.*;
import java.util.function.Predicate;
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

    public static final String CAPTION = "Null dereference issues reported by infer eradicate (erad) and nullsafe (nullsf) before and after annotation";
    public static final String LABEL = "tab:fbinfer";

    public static final Predicate<FBInferIssue> IS_NULLSAFE_ISSUE = i -> i.getBug_type().equals("NULL_DEREFERENCE") || i.getBug_type().equals("NULLPTR_DEREFERENCE");
    public static final Predicate<FBInferIssue> IS_ERADICATE_ISSUE = i -> i.getBug_type().startsWith("ERADICATE_");


    public static void main (String[] args) throws IOException {


        Preconditions.checkArgument(args.length==3,"three arguments required -- folders containing issues reported by infer for original and annotated projects, extracted with nullsafe (folder 1) and eradicate (folder 2)  (json), and output file (.tex)");

        File nullsafeIssuesFolder = new File(args[0]);
        Preconditions.checkState(nullsafeIssuesFolder.exists(),"folder does not exits: " + nullsafeIssuesFolder.getAbsolutePath());
        Preconditions.checkState(nullsafeIssuesFolder.isDirectory(),"folder must be folder: " + nullsafeIssuesFolder.getAbsolutePath());

        File orginalNullsafeProjectFolder = new File(nullsafeIssuesFolder,"original");
        Preconditions.checkState(orginalNullsafeProjectFolder.exists(),"folder does not exits: " + orginalNullsafeProjectFolder.getAbsolutePath());
        Preconditions.checkState(orginalNullsafeProjectFolder.isDirectory(),"folder must be folder: " + orginalNullsafeProjectFolder.getAbsolutePath());

        File annotatedNullsafeProjectFolder = new File(nullsafeIssuesFolder,"annotated");
        Preconditions.checkState(annotatedNullsafeProjectFolder.exists(),"folder does not exits: " + annotatedNullsafeProjectFolder.getAbsolutePath());
        Preconditions.checkState(annotatedNullsafeProjectFolder.isDirectory(),"folder must be folder: " + annotatedNullsafeProjectFolder.getAbsolutePath());

        File eradicateIssuesFolder = new File(args[1]);
        Preconditions.checkState(eradicateIssuesFolder.exists(),"folder does not exits: " + eradicateIssuesFolder.getAbsolutePath());
        Preconditions.checkState(eradicateIssuesFolder.isDirectory(),"folder must be folder: " + eradicateIssuesFolder.getAbsolutePath());

        File orginalEradicateProjectFolder = new File(eradicateIssuesFolder,"original");
        Preconditions.checkState(orginalEradicateProjectFolder.exists(),"folder does not exits: " + orginalEradicateProjectFolder.getAbsolutePath());
        Preconditions.checkState(orginalEradicateProjectFolder.isDirectory(),"folder must be folder: " + orginalEradicateProjectFolder.getAbsolutePath());

        File annotatedEradicateProjectFolder = new File(eradicateIssuesFolder,"annotated");
        Preconditions.checkState(annotatedEradicateProjectFolder.exists(),"folder does not exits: " + annotatedEradicateProjectFolder.getAbsolutePath());
        Preconditions.checkState(annotatedEradicateProjectFolder.isDirectory(),"folder must be folder: " + annotatedEradicateProjectFolder.getAbsolutePath());

        List<String> projects = Stream.of(orginalNullsafeProjectFolder.listFiles())
            .filter(f -> f.isDirectory() && !f.isHidden())
            .map(f -> f.getName())
            .sorted()
            .collect(Collectors.toList());

        List<String> projects2 = Stream.of(annotatedNullsafeProjectFolder.listFiles())
            .filter(f -> f.isDirectory() && !f.isHidden())
            .map(f -> f.getName())
            .sorted()
            .collect(Collectors.toList());

        List<String> projects3 = Stream.of(orginalEradicateProjectFolder.listFiles())
            .filter(f -> f.isDirectory() && !f.isHidden())
            .map(f -> f.getName())
            .sorted()
            .collect(Collectors.toList());

        List<String> projects4 = Stream.of(annotatedEradicateProjectFolder.listFiles())
            .filter(f -> f.isDirectory() && !f.isHidden())
            .map(f -> f.getName())
            .sorted()
            .collect(Collectors.toList());


        Preconditions.checkState(projects.equals(projects2),"original and annotated project list does not match");
        Preconditions.checkState(projects2.equals(projects3),"original and annotated project list does not match");
        Preconditions.checkState(projects3.equals(projects4),"original and annotated project list does not match");

        System.out.println("Results for the following projects will be analysed: " + projects.stream().collect(Collectors.joining(", ")));

        File outputFile = new File(args[2]);
        try (PrintWriter out = new PrintWriter(outputFile)) {

            addProvenanceToLatexOutput(out, ReportDeduplicationResults.class);

            out.println("\\begin{table}[h!]");
            out.println("\\begin{tabular}{|l|rr|rr|}");
            out.println(" \\hline");
            out.println("\\multicolumn{1}{|c}{\\multirow{2}{*}{project}} & \\multicolumn{2}{|c|}{original} & \\multicolumn{2}{|c|}{annotated} \\\\ ");
            out.println(" & erad & nullsf & erad & nullsf \\\\ ");
            out.println(" \\hline");

            // start latex generation
            for (String project:projects) {
                Set<FBInferIssue> nullsafeIssuesInOriginal = loadInferIssues(orginalNullsafeProjectFolder,project);
                System.out.println("issues loaded for " + project + " (original): " + nullsafeIssuesInOriginal.size());
                nullsafeIssuesInOriginal = nullsafeIssuesInOriginal.stream()
                    .filter(IS_NULLSAFE_ISSUE)
                    .collect(Collectors.toSet());
                System.out.println("\tnullderef nullsafe issues in original: " + nullsafeIssuesInOriginal.size());

                Set<FBInferIssue> nullsafeIssuesInAnnotated= loadInferIssues(annotatedNullsafeProjectFolder,project);
                System.out.println("issues loaded for " + project + " (annotated): " + nullsafeIssuesInAnnotated.size());
                nullsafeIssuesInAnnotated = nullsafeIssuesInAnnotated.stream()
                    .filter(IS_NULLSAFE_ISSUE)
                    .collect(Collectors.toSet());
                System.out.println("\tnullderef nullsafe issues in annotated: " + nullsafeIssuesInAnnotated.size());

                Set<FBInferIssue> eradicateIssuesInOriginal = loadInferIssues(orginalEradicateProjectFolder,project);
                System.out.println("issues loaded for " + project + " (original): " + eradicateIssuesInOriginal.size());
                eradicateIssuesInOriginal = eradicateIssuesInOriginal.stream()
                    .filter(IS_ERADICATE_ISSUE)
                    .collect(Collectors.toSet());
                System.out.println("\tnullderef eradicate issues: " + eradicateIssuesInOriginal.size());

                Set<FBInferIssue> eradicateIssuesInAnnotated = loadInferIssues(annotatedEradicateProjectFolder,project);
                System.out.println("issues loaded for " + project + " (annotated): " + eradicateIssuesInAnnotated.size());
                eradicateIssuesInAnnotated = eradicateIssuesInAnnotated.stream()
                    .filter(IS_ERADICATE_ISSUE)
                    .collect(Collectors.toSet());
                System.out.println("\tnullderef eradicate issues: " + eradicateIssuesInAnnotated.size());

                out.print(project);
                out.print(" & ");
                out.print(format(eradicateIssuesInOriginal.size()));
                out.print(" & ");
                out.print(format(nullsafeIssuesInOriginal.size()));
                out.print(" & ");
                out.print(format(eradicateIssuesInAnnotated.size()));
                out.print(" & ");
                out.print(format(nullsafeIssuesInAnnotated.size()));

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
        // Preconditions.checkState(inferResults.exists(),"file missing: " + inferResults.getAbsolutePath());
        try (Reader in = new FileReader(inferResults)) {
            FBInferIssue[] loaded = new Gson().fromJson(in, FBInferIssue[].class);
            // System.out.println("\t" + loaded.length + " imported");
            for (FBInferIssue issue : loaded) {
                    issues.add(issue);
            }
        } catch (IOException e) {
            // e.printStackTrace();
        }
        return issues;

    }




}
