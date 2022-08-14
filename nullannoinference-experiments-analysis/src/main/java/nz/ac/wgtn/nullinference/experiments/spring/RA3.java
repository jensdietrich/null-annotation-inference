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
 * Script to produce data for RA3.
 * @author jens dietrich
 */
public class RA3 extends Experiment {

    public static final File EXTRACTED_ISSUES_FOLDER = new File("experiments-spring/results/extracted");
    public static final File EXTRACTED_PLUS_ISSUES_FOLDER = new File("experiments-spring/results/extracted+");
    public static final File OBSERVED_PLUS_ISSUES_FOLDER = new File("experiments-spring/results/observed+");
    public static final File SANITIZED_ISSUES_FOLDER = new File("experiments-spring/results/sanitized");
    public static final File SHADING_SPECS = new File("experiments-spring/shaded.json");
    public static final File OUTPUT_CSV = new File("experiments-spring/results/ra/ra3.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/ra/ra3.tex");


    public static void main (String[] args) throws IOException {

        Preconditions.checkArgument(EXTRACTED_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(EXTRACTED_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(EXTRACTED_PLUS_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(EXTRACTED_PLUS_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(OBSERVED_PLUS_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(OBSERVED_PLUS_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(SANITIZED_ISSUES_FOLDER.exists());
        Preconditions.checkArgument(SANITIZED_ISSUES_FOLDER.isDirectory());
        Preconditions.checkArgument(SHADING_SPECS.exists());

        new RA3().analyse();
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
                        return "san(DMNS)";
                    }
                    @Override public String value(String dataName) {
                        return Utils.format(countIssues(SANITIZED_ISSUES_FOLDER,dataName,true));
                    }
                },
                new Column() {
                    @Override public String name() {
                        return "inf";
                    }
                    @Override public String value(String dataName) {
                        return Utils.format(countIssues(OBSERVED_PLUS_ISSUES_FOLDER,dataName,true));
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
                new Column() {
                    @Override public String name() {
                        return "recall+prec(inf)";
                    }
                    @Override public String value(String dataName) {
                        return recallPrecision(EXTRACTED_PLUS_ISSUES_FOLDER,OBSERVED_PLUS_ISSUES_FOLDER,dataName);
                    }
                },

        };

        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);

        this.run(SPRING_MODULES,"RA3","tab:ra3",columns,csvOutput);

    }

}
