#!/bin/bash

## use the issues collected and refined to annotate actual projects
## @author jens dietrich

NAME=$1
ROOT="$(pwd)"
PROJECT_FOLDER=$ROOT/projects/original/$NAME
ANNOTATED_PROJECT_FOLDER=$ROOT/projects/annotated/$NAME
INFERRED_ISSUES_FOLDER=$ROOT/issues-inferred/$NAME

ANNOTATOR=nullannoinference-annotator.jar
ANNOTATOR_PATH=nullannoinference-annotator/target/nullannoinference-annotator.jar

if [ ! -d "$PROJECT_FOLDER" ]; then
    echo "Project missing: $PROJECT_FOLDER"
    exit 1
fi

if [ ! -d "$INFERRED_ISSUES_FOLDER" ]; then
    echo "No issues found, run collect-nullability-*.sh and refine-collected-*.sh scripts first: $INFERRED_ISSUES_FOLDER"
    exit 1
fi

cd ..

if [ ! -f "$ANNOTATOR_PATH" ]; then
    echo "Project must be build first with \"mvn package\" in order to create the annotator executable: $ANNOTATOR_PATH"
    exit 1
fi

cd $ROOT
if [ -f "$ANNOTATOR" ]; then
    echo "delete existing annotator to ensure latest version is used"
    rm $ANNOTATOR
fi


echo "running annotator"
cd $ROOT

cd ..
cp $ANNOTATOR_PATH $ROOT
cd $ROOT
java -jar $ANNOTATOR -p $PROJECT_FOLDER -o $ANNOTATED_PROJECT_FOLDER -i $INFERRED_ISSUES_FOLDER

