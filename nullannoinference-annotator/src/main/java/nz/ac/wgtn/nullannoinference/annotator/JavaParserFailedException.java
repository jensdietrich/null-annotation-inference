package nz.ac.wgtn.nullannoinference.annotator;

/**
 * Exception to signal that the parsing of a file has failed.
 * @author jens dietrich
 */
public class JavaParserFailedException extends Exception {
    public JavaParserFailedException() {
    }

    public JavaParserFailedException(String message) {
        super(message);
    }

    public JavaParserFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public JavaParserFailedException(Throwable cause) {
        super(cause);
    }

    public JavaParserFailedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
