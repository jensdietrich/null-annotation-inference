package nz.ac.wgtn.nullannoinference.commons;

/**
 * Abstraction describing a nullable annotation.
 * @author jens dietrich
 */
public interface NullableAnnotationProvider {

    String getName();

    String getNullableAnnotationName();

    String getNullableAnnotationPackageName();

    String getNullableAnnotationArtifactGroupId();

    String getNullableAnnotationArtifactId();

    String getNullableAnnotationArtifactVersion();

    default String getNullableAnnotationQName() {
        return getNullableAnnotationPackageName() + '.' + getNullableAnnotationName();
     }

}
