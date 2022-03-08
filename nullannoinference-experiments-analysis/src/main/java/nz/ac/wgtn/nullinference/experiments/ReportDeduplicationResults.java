package nz.ac.wgtn.nullinference.experiments;

import com.google.common.base.Preconditions;
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
 * Analyse and report the level of deduplication applied..
 * @author jens dietrich
 */
public class ReportDeduplicationResults {

    public static final String CAPTION = "Impact of deduplication";
    public static final String LABEL = "tab:deduplicated";

    public static void main (String[] args) throws FileNotFoundException {

        Preconditions.checkArgument(args.length==2,"two arguments required -- a folder containing project-specific subfolders with collected issues (json), and output file (.tex)");
        File collectedIssuesFolder = new File(args[0]);
        Preconditions.checkState(collectedIssuesFolder.exists(),"folder does not exits: " + collectedIssuesFolder.getAbsolutePath());
        Preconditions.checkState(collectedIssuesFolder.isDirectory(),"folder must be folder: " + collectedIssuesFolder.getAbsolutePath());

        File outputFile = new File(args[1]);

        List<String> projects = getListOfProjects(collectedIssuesFolder);
        System.out.println("Results for the following projects will be analysed: " + projects.stream().collect(Collectors.joining(", ")));

        try (PrintWriter out = new PrintWriter(outputFile)) {

            addProvenanceToLatexOutput(out, ReportDeduplicationResults.class);

            out.println("\\begin{table}[h!]");
            out.println("\\begin{tabular}{|l|rr|r|}");
            out.println(" \\hline");
            out.println("\\multicolumn{1}{|c}{\\multirow{2}{*}{project}}  & \\multicolumn{2}{|c|}{collected issues} & \\multicolumn{1}{|c|}{\\multirow{2}{*}{ratio}}  \\\\ ");
            out.println("  & raw & dedupl. &  \\\\ \\hline");

            // start latex generation
            for (String project:projects) {
                File projectFolder = new File(collectedIssuesFolder,project);
                Set<Issue> collectedIssues = loadIssues(projectFolder);
                Set<IssueKernel> aggregatedIssues = IssueAggregator.aggregate(collectedIssues);
                double ratio = (double)aggregatedIssues.size() / (double)collectedIssues.size() ;

                out.print(project);
                out.print(" & ");
                out.print(format(collectedIssues.size()));
                out.print(" & ");
                out.print(format(aggregatedIssues.size()));
                out.print(" & ");
                out.print(format2(ratio));
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
