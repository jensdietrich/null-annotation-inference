#!/bin/bash

## script to run infer
## @author jens dietrich

NAME=$1
ROOT="$(pwd)"
ORIGINAL_PROJECT_FOLDER=$ROOT/projects/original/$NAME
ANNOTATED_PROJECT_FOLDER=$ROOT/projects/annotated/$NAME
INFER_RESULTS=$ROOT/infer-results
INFER_RESULTS_PROJECT_ANNOTATED=$INFER_RESULTS/annotated/$NAME
INFER_RESULTS_PROJECT_ORIGINAL=$INFER_RESULTS/original/$NAME

if ! command -v infer &> /dev/null
then
    echo "infer could not be found, follow instruction on web site to install infer (tested version is v1.1.0-63a78acdd): https://fbinfer.com/docs/getting-started/"
    exit
fi

if [ ! -d "$ORIGINAL_PROJECT_FOLDER" ]; then
    echo "Original project folder missing: $ORIGINAL_PROJECT_FOLDER"
    exit 1
fi

if [ ! -d "$ANNOTATED_PROJECT_FOLDER" ]; then
    echo "Annotated project folder missing: $ANNOTATED_PROJECT_FOLDER"
    exit 1
fi

# reset result folders
if [ -d "$INFER_RESULTS_PROJECT_ORIGINAL" ]; then
    rm -r -f $INFER_RESULTS_PROJECT_ORIGINAL
fi
mkdir -p $INFER_RESULTS_PROJECT_ORIGINAL

if [ -d "$INFER_RESULTS_PROJECT_ANNOTATED" ]; then
    rm -r -f $INFER_RESULTS_PROJECT_ANNOTATED
fi
mkdir -p $INFER_RESULTS_PROJECT_ANNOTATED

cd $ORIGINAL_PROJECT_FOLDER
rm -r -f $ORIGINAL_PROJECT_FOLDER/infer-out
echo "Running infer on un-annotated project folder $ORIGINAL_PROJECT_FOLDER"
mvn clean
infer run -- mvn compile -Drat.skip=true
mv $ORIGINAL_PROJECT_FOLDER/infer-out $INFER_RESULTS_PROJECT_ORIGINAL

cd $ANNOTATED_PROJECT_FOLDER
rm -r -f $ANNOTATED_PROJECT_FOLDER/infer-out
echo "Running infer on annotated project folder $ANNOTATED_PROJECT_FOLDER"
mvn clean
infer run -- mvn compile -Drat.skip=true
mv $ANNOTATED_PROJECT_FOLDER/infer-out $INFER_RESULTS_PROJECT_ANNOTATED