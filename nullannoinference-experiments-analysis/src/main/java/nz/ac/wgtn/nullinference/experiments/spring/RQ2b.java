package nz.ac.wgtn.nullinference.experiments.spring;

import java.io.File;
import java.io.IOException;
import static nz.ac.wgtn.nullinference.experiments.spring.Config.SPRING_MODULES;
import static nz.ac.wgtn.nullinference.experiments.spring.Config.*;

/**
 * Script to produce data for RQ2.
 * @author jens dietrich
 */
public class RQ2b extends Experiment {

    public static final File OUTPUT_CSV = new File("experiments-spring/results/rq/rq2b.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/rq/rq2b.tex");

    public static void main (String[] args) throws IOException, InterruptedException {
        new RQ2b().analyse();
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
                    return "r,p(D)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER,SANITIZED_ISSUES_DEPRECATED_FOLDER,dataName);
                }
            },
            new Column() {
                @Override public String name() {
                    return "r,p(M)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER,SANITIZED_ISSUES_MAINSCOPE_FOLDER,dataName);
                }
            },
            new Column() {
                @Override public String name() {
                    return "r,p(N)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER,SANITIZED_ISSUES_NEGATIVETESTS_FOLDER,dataName);
                }
            },
            new Column() {
                @Override public String name() {
                    return "r,p(S)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER,SANITIZED_ISSUES_SHADED_FOLDER,dataName);
                }
            },
            new Column() {
                @Override public String name() {
                    return "r,p(all)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER,SANITIZED_ISSUES_FOLDER,dataName);
                }
            },
        };


        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);
        TableGenerator latexOutput = new LatexTableGenerator(OUTPUT_LATEX,"|lrrrrr|");

        this.run(SPRING_MODULES,
                "RQ2b -- precision and recall w.r.t. existing annotations after applying sanitisers (" + SANITIZER_NAMES + ")",
                "tab:rq2b",columns,csvOutput,latexOutput);

    }

}
