#!/bin/bash

## sanitize the observed, sanitized and propagated issues (inferred issues need to be sanitised as well)
## @author jens dietrich

. ./spring.env

OUTPUT=$RESULT_FOLDER_OBSERVED_PROPAGATED_AND_SANITIZED
INPUT=$RESULT_FOLDER_OBSERVED_PROPAGATED

if [ ! -d "$INPUT" ]; then
    echo "Project folder does not exist, fetch projects first:" $INPUT
    exit 1;
fi

if [ ! -d "$OUTPUT" ]; then
  echo "Result folder does not exit, creating folder:" $OUTPUT
  mkdir -p $OUTPUT
else
  echo "Sanitized issues will be saved in " $OUTPUT
fi

for module in "${MODULES[@]}" ;do
  java -Xmx16g -jar $SANITIZER -s -d -m -n -i ${INPUT}/$NULLABLE-${module}.json -p ${PROJECT_FOLDER}/${module} -t gradle_multilang -o ${OUTPUT}/$NULLABLE-${module}.json -sh shaded.json
done