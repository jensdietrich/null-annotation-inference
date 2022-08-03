package nz.ac.wgtn.nullinference.experiments.spring;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullinference.experiments.Utils;
import java.io.File;
import java.io.IOException;

import static nz.ac.wgtn.nullinference.experiments.spring.DataSet.SPRING_MODULES;

/**
 * Script to produce a (latex) table for RA1.
 * @author jens dietrich
 */
public class RA1 extends Experiment {

    public static final File EXTRACTED_ISSUES_FOLDER = new File("experiments-spring/results/extracted");
    public static final File EXTRACTED_PLUS_ISSUES_FOLDER = new File("experiments-spring/results/extracted+");
    public static final File OBSERVED_ISSUES_FOLDER = new File("experiments-spring/results/observed");
    public static final File OUTPUT_CSV = new File("ra1.csv");
    public static final File OUTPUT_LATEX = new File("ra1.tex");

    public static void main (String[] args) throws IOException {

        Preconditions.checkArgument(EXTRACTED_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(EXTRACTED_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(EXTRACTED_PLUS_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(EXTRACTED_PLUS_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(OBSERVED_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(OBSERVED_ISSUES_FOLDER.isDirectory());

        new RA1().analyse();
    }

    public void analyse()  {

        Column[] columns = new Column[] {
            Column.First,
            new Column() {
                @Override public String name() {
                    return "extract.";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(EXTRACTED_ISSUES_FOLDER,dataName,false));
                }
            },
            new Column() {
                @Override public String name() {
                    return "extract+inf";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(EXTRACTED_PLUS_ISSUES_FOLDER,dataName,false));
                }
            },

            new Column() {
                @Override public String name() {
                    return "extract (agg)";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(EXTRACTED_ISSUES_FOLDER,dataName,true));
                }
            },
            new Column() {
                @Override public String name() {
                    return "extract+inf (agg)";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(EXTRACTED_PLUS_ISSUES_FOLDER,dataName,true));
                }
            },
            new Column() {
                @Override public String name() {
                    return "observed";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(OBSERVED_ISSUES_FOLDER,dataName,false));
                }
            },
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
                    return "jacc-sim";
                }
                @Override public String value(String dataName) {
                    return Utils.format(jaccardSimilarity(EXTRACTED_ISSUES_FOLDER,OBSERVED_ISSUES_FOLDER,dataName));
                }
            }
        };


        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);


        this.run(SPRING_MODULES,"RA1","tab:ra1",columns,csvOutput);


    }

}
