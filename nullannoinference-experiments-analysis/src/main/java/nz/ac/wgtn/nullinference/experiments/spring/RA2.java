package nz.ac.wgtn.nullinference.experiments.spring;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullinference.experiments.Utils;
import java.io.File;
import java.io.IOException;
import static nz.ac.wgtn.nullinference.experiments.spring.DataSet.SPRING_MODULES;

/**
 * Script to produce data for RA2.
 * @author jens dietrich
 */
public class RA2 extends Experiment {

    public static final File EXTRACTED_ISSUES_FOLDER = new File("experiments-spring/results/extracted");
    public static final File EXTRACTED_PLUS_ISSUES_FOLDER = new File("experiments-spring/results/extracted+");
    public static final File OBSERVED_ISSUES_FOLDER = new File("experiments-spring/results/observed");
    public static final File SANITIZED_ISSUES_FOLDER = new File("experiments-spring/results/sanitized");
    public static final File SANITIZED_ISSUES_DEPRECATED_FOLDER = new File("experiments-spring/results/sanitizedD");
    public static final File SANITIZED_ISSUES_MAINSCOPE_FOLDER = new File("experiments-spring/results/sanitizedM");
    public static final File SANITIZED_ISSUES_NEGATIVETESTS_FOLDER = new File("experiments-spring/results/sanitizedN");
    public static final File SANITIZED_ISSUES_SHADED_FOLDER = new File("experiments-spring/results/sanitizedS");


    public static final File OUTPUT_CSV = new File("experiments-spring/results/ra/ra2.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/ra/ra2.tex");


    public static void main (String[] args) throws IOException, InterruptedException {

        Preconditions.checkArgument(EXTRACTED_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(EXTRACTED_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(EXTRACTED_PLUS_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(EXTRACTED_PLUS_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(OBSERVED_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(OBSERVED_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(SANITIZED_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(SANITIZED_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(SANITIZED_ISSUES_SHADED_FOLDER.exists());
        Preconditions.checkArgument(SANITIZED_ISSUES_SHADED_FOLDER.isDirectory());
        Preconditions.checkArgument(SANITIZED_ISSUES_DEPRECATED_FOLDER.exists());
        Preconditions.checkArgument(SANITIZED_ISSUES_DEPRECATED_FOLDER.isDirectory());
        Preconditions.checkArgument(SANITIZED_ISSUES_MAINSCOPE_FOLDER.exists());
        Preconditions.checkArgument(SANITIZED_ISSUES_MAINSCOPE_FOLDER.isDirectory());
        Preconditions.checkArgument(SANITIZED_ISSUES_NEGATIVETESTS_FOLDER.exists());
        Preconditions.checkArgument(SANITIZED_ISSUES_NEGATIVETESTS_FOLDER.isDirectory());

        Thread.sleep(10_000);
        new RA2().analyse();
    }

    public void analyse()  {
        Column[] columns = new Column[] {
            Column.First,
            new Column() {
                @Override public String name() {
                    return "extracted";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(EXTRACTED_ISSUES_FOLDER,dataName,false));
                }
            },
//            new Column() {
//                @Override public String name() {
//                    return "extracted+inf";
//                }
//                @Override public String value(String dataName) {
//                    return Utils.format(countIssues(EXTRACTED_PLUS_ISSUES_FOLDER,dataName,false));
//                }
//            },

//            new Column() {
//                @Override public String name() {
//                    return "extracted+inf (non-shaded)";
//                }
//                @Override public String value(String dataName) {
//                    return Utils.format(countIssues(EXTRACTED_PLUS_ISSUES_FOLDER,dataName,false,shaded.negate()));
//                }
//            },

            new Column() {
                @Override public String name() {
                    return "observed (agg)";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(OBSERVED_ISSUES_FOLDER,dataName,true));
                }
            },
            new Column() {
                @Override public String name() {
                    return "san(D)";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(SANITIZED_ISSUES_DEPRECATED_FOLDER,dataName,true));
                }
            },
            new Column() {
                @Override public String name() {
                    return "san(M)";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(SANITIZED_ISSUES_MAINSCOPE_FOLDER,dataName,true));
                }
            },
            new Column() {
                @Override public String name() {
                    return "san(N)";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(SANITIZED_ISSUES_NEGATIVETESTS_FOLDER,dataName,true));
                }
            },
            new Column() {
                @Override public String name() {
                    return "san(S)";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(SANITIZED_ISSUES_SHADED_FOLDER,dataName,true));
                }
            },

            new Column() {
                @Override public String name() {
                    return "san(DMNS)";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(SANITIZED_ISSUES_FOLDER,dataName,true));
                }
            },

            new Column() {
                @Override public String name() {
                    return "recall+prec(base)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER,OBSERVED_ISSUES_FOLDER,dataName);
                }
            },

            new Column() {
                @Override public String name() {
                    return "recall+prec(D)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER,SANITIZED_ISSUES_DEPRECATED_FOLDER,dataName);
                }
            },
            new Column() {
                @Override public String name() {
                    return "recall+prec(M)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER,SANITIZED_ISSUES_MAINSCOPE_FOLDER,dataName);
                }
            },
            new Column() {
                @Override public String name() {
                    return "recall+prec(N)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER,SANITIZED_ISSUES_NEGATIVETESTS_FOLDER,dataName);
                }
            },
            new Column() {
                @Override public String name() {
                    return "recall+prec(S)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER,SANITIZED_ISSUES_SHADED_FOLDER,dataName);
                }
            },
            new Column() {
                @Override public String name() {
                    return "recall+prec(DMNS)";
                }
                @Override public String value(String dataName) {
                    return recallPrecision(EXTRACTED_ISSUES_FOLDER,SANITIZED_ISSUES_FOLDER,dataName);
                }
            },
        };


        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);


        this.run(SPRING_MODULES,"RA2","tab:ra2",columns,csvOutput);


    }

}
