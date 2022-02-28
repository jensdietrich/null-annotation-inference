package nz.ac.wgtn.nullinference.experiments;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.Issue;
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

    public static final String CAPTION = "inferred issues by type ";
    public static final String LABEL = "tab:inferred";

    public static void main (String[] args) throws FileNotFoundException {

        Preconditions.checkArgument(args.length==3,"three arguments required -- two folders containing project-specific subfolders with collected / inferred issues (json), and output file");

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

            out.println("\\begin{table*}[h!]");
            out.println("\\begin{tabular}{|l|rrrr|rrrr|rrr|}");
            out.println(" \\hline");
            out.println("\\multicolumn{1}{|c}{\\multirow{2}{*}{project}}  & \\multicolumn{4}{|c|}{collected (main scope)} & \\multicolumn{4}{|c|}{neg. test santitised} & \\multicolumn{3}{|c|}{LSP inference} \\\\");
            out.println(" & RET & ARG & FLD & ALL & -RET & -ARG & -FLD & -ALL & +RET & +ARG & +ALL \\\\ \\hline");

            // start latex generation
            for (String project:projects) {
                File projectFolderWithCollectedIssues = new File(collectedIssuesFolder,project);
                File projectFolderWithInferredIssues = new File(inferredIssuesFolder,project);

                Set<Issue> collectedIssues = loadIssues(projectFolderWithCollectedIssues,true,IssueFilters.THIS_PROJECT,IssueFilters.COLLECTED,IssueFilters.MAIN_SCOPE);
                Set<Issue> collectedIssuesReturn = collectedIssues.parallelStream().filter(IssueFilters.RETURN).collect(Collectors.toSet());
                Set<Issue> collectedIssuesArg = collectedIssues.parallelStream().filter(IssueFilters.ARG).collect(Collectors.toSet());
                Set<Issue> collectedIssuesField = collectedIssues.parallelStream().filter(IssueFilters.FIELD).collect(Collectors.toSet());

                // sanitised issues -- some collected issues have been removed
                Set<Issue> sanitisedIssues = loadIssues(projectFolderWithInferredIssues,true,IssueFilters.THIS_PROJECT,IssueFilters.COLLECTED,IssueFilters.MAIN_SCOPE);
                Set<Issue> sanitisedIssuesReturn = sanitisedIssues.parallelStream().filter(IssueFilters.RETURN).collect(Collectors.toSet());
                Set<Issue> sanitisedIssuesArg = sanitisedIssues.parallelStream().filter(IssueFilters.ARG).collect(Collectors.toSet());
                Set<Issue> sanitisedIssuesField = sanitisedIssues.parallelStream().filter(IssueFilters.FIELD).collect(Collectors.toSet());

                // inferred issues do not have the scope attribute set
                Set<Issue> inferredIssues = loadIssues(projectFolderWithInferredIssues,true,IssueFilters.THIS_PROJECT,IssueFilters.INFERRED);
                Set<Issue> inferredIssuesReturn = inferredIssues.parallelStream().filter(IssueFilters.RETURN).collect(Collectors.toSet());
                Set<Issue> inferredIssuesArg = inferredIssues.parallelStream().filter(IssueFilters.ARG).collect(Collectors.toSet());

                assert inferredIssues.size() == inferredIssuesReturn.size() + inferredIssuesArg.size() ;

                out.print(project);

                // baseline
                out.print(" & ");
                out.print(format(collectedIssuesReturn.size()));
                out.print(" & ");
                out.print(format(collectedIssuesArg.size()));
                out.print(" & ");
                out.print(format(collectedIssuesField.size()));
                out.print(" & ");
                out.print(format(collectedIssues.size()));

                // rejected
                out.print(" & ");
                out.print("-"+format(collectedIssuesReturn.size()-sanitisedIssuesReturn.size()));
                out.print(" & ");
                out.print("-"+format(collectedIssuesArg.size()-sanitisedIssuesArg.size()));
                out.print(" & ");
                out.print("-"+format(collectedIssuesField.size()-sanitisedIssuesField.size()));
                out.print(" & ");
                out.print("-"+format(collectedIssues.size()-sanitisedIssues.size()));
                // inferred
                out.print(" & ");
                out.print("+"+format(inferredIssuesReturn.size()));
                out.print(" & ");
                out.print("+"+format(inferredIssuesArg.size()));
                out.print(" & ");
                out.print("+"+format(inferredIssues.size()));
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
