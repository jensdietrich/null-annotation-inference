package nz.ac.wgtn.nullinference.experiments;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import nz.ac.wgtn.nullannoinference.commons.Issue;

import java.io.*;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static nz.ac.wgtn.nullinference.experiments.Utils.*;

/**
 * Aggregate and report the annotation results.
 * Those summaries are generated by nz.ac.wgtn.nullannoinference.annotator.LoggingAnnotationListener -- check for keys for details
 * @author jens dietrich
 */
public class ReportAnnotationResults {

    public static final String CAPTION = "Annotation recall";
    public static final String LABEL = "tab:annotated";

    public static void main (String[] args) throws FileNotFoundException {

        Preconditions.checkArgument(args.length==2,"two arguments required -- a folder containing annotation result summaries (<project-name>.json, folder default name is .annotation-results), and output file (.tex)");
        File annotationSummaryFolder = new File(args[0]);
        Preconditions.checkState(annotationSummaryFolder.exists(),"folder does not exits: " + annotationSummaryFolder.getAbsolutePath());
        Preconditions.checkState(annotationSummaryFolder.isDirectory(),"folder must be folder: " + annotationSummaryFolder.getAbsolutePath());

        File outputFile = new File(args[1]);

        List<String> projects = Stream.of(annotationSummaryFolder.listFiles())
            .filter(f -> !f.isDirectory())
            .filter(f -> !f.isHidden())
            .filter(f -> !f.getName().contains("open-issues"))
            .map(f -> f.getName().replace(".json",""))
            .sorted()
            .collect(Collectors.toList());
        System.out.println("Results for the following projects will be analysed: " + projects.stream().collect(Collectors.joining(", ")));

        try (PrintWriter out = new PrintWriter(outputFile)) {

            out.println("\\begin{table}[h!]");
            out.println("\\begin{tabular}{|lrrr|}");
            out.println(" \\hline");
            out.println(" project & issues & open issues & recall  \\\\ \\hline");

            // start latex generation
            for (String project:projects) {
                File projectDataFile = new File(annotationSummaryFolder,project + ".json");
                Map<String, Integer> projectData = null;
                try (FileReader reader = new FileReader(projectDataFile)) {
                    projectData = new Gson().fromJson(reader, new TypeToken<Map<String, Integer>>() {}.getType());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                assert projectData != null;

                Integer totalIssues = projectData.get("total-issues");
                assert totalIssues != null;
                Integer openIssues = projectData.get("open-issues");
                assert openIssues != null;

                double recall = (double)(totalIssues-openIssues)/(double)totalIssues;

                out.print(project);
                out.print(" & ");
                out.print(format(totalIssues));
                out.print(" & ");
                out.print(format(openIssues));
                out.print(" & ");
                out.print(format(recall));
                out.println("\\\\");
            }
            out.println("\\hline");
            out.println("\\end{tabular}");
            out.println("\\caption{" + CAPTION + "}");
            out.println("\\label{" + LABEL + "}");
            out.println("\\end{table}");

            System.out.println("result summary written to " + outputFile.getAbsolutePath());

        }

    }

}
