#!/bin/bash

## extract existing Nullable annotations
## @author jens dietrich

. ./additional.env
if [ ! -d "$PROJECT_FOLDER" ]; then
    echo "Project folder does not exist, fetch projects first: $PROJECT_FOLDER"
    exit 1;
fi

if [ ! -d "$RESULT_FOLDER_EXTRACTED" ]; then
  echo "Result folder does not exit, creating folder: " $RESULT_FOLDER_EXTRACTED
  mkdir -p $RESULT_FOLDER_EXTRACTED
else
  echo "Results will be saved in " $RESULT_FOLDER_EXTRACTED
fi

echo "extracting existing nullable annotations from: guava"
java -jar $EXTRACTOR -p ${PROJECT_FOLDER}/guava -t special.guava -o $RESULT_FOLDER_EXTRACTED/$NULLABLE-guava.json

echo "extracting existing nullable annotations from: error-prone"
java -jar $EXTRACTOR -p ${PROJECT_FOLDER}/error-prone -t special.errorprone -o $RESULT_FOLDER_EXTRACTED/$NULLABLE-error-prone.json
