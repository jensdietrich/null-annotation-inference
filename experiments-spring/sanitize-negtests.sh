#!/bin/bash

##  sanitise issues observed during the execution of negative tests
## @author jens dietrich

. ./spring.env
if [ ! -d "$PROJECT_FOLDER" ]; then
    echo "Project folder does not exist, fetch projects first: $PROJECT_FOLDER"
    exit 1;
fi

if [ ! -d "$RESULT_FOLDER_OBSERVED3" ]; then
  echo "Result folder does not exit, creating folder: " $RESULT_FOLDER_OBSERVED3
  mkdir -p $RESULT_FOLDER_OBSERVED3
else
  echo "Issues scoped will be saved in " $RESULT_FOLDER_OBSERVED3
fi

# add scope
for module in "${MODULES[@]}" ;do
  echo "Adding scope attribute to collected issues: " $RESULT_FOLDER_OBSERVED/$NULLABLE-${module}.json
  java -jar $SANITIZER -p ${PROJECT_FOLDER}/${module} -n $RESULT_FOLDER_OBSERVED3/negative-tests-${module}.csv -t gradle_multilang -i $RESULT_FOLDER_OBSERVED2/$NULLABLE-${module}.json -s $RESULT_FOLDER_OBSERVED3/$NULLABLE-${module}.json
done



