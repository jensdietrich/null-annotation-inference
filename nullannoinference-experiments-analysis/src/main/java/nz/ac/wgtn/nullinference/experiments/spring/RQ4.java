package nz.ac.wgtn.nullinference.experiments.spring;

import nz.ac.wgtn.nullinference.experiments.Utils;

import java.io.File;
import java.io.IOException;

import static nz.ac.wgtn.nullinference.experiments.spring.Config.*;

/**
 * Script to produce data for RQ4.
 * @author jens dietrich
 */
public class RQ4 extends Experiment {

    public static final File OUTPUT_CSV = new File("experiments-spring/results/rq/rq4.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/rq/rq4.tex");

    public static void main (String[] args) throws IOException, InterruptedException {
        new RQ4().analyse();
    }

    public void analyse()  {
        Column[] columns = new Column[]{
            Column.First,
            new Column() {
                @Override
                public String name() {
                    return "original";
                }

                @Override
                public String value(String dataName) {
                    return Utils.format(countIssues(EXTRACTED_ISSUES_FOLDER, dataName, true));
                }
            },
            new Column() {
                @Override
                public String name() {
                    return "re-annotated";
                }

                @Override
                public String value(String dataName) {
                    return Utils.format(countIssues(EXTRACTED_FROM_REANNOTATED_ISSUES_FOLDER, dataName, true));
                }
            },
            new Column() {
                @Override
                public String name() {
                    return "r,p";
                }

                @Override
                public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER,EXTRACTED_FROM_REANNOTATED_ISSUES_FOLDER, dataName);
                }
            }
        };

        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);
        TableGenerator latexOutput = new LatexTableGenerator(OUTPUT_LATEX,"|lrrr|");

        this.run(SPRING_MODULES,"RQ4 -- nullable annotation extracted from original projects, re-annotated projects, and recall / precision of automatically inserted annotations with respect to original annotations","tab:rq4",columns,csvOutput,latexOutput);
    }

}
