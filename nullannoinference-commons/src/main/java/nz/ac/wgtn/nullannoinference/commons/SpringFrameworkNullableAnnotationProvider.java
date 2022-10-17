package nz.ac.wgtn.nullannoinference.commons;

/**
 * Specifies the JSR305 nullable annotation.
 * @author jens dietrich
 */

public class SpringFrameworkNullableAnnotationProvider implements NullableAnnotationProvider {

    @Override
    public String getName() {
        return "spring";
    }

    @Override
    public String getNullableAnnotationName() {
        return "Nullable";
    }

    @Override
    public String getNullableAnnotationPackageName() {
        return "org.springframework.lang";
    }

    @Override
    public String getNullableAnnotationArtifactGroupId() {
        return "org.springframework";
    }

    @Override
    public String getNullableAnnotationArtifactId() {
        return "spring-core";
    }

    // needs to be manually specified
    @Override
    public String getNullableAnnotationArtifactVersion() {
        return null;
    }
}
