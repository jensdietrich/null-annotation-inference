package nz.ac.wgtn.nullannoinference.annotator;

import com.google.common.base.Preconditions;
import nz.ac.wgtn.nullannoinference.commons.JSR305NullableAnnotationProvider;
import nz.ac.wgtn.nullannoinference.commons.NullableAnnotationProvider;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Utility to add a dependency (containing annotations) to a pom.
 * @author jens dietrich
 */
public class POMDependencyInjector {

    public POMDependencyInjector(@Nonnull NullableAnnotationProvider annotationSpec) {
        this.annotationSpec = annotationSpec;
    }
    private @Nonnull
    NullableAnnotationProvider annotationSpec = new JSR305NullableAnnotationProvider();

    public NullableAnnotationProvider getAnnotationSpec() {
        return annotationSpec;
    }

    public void setAnnotationSpec(@Nonnull NullableAnnotationProvider annotationSpec) {
        this.annotationSpec = annotationSpec;
    }

    public boolean addDependency(@Nonnull File originalPom,@Nonnull File transformedPom) throws IOException, JDOMException {
        Preconditions.checkArgument(originalPom.exists());

        System.out.println("Analysing pom: " + originalPom.toPath().toString());
        Namespace ns = Namespace.getNamespace("http://maven.apache.org/POM/4.0.0");
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(originalPom);
        Element root = doc.getRootElement();
        Element eDependencies = root.getChild("dependencies",ns);

        if (eDependencies==null) {
            eDependencies = new Element("dependencies",ns);
            root.addContent(eDependencies);
        }

        // look whether dependency already exists, ignoring version
        // if so, print warning and return false
        for (Element eDependency:eDependencies.getChildren("dependency",ns)) {
            if (eDependency.getChild("groupId")!=null
                && eDependency.getChild("groupId").getText().equals(this.annotationSpec.getNullableAnnotationArtifactGroupId())
                && eDependency.getChild("artifactId")!=null
                && eDependency.getChild("artifactId").getText().equals(this.annotationSpec.getNullableAnnotationArtifactId())) {
                System.out.println("Dependency already exists in " + originalPom.getAbsolutePath() + " nothing to do");
                return false;
            }
        }

        Element eDependency = new Element("dependency",ns);
        eDependency.addContent(new Element("groupId",ns).addContent(this.annotationSpec.getNullableAnnotationArtifactGroupId()));
        eDependency.addContent(new Element("artifactId",ns).addContent(this.annotationSpec.getNullableAnnotationArtifactId()));
        eDependency.addContent(new Element("version",ns).addContent(this.annotationSpec.getNullableAnnotationArtifactVersion()));
        eDependencies.addContent(eDependency);

        XMLOutputter xmlOutputter = new XMLOutputter();
        try (FileWriter out = new FileWriter(transformedPom)) {
            xmlOutputter.setFormat(Format.getPrettyFormat());
            xmlOutputter.output(doc,out);
            System.out.println("Transformed pom written to " + transformedPom.getAbsolutePath());
            return true;
        }

    }

}
