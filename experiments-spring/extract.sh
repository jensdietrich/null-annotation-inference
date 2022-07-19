#!/bin/bash

## extract existing Nullable annotations
## @author jens dietrich

. ./spring.env
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

for module in "${MODULES[@]}" ;do
    echo "extracting existing nullable annotations from: $module"
    java -jar $EXTRACTOR -p ${PROJECT_FOLDER}/${module} -t gradle_multilang -o $RESULT_FOLDER_EXTRACTED/$NULLABLE-${module}.json
done





