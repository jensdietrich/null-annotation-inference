package nz.ac.wgtn.nullinference.experiments.spring;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullinference.experiments.Utils;
import java.io.File;
import static nz.ac.wgtn.nullinference.experiments.spring.Config.*;

/**
 * Script to produce data for RA1.
 * @author jens dietrich
 */
public class DatasetSummary extends Experiment {

    public static final File OUTPUT_CSV = new File("experiments-spring/results/ra/dataset-summary.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/ra/dataset-summary.tex");

    public static void main (String[] args) throws Exception {
        new DatasetSummary().analyse();
    }

    public void analyse()  {

        Column[] columns = new Column[]{
            Column.First,
            new Column() {
                @Override
                public String name() {
                    return "java";
                }
                @Override
                public String value(String dataName) {
                    File sourceFolder = new File(SPRING_PROJECTS, dataName + "/src/main/java");
                    Preconditions.checkState(sourceFolder.exists(),"Folder does not exist: " + sourceFolder);
                    return Utils.format(getFiles(sourceFolder, f -> f.getName().endsWith(".java")).size());
                }
            },

            new Column() {
                @Override
                public String name() {
                    return "kotlin";
                }
                @Override
                public String value(String dataName) {
                    File sourceFolder = new File(SPRING_PROJECTS, dataName + "/src/main/kotlin");
                    // don't check folder for existence, not all modules have this
                    return Utils.format(getFiles(sourceFolder, f -> f.getName().endsWith(".kt")).size());
                }
            },

            new Column() {
                @Override
                public String name() {
                    return "groovy";
                }
                @Override
                public String value(String dataName) {
                    File sourceFolder = new File(SPRING_PROJECTS, dataName + "/src/main/groovy");
                    // don't check folder for existence, not all modules have this
                    return Utils.format(getFiles(sourceFolder, f -> f.getName().endsWith(".groovy")).size());
                }
            },


            new Column() {
                @Override
                public String name() {
                    return "java";
                }
                @Override
                public String value(String dataName) {
                    File sourceFolder = new File(SPRING_PROJECTS, dataName + "/src/test/java");
                    Preconditions.checkState(sourceFolder.exists(),"Folder does not exist: " + sourceFolder);
                    return Utils.format(getFiles(sourceFolder, f -> f.getName().endsWith(".java")).size());
                }
            },

            new Column() {
                @Override
                public String name() {
                    return "kotlin";
                }
                @Override
                public String value(String dataName) {
                    File sourceFolder = new File(SPRING_PROJECTS, dataName + "/src/test/kotlin");
                    // don't check folder for existence, not all modules have this
                    return Utils.format(getFiles(sourceFolder, f -> f.getName().endsWith(".kt")).size());
                }
            },

            new Column() {
                @Override
                public String name() {
                    return "groovy";
                }
                @Override
                public String value(String dataName) {
                    File sourceFolder = new File(SPRING_PROJECTS, dataName + "/src/test/groovy");
                    // don't check folder for existence, not all modules have this
                    return Utils.format(getFiles(sourceFolder, f -> f.getName().endsWith(".groovy")).size());
                }
            },

            new Column() {
                @Override
                public String name() {
                    return "coverage";
                }
                @Override
                public String value(String dataName) {
                    // hard-coded
                    String value = "n/a";
                    if ("spring-beans".equals(dataName)) {
                        value = "76\\%";
                    }
                    else if ("spring-context".equals(dataName)) {
                        value = "78\\%";
                    }
                    else if ("spring-core".equals(dataName)) {
                        value = "35\\%";
                    }
                    else if ("spring-orm".equals(dataName)) {
                        value = "54\\%";
                    }
                    else if ("spring-oxm".equals(dataName)) {
                        value = "60\\%";
                    }
                    else if ("spring-web".equals(dataName)) {
                        value = "73\\%";
                    }
                    else if ("spring-webmvc".equals(dataName)) {
                        value = "85\\%";
                    }
                    return value;
                }
            }
        };


        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);
        TableGenerator latexOutput = new LatexTableGenerator(OUTPUT_LATEX,"|l|rrr|rrr|r|")
                .addMultiColumn(1,"","|l")
                .addMultiColumn(3,"main","|c|")
                .addMultiColumn(3,"test","c|")
                .addMultiColumn(1,"","c|");

        this.run(SPRING_MODULES,"project summary, reporting the number of Java, Kotlin and Groovy source code files for both main and test scope, as well as line coverage","tab:data",columns,csvOutput,latexOutput);

    }

}
