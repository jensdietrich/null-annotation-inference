package nz.ac.wgtn.nullinference.experiments.spring;

import java.io.File;
import java.io.IOException;

import static nz.ac.wgtn.nullinference.experiments.spring.Config.*;

/**
 * Script to produce data for RQ2.
 * The ground truth is changes here to also consider Void as nullable, even if it is not annotated.
 * @author jens dietrich
 */
public class RQ2c extends Experiment {

    public static final File OUTPUT_CSV = new File("experiments-spring/results/rq/rq2c.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/rq/rq2c.tex");

    public static void main (String[] args) throws IOException, InterruptedException {
        new RQ2c().analyse();
    }

    public void analyse()  {
        Column[] columns = new Column[] {
            Column.First,
//            new Column() {
//                @Override public String name() {
//                    return "extracted";
//                }
//                @Override public String value(String dataName) {
//                    return Utils.format(countIssues(EXTRACTED_ISSUES_FOLDER,dataName,false));
//                }
//            },
//


            new Column() {
                @Override public String name() {
                    return "r,lpb(D)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_WITH_VOID_FOLDER,SANITIZED_ISSUES_DEPRECATED_FOLDER,dataName);
                }
            },
            new Column() {
                @Override public String name() {
                    return "r,lpb(M)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_WITH_VOID_FOLDER,SANITIZED_ISSUES_MAINSCOPE_FOLDER,dataName);
                }
            },
            new Column() {
                @Override public String name() {
                    return "r,lpb(N)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_WITH_VOID_FOLDER,SANITIZED_ISSUES_NEGATIVETESTS_FOLDER,dataName);
                }
            },
            new Column() {
                @Override public String name() {
                    return "r,lpb(S)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_WITH_VOID_FOLDER,SANITIZED_ISSUES_SHADED_FOLDER,dataName);
                }
            },
            new Column() {
                @Override public String name() {
                    return "r,lpb(all)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_WITH_VOID_FOLDER,SANITIZED_ISSUES_FOLDER,dataName);
                }
            },
        };


        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);
        TableGenerator latexOutput = new LatexTableGenerator(OUTPUT_LATEX,"|lrrrrr|");

        this.run(ADDITIONAL_PROGRAMS,
                "RQ2c -- recall and lower precision bound (r,lpb) w.r.t. existing annotations after applying sanitisers (" + SANITIZER_NAMES + "), compared against existing annotations considering java.lang.Void as implicitly annotated @nullable",
                "tab:rq2c",columns,csvOutput,latexOutput);

    }

}
