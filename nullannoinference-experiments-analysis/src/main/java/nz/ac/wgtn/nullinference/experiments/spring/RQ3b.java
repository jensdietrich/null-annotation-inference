package nz.ac.wgtn.nullinference.experiments.spring;

import nz.ac.wgtn.nullinference.experiments.Utils;

import java.io.File;
import java.io.IOException;

import static nz.ac.wgtn.nullinference.experiments.spring.Config.*;

/**
 * Script to produce data for RQ3 (by issue type).
 * @author jens dietrich
 */
public class RQ3b extends Experiment {


    public static final File OUTPUT_CSV = new File("experiments-spring/results/rq/rq3b.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/rq/rq3b.tex");


    public static void main (String[] args) throws IOException, InterruptedException {
        new RQ3b().analyse();
    }

    public void analyse()  {
        Column[] columns = new Column[] {
            Column.First,
            new Column() {
                @Override
                public String name() {
                    return "prop(F)";
                }

                @Override
                public String value(String dataName) {
                    return Utils.format(countIssues(OBSERVED_AND_PROPAGATED_SANITIZED_ISSUES_FOLDER, dataName, true, FIELDS_ONLY));
                }
            },
            new Column() {
                @Override
                public String name() {
                    return "prop(P)";
                }

                @Override
                public String value(String dataName) {
                    return Utils.format(countIssues(OBSERVED_AND_PROPAGATED_SANITIZED_ISSUES_FOLDER, dataName, true, PARAM_ONLY));
                }
            },
            new Column() {
                @Override
                public String name() {
                    return "prop(R)";
                }

                @Override
                public String value(String dataName) {
                    return Utils.format(countIssues(OBSERVED_AND_PROPAGATED_SANITIZED_ISSUES_FOLDER, dataName, true, RETURNS_ONLY));
                }
            },

            new Column() {
                @Override
                public String name() {
                    return "r,lpb(F)";
                }

                @Override
                public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER, OBSERVED_AND_PROPAGATED_SANITIZED_ISSUES_FOLDER,dataName,FIELDS_ONLY);
                }
            },
            new Column() {
                @Override
                public String name() {
                    return "r,lpb(P)";
                }

                @Override
                public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER, OBSERVED_AND_PROPAGATED_SANITIZED_ISSUES_FOLDER,dataName,PARAM_ONLY);
                }
            },
            new Column() {
                @Override
                public String name() {
                    return "r,lpb(R)";
                }

                @Override
                public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER, OBSERVED_AND_PROPAGATED_SANITIZED_ISSUES_FOLDER,dataName,RETURNS_ONLY);
                }
            }

        };


        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);
        TableGenerator latexOutput = new LatexTableGenerator(OUTPUT_LATEX,"|lrrrrrr|");

        this.run(FULL_DATASET,"RQ3b - number of propagated issues and recall / lower precision bound of propagated issues by type (F - field, P - method parameters, R - method return types)","tab:rq3c", columns, csvOutput,latexOutput );

    }

}
