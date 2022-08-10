package nz.ac.wgtn.nullannoinference.sanitizer.mainscope;

import nz.ac.wgtn.nullannoinference.commons.Issue;
import nz.ac.wgtn.nullannoinference.commons.ProjectType;
import nz.ac.wgtn.nullannoinference.sanitizer.LogSystem;
import nz.ac.wgtn.nullannoinference.sanitizer.Sanitizer;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Sanitizer to remove issues in classes not in main scope.
 * In particular, this removes tests.
 * @author jens dietrich
 */
public class MainScopeSanitizer implements Sanitizer<Issue>  {

    private Set<String> classesInMainScope = null;
    public static final Logger LOGGER = LogSystem.getLogger("main-scope-analysis");

    public MainScopeSanitizer(ProjectType projectType, File projectRootFolder) {
        Collection<File> classFiles = projectType.getCompiledMainClasses(projectRootFolder);
        classesInMainScope = new HashSet<>();
        for (File f:classFiles) {
            collectClassNames(f,classesInMainScope);
        }
    }

    @Override
    public boolean test(Issue issue) {
        String clazz = issue.getClassName();
        if (classesInMainScope.contains(clazz)) {
            return true;
        }
        return false;
    }

    private void collectClassNames(File classFile, Set<String> classes)  {
        try {
            ClassReader clReader = new ClassReader(new FileInputStream(classFile));
            clReader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    super.visit(version, access, name, signature, superName, interfaces);
                    if ((access & Opcodes.ACC_SYNTHETIC) == 0) {
                        classes.add(name.replace('/', '.'));
                    }
                }
            },0);
        } catch (IOException e) {
            LOGGER.error("Cannot analyse class file: " + classFile.getAbsolutePath(),e);
        }
    }

}
