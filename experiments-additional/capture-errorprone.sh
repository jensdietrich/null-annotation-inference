#!/bin/bash

## observe nullability while exercising a program running its tests
## @author jens dietrich


. ./additional.env
NAME=error-prone
PROJECT_FOLDER=$PROJECT_FOLDERS/$NAME

if [ ! -d "$PROJECT_FOLDER" ]; then
    echo "Project folder does not exist, fetch projects first: $PROJECT_FOLDER"
fi

INSTRUMENTED_PROJECT_FOLDER=$INSTRUMENTED_PROJECT_FOLDERS/$NAME
    
if [ -d "$INSTRUMENTED_PROJECT_FOLDER" ]; then
    echo "Instrumented project already exists, will reuse (delete folder to recreate): $INSTRUMENTED_PROJECT_FOLDER"
else
    echo "copying project"
    mkdir -p $INSTRUMENTED_PROJECT_FOLDER
    cp -r $PROJECT_FOLDER $INSTRUMENTED_PROJECT_FOLDERS

    echo "replacing build script by instrumented script"
    cp ${INSTRUMENTED_BUILD_SCRIPT_FOLDER}/${NAME}/pom.xml $INSTRUMENTED_PROJECT_FOLDER
fi


if [ ! -d "$RESULT_FOLDER_OBSERVED" ]; then
  echo "Result folder does not exit, creating folder: " $RESULT_FOLDER_OBSERVED
  mkdir -p $RESULT_FOLDER_OBSERVED
else
  echo "Issues observed will be saved in " $RESULT_FOLDER_OBSERVED
fi

# copy agents
echo "copying agents"
cp $AGENT2 $INSTRUMENTED_PROJECT_FOLDER/core
cp $AGENT_NOBB $INSTRUMENTED_PROJECT_FOLDER/core

echo "cleaning old issues"
rm $INSTRUMENTED_PROJECT_FOLDER/core/null-*.json

cd $INSTRUMENTED_PROJECT_FOLDER

echo "running instrumented build"
start=`date +%s`
mvn clean test -Dmaven.test.failure.ignore=true -Dmaven.test.error.ignore=true
end=`date +%s`
runtime=$((end-start))
echo "$runtime" > $RESULT_FOLDER_OBSERVED/capture-runtime-${NAME}.log

echo "merging results"
java -Xmx20g -jar $MERGER -i ${INSTRUMENTED_PROJECT_FOLDER}/core -o ${RESULT_FOLDER_OBSERVED}/${NULLABLE}-${NAME}.json

echo "cleaning raw captured data"
rm $INSTRUMENTED_PROJECT_FOLDER/core/null-*.json
echo "done -- merged results written to ${RESULT_FOLDER_OBSERVED}/$NULLABLE-${NAME}.json"
