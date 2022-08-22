package nz.ac.wgtn.nullannoinference.sanitizer;

import nz.ac.wgtn.nullannoinference.commons.AbstractIssue;
import nz.ac.wgtn.nullannoinference.commons.Issue;

import java.util.function.Predicate;

/**
 * Abstract sanitizer.
 * @author jens dietrich
 */
public interface Sanitizer<T extends AbstractIssue> extends Predicate<T> {

    String SANITIZATION_VALUE_KEY = "sanitization.value";
    String SANITIZATION_SANITIZER_KEY = "sanitization.sanitizer";

    Sanitizer ALL = new Sanitizer() {
        @Override
        public String name() {
            return "all";
        }
        @Override
        public boolean test(Object o) {
            return true;
        }
    };

    Sanitizer NONE = new Sanitizer() {
        @Override
        public String name() {
            return "none";
        }
        @Override
        public boolean test(Object o) {
            return false;
        }
    };

    String name();

    @Override
    default Sanitizer<T> and(Predicate<? super T> other) {
        return new Sanitizer<T>() {
            @Override
            public boolean test(T t) {
                return Sanitizer.this.test(t) && other.test(t);
            }
            @Override
            public String name() {
                if (Sanitizer.this==ALL) return ((Sanitizer)other).name();
                return Sanitizer.this.name() + " & " + ((Sanitizer)other).name();
            }
        };
    }

    @Override
    default Sanitizer<T> or(Predicate<? super T> other) {
        return new Sanitizer<T>() {
            @Override
            public boolean test(T t) {
                return Sanitizer.this.test(t) || other.test(t);
            }
            @Override
            public String name() {
                if (Sanitizer.this==NONE) return ((Sanitizer)other).name();
                if (Sanitizer.this==NONE) return ((Sanitizer)other).name();
                return Sanitizer.this.name() + " | " + ((Sanitizer)other).name();
            }
        };
    }

    @Override
    default Sanitizer<T> negate() {
        return new Sanitizer<T>() {
            @Override
            public boolean test(T t) {
                return !Sanitizer.this.test(t);
            }
            @Override
            public String name() {
                return "not " + Sanitizer.this.name();
            }
        };
    }

    // indirection can be used to add provenance
    static boolean sanitize(Issue issue, Sanitizer<Issue> sanitizer) {
        boolean result = sanitizer.test(issue);
        issue.setProperty(SANITIZATION_VALUE_KEY,""+result);
        issue.setProperty(SANITIZATION_SANITIZER_KEY,sanitizer.name());
        return result;
    }
}