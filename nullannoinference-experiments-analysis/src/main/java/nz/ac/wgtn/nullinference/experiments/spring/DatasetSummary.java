package nz.ac.wgtn.nullinference.experiments.spring;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import nz.ac.wgtn.nullinference.experiments.Utils;
import java.io.File;
import static nz.ac.wgtn.nullinference.experiments.spring.Config.*;

/**
 * Script to produce data for RA1.
 * @author jens dietrich
 */
public class DatasetSummary extends Experiment {

    public static final File OUTPUT_CSV = new File("experiments-spring/results/rq/dataset-summary.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/rq/dataset-summary.tex");

    public static void main (String[] args) throws Exception {
        new DatasetSummary().analyse();
    }

    public void analyse()  {

        Column[] columns = new Column[]{
            Column.First,
            new Column() {
                @Override
                public String name() {
                    return "version";
                }
                @Override
                public String value(String dataName) {
                    // hard-coded
                    String value = "n/a";
                    if (dataName.startsWith("spring-")) {
                        value = "5.3.22";
                    }
                    else if ("guava".equals(dataName)) {
                        value = "31.1";
                    }
                    else if ("error-prone".equals(dataName)) {
                        value = "2.18.0";
                    }
                    return value;
                }
            },
            new Column() {
                @Override
                public String name() {
                    return "java";
                }
                @Override
                public String value(String dataName) {

                    File project = locateProject(dataName);
                    ProjectType projectType = getProjectType(dataName);
                    File sourceFolder = projectType.getJavaSourceFolder(project);
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
                    File project = locateProject(dataName);
                    ProjectType projectType = getProjectType(dataName);
                    File sourceFolder = projectType.getKotlinSourceFolder(project);
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
                    File project = locateProject(dataName);
                    ProjectType projectType = getProjectType(dataName);
                    File sourceFolder = projectType.getGroovySourceFolder(project);
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
                    File project = locateProject(dataName);
                    ProjectType projectType = getProjectType(dataName);
                    File sourceFolder = projectType.getJavaTestSourceFolder(project);
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
                    File project = locateProject(dataName);
                    ProjectType projectType = getProjectType(dataName);
                    File sourceFolder = projectType.getKotlinTestSourceFolder(project);
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
                    File project = locateProject(dataName);
                    ProjectType projectType = getProjectType(dataName);
                    File sourceFolder = projectType.getGroovyTestSourceFolder(project);
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
                        value = "60\\%"; //
                    }
                    else if ("spring-context".equals(dataName)) {
                        value = "63\\%"; //
                    }
                    else if ("spring-core".equals(dataName)) {
                        value = "66\\%"; //
                    }
                    else if ("spring-orm".equals(dataName)) {
                        value = "39\\%"; //
                    }
                    else if ("spring-oxm".equals(dataName)) {
                        value = "58\\%"; //
                    }
                    else if ("spring-web".equals(dataName)) {
                        value = "18\\%";
                    }
                    else if ("spring-webmvc".equals(dataName)) {
                        value = "39\\%";
                    }
                    else if ("guava".equals(dataName)) {
                        value = "70\\%";
                    }
                    else if ("error-prone".equals(dataName)) {
                        value = "73\\%";
                    }
                    return value;
                }
            }
        };

        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);
        TableGenerator latexOutput = new LatexTableGenerator(OUTPUT_LATEX,"|lr|rrr|rrr|r|")
            .addMultiColumn(2,"","|l")
            .addMultiColumn(3,"main","|c|")
            .addMultiColumn(3,"test","c|")
            .addMultiColumn(1,"","c|");

        this.run(FULL_DATASET,"project summary, reporting the number of Java, Kotlin and Groovy source code files for both main and test scope, and branch coverage","tab:data",columns,csvOutput,latexOutput);

    }

}
