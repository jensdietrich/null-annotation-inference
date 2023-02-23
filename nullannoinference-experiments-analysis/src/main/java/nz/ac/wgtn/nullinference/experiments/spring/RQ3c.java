package nz.ac.wgtn.nullinference.experiments.spring;

import nz.ac.wgtn.nullinference.experiments.Utils;

import java.io.File;
import java.io.IOException;

import static nz.ac.wgtn.nullinference.experiments.spring.Config.*;

/**
 * Script to produce data for RQ3.
 * @author jens dietrich
 */
public class RQ3c extends Experiment {

    public static final File OUTPUT_CSV = new File("experiments-spring/results/rq/rq3c.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/rq/rq3c.tex");

    public static void main (String[] args) throws IOException, InterruptedException {
        new RQ3c().analyse();
    }

    public void analyse()  {
        Column[] columns = new Column[] {
            Column.First,
            new Column() {
                @Override
                public String name() {
                    return "s";
                }

                @Override
                public String value(String dataName) {
                    return Utils.format(countIssues(SANITIZED_ISSUES_FOLDER, dataName, true));
                }
            },
            new Column() {
                @Override public String name() {
                    return "sp";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(OBSERVED_AND_PROPAGATED_ISSUES_FOLDER,dataName,true));
                }
            },
            new Column() {
                @Override public String name() {
                    return "sps";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(OBSERVED_AND_PROPAGATED_SANITIZED_ISSUES_FOLDER,dataName,true));
                }
            },
            new Column() {
                @Override public String name() {
                    return "r,lpb(s)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_WITH_VOID_FOLDER,SANITIZED_ISSUES_FOLDER,dataName);
                }
            },
            new Column() {
                @Override public String name() {
                    return "r,lpb(sp)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_WITH_VOID_FOLDER,OBSERVED_AND_PROPAGATED_ISSUES_FOLDER,dataName);
                }
            },
            new Column() {
                @Override public String name() {
                    return "r,lpb(sps)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_WITH_VOID_FOLDER,OBSERVED_AND_PROPAGATED_SANITIZED_ISSUES_FOLDER,dataName);
                }
            }
        };

        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);
        TableGenerator latexOutput = new LatexTableGenerator(OUTPUT_LATEX,"|lrrrrrr|");

        this.run(ADDITIONAL_PROGRAMS,"RQ3c -- effect of propagation, aggregated issue counts and recall / lower precision bound for santitised issues (s), santitised and then propagated issues (sp) and santitised, propagated and resanitised issues (sps), compared against existing annotations considering java.lang.Void as implicitly annotated @nullable","tab:rq3a",columns,csvOutput,latexOutput);

    }

}
