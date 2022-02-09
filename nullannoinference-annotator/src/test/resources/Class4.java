// simple Java class

// some imports, not used
import java.util.Comparator;
import java.util.List;
import java.io.*;

public class Class4 {

    class Inner {
        public String foo(String s) {
            Object printable = new Object() {
                @Override
                public String toString() throws Exception {
                    return "foo";
                }
            };
            return printable.toString();
        };
    }

    public void foo() {
        Comparator<String> comparator = new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return 0;
            }

            // to test multiple methods in an ano inner class
            @Override
            public String toString() {
                return "foo";
            }
        };
    }
}