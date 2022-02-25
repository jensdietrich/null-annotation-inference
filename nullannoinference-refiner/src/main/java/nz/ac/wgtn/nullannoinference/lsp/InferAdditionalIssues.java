package nz.ac.wgtn.nullannoinference.lsp;

import com.google.common.base.Preconditions;
import com.google.common.graph.Graph;
import com.google.common.graph.Graphs;
import com.google.common.graph.Traverser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import nz.ac.wgtn.nullannoinference.IssueAggregator;
import nz.ac.wgtn.nullannoinference.LogSystem;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Infer additional issues by applying LSP.
 * @author jens dietrich
 */
public class InferAdditionalIssues {

    public static final Logger LOGGER = LogSystem.getLogger("infer-additional-issues");
    public static final String COUNT_NULLABILITY_ISSUES_INFERRED = "additional issues inferred";
    public static final String COUNT_NULLABILITY_ISSUES_INFERRED_AGGREGATED = "additional issues inferred (aggregated)";

    public static void run (File issueInputFolder,File projectFolder, File outputFile,String prefix, boolean propagateNullabilityInArguments,Map<String,Integer> counts) throws Exception {

        Preconditions.checkArgument(issueInputFolder.exists(), issueInputFolder.getAbsolutePath() + " must exist");
        Preconditions.checkArgument(issueInputFolder.isDirectory(), issueInputFolder.getAbsolutePath() + " must be a folder");

        Preconditions.checkArgument(projectFolder.exists(), projectFolder.getAbsolutePath() + " must exist");
        Preconditions.checkArgument(projectFolder.isDirectory(), projectFolder.getAbsolutePath() + " must be a folder");

        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        LOGGER.info("reading issues from " + issueInputFolder.getAbsolutePath());
        LOGGER.info("analysing project" + projectFolder.getAbsolutePath());
        LOGGER.info("additional inferred issues will be written to  " + outputFile.getAbsolutePath());
        LOGGER.info("only classes starting with the following name will be considered  " + prefix);


        // extract overrides
        File[] arr = new File[]{projectFolder};
        Graph<OwnedMethod> overrides = OverrideExtractor.extractOverrides(t -> t.startsWith(prefix),arr);

        LOGGER.info("" + overrides.edges().size() + " override relationships found");

        // extract issues
        Set<Issue> issues = new HashSet<>();
        for (File file:FileUtils.listFiles(issueInputFolder,new String[]{"json"},true)) {
            LOGGER.info("Processing " + file.getAbsolutePath());
            Gson gson = new Gson();
            try (Reader in = new FileReader(file)) {
                Type listType = new TypeToken<ArrayList<Issue>>() {}.getType();
                List<Issue> issues2 = gson.fromJson(in, listType);
                issues.addAll(issues2);
            }
        }
        LOGGER.info("" + issues.size() + " issues imported");

        Set<InferredIssue> inferredIssues = inferIssuesViaLSPPropagation(issues,overrides,propagateNullabilityInArguments);
        try (Writer out = new FileWriter(outputFile)) {
            Gson gson = new Gson();
            gson.toJson(inferredIssues,out);
            LOGGER.info("Additional issues written to " + outputFile.getAbsolutePath());
        }
        counts.put(COUNT_NULLABILITY_ISSUES_INFERRED,inferredIssues.size());

        Collection<Issue> aggregatedIssues = IssueAggregator.aggregate(inferredIssues);
        counts.put(COUNT_NULLABILITY_ISSUES_INFERRED_AGGREGATED,aggregatedIssues.size());

    }

    static Set<InferredIssue> inferIssuesViaLSPPropagation(Set<Issue> issues, Graph<OwnedMethod> overrides, boolean propagateNullabilityInArguments) {
        AtomicInteger countInferredReturnIssues = new AtomicInteger(0);
        AtomicInteger countInferredArgIssues = new AtomicInteger(0);
        Set<InferredIssue> inferredIssues = new HashSet<>();
        Graph<OwnedMethod> overriden = Graphs.transpose(overrides);
        for (Issue issue:issues) {
            OwnedMethod method = new OwnedMethod(issue.getClassName(),issue.getMethodName(),issue.getDescriptor());
            if (overrides.nodes().contains(method)) {
                // propagate nullable return types to overridden methods in super types
                if (issue.getKind() == Issue.IssueType.RETURN_VALUE) {
                    Traverser.forGraph(overrides)
                        .breadthFirst(method)
                        .forEach(m -> {
                            if (!Objects.equals(method,m)) {
                                assert Objects.equals(method.getName(),m.getName());
                                assert Objects.equals(method.getDescriptor(),m.getDescriptor());
                                InferredIssue newIssue = new InferredIssue(m.getOwner(),m.getName(),m.getDescriptor(), Issue.IssueType.RETURN_VALUE,-1, InferredIssue.Inference.PROPAGATE_NULLABLE_RETURN_TO_OVERRIDEN_METHOD, issue);
                                if (inferredIssues.add(newIssue)) {
                                    countInferredReturnIssues.incrementAndGet();
                                }
                            }
                        });
                }

                // propagate nullable argument types to overriding methods in arg types
                if (propagateNullabilityInArguments && issue.getKind() == Issue.IssueType.ARGUMENT) {
                    Traverser.forGraph(overriden)
                        .breadthFirst(method)
                        .forEach(m -> {
                            if (!Objects.equals(method,m)) {
                                assert Objects.equals(method.getName(),m.getName());
                                assert Objects.equals(method.getDescriptor(),m.getDescriptor());
                                countInferredArgIssues.incrementAndGet();
                                InferredIssue newIssue = new InferredIssue(m.getOwner(),m.getName(),m.getDescriptor(), Issue.IssueType.ARGUMENT, issue.getArgsIndex(), InferredIssue.Inference.PROPAGATE_ARGUMENT_TO_OVERRIDING_METHOD, issue);
                                if (inferredIssues.add(newIssue)) {
                                    countInferredArgIssues.incrementAndGet();
                                }
                            }
                        });
                }
            }
        }

        LOGGER.info("Nullable return type issues inferred: " + countInferredReturnIssues.get());
        LOGGER.info("Nullable arg type issues inferred: " + countInferredArgIssues.get());

        return inferredIssues;
    }

    private static List<File> getProjectFolders(File rootFolder) {
        return Stream.of(rootFolder.listFiles(f -> f.isDirectory() && !f.isHidden()))
            .collect(Collectors.toList());
    }


}
