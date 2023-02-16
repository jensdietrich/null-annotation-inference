package nz.ac.wgtn.nullinference.experiments.spring;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import nz.ac.wgtn.nullannoinference.commons.*;
import nz.ac.wgtn.nullinference.experiments.descr.DescriptorParser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sample the difference between two issue sets.
 * @author jens dietrich
 */
public class SampleDiff extends Experiment {

    public static final String MODULE = "error-prone";

    public static final int ISSUE_KERNEL_LIMIT = 3;
    public static final int ISSUE_INSTANCE_PER_KERNEL_LIMIT = 1;

    public static final Predicate<IssueKernel> ALL = issueKernel -> true;
    public static final Predicate<IssueKernel> ARG = issueKernel -> issueKernel.getKind()== Issue.IssueType.ARGUMENT;
    public static final Predicate<IssueKernel> RET = issueKernel -> issueKernel.getKind()== Issue.IssueType.RETURN_VALUE;
    public static final Predicate<IssueKernel> FLD = issueKernel -> issueKernel.getKind()== Issue.IssueType.FIELD;

    public static final Predicate<IssueKernel> NOTSHADED = issueKernel -> true; //!issueKernel.getClassName().startsWith("org.springframework.asm.");

    public static final Map<String,Predicate<IssueKernel>> FILTERS = new LinkedHashMap(){{
        put("all", ALL);
        put("arg", ARG);
        put("ret", RET);
        put("fld", FLD);
    }};

    public static void main (String[] args) throws IOException {
        new SampleDiff().analyse();
    }

    public void analyse()  {

        Set<Issue> extractedIssues = (Set<Issue>) readIssues(Config.EXTRACTED_ISSUES_FOLDER, MODULE, false);
        Set<Issue> observedIssues = (Set<Issue>) readIssues(Config.SANITIZED_ISSUES_FOLDER, MODULE, false);

        Map<IssueKernel,Set<Issue>> extractedIssueAggregation = IssueAggregator.aggregateAsMap(extractedIssues);
        Map<IssueKernel,Set<Issue>> observedIssueAggregation = IssueAggregator.aggregateAsMap(observedIssues);

        printDataset("extracted",extractedIssueAggregation.keySet());
        printDataset("observed",observedIssueAggregation.keySet());

        System.out.println("extracted (all): " + extractedIssueAggregation.size());
        System.out.println("observed (all): " + observedIssueAggregation.size());

        Set<IssueKernel> xtrMinusObs = Sets.difference(extractedIssueAggregation.keySet(),observedIssueAggregation.keySet());
        Set<IssueKernel> obsMinusXtr= Sets.difference(observedIssueAggregation.keySet(),extractedIssueAggregation.keySet());

        // by type
        printDataset("extracted-observed",xtrMinusObs);
        printDataset("observed-extracted",obsMinusXtr);

        System.out.println("exporting interesting diffs ");
        writeIssues(obsMinusXtr.stream().filter(RET).filter(NOTSHADED).collect(Collectors.toList()), observedIssueAggregation,"observed-minus-extracted-return-"+MODULE);
        writeIssues(obsMinusXtr.stream().filter(FLD).filter(NOTSHADED).collect(Collectors.toList()), observedIssueAggregation,"observed-minus-extracted-fields-"+MODULE);
        writeIssues(obsMinusXtr.stream().filter(ARG).filter(NOTSHADED).collect(Collectors.toList()), observedIssueAggregation,"observed-minus-extracted-args-"+MODULE);

    }

    private static void writeIssues(Collection<IssueKernel> selected, Map<IssueKernel,Set<Issue>> aggregation, String fileName) {
        writeIssuesToJSON(selected,aggregation,new File(fileName+".json"));
        writeIssuesToMarkdown(selected,aggregation,new File(fileName+".md"));
    }
    private static void writeIssuesToJSON(Collection<IssueKernel> selected, Map<IssueKernel,Set<Issue>> aggregation, File file) {
        List<Issue> obsMinusXtrReturnIssues = selected.stream()
            .sorted(Comparator.comparing(IssueKernel::getClassName))
            .flatMap(kernel -> aggregation.get(kernel).stream().limit(ISSUE_INSTANCE_PER_KERNEL_LIMIT))
            .collect(Collectors.toList());
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter out = new FileWriter(file)) {
            gson.toJson(obsMinusXtrReturnIssues,out);
            System.out.println("issues exported to " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeIssuesToMarkdown(Collection<IssueKernel> selected, Map<IssueKernel,Set<Issue>> aggregation, File file) {
        try (PrintWriter out = new PrintWriter(new FileWriter(file))){
            selected.stream()
                .sorted(Comparator.comparing(IssueKernel::getClassName))
                .forEach(issueKernel -> {

                    String params = null;
                    if (issueKernel.getKind() == Issue.IssueType.FIELD) {
                        params = DescriptorParser.parseFieldDescriptor(issueKernel.getDescriptor());
                    }
                    else {
                        params = DescriptorParser.parseMethodDescriptor(issueKernel.getDescriptor()).getParamTypes().stream().collect(Collectors.joining(",","(",")"));
                    }

                    String pos = "";
                    if (issueKernel.getKind()==Issue.IssueType.RETURN_VALUE) {
                        pos = "return type of";
                    }
                    else if (issueKernel.getKind()==Issue.IssueType.ARGUMENT) {
                        pos = "argument #" + issueKernel.getArgsIndex() + " of";
                    }

                    out.println("### Add `@Nullable` to " + pos + "`" + issueKernel.getClassName() + "::" + issueKernel.getMethodName() + " (type signature: " + params + ")");
                    out.println("");
                    out.println("Supporting test traces illustrating the use of null in actual program behaviour (max " + ISSUE_INSTANCE_PER_KERNEL_LIMIT + " is shown): ");
                    aggregation.get(issueKernel).stream().limit(ISSUE_INSTANCE_PER_KERNEL_LIMIT).forEach(
                        issue -> {
                            out.println("```");
                            for (String line:issue.getStacktrace()) {
                                out.println(line);
                            }
                            out.println("```");
                        }
                    );
                    out.println("");
                    }
                );

            out.println();
            if (selected.size()==1) {
                out.println("This issue has been detected by an automated analysis. A description of the analysis performed can be found here: (https://github.com/jensdietrich/null-annotation-inference)");
            }
            else if (selected.size()>1) {
                out.println("These issues have been detected by an automated analysis. A description of the analysis performed can be found here: (https://github.com/jensdietrich/null-annotation-inference)");
            }
            System.out.println("issues exported to " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void printDataset(String name, Set<IssueKernel> dataset) {
        for (String filterName: FILTERS.keySet()) {
            Predicate<IssueKernel> filter = FILTERS.get(filterName);
            System.out.print(name + " (" + filterName + "): ");
            System.out.println(dataset.stream().filter(filter).count());
        }
        System.out.println();
    }



}
