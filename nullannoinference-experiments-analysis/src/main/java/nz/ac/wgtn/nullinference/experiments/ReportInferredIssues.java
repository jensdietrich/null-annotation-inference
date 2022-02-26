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

    public static final String CAPTION = "inferred issues by type";
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

            out.println("\\begin{table}[h!]");
            out.println("\\begin{tabular}{|l|rrrr|}");
            out.println(" \\hline");
            out.println("project & RET & ARG & ALL  \\hline");

            // start latex generation
            for (String project:projects) {
                File projectFolder = new File(inferredIssuesFolder,project);
                Set<Issue> inferredIssues = loadIssues(projectFolder,true,IssueFilters.THIS_PROJECT,IssueFilters.INFERRED);
                Set<Issue> inferredIssuesReturn = loadIssues(projectFolder,true,IssueFilters.THIS_PROJECT,IssueFilters.RETURN,IssueFilters.INFERRED);
                Set<Issue> inferredIssuesArg = loadIssues(projectFolder,true,IssueFilters.THIS_PROJECT,IssueFilters.ARG,IssueFilters.INFERRED);

                assert inferredIssues.size() == inferredIssuesReturn.size() + inferredIssuesArg.size() ;

                out.print(project);
                out.print(" & ");
                out.print(inferredIssuesReturn.size());
                out.print(" & ");
                out.print(inferredIssuesArg.size());
                out.print(" & ");
                out.print(inferredIssues.size());
                out.println("\\\\");
            }
            out.println("\\hline");
            out.println("\\end{tabular}");
            out.println("\\caption{" + CAPTION + "}");
            out.println("\\label{" + LABEL + "}");
            out.println("\\end{table}");

        }


    }


}
