package nz.ac.wgtn.nullannoinference.propagator;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.graph.Graph;
import com.google.common.graph.Graphs;
import com.google.common.graph.Traverser;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import nz.ac.wgtn.nullannoinference.commons.IssueAggregator;
import nz.ac.wgtn.nullannoinference.commons.IssueKernel;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import nz.ac.wgtn.nullannoinference.commons.Issue;
import org.apache.logging.log4j.Logger;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Infer additional issues by applying LSP.
 * @author jens dietrich
 */
public class InferAdditionalIssues {

    public static final Logger LOGGER = LogSystem.getLogger("infer-additional-issues");
    private static final Type ISSUE_SET_TYPE = new TypeToken<Set<Issue>>() {}.getType();

    public static void run (ProjectType projectType, File issueInputFile, File projectFolder, File outputFile, String prefix, boolean propagateNullabilityInArguments, Predicate<Issue> issueFilter) throws Exception {

        Preconditions.checkArgument(issueInputFile.exists(), issueInputFile.getAbsolutePath() + " must exist");
        Preconditions.checkArgument(projectFolder.exists(), projectFolder.getAbsolutePath() + " must exist");
        Preconditions.checkArgument(projectFolder.isDirectory(), projectFolder.getAbsolutePath() + " must be a folder");

        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        LOGGER.info("reading issues from " + issueInputFile.getAbsolutePath());
        LOGGER.info("analysing project" + projectFolder.getAbsolutePath());
        LOGGER.info("additional inferred issues will be written to  " + outputFile.getAbsolutePath());
        LOGGER.info("only classes starting with the following name will be considered  " + prefix);


        // extract overrides
        Collection<File> classFiles = projectType.getCompiledMainClasses(projectFolder);
        Graph<OwnedMethodInfo> overrides = OverrideExtractor.extractOverrides(t -> t.startsWith(prefix),classFiles);

        LOGGER.info("" + overrides.edges().size() + " override relationships found");

        // extract issues
        Set<Issue> issues = new HashSet<>();
        LOGGER.info("reading issues from " + issueInputFile.getAbsolutePath());
        Gson gson = new Gson();
        try (Reader in = new FileReader(issueInputFile)) {
            Set<Issue> issues2 = gson.fromJson(in, ISSUE_SET_TYPE);
            issues.addAll(issues2.parallelStream().filter(issueFilter).collect(Collectors.toSet()));
        }
        catch (Exception x) {
            LOGGER.error("error reading issues from " + issueInputFile.getAbsolutePath(),x);
        }
        LOGGER.info("Imported issues: " + issues.size());

        Set<Issue> inferredIssues = inferIssuesViaLSPPropagation(issues,overrides,propagateNullabilityInArguments);
        LOGGER.info("Inferred issues: " + inferredIssues.size());

        // merge old and new issues (can still be separated using meta data , see Issue::provenanceType)
        Set<Issue> allIssues = Sets.union(issues,inferredIssues);
        LOGGER.info("Combined issues: " + allIssues.size());

        LOGGER.info("Writing issues to " + outputFile.getAbsolutePath());
        gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer out = new FileWriter(outputFile)) {
            gson.toJson(allIssues,ISSUE_SET_TYPE,out);
            LOGGER.info("original and inferred issues written to " + outputFile.getAbsolutePath());
        }
        catch (Exception x) {
            LOGGER.error("error writing issues to " + outputFile.getAbsolutePath(),x);
        }

    }

    static Set<Issue> inferIssuesViaLSPPropagation(Set<Issue> issues, Graph<OwnedMethodInfo> overrides, boolean propagateNullabilityInArguments) {
        AtomicInteger countInferredReturnIssues = new AtomicInteger(0);
        AtomicInteger countInferredArgIssues = new AtomicInteger(0);
        Set<Issue> inferredIssues = new HashSet<>();
        Graph<OwnedMethodInfo> overriden = Graphs.transpose(overrides);
        for (Issue issue:issues) {
            OwnedMethodInfo method = new OwnedMethodInfo(issue.getClassName(),issue.getMethodName(),issue.getDescriptor());
            if (overrides.nodes().contains(method)) {
                // propagate nullable return types to overridden methods in super types
                if (issue.getKind() == Issue.IssueType.RETURN_VALUE) {
                    Traverser.forGraph(overrides)
                        .breadthFirst(method)
                        .forEach(m -> {
                            if (!Objects.equals(method,m)) {
                                assert Objects.equals(method.getName(),m.getName());
                                assert Objects.equals(method.getDescriptor(),m.getDescriptor());
                                Issue newIssue = new Issue(m.getOwner(),m.getName(),m.getDescriptor(),issue.getContext(), Issue.IssueType.RETURN_VALUE,-1);
                                newIssue.setParent(issue);
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
                                Issue newIssue = new Issue(m.getOwner(),m.getName(),m.getDescriptor(), issue.getContext(),Issue.IssueType.ARGUMENT, issue.getArgsIndex());
                                newIssue.setParent(issue);
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
