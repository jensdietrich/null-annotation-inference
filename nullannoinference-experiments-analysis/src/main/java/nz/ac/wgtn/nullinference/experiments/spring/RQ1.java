package nz.ac.wgtn.nullinference.experiments.spring;

import nz.ac.wgtn.nullinference.experiments.Utils;
import java.io.File;
import static nz.ac.wgtn.nullinference.experiments.spring.Config.SPRING_MODULES;
import static nz.ac.wgtn.nullinference.experiments.spring.Config.*;

/**
 * Script to produce data for RQ1.
 * @author jens dietrich
 */
public class RQ1 extends Experiment {

    public static final File OUTPUT_CSV = new File("experiments-spring/results/rq/rq1.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/rq/rq1.tex");

    public static void main (String[] args) throws Exception {
//        Thread.sleep(10_000);  // time to connect profiler
        new RQ1().analyse();
    }

    public void analyse()  {

        Column[] columns = new Column[] {
            Column.First,
            new Column() {
                @Override public String name() {
                    return "ex";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(EXTRACTED_ISSUES_FOLDER,dataName,false));
                }
            },
//            new Column() {
//                @Override public String name() {
//                    return "extracted+";
//                }
//                @Override public String value(String dataName) {
//                    return Utils.format(countIssues(EXTRACTED_PLUS_ISSUES_FOLDER,dataName,false));
//                }
//            },
            new Column() {
                @Override public String name() {
                    return "obs";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(OBSERVED_ISSUES_FOLDER,dataName,false));
                }
            },
            new Column() {
                @Override public String name() {
                    return "agg";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(OBSERVED_ISSUES_FOLDER,dataName,true));
                }
            },
            new Column() {
                @Override public String name() {
                    return "agg/obs";
                }
                @Override public String value(String dataName) {
                    return Utils.format2(compressionRatio(OBSERVED_ISSUES_FOLDER,dataName));
                }
            },
            new Column() {
                @Override public String name() {
                    return "r,p";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER,OBSERVED_ISSUES_FOLDER,dataName);
                }
            },
        };


        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);
        TableGenerator latexOutput = new LatexTableGenerator(OUTPUT_LATEX,"|lrrrrr|");

        this.run(SPRING_MODULES,"RQ1 - existing (ex) vs observed (obs) issues, also reported are the aggregation of observed issues (agg), aggregation ratios (agg/obs) and recall / precision (r,p)","tab:rq1",columns,csvOutput,latexOutput);

    }

}
