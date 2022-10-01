package nz.ac.wgtn.nullinference.experiments.spring;

import nz.ac.wgtn.nullinference.experiments.Utils;

import java.io.File;
import java.io.IOException;

import static nz.ac.wgtn.nullinference.experiments.spring.Config.*;

/**
 * Script to produce data for RA3. Reports sanitized propagated issues
 * @author jens dietrich
 */
public class RA3b extends Experiment {


    public static final File OUTPUT_CSV = new File("experiments-spring/results/ra/ra3b.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/ra/ra3b.tex");

    public static void main (String[] args) throws IOException, InterruptedException {
        new RA3b().analyse();
    }

    public void analyse()  {
        Column[] columns = new Column[] {
            Column.First,
            new Column() {
                @Override
                public String name() {
                    return "san(all)";
                }

                @Override
                public String value(String dataName) {
                    return Utils.format(countIssues(SANITIZED_ISSUES_FOLDER, dataName, true));
                }
            },
            new Column() {
                @Override public String name() {
                    return "prop";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(OBSERVED_AND_PROPAGATED_SANITIZED_ISSUES_FOLDER,dataName,true));
                }
            },
            new Column() {
                @Override public String name() {
                    return "r,p(san-all)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER,SANITIZED_ISSUES_FOLDER,dataName);
                }
            },
            new Column() {
                @Override public String name() {
                    return "r,p(prop)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER,OBSERVED_AND_PROPAGATED_SANITIZED_ISSUES_FOLDER,dataName);
                }
            }
        };

        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);
        TableGenerator latexOutput = new LatexTableGenerator(OUTPUT_LATEX,"|lrrrr|");

        this.run(SPRING_MODULES,"RA3b -- number of propagated issues and recall / precision of propagation, compared to sanitised issues (after applying all sanitisers), with sanitization applied to propagated issues","tab:ra3b",columns,csvOutput,latexOutput);

    }

}
