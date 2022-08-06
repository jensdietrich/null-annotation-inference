package nz.ac.wgtn.nullinference.experiments.spring;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import nz.ac.wgtn.nullannoinference.commons.AbstractIssue;
import nz.ac.wgtn.nullannoinference.commons.ShadingSpec;
import nz.ac.wgtn.nullinference.experiments.Utils;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static nz.ac.wgtn.nullinference.experiments.spring.DataSet.SPRING_MODULES;

/**
 * Script to produce data for RA2.
 * @author jens dietrich
 */
public class RA2 extends Experiment {

    public static final File EXTRACTED_ISSUES_FOLDER = new File("experiments-spring/results/extracted");
    public static final File EXTRACTED_PLUS_ISSUES_FOLDER = new File("experiments-spring/results/extracted+");
    public static final File OBSERVED_ISSUES_FOLDER = new File("experiments-spring/results/observed");
    public static final File OBSERVED_SANITIZED_ISSUES_FOLDER = new File("experiments-spring/results/observed3");
    public static final File SHADING_SPECS = new File("experiments-spring/shaded.json");
    public static final File OUTPUT_CSV = new File("experiments-spring/results/ra/ra2.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/ra/ra2.tex");


    public static void main (String[] args) throws IOException {

        Preconditions.checkArgument(EXTRACTED_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(EXTRACTED_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(EXTRACTED_PLUS_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(EXTRACTED_PLUS_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(OBSERVED_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(OBSERVED_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(OBSERVED_SANITIZED_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(OBSERVED_SANITIZED_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(SHADING_SPECS.exists());

        new RA2().analyse();
    }

    public void analyse()  {

        // read shading specs
        Set<ShadingSpec> shadingSpecs = this.readShadingSpecs(SHADING_SPECS);
        Predicate<? extends AbstractIssue> shaded =
            issue -> shadingSpecs.stream().anyMatch(spec -> issue.getClassName().startsWith(spec.getRenamed()));

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
            new Column() {
                @Override public String name() {
                    return "extracted+inf";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(EXTRACTED_PLUS_ISSUES_FOLDER,dataName,false));
                }
            },

            new Column() {
                @Override public String name() {
                    return "extracted+inf (non-shaded)";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(EXTRACTED_PLUS_ISSUES_FOLDER,dataName,false,shaded.negate()));
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
                    return "observed (non-shaded, agg)";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(OBSERVED_ISSUES_FOLDER,dataName,true,shaded.negate()));
                }
            },

            new Column() {
                @Override public String name() {
                    return "observed (san, agg)";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(OBSERVED_SANITIZED_ISSUES_FOLDER,dataName,true));
                }
            },

            new Column() {
                @Override public String name() {
                    return "observed (san, agg, non-shaded)";
                }
                @Override public String value(String dataName) {
                    return Utils.format(countIssues(OBSERVED_SANITIZED_ISSUES_FOLDER,dataName,true,shaded.negate()));
                }
            },

            new Column() {
                @Override public String name() {
                    return "diff-extr-obs";
                }
                @Override public String value(String dataName) {
                    return diffMetrics(EXTRACTED_ISSUES_FOLDER,OBSERVED_ISSUES_FOLDER,dataName);
                }
            },

            new Column() {
                @Override public String name() {return "diff-extr-obs (non-shaded)";}
                @Override public String value(String dataName) {
                    return diffMetrics(EXTRACTED_ISSUES_FOLDER,OBSERVED_ISSUES_FOLDER,dataName,shaded.negate());
                }
            },

            new Column() {
                @Override public String name() {return "diff-extr-obs (sanitised)";}
                @Override public String value(String dataName) {
                    return diffMetrics(EXTRACTED_ISSUES_FOLDER,OBSERVED_SANITIZED_ISSUES_FOLDER,dataName);
                }
            },

            new Column() {
                @Override public String name() {return "diff-extr-obs (sanitised, non-shaded)";}
                @Override public String value(String dataName) {
                    return diffMetrics(EXTRACTED_ISSUES_FOLDER,OBSERVED_SANITIZED_ISSUES_FOLDER,dataName,shaded.negate());
                }
            }
        };


        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);


        this.run(SPRING_MODULES,"RA1","tab:ra1",columns,csvOutput);


    }

}
