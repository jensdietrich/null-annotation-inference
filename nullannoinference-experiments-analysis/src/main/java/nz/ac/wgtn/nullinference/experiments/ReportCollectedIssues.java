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

    public static final String CAPTION = "Collected issues by type and scope";
    public static final String LABEL = "tab:collected";

    public static void main (String[] args) throws FileNotFoundException {

        Preconditions.checkArgument(args.length==2,"two arguments required -- a folder containing project-specific subfolders with collected issues (json), and output file (.tex)");
        File collectedIssuesFolder = new File(args[0]);
        Preconditions.checkState(collectedIssuesFolder.exists(),"folder does not exits: " + collectedIssuesFolder.getAbsolutePath());
        Preconditions.checkState(collectedIssuesFolder.isDirectory(),"folder must be folder: " + collectedIssuesFolder.getAbsolutePath());

        File outputFile = new File(args[1]);

        List<String> projects = getListOfProjects(collectedIssuesFolder);
        System.out.println("Results for the following projects will be analysed: " + projects.stream().collect(Collectors.joining(", ")));

        try (PrintWriter out = new PrintWriter(outputFile)) {

            out.println("\\begin{table*}[h!]");
            out.println("\\begin{tabular}{|l|rrrr|rrrr|rrrr|}");
            out.println(" \\hline");
            out.println("\\multicolumn{1}{|c}{\\multirow{2}{*}{project}}  & \\multicolumn{4}{|c|}{main} & \\multicolumn{4}{|c|}{test} & \\multicolumn{4}{|c|}{other} \\\\ ");
            out.println("  & RET & ARG & FLD & ALL & RET & ARG & FLD & ALL & RET & ARG & FLD & ALL  \\\\ \\hline");

            // start latex generation
            for (String project:projects) {
                File projectFolder = new File(collectedIssuesFolder,project);
                Set<Issue> collectedIssuesMain = loadIssues(projectFolder,true,IssueFilters.MAIN_SCOPE);
                Set<Issue> collectedIssuesReturnMain = collectedIssuesMain.parallelStream().filter(IssueFilters.RETURN).collect(Collectors.toSet());
                Set<Issue> collectedIssuesArgMain = collectedIssuesMain.parallelStream().filter(IssueFilters.ARG).collect(Collectors.toSet());
                Set<Issue> collectedIssuesFieldMain = collectedIssuesMain.parallelStream().filter(IssueFilters.FIELD).collect(Collectors.toSet());

                Set<Issue> collectedIssuesTest = loadIssues(projectFolder,true,IssueFilters.TEST_SCOPE);
                Set<Issue> collectedIssuesReturnTest = collectedIssuesTest.parallelStream().filter(IssueFilters.RETURN).collect(Collectors.toSet());
                Set<Issue> collectedIssuesArgTest = collectedIssuesTest.parallelStream().filter(IssueFilters.ARG).collect(Collectors.toSet());
                Set<Issue> collectedIssuesFieldTest = collectedIssuesTest.parallelStream().filter(IssueFilters.FIELD).collect(Collectors.toSet());

                Set<Issue> collectedIssuesOther = loadIssues(projectFolder,true,IssueFilters.OTHER_SCOPE);
                Set<Issue> collectedIssuesReturnOther = collectedIssuesOther.parallelStream().filter(IssueFilters.RETURN).collect(Collectors.toSet());
                Set<Issue> collectedIssuesArgOther = collectedIssuesOther.parallelStream().filter(IssueFilters.ARG).collect(Collectors.toSet());
                Set<Issue> collectedIssuesFieldOther = collectedIssuesOther.parallelStream().filter(IssueFilters.FIELD).collect(Collectors.toSet());

                assert collectedIssuesMain.size() == collectedIssuesReturnMain.size() + collectedIssuesArgMain.size() + collectedIssuesFieldMain.size();
                assert collectedIssuesTest.size() == collectedIssuesReturnTest.size() + collectedIssuesArgTest.size() + collectedIssuesFieldTest.size();
                assert collectedIssuesOther.size() == collectedIssuesReturnOther.size() + collectedIssuesArgOther.size() + collectedIssuesFieldOther.size();

                out.print(project);
                out.print(" & ");
                out.print(format(collectedIssuesReturnMain.size()));
                out.print(" & ");
                out.print(format(collectedIssuesArgMain.size()));
                out.print(" & ");
                out.print(format(collectedIssuesFieldMain.size()));
                out.print(" & ");
                out.print(format(collectedIssuesMain.size()));
                out.print(" & ");
                out.print(format(collectedIssuesReturnTest.size()));
                out.print(" & ");
                out.print(format(collectedIssuesArgTest.size()));
                out.print(" & ");
                out.print(format(collectedIssuesFieldTest.size()));
                out.print(" & ");
                out.print(format(collectedIssuesTest.size()));
                out.print(" & ");
                out.print(format(collectedIssuesReturnOther.size()));
                out.print(" & ");
                out.print(format(collectedIssuesArgOther.size()));
                out.print(" & ");
                out.print(format(collectedIssuesFieldOther.size()));
                out.print(" & ");
                out.print(format(collectedIssuesOther.size()));
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
