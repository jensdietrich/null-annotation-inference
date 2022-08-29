package nz.ac.wgtn.nullinference.experiments.spring;

import nz.ac.wgtn.nullinference.experiments.Utils;
import java.io.File;
import java.io.IOException;
import static nz.ac.wgtn.nullinference.experiments.spring.Config.SPRING_MODULES;
import static nz.ac.wgtn.nullinference.experiments.spring.Config.*;

/**
 * Script to produce data for RA1.
 * @author jens dietrich
 */
public class RA1 extends Experiment {

    public static final File OUTPUT_CSV = new File("experiments-spring/results/ra/ra1.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/ra/ra1.tex");

    public static void main (String[] args) throws IOException {

        new RA1().analyse();
    }

    public void analyse()  {

        Column[] columns = new Column[] {
            Column.First,
            new Column() {
                @Override public String name() {
                    return "existing";
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
                    return "obs.(raw)";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(OBSERVED_ISSUES_FOLDER,dataName,false));
                }
            },
            new Column() {
                @Override public String name() {
                    return "obs. (agg)";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(OBSERVED_ISSUES_FOLDER,dataName,true));
                }
            },
            new Column() {
                @Override public String name() {
                    return "obs. (raw/agg)";
                }
                @Override public String value(String dataName) {
                    return Utils.format2(compressionRatio(OBSERVED_ISSUES_FOLDER,dataName));
                }
            }
        };


        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);
        TableGenerator latexOutput = new LatexTableGenerator(OUTPUT_LATEX,"|lrrrr|");

        this.run(SPRING_MODULES,"RA1 - extracted vs observed (obs). issues, also reported aggregation of (raw) observed issues and aggregation ratios","tab:ra1",columns,csvOutput,latexOutput);

    }

}
