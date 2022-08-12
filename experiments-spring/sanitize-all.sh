#!/bin/bash

## sanitize tests applying all filters (shaded, deprecated, negative test, main scope)
## @author jens dietrich

. ./spring.env

OUTPUT=$RESULT_FOLDER_SANITIZED
INPUT=$RESULT_FOLDER_OBSERVED

if [ ! -d "$INPUT" ]; then
    echo "Project folder does not exist, fetch projects first:" $INPUT
    exit 1;
fi

if [ ! -d "$RESULT_FOLDER_OBSERVED" ]; then
    echo "Input issues missing:" $RESULT_FOLDER_OBSERVED
    exit 1;
fi

if [ ! -d "$OUTPUT" ]; then
  echo "Result folder does not exit, creating folder:" $OUTPUT
  mkdir -p $OUTPUT
else
  echo "Sanitized issues scoped will be saved in " $OUTPUT
fi

for module in "${MODULES[@]}" ;do
  java -jar $SANITIZER -s -d -m -n -i ${INPUT}/$NULLABLE-${module}.json -p ${PROJECT_FOLDER}/${module} -t gradle_multilang -o ${OUTPUT}/$NULLABLE-${module}.json -sh shaded.json
done