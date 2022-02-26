package nz.ac.wgtn.nullinference.experiments;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import static nz.ac.wgtn.nullinference.experiments.Utils.*;

/**
 * Analyse results organised in folders for collected issues, report numbers as table.
 * @author jens dietrich
 */
public class ReportCollectedIssues {

    public static final String CAPTION = "collected issues by type";
    public static final String LABEL = "tab:collected";

    public static void main (String[] args) throws FileNotFoundException {

        Preconditions.checkArgument(args.length==2,"two arguments required -- a folder containing project-specific subfolders with collected issues (json), and output file");
        File collectedIssuesFolder = new File(args[0]);
        Preconditions.checkState(collectedIssuesFolder.exists(),"folder does not exits: " + collectedIssuesFolder.getAbsolutePath());
        Preconditions.checkState(collectedIssuesFolder.isDirectory(),"folder must be folder: " + collectedIssuesFolder.getAbsolutePath());

        File outputFile = new File(args[1]);

        List<String> projects = getListOfProjects(collectedIssuesFolder);
        System.out.println("Results for the following projects will be analysed: " + projects.stream().collect(Collectors.joining(", ")));

        try (PrintWriter out = new PrintWriter(outputFile)) {

            out.println("\\begin{table}[h!]");
            out.println("\\begin{tabular}{|l|rrrr|}");
            out.println(" \\hline");
            out.println("project & RET & ARG & FLD & ALL  \\hline");

            // start latex generation
            for (String project:projects) {
                File projectFolder = new File(collectedIssuesFolder,project);
                Set<Issue> collectedIssues = loadIssues(projectFolder,true,IssueFilters.THIS_PROJECT);
                Set<Issue> collectedIssuesReturn = loadIssues(projectFolder,true,IssueFilters.THIS_PROJECT,IssueFilters.RETURN);
                Set<Issue> collectedIssuesArg = loadIssues(projectFolder,true,IssueFilters.THIS_PROJECT,IssueFilters.ARG);
                Set<Issue> collectedIssuesField = loadIssues(projectFolder,true,IssueFilters.THIS_PROJECT,IssueFilters.FIELD);

                assert collectedIssues.size() == collectedIssuesReturn.size() + collectedIssuesArg.size() + collectedIssuesField.size();

                out.print(project);
                out.print(" & ");
                out.print(format(collectedIssuesReturn.size()));
                out.print(" & ");
                out.print(format(collectedIssuesArg.size()));
                out.print(" & ");
                out.print(format(collectedIssuesField.size()));
                out.print(" & ");
                out.print(format(collectedIssues.size()));
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
