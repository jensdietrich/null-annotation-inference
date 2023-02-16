package nz.ac.wgtn.nullinference.experiments.spring;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.IssueAggregator;
import nz.ac.wgtn.nullannoinference.commons.IssueKernel;
import nz.ac.wgtn.nullinference.experiments.descr.DescriptorParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Adhoc script to find issues that use java.lang.Void in descriptors.
 * Void is not instantiable so it can only be null -- it should therefore be annotated with @Nullable
 * errorprone has a rule VoidMissingNullable for this purpose
 * @author jens dietrich
 */
public class FindIssuesUsingVoid extends Experiment {

    public static final String MODULE = "error-prone";

    public static void main (String[] args) throws IOException {
        new FindIssuesUsingVoid().analyse();
    }


    public static final Predicate<IssueKernel> VOID_FILTER = issueKernel -> issueKernel.getDescriptor().contains("Ljava/lang/Void;");

    public void analyse()  {

        Set<Issue> extractedIssues = (Set<Issue>) readIssues(Config.EXTRACTED_ISSUES_FOLDER, MODULE, false);
        Set<Issue> observedIssues = (Set<Issue>) readIssues(Config.SANITIZED_ISSUES_FOLDER, MODULE, false);

        Map<IssueKernel,Set<Issue>> extractedIssueAggregation = IssueAggregator.aggregateAsMap(extractedIssues);
        Map<IssueKernel,Set<Issue>> observedIssueAggregation = IssueAggregator.aggregateAsMap(observedIssues);

        System.out.println("extracted (aggregated): " + extractedIssueAggregation.size());
        System.out.println("observed (aggregated): " + observedIssueAggregation.size());

        Set<IssueKernel> xtrMinusObs = Sets.difference(extractedIssueAggregation.keySet(),observedIssueAggregation.keySet());
        Set<IssueKernel> obsMinusXtr= Sets.difference(observedIssueAggregation.keySet(),extractedIssueAggregation.keySet());

        System.out.println("extracted - captured (FN?): " + xtrMinusObs.size());
        System.out.println("captured - extracted (FP?): " + obsMinusXtr.size());

        Set<IssueKernel> obsMinusXtrWithVoid = obsMinusXtr.stream()
                .filter(VOID_FILTER)
                .collect(Collectors.toSet());

        System.out.println("captured - extracted (FP?) with java.lang.Void in descriptor: " + obsMinusXtrWithVoid.size());





    }




}
