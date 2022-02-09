package nz.ac.wgtn.nullannoinference.annotator;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract test superclass with a common fixture and some useful utilities.
 * @author jens dietrich
 */
public abstract class AbstractInjectAnnotationTest {

    public static File TMP = new File(".tmp");
    protected ClassAnnotator annotator = null;

    @BeforeEach
    public void setup() {
        this.annotator = new ClassAnnotator(new JSR305NullableAnnotationProvider());
    }

    @AfterEach
    public void tearDown() {
        this.annotator = null;
    }
    @BeforeAll
    public static void globalSetup() {
        if (!TMP.exists()) {
            TMP.mkdirs();
        }
    }
    // dont clean up for inspection


    protected MethodDeclaration findMethod(File src,String className,String methodName,String descriptor) throws FileNotFoundException {
        ParseResult<CompilationUnit> result = new JavaParser().parse(src);
        assertTrue(result.isSuccessful());
        CompilationUnit cu = result.getResult().get();
        assertNotNull(cu);
        MethodDeclaration method = ClassAnnotator.locateMethod(cu,className,methodName,descriptor);
        assertNotNull(method);
        return method;
    }

    protected List<MethodDeclaration> findAnoInnerMethods(File src,String className,String methodName,String descriptor) throws FileNotFoundException {
            final List<MethodDeclaration> matchingMethods = new ArrayList<>(); // deliberate, to detect duplicates later
            JavaParser javaParser = new JavaParser();
            ParseResult<CompilationUnit> result = javaParser.parse(src);
            assertTrue(result.isSuccessful());
            CompilationUnit cu = result.getResult().get();
            cu.accept(new VoidVisitorAdapter<Object>() {
                @Override
                public void visit(ObjectCreationExpr n, Object arg) {
                    super.visit(n, arg);
                    Optional<NodeList<BodyDeclaration<?>>> optClassBody = n.getAnonymousClassBody();
                    if (optClassBody.isPresent()) {
                        List<MethodDeclaration> matchingMethods2 = optClassBody.get().stream()
                            .filter(bd -> bd.isMethodDeclaration())
                            .map(bd -> bd.asMethodDeclaration())
                            .filter(md -> ClassAnnotator.nonAnoTypePartMatches(md,className))
                            .filter(md -> md.getNameAsString().equals(methodName))
                            .filter(md -> ClassAnnotator.descriptorMatches(md,descriptor))
                            .collect(Collectors.toList());
                        matchingMethods.addAll(matchingMethods2);
                    }
                }
            },null);

            return matchingMethods;
        }

}
