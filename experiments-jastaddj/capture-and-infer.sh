#! sh

PROJECTS=("commons-io-2.5" "commons-cli-3.1" "commons-lang-3.0")

for PROJECT in "${PROJECTS[@]}"; do
    echo "STEP 1: merging captured issues for ${PROJECT} ====== "
    java -jar analyses/merger.jar -i $PROJECT"-inferred" -o results/captured/null-issues-$PROJECT.json
    echo ""
    echo "=========="
    echo ""
    
    # note: none of the project is using shaded dependencies, so shading analysis is ommitted
    echo "STEP 2: sanitising captured issues for ${PROJECT} ====== "
    java -jar analyses/sanitizer.jar -d -n -m -t mvn -i results/captured/null-issues-$PROJECT.json -p $PROJECT-inferred -o results/sanitised/null-issues-$PROJECT.json
    echo ""
    echo "=========="
    echo ""
    
    echo "STEP 3: propagating issues for ${PROJECT} ====== "
    java -jar analyses/propagator.jar -i results/sanitised/null-issues-$PROJECT.json -o results/propagated/null-issues-$PROJECT.json -t mvn -x org.apache.commons -p $PROJECT-inferred
    echo ""
    echo "=========="
    echo ""
done
