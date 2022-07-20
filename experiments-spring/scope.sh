#!/bin/bash

## add scope attribute to issues
## @author jens dietrich

. ./spring.env
if [ ! -d "$PROJECT_FOLDER" ]; then
    echo "Project folder does not exist, fetch projects first: $PROJECT_FOLDER"
    exit 1;
fi

if [ ! -d "$RESULT_FOLDER_OBSERVED2" ]; then
  echo "Result folder does not exit, creating folder: " $RESULT_FOLDER_OBSERVED2
  mkdir -p $RESULT_FOLDER_OBSERVED2
else
  echo "Issues scoped will be saved in " $RESULT_FOLDER_OBSERVED2
fi

# add scope
for module in "${MODULES[@]}" ;do
  echo "Adding scope attribute to collected issues: " $RESULT_FOLDER_OBSERVED/$NULLABLE-${module}.json
  java -jar $SCOPER -p ${PROJECT_FOLDER}/${module} -t gradle_multilang -i $RESULT_FOLDER_OBSERVED/$NULLABLE-${module}.json -o $RESULT_FOLDER_OBSERVED2/$NULLABLE-${module}.json
done



