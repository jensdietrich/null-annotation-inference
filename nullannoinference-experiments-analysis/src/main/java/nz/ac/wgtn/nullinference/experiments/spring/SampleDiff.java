package nz.ac.wgtn.nullinference.experiments.spring;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import nz.ac.wgtn.nullannoinference.commons.*;
import nz.ac.wgtn.nullinference.experiments.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static nz.ac.wgtn.nullinference.experiments.spring.DataSet.SPRING_MODULES;

/**
 * Sample the difference between two issue sets.
 * @author jens dietrich
 */
public class SampleDiff extends Experiment {

    public static final File EXTRACTED_PLUS_ISSUES_FOLDER = new File("experiments-spring/results/extracted+");
    public static final File OBSERVED_PLUS_ISSUES_FOLDER = new File("experiments-spring/results/observed+");
    public static final File SHADING_SPECS = new File("experiments-spring/shaded.json");


    public static final String EXTRACTED_NOT_OBSERVED = "experiments-spring/results/diff/extracted-observed-<module>.json";
    public static final String OBSERVED_NOT_EXTRACTED = "experiments-spring/results/diff/observed-extracted-<module>.json";
    public static final int LIMIT = Integer.MAX_VALUE;

    public static void main (String[] args) throws IOException {

        Preconditions.checkArgument(EXTRACTED_PLUS_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(EXTRACTED_PLUS_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(OBSERVED_PLUS_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(OBSERVED_PLUS_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(SHADING_SPECS.exists());

        new SampleDiff().analyse();
    }

    public void analyse()  {

        // read shading specs
        Set<ShadingSpec> shadingSpecs = this.readShadingSpecs(SHADING_SPECS);
        Predicate<? extends AbstractIssue> shaded =
            issue -> shadingSpecs.stream().anyMatch(spec -> issue.getClassName().startsWith(spec.getRenamed()));

        for (String module: SPRING_MODULES) {
            Set<Issue> extracted = (Set<Issue>) readIssues(EXTRACTED_PLUS_ISSUES_FOLDER, module, false);
            Set<Issue> observed = (Set<Issue>) readIssues(OBSERVED_PLUS_ISSUES_FOLDER, module, false);

            Map<IssueKernel,Set<Issue>> extractedAggregated = IssueAggregator.aggregateAsMap(extracted);
            Map<IssueKernel,Set<Issue>> observedAggregated = IssueAggregator.aggregateAsMap(observed);

            Set<IssueKernel> extractedMinusObservedAggregated = Sets.difference(extractedAggregated.keySet(),observedAggregated.keySet());
            Set<IssueKernel> observedMinusExtractedAggregated = Sets.difference(observedAggregated.keySet(),extractedAggregated.keySet());
            Set<Issue> extractedMinusObserved = extractedMinusObservedAggregated.stream()
                .flatMap(kernel -> extractedAggregated.get(kernel).stream())
                .collect(Collectors.toSet());
            Set<Issue> observedMinusExtracted = observedMinusExtractedAggregated.stream()
                .flatMap(kernel -> observedAggregated.get(kernel).stream())
                .collect(Collectors.toSet());

            File fileExtractedMinusObserved = new File(EXTRACTED_NOT_OBSERVED.replace("<module>",module));
            IssuePersistency.save(extractedMinusObserved,fileExtractedMinusObserved);

            File fileObservedMinusExtracted = new File(OBSERVED_NOT_EXTRACTED.replace("<module>",module));
            IssuePersistency.save(observedMinusExtracted,fileObservedMinusExtracted);

        }
    }

}
