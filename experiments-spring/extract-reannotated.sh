#!/bin/bash

## extract existing Nullable annotations
## @author jens dietrich

. ./spring.env
if [ ! -d "$PROJECT_FOLDER" ]; then
    echo "Project folder does not exist, fetch projects first: $PROJECT_FOLDER"
    exit 1;
fi

if [ ! -d "$RESULT_FOLDER_EXTRACTED_REANNOTATED" ]; then
  echo "Result folder does not exit, creating folder: " $RESULT_FOLDER_EXTRACTED_REANNOTATED
  mkdir -p $RESULT_FOLDER_EXTRACTED_REANNOTATED
else
  echo "Results will be saved in " $RESULT_FOLDER_EXTRACTED_REANNOTATED
fi

for module in "${MODULES[@]}" ;do
    echo "extracting existing nullable annotations from: $module"
    java -jar $EXTRACTOR -p ${PROJECT_FOLDER_REANNOTATED}/${module} -t gradle_multilang -o $RESULT_FOLDER_EXTRACTED_REANNOTATED/$NULLABLE-${module}.json
done





