package nz.ac.wgtn.nullinference.experiments.spring;

import nz.ac.wgtn.nullinference.experiments.Utils;

import java.io.File;
import java.io.IOException;

import static nz.ac.wgtn.nullinference.experiments.spring.Config.*;

/**
 * Script to produce data for RA4.
 * @author jens dietrich
 */
public class RA4 extends Experiment {


    public static final File OUTPUT_CSV = new File("experiments-spring/results/ra/ra4.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/ra/ra4.tex");

    public static void main (String[] args) throws IOException, InterruptedException {
        new RA4().analyse();
    }

    public void analyse()  {
        Column[] columns = new Column[] {
            Column.First,
            new Column() {
                @Override
                public String name() {
                    return "existing";
                }

                @Override
                public String value(String dataName) {
                    return Utils.format(countIssues(EXTRACTED_ISSUES_FOLDER, dataName, true));
                }
            },
            new Column() {
                @Override public String name() {
                    return "propagated";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(EXTRACTED_PLUS_ISSUES_FOLDER,dataName,true));
                }
            }
        };

        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);
        TableGenerator latexOutput = new LatexTableGenerator(OUTPUT_LATEX,"|lrr|");

        this.run(SPRING_MODULES,"RA4 -- existing annotations and additional annotation inferred via propagation","tab:ra4",columns,csvOutput,latexOutput);

    }

}
