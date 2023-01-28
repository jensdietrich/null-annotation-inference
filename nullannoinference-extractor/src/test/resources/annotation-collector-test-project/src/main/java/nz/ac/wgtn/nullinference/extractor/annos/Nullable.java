package nz.ac.wgtn.nullinference.extractor.annos;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import static java.lang.annotation.ElementType.TYPE_PARAMETER;
import static java.lang.annotation.ElementType.TYPE_USE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Special TYPE_USE annotation, similar to org.checkerframework.checker.nullness.qual.Nullable
 * @author jens dietrich
 */
@Retention(value=RUNTIME)
@Target(value={TYPE_USE,TYPE_PARAMETER})

public @interface Nullable {
}
