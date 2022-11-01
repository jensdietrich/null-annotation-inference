#! sh

PROJECTS=("commons-io-2.5" "commons-cli-3.1" "commons-maths-3.0" "commons-lang-3.0")

# EXTRACT NON_NULL ISSUES
echo "Extracting inferred non-null issues"
for PROJECT in "${PROJECTS[@]}"; do
    PROJECT_INFERRED=$PROJECT"-inferred"
    echo "Extracting inferred non-null issues from $PROJECT_INFERRED  (project must have been build with mvn compile)"
    java -cp tools/target/tools.jar ExtractNonNullAnnotations $PROJECT_INFERRED results/extracted/nonnull-$PROJECT.json
done
