// simple Java class

// some imports, not used
import java.util.List;
import java.io.*;

public class Class3 {

    public class Inner1 {
        public Object foo() {
            return arg1;
        }
    }

    public class Inner2 {
        public Object foo() {
            return arg1;
        }
    }

    public class Inner3 {
        public class Inner31 {
            public Object foo() {
                return arg1;
            }
        }
        public class Inner32 {
            public Object foo() {
                return arg1;
            }
        }
    }
}