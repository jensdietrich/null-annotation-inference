package nz.ac.wgtn.nullinference.experiments.spring;

public interface Column {
    String name();
    String value(String dataName);


    Column First = new Column() {
        public String name() {
            return "program";
        }
        public String value(String dataName) {
            return dataName;
        }
    };
}
