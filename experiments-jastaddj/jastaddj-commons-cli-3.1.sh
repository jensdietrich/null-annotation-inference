#! sh

PROJECT=commons-cli-3.1
SRC=$PROJECT/src/main/java
INFERRED=inferred

rm -r $INFERRED

java -jar JavaNonNullInferencer.jar \
-disableraw \
-sourcepath $SRC \
-d $INFERRED \
$SRC/org/apache/commons/cli/HelpFormatter.java $SRC/org/apache/commons/cli/CommandLineParser.java $SRC/org/apache/commons/cli/TypeHandler.java $SRC/org/apache/commons/cli/Options.java $SRC/org/apache/commons/cli/DefaultParser.java $SRC/org/apache/commons/cli/BasicParser.java $SRC/org/apache/commons/cli/MissingOptionException.java $SRC/org/apache/commons/cli/AlreadySelectedException.java $SRC/org/apache/commons/cli/CommandLine.java $SRC/org/apache/commons/cli/ParseException.java $SRC/org/apache/commons/cli/OptionBuilder.java $SRC/org/apache/commons/cli/PatternOptionBuilder.java $SRC/org/apache/commons/cli/GnuParser.java $SRC/org/apache/commons/cli/UnrecognizedOptionException.java $SRC/org/apache/commons/cli/AmbiguousOptionException.java $SRC/org/apache/commons/cli/OptionValidator.java $SRC/org/apache/commons/cli/Util.java $SRC/org/apache/commons/cli/PosixParser.java $SRC/org/apache/commons/cli/OptionGroup.java $SRC/org/apache/commons/cli/MissingArgumentException.java $SRC/org/apache/commons/cli/Parser.java $SRC/org/apache/commons/cli/Option.java

# replace inferred annotations @NonNull   -> @javax.annotations.Nonnull
# to make this an "actual" annotation and the project compilable
java -cp tools/target/tools.jar TranslateNonnullAnnotations $INFERRED
