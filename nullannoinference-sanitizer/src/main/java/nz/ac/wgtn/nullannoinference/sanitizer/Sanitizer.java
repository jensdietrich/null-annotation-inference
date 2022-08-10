package nz.ac.wgtn.nullannoinference.sanitizer;

import nz.ac.wgtn.nullannoinference.commons.AbstractIssue;
import java.util.function.Predicate;

/**
 * Abstract sanitizer.
 * @author jens dietrich
 */
public interface Sanitizer<T extends AbstractIssue> extends Predicate<T> {

    @Override
    default Sanitizer<T> and(Predicate<? super T> other) {
        return and(other);
    }

    @Override
    default Sanitizer<T> or(Predicate<? super T> other) {
        return or(other);
    }

    @Override
    default Sanitizer<T> negate() {
        return negate();
    }
}


