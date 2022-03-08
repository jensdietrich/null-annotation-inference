package nz.ac.wgtn.nullinference.experiments;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueAggregator;
import nz.ac.wgtn.nullannoinference.commons.IssueKernel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static nz.ac.wgtn.nullinference.experiments.Utils.*;

/**
 * Analyse results organised in folders for inferred issues, report numbers as table.
 * @author jens dietrich
 */
public class ReportInferredIssues {

    public static final String CAPTION = "Refined issues by type";
    public static final String LABEL = "tab:inferred";

    public static void main (String[] args) throws FileNotFoundException {

        Preconditions.checkArgument(args.length==3,"three arguments required -- two folders containing project-specific subfolders with collected / inferred issues (json), and output file (.tex)");

        File collectedIssuesFolder = new File(args[0]);
        Preconditions.checkState(collectedIssuesFolder.exists(),"folder does not exits: " + collectedIssuesFolder.getAbsolutePath());
        Preconditions.checkState(collectedIssuesFolder.isDirectory(),"folder must be folder: " + collectedIssuesFolder.getAbsolutePath());

        File inferredIssuesFolder = new File(args[1]);
        Preconditions.checkState(inferredIssuesFolder.exists(),"folder does not exits: " + inferredIssuesFolder.getAbsolutePath());
        Preconditions.checkState(inferredIssuesFolder.isDirectory(),"folder must be folder: " + inferredIssuesFolder.getAbsolutePath());

        File outputFile = new File(args[2]);

        List<String> projects = getListOfProjectsAndCheckConsistency(collectedIssuesFolder,inferredIssuesFolder);
        System.out.println("Results for the following projects will be analysed: " + projects.stream().collect(Collectors.joining(", ")));

        try (PrintWriter out = new PrintWriter(outputFile)) {

            addProvenanceToLatexOutput(out,ReportInferredIssues.class);

            out.println("\\begin{table*}[h!]");
            out.println("\\begin{tabular}{|l|rrrr|rrrr|rrr|rrrr|}");
            out.println(" \\hline");
            out.println("\\multicolumn{1}{|c}{\\multirow{2}{*}{project}}  & \\multicolumn{4}{|c|}{collected (main scope)} & \\multicolumn{4}{|c|}{neg. test santitised} & \\multicolumn{3}{|c|}{LSP inference} & \\multicolumn{4}{|c|}{refined issues} \\\\");
            out.println(" & RET & ARG & FLD & ALL & -RET & -ARG & -FLD & -ALL & +RET & +ARG & +ALL & RET & ARG & FLD & ALL \\\\ \\hline");

            // start latex generation
            for (String project:projects) {
                File projectFolderWithCollectedIssues = new File(collectedIssuesFolder,project);
                File projectFolderWithInferredIssues = new File(inferredIssuesFolder,project);

                Set<Issue> collectedIssues = loadIssues(projectFolderWithCollectedIssues,IssueFilters.THIS_PROJECT,IssueFilters.COLLECTED,IssueFilters.MAIN_SCOPE);
                Set<Issue> collectedIssuesReturn = collectedIssues.parallelStream().filter(IssueFilters.RETURN).collect(Collectors.toSet());
                Set<Issue> collectedIssuesArg = collectedIssues.parallelStream().filter(IssueFilters.ARG).collect(Collectors.toSet());
                Set<Issue> collectedIssuesField = collectedIssues.parallelStream().filter(IssueFilters.FIELD).collect(Collectors.toSet());
                assert collectedIssues.size() == collectedIssuesReturn.size() + collectedIssuesArg.size() + collectedIssuesField.size() ;

                // sanitised issues -- some collected issues have been removed
                Set<Issue> sanitisedIssues = loadIssues(projectFolderWithInferredIssues,IssueFilters.THIS_PROJECT,IssueFilters.COLLECTED,IssueFilters.MAIN_SCOPE);
                Set<Issue> sanitisedIssuesReturn = sanitisedIssues.parallelStream().filter(IssueFilters.RETURN).collect(Collectors.toSet());
                Set<Issue> sanitisedIssuesArg = sanitisedIssues.parallelStream().filter(IssueFilters.ARG).collect(Collectors.toSet());
                Set<Issue> sanitisedIssuesField = sanitisedIssues.parallelStream().filter(IssueFilters.FIELD).collect(Collectors.toSet());
                assert sanitisedIssues.size() == sanitisedIssuesReturn.size() + sanitisedIssuesArg.size() + sanitisedIssuesField.size() ;

                // inferred issues do not have the scope attribute set
                Set<Issue> inferredIssues = loadIssues(projectFolderWithInferredIssues,IssueFilters.THIS_PROJECT,IssueFilters.INFERRED);
                Set<Issue> inferredIssuesReturn = inferredIssues.parallelStream().filter(IssueFilters.RETURN).collect(Collectors.toSet());
                Set<Issue> inferredIssuesArg = inferredIssues.parallelStream().filter(IssueFilters.ARG).collect(Collectors.toSet());
                assert inferredIssues.size() == inferredIssuesReturn.size() + inferredIssuesArg.size() ;

                Set<Issue> refinedIssues = loadIssues(projectFolderWithInferredIssues,IssueFilters.THIS_PROJECT);
                Set<Issue> refinedIssuesReturn = refinedIssues.parallelStream().filter(IssueFilters.RETURN).collect(Collectors.toSet());
                Set<Issue> refinedIssuesArg = refinedIssues.parallelStream().filter(IssueFilters.ARG).collect(Collectors.toSet());
                Set<Issue> refinedIssuesField = refinedIssues.parallelStream().filter(IssueFilters.FIELD).collect(Collectors.toSet());
                assert refinedIssues.size() == refinedIssuesReturn.size() + refinedIssuesArg.size() + refinedIssuesField.size() ;

//                Set<Issue> rejectedIssues = Sets.difference(collectedIssues,sanitisedIssues);
//                Set<Issue> rejectedIssuesReturn = rejectedIssues.parallelStream().filter(IssueFilters.RETURN).collect(Collectors.toSet());
//                Set<Issue> rejectedIssuesArg = rejectedIssues.parallelStream().filter(IssueFilters.ARG).collect(Collectors.toSet());
//                Set<Issue> rejectedIssuesField = rejectedIssues.parallelStream().filter(IssueFilters.FIELD).collect(Collectors.toSet());
//                assert rejectedIssues.size() == rejectedIssuesReturn.size() + rejectedIssuesArg.size() + rejectedIssuesField.size() ;

                // aggregation
                Set<IssueKernel> aggregatedCollectedIssues = IssueAggregator.aggregate(collectedIssues);
                Set<IssueKernel> aggregatedCollectedIssuesReturn = IssueAggregator.aggregate(collectedIssuesReturn);
                Set<IssueKernel> aggregatedCollectedIssuesArg = IssueAggregator.aggregate(collectedIssuesArg);
                Set<IssueKernel> aggregatedCollectedIssuesField = IssueAggregator.aggregate(collectedIssuesField);
                assert aggregatedCollectedIssues.size() == aggregatedCollectedIssuesReturn.size() + aggregatedCollectedIssuesArg.size() + aggregatedCollectedIssuesField.size() ;

                Set<IssueKernel> aggregatedSanitisedIssues = IssueAggregator.aggregate(sanitisedIssues);
                Set<IssueKernel> aggregatedSanitisedIssuesReturn = IssueAggregator.aggregate(sanitisedIssuesReturn);
                Set<IssueKernel> aggregatedSanitisedIssuesArg = IssueAggregator.aggregate(sanitisedIssuesArg);
                Set<IssueKernel> aggregatedSanitisedIssuesField = IssueAggregator.aggregate(sanitisedIssuesField);
                assert aggregatedSanitisedIssues.size() == aggregatedSanitisedIssuesReturn.size() + aggregatedSanitisedIssuesArg.size() + aggregatedSanitisedIssuesField.size() ;

                // inferred issues do not have the scope attribute set
                Set<IssueKernel> aggregatedInferredIssues = IssueAggregator.aggregate(inferredIssues);
                Set<IssueKernel> aggregatedInferredIssuesReturn = IssueAggregator.aggregate(inferredIssuesReturn);
                Set<IssueKernel>  aggregatedInferredIssuesArg = IssueAggregator.aggregate(inferredIssuesArg);
                assert aggregatedInferredIssues.size() == aggregatedInferredIssuesReturn.size() + aggregatedInferredIssuesArg.size() ;

                Set<IssueKernel> aggregatedRefinedIssues = IssueAggregator.aggregate(refinedIssues);
                Set<IssueKernel> aggregatedRefinedIssuesReturn = IssueAggregator.aggregate(refinedIssuesReturn);
                Set<IssueKernel> aggregatedRefinedIssuesArg = IssueAggregator.aggregate(refinedIssuesArg);
                Set<IssueKernel> aggregatedRefinedIssuesField = IssueAggregator.aggregate(refinedIssuesField);
                assert aggregatedRefinedIssues.size() == aggregatedRefinedIssuesReturn.size() + aggregatedRefinedIssuesArg.size() + aggregatedRefinedIssuesField.size() ;

                Set<IssueKernel> aggregatedRejectedIssues = Sets.difference(aggregatedCollectedIssues,aggregatedSanitisedIssues);
                Set<IssueKernel> aggregatedRejectedIssuesReturn = aggregatedRejectedIssues.parallelStream().filter(k -> k.getKind()== Issue.IssueType.RETURN_VALUE).collect(Collectors.toSet());
                Set<IssueKernel> aggregatedRejectedIssuesArg = aggregatedRejectedIssues.parallelStream().filter(k -> k.getKind()== Issue.IssueType.ARGUMENT).collect(Collectors.toSet());
                Set<IssueKernel> aggregatedRejectedIssuesField = aggregatedRejectedIssues.parallelStream().filter(k -> k.getKind()== Issue.IssueType.FIELD).collect(Collectors.toSet());
                assert aggregatedRejectedIssues.size() == aggregatedRejectedIssuesReturn.size() + aggregatedRejectedIssuesArg.size() + aggregatedRejectedIssuesField.size() ;

                out.print(project);

                // baseline
                out.print(" & ");
                out.print(format(aggregatedCollectedIssuesReturn.size()));
                out.print(" & ");
                out.print(format(aggregatedCollectedIssuesArg.size()));
                out.print(" & ");
                out.print(format(aggregatedCollectedIssuesField.size()));
                out.print(" & ");
                out.print(format(aggregatedCollectedIssues.size()));

                // rejected
                out.print(" & ");
                out.print("-"+format(aggregatedRejectedIssuesReturn.size()));
                out.print(" & ");
                out.print("-"+format(aggregatedRejectedIssuesArg.size()));
                out.print(" & ");
                out.print("-"+format(aggregatedRejectedIssuesField.size()));
                out.print(" & ");
                out.print("-"+format(aggregatedRejectedIssues.size()));

                // inferred
                out.print(" & ");
                out.print("+"+format(aggregatedInferredIssuesReturn.size()));
                out.print(" & ");
                out.print("+"+format(aggregatedInferredIssuesArg.size()));
                out.print(" & ");
                out.print("+"+format(aggregatedInferredIssues.size()));

                // total
                out.print(" & ");
                out.print(format(aggregatedRefinedIssuesReturn.size()));
                out.print(" & ");
                out.print(format(aggregatedRefinedIssuesArg.size()));
                out.print(" & ");
                out.print(format(aggregatedRefinedIssuesField.size()));
                out.print(" & ");
                out.print(format(aggregatedRefinedIssues.size()));

                out.println("\\\\");
            }
            out.println("\\hline");
            out.println("\\end{tabular}");
            out.println("\\caption{" + CAPTION + "}");
            out.println("\\label{" + LABEL + "}");
            out.println("\\end{table*}");

        }


    }


}
