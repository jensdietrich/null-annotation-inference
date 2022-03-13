package nz.ac.wgtn.nullinference.experiments;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import nz.ac.wgtn.nullannoinference.commons.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static nz.ac.wgtn.nullinference.experiments.Utils.*;

/**
 * Compare and report inferred vs actual annotations found in gradle projects such as spring modules.
 * @author jens dietrich
 */
public class ReportInferredVsActualAnnotationsInSpringProjects {

    public static Predicate<String> IS_SPRING_NULLABLE_ANNOTATION = descriptor -> descriptor.equals("Lorg/springframework/lang/Nullable;");

    public static final String CAPTION = "Actual and inferred nullability issues in spring projects";
    public static final String LABEL = "tab:spring";

    public static void main (String[] args) {
        Preconditions.checkArgument(args.length == 3, "three arguments required -- a folders containing project-specific subfolders with inferred issues (json), a folder containing projects, and output file (.tex)");

        File inferredIssuesFolder = new File(args[0]);
        Preconditions.checkState(inferredIssuesFolder.exists(), "folder does not exits: " + inferredIssuesFolder.getAbsolutePath());
        Preconditions.checkState(inferredIssuesFolder.isDirectory(), "folder must be folder: " + inferredIssuesFolder.getAbsolutePath());

        File projectRootFolder = new File(args[1]);
        Preconditions.checkState(projectRootFolder.exists(), "folder does not exits: " + projectRootFolder.getAbsolutePath());
        Preconditions.checkState(projectRootFolder.isDirectory(), "folder must be folder: " + projectRootFolder.getAbsolutePath());

        File outputFile = new File(args[2]);

        List<String> projects = getListOfProjects(inferredIssuesFolder);
        System.out.println("Results for the following projects will be analysed: " + projects.stream().collect(Collectors.joining(", ")));

        Project projectType = new MultiLanguageGradleProject();

        try (PrintWriter out = new PrintWriter(outputFile)) {

            addProvenanceToLatexOutput(out, ReportAnnotationResults.class);
            out.println("\\begin{table}[h!]");
            out.println("\\begin{tabular}{|lrrrr|}");
            out.println(" \\hline");
            out.println(" project & actual & inferred & recall & precision  \\\\ \\hline");

            for (String project : projects) {
                // load issues
                Set<Issue> inferredIssues = loadIssues(new File(inferredIssuesFolder, project), IssueFilters.MAIN_SCOPE);
                Set<IssueKernel> aggregatedInferredIssues = IssueAggregator.aggregate(inferredIssues);

                Set<Issue> foundIssues = NullableAnnotationAnalyser.findNullAnnotated(projectType, new File(projectRootFolder, project), IS_SPRING_NULLABLE_ANNOTATION);
                Set<IssueKernel> aggregatedFoundIssues = IssueAggregator.aggregate(foundIssues);

                System.out.println("Issues inferred: " + aggregatedInferredIssues.size());
                System.out.println("Issues found: " + aggregatedFoundIssues.size());

                Set<IssueKernel> FP = Sets.difference(aggregatedInferredIssues, aggregatedFoundIssues);
                Set<IssueKernel> FN = Sets.difference(aggregatedFoundIssues, aggregatedInferredIssues);
                Set<IssueKernel> TP = Sets.intersection(aggregatedFoundIssues, aggregatedInferredIssues);

                double precision = ((double) TP.size()) / ((double) (TP.size() + FP.size()));
                double recall = ((double) TP.size()) / ((double) (TP.size() + FN.size()));

                CollectMethodsByCallsite.CallsitePredicate callsiteSpec = new CollectMethodsByCallsite.CallsitePredicate() {
                    @Override
                    public boolean test(String klass, String method, String descriptor) {
                        return klass.equals("org/springframework/util/Assert") && method.equals("notNull");
                    }
                };
                Set<String> methodsWithDynamicCheck = CollectMethodsByCallsite.findMethodsWithCallsites(projectType, new File(projectRootFolder, project), callsiteSpec);

                System.out.println("FPs: " + FP.size());
                System.out.println("FNs: " + FN.size());
                System.out.println("TPs: " + TP.size());
                System.out.println("precision " + precision);
                System.out.println("recall " + recall);

                out.print(project);
                out.print(" & ");
                out.print(format(aggregatedFoundIssues.size()));
                out.print(" & ");
                out.print(format(aggregatedInferredIssues.size()));
                out.print(" & ");
                out.print(format(recall));
                out.print(" & ");
                out.print(format(precision));
                out.println(" \\\\");

            }
            out.println("\\hline");
            out.println("\\end{tabular}");
            out.println("\\caption{" + CAPTION + "}");
            out.println("\\label{" + LABEL + "}");
            out.println("\\end{table}");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }
}
