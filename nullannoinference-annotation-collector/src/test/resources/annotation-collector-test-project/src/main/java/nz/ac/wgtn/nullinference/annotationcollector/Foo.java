package nz.ac.wgtn.nullinference.annotationcollector;

import javax.annotation.Nullable;

public class Foo {

    private @Nullable String field1 = null;
    private String field2 = null;

    public @Nullable String m1(@Nullable String arg) {
        return arg;
    }

    public String m2(String arg) {
        return arg;
    }

}
