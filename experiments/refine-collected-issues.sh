#!/bin/bash

## script to refine fetched nullability issues, rejecting some and inferring additional ones
## @author jens dietrich

NAME=$1
PREFIX=$2
ROOT="$(pwd)"
PROJECT_FOLDER=$ROOT/projects/original/$NAME
ISSUE_FOLDER=$ROOT/issues-collected/$NAME
INFERRED_ISSUES_FOLDER=$ROOT/issues-inferred/$NAME
NEGATIVE_TEST_LIST=$INFERRED_ISSUES_FOLDER/negative-tests.csv
SUMMARY=$INFERRED_ISSUES_FOLDER/summary.csv
REFINER=nullannoinference-refiner.jar
REFINER_PATH=nullannoinference-refiner/target/nullannoinference-refiner.jar

if [ ! -d "$PROJECT_FOLDER" ]; then
    echo "Project missing: $PROJECT_FOLDER"
    exit 1
fi

if [ ! -d "$ISSUE_FOLDER" ]; then
    echo "No issues found, run collect-nullability-*.sh script first: $ISSUE_FOLDER"
    exit 1
fi

cd ..

if [ ! -f "$REFINER_PATH" ]; then
    echo "Project must be build first with \"mvn package\" in order to create the refiner executable: $REFINER_PATH"
    exit 1
fi

cd $ROOT
if [ -f "$REFINER" ]; then
    echo "delete existing refiner to ensure latest version is used"
    rm $REFINER
fi


if [ ! -d "$INFERRED_ISSUES_FOLDER" ]; then
    echo "create non-existing folder $INFERRED_ISSUES_FOLDER"
    mkdir -p "$INFERRED_ISSUES_FOLDER"
fi

echo "rebuilding project in $PROJECT_FOLDER"
cd $PROJECT_FOLDER
mvn clean test-compile -Drat.skip=true

echo "running inference"
cd $ROOT

cd ..
cp nullannoinference-refiner/target/$REFINER $ROOT
cd $ROOT
echo "name: $NAME"
echo "prefix: $PREFIX"
java -jar $REFINER -i $ISSUE_FOLDER -a -s $INFERRED_ISSUES_FOLDER -n $NEGATIVE_TEST_LIST -p $PROJECT_FOLDER -o $SUMMARY -x $PREFIX -t mvn

