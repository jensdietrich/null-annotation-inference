package nz.ac.wgtn.nullinference.experiments.spring;

import nz.ac.wgtn.nullinference.experiments.Utils;
import java.io.File;
import java.io.IOException;
import static nz.ac.wgtn.nullinference.experiments.spring.Config.SPRING_MODULES;
import static nz.ac.wgtn.nullinference.experiments.spring.Config.*;

/**
 * Script to produce data for RA2.
 * @author jens dietrich
 */
public class RA2a extends Experiment {


    public static final File OUTPUT_CSV = new File("experiments-spring/results/ra/ra2a.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/ra/ra2a.tex");


    public static void main (String[] args) throws IOException, InterruptedException {
        new RA2a().analyse();
    }

    public void analyse()  {
        Column[] columns = new Column[]{
                Column.First,
                new Column() {
                    @Override
                    public String name() {
                        return "san(none)";
                    }

                    @Override
                    public String value(String dataName) {
                        return Utils.format(countIssues(OBSERVED_ISSUES_FOLDER, dataName, true));
                    }
                },
                new Column() {
                    @Override
                    public String name() {
                        return "san(D)";
                    }

                    @Override
                    public String value(String dataName) {
                        return Utils.format(countIssues(SANITIZED_ISSUES_DEPRECATED_FOLDER, dataName, true));
                    }
                },
                new Column() {
                    @Override
                    public String name() {
                        return "san(M)";
                    }

                    @Override
                    public String value(String dataName) {
                        return Utils.format(countIssues(SANITIZED_ISSUES_MAINSCOPE_FOLDER, dataName, true));
                    }
                },
                new Column() {
                    @Override
                    public String name() {
                        return "san(N)";
                    }

                    @Override
                    public String value(String dataName) {
                        return Utils.format(countIssues(SANITIZED_ISSUES_NEGATIVETESTS_FOLDER, dataName, true));
                    }
                },
                new Column() {
                    @Override
                    public String name() {
                        return "san(S)";
                    }

                    @Override
                    public String value(String dataName) {
                        return Utils.format(countIssues(SANITIZED_ISSUES_SHADED_FOLDER, dataName, true));
                    }
                },

                new Column() {
                    @Override
                    public String name() {
                        return "san(all)";
                    }

                    @Override
                    public String value(String dataName) {
                        return Utils.format(countIssues(SANITIZED_ISSUES_FOLDER, dataName, true));
                    }
                }
        };

        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);
        TableGenerator latexOutput = new LatexTableGenerator(OUTPUT_LATEX,"|lrrrrrr|");

        this.run(SPRING_MODULES,"RA2a -- observed issues after applying sanitisers (" + SANITIZER_NAMES + ")","tab:ra2a",columns,csvOutput,latexOutput);


    }

}
