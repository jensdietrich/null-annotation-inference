package nz.ac.wgtn.nullannoinference.commons;

/**
 * Specifies the JSR305 nullable annotation.
 * @author jens dietrich
 */

public class JSR305NullableAnnotationProvider implements NullableAnnotationProvider {

    @Override
    public String getName() {
        return "JSR305";
    }

    @Override
    public String getNullableAnnotationName() {
        return "Nullable";
    }

    @Override
    public String getNullableAnnotationPackageName() {
        return "javax.annotation";
    }

    @Override
    public String getNullableAnnotationArtifactGroupId() {
        return "com.google.code.findbugs";
    }

    @Override
    public String getNullableAnnotationArtifactId() {
        return "jsr305";
    }

    @Override
    public String getNullableAnnotationArtifactVersion() {
        return "3.0.2";
    }
}
