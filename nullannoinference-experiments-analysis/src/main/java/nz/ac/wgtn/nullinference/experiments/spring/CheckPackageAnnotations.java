package nz.ac.wgtn.nullinference.experiments.spring;

import com.google.common.collect.Sets;
import nz.ac.wgtn.nullinference.experiments.Utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static nz.ac.wgtn.nullinference.experiments.spring.Config.*;

/**
 * Script to check package annotations.
 * @author jens dietrich
 */

public class CheckPackageAnnotations extends Experiment {

    public static final File OUTPUT_CSV = new File("experiments-spring/results/ra/package-annotations.csv");
    public static final File OUTPUT_LATEX = new File("experiments-spring/results/ra/package-annotations.tex");

    public static void main (String[] args) throws Exception {
        new CheckPackageAnnotations().analyse();
    }

    public void analyse() throws IOException {
        Map<String,Set<File>> nonNullApiByModule = new TreeMap<>();
        Map<String,Set<File>> nonNullFieldByModule = new TreeMap<>();
        Map<String,Set<File>> packageInfoExistsByModule = new TreeMap<>();
        Map<String,Set<File>> packagesByModule = new TreeMap<>();

        for (String module: SPRING_MODULES) {
            File src = new File(Config.PROJECTS,module + "/src/main");

            Set<File> packages = null;
            try (Stream<Path> walk = Files.walk(src.toPath())) {
                packages = walk
                    .filter(Files::isDirectory)
                    .filter(f -> !f.toFile().getName().startsWith("."))
                    .filter(f -> containsClasses(f.toFile()))
                    .map(p -> p.toFile())
                    .collect(Collectors.toSet());
            }
            catch (IOException x) {
                x.printStackTrace();
            }
            packagesByModule.put(module,packages);

            Set<File> packageInfoExists = packages.stream()
                .filter(pck -> new File(pck,"package-info.java").exists())
                .collect(Collectors.toSet());
            packageInfoExistsByModule.put(module,packageInfoExists);

            Set<File>  nonNullApi = packages.stream()
                .filter(f -> new File(f,"package-info.java").exists())
                .filter(f -> containsLine(new File(f,"package-info.java"),"@NonNullApi"))
                .collect(Collectors.toSet());
            nonNullApiByModule.put(module,nonNullApi);

            Set<File>  nonNullFields = packages.stream()
                .filter(f -> new File(f,"package-info.java").exists())
                .filter(f -> containsLine(new File(f,"package-info.java"),"@NonNullFields"))
                .collect(Collectors.toSet());
            nonNullFieldByModule.put(module,nonNullFields);


            // run some analysis
            Set<File> packagesWithoutInfoExists = Sets.difference(packages,packageInfoExists);
            if (!packagesWithoutInfoExists.isEmpty()) {
                System.out.println("Packages without package-info.java in modules " + module);
                for (File f:packagesWithoutInfoExists) {
                    System.out.println("\t" + f.getAbsolutePath());
                }
            }

            Set<File> packagesWithoutNonNullApi = Sets.difference(packageInfoExists,nonNullApi);
            if (!packagesWithoutNonNullApi.isEmpty()) {
                System.out.println("Packages with package-info.java but without @NonNullApi in modules " + module);
                for (File f:packagesWithoutNonNullApi) {
                    System.out.println("\t" + f.getAbsolutePath());
                }
            }

            Set<File> packagesWithoutNonNullField = Sets.difference(packageInfoExists,nonNullFields);
            if (!packagesWithoutNonNullField.isEmpty()) {
                System.out.println("Packages with package-info.java but without @NonNullFields in modules " + module);
                for (File f:packagesWithoutNonNullField) {
                    System.out.println("\t" + f.getAbsolutePath());
                }
            }
        }

        // print out
        try (PrintWriter out = new PrintWriter(new FileWriter(OUTPUT_CSV))) {
            out.println("project\tpackages\tpackage-info\t@NonNullApi\tNonNullFields");
            for (String module:SPRING_MODULES) {
                out.print(module);
                out.print("\t");
                out.print(packagesByModule.get(module).size());
                out.print("\t");
                out.print(packageInfoExistsByModule.get(module).size());
                out.print("\t");
                out.print(nonNullApiByModule.get(module).size());
                out.print("\t");
                out.println(nonNullFieldByModule.get(module).size());
            }
        }

        Column[] columns = new Column[]{
            Column.First,
            new Column() {
                @Override
                public String name() {
                    return "pck";
                }

                @Override
                public String value(String dataName) {
                    return Utils.format(packagesByModule.get(dataName).size());
                }
            },
            new Column() {
                @Override
                public String name() {
                    return "pck-pi";
                }

                @Override
                public String value(String dataName) {
                    return Utils.format(packageInfoExistsByModule.get(dataName).size());
                }
            },
            new Column() {
                @Override
                public String name() {
                    return "-nnullApi";
                }

                @Override
                public String value(String dataName) {
                    return Utils.format(nonNullApiByModule.get(dataName).size());
                }
            },
            new Column() {
                @Override
                public String name() {
                    return "-nnullField";
                }

                @Override
                public String value(String dataName) {
                    return Utils.format(nonNullFieldByModule.get(dataName).size());
                }
            },
        };

        TableGenerator csvOutput = new CSVTableGenerator(OUTPUT_CSV);
        TableGenerator latexOutput = new LatexTableGenerator(OUTPUT_LATEX,"|lrrrr|");

        this.run(SPRING_MODULES,"number of packages in selected spring modules (pck), packages with package-info.java (pck-pi), packages without \\texttt{@NonNullApi} (-nnullApi) and packages without  \\texttt{@NonNullFields} (-nnullFields) annotations","tab:package-annotations",columns,csvOutput,latexOutput);


    }

    private static boolean containsLine(File file,String line) {

        try {
            return Files.readAllLines(file.toPath()).stream()
                .map(l -> l.trim())
                .anyMatch(l -> l.equals(line));
        }
        catch (IOException x) {
            throw new RuntimeException(x);
        }
    }

    private static boolean containsClasses(File folder) {
        return Stream.of(folder.listFiles()).anyMatch(f -> f.getName().endsWith(".java"));
    }

}
