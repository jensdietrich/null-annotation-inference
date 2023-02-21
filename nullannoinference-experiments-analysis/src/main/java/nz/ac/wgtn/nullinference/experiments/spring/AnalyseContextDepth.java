package nz.ac.wgtn.nullinference.experiments.spring;

import nz.ac.wgtn.nullannoinference.commons.IssueKernel;
import nz.ac.wgtn.nullinference.experiments.Utils;

import java.io.File;
import java.util.Map;
import java.util.function.Predicate;

import static nz.ac.wgtn.nullinference.experiments.spring.Config.*;

/**
 * Script to produce data for RA1b.
 * @author jens dietrich
 */
public class AnalyseContextDepth extends Experiment {

    public static final File OUTPUT_CSV = new File("experiments-spring/results/rq/context-depth.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/rq/context-depth.tex");

    public static void main (String[] args) throws Exception {
//        Thread.sleep(10_000);  // time to connect profiler
        new AnalyseContextDepth().analyse();
    }

    private int countContextDepth(Map<IssueKernel,Integer> contextDepths, Predicate<Integer> filter) {
//        return (int)contextDepths.keySet().stream()
//            .map(issueKernel -> contextDepths.get(issueKernel))
//            .filter(filter)
//            .count();
        int count = 0;
        for (IssueKernel kernel:contextDepths.keySet()) {
            int depth = contextDepths.get(kernel);
            if (filter.test(depth)) {
                count = count + 1;
            }
        }
        return count;

    }

    public void analyse()  {

        Column[] columns = new Column[] {
            Column.First,
            new Column() {
                @Override public String name() {
                    return "all";
                }
                @Override public String value(String dataName) {
                    int count = countIssues(SANITIZED_ISSUES_FOLDER, dataName,true);
                    return Utils.format(count);
                }
            },

            new Column() {
                @Override public String name() {
                    return "2";
                }
                @Override public String value(String dataName) {
                    Map<IssueKernel,Integer> minContextDepths = readAndAggregateIssuesCountMinContextDepth(SANITIZED_ISSUES_FOLDER, dataName);
                    return Utils.format(countContextDepth(minContextDepths,i -> i==2));
                }
            },
            new Column() {
                @Override public String name() {
                    return "3";
                }
                @Override public String value(String dataName) {
                    Map<IssueKernel,Integer> minContextDepths = readAndAggregateIssuesCountMinContextDepth(SANITIZED_ISSUES_FOLDER, dataName);
                    return Utils.format(countContextDepth(minContextDepths,i -> i==3));
                }
            },
            new Column() {
                @Override public String name() {
                    return "4";
                }
                @Override public String value(String dataName) {
                    Map<IssueKernel,Integer> minContextDepths = readAndAggregateIssuesCountMinContextDepth(SANITIZED_ISSUES_FOLDER, dataName);
                    return Utils.format(countContextDepth(minContextDepths,i -> i==4));
                }
            },
            new Column() {
                @Override public String name() {
                    return "5";
                }
                @Override public String value(String dataName) {
                    Map<IssueKernel,Integer> minContextDepths = readAndAggregateIssuesCountMinContextDepth(SANITIZED_ISSUES_FOLDER, dataName);
                    return Utils.format(countContextDepth(minContextDepths,i -> i==5));
                }
            },
            new Column() {
                @Override public String name() {
                    return "6";
                }
                @Override public String value(String dataName) {
                    Map<IssueKernel,Integer> minContextDepths = readAndAggregateIssuesCountMinContextDepth(SANITIZED_ISSUES_FOLDER, dataName);
                    return Utils.format(countContextDepth(minContextDepths,i -> i==6));
                }
            },
            new Column() {
                @Override public String name() {
                    return "7";
                }
                @Override public String value(String dataName) {
                    Map<IssueKernel,Integer> minContextDepths = readAndAggregateIssuesCountMinContextDepth(SANITIZED_ISSUES_FOLDER, dataName);
                    return Utils.format(countContextDepth(minContextDepths,i -> i==7));
                }
            },
            new Column() {
                @Override public String name() {
                    return "8";
                }
                @Override public String value(String dataName) {
                    Map<IssueKernel,Integer> minContextDepths = readAndAggregateIssuesCountMinContextDepth(SANITIZED_ISSUES_FOLDER, dataName);
                    return Utils.format(countContextDepth(minContextDepths,i -> i==8));
                }
            },
            new Column() {
                @Override public String name() {
                    return "9";
                }
                @Override public String value(String dataName) {
                    Map<IssueKernel,Integer> minContextDepths = readAndAggregateIssuesCountMinContextDepth(SANITIZED_ISSUES_FOLDER, dataName);
                    return Utils.format(countContextDepth(minContextDepths,i -> i==9));
                }
            },
            new Column() {
                @Override public String name() {
                    return "10";
                }
                @Override public String value(String dataName) {
                    Map<IssueKernel,Integer> minContextDepths = readAndAggregateIssuesCountMinContextDepth(SANITIZED_ISSUES_FOLDER, dataName);
                    return Utils.format(countContextDepth(minContextDepths,i -> i==10));
                }
            },
            new Column() {
                @Override public String name() {
                    return "$>$10";
                }
                @Override public String value(String dataName) {
                    Map<IssueKernel,Integer> minContextDepths = readAndAggregateIssuesCountMinContextDepth(SANITIZED_ISSUES_FOLDER, dataName);
                    return Utils.format(countContextDepth(minContextDepths,i -> i>10));
                }
            },
        };


        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);
        TableGenerator latexOutput = new LatexTableGenerator(OUTPUT_LATEX,"|lrrrrrrrrrrr|");

        this.run(FULL_DATASET,"Observed and sanitised issues by context depths","tab:context-depth",columns,csvOutput,latexOutput);

    }

}
