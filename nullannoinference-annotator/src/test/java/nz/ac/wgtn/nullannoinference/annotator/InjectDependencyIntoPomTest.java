package nz.ac.wgtn.nullannoinference.annotator;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test injection of dependency into pom.
 * @author jens dietrich
 */
public class InjectDependencyIntoPomTest {

    public static File TMP = new File(".tmp");

    private POMDependencyInjector injector = null;

    @BeforeEach
    public void setup() {
        this.injector = new POMDependencyInjector(new JSR305NullableAnnotationProvider());
    }

    @AfterEach
    public void tearDown() {
        this.injector = null;
    }
    @BeforeAll
    public static void globalSetup() {
        if (!TMP.exists()) {
            TMP.mkdirs();
        }
    }
    // dont clean up for inspection


    @Test
    public void testInjectDependencyIntoExistingDependenciesElement() throws Exception {
        File in = new File(BasicTests.class.getResource("/pom1.xml").getFile());
        File out = new File(TMP,"pom1.xml");
        boolean success = injector.addDependency(in,out);
        assertTrue(success);
        assertTrue(hasDependency(out));
    }

    @Test
    public void testInjectDependencyIntoNonExistingDependenciesElement() throws Exception {
        File in = new File(BasicTests.class.getResource("/pom2.xml").getFile());
        File out = new File(TMP,"pom2.xml");
        boolean success = injector.addDependency(in,out);
        assertTrue(success);
        assertTrue(hasDependency(out));
    }


    private boolean hasDependency(File pom) throws IOException, JDOMException {
        Namespace ns = Namespace.getNamespace("http://maven.apache.org/POM/4.0.0");
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(pom);
        Element root = doc.getRootElement();
        Element eDependencies = root.getChild("dependencies",ns);

        if (eDependencies==null) {
            return false;
        }

        for (Element eDependency:eDependencies.getChildren("dependency",ns)) {
            if (eDependency.getChild("groupId",ns)!=null
                    && eDependency.getChild("groupId",ns).getText().equals(this.injector.getAnnotationSpec().getNullableAnnotationArtifactGroupId())
                    && eDependency.getChild("artifactId",ns)!=null
                    && eDependency.getChild("artifactId",ns).getText().equals(this.injector.getAnnotationSpec().getNullableAnnotationArtifactId())) {
                return true;
            }
        }
        return false;
    }




}
