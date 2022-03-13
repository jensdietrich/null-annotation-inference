#!/bin/bash

## script to refine fetched nullability issues, rejecting some and inferring additional ones
## @author jens dietrich

NAME=$1
PREFIX=$2
ROOT="$(pwd)"
PROJECT_FOLDER=$ROOT/spring-framework
ISSUE_INFERRED=$ROOT/issues-inferred
ISSUES_COLLECTED=$ROOT/issues-collected
REFINER=nullannoinference-refiner.jar
REFINER_PATH=nullannoinference-refiner/target/$REFINER
modules=('spring-core' 'spring-beans')
declare -A prefixes
prefixes[spring-core]="org.springframework"
prefixes[spring-beans]="org.springframework.beans"

# copy refiner
cd ..
cd ..
if [ ! -f "$REFINER_PATH" ]; then
    echo "Refiner not found, must build project first: $REFINER_PATH"
    exit 1
fi
cp $REFINER_PATH $ROOT

cd $ROOT
for module in "${modules[@]}" ;do
    mkdir -p $ISSUE_INFERRED/$module
    rm $ISSUE_INFERRED/$module/*.json
    NEGATIVE_TEST_LIST=$ISSUE_INFERRED/$module/negative-tests.csv
    SUMMARY=$ISSUE_INFERRED/$module/summary.csv
    echo "running inference for module $module"
    cd $ROOT
    java -jar $REFINER -i $ROOT/issues-collected/$module -a -s $ISSUE_INFERRED -n $NEGATIVE_TEST_LIST -p $PROJECT_FOLDER/$module -o $SUMMARY -x prefixes[$module]
done





cd $ROOT




