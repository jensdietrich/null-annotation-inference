#!/bin/bash

## sanitise issues by removing issue in deprecated elements
## @author jens dietrich

. ./spring.env

OUTPUT=$RESULT_FOLDER_SANITIZEDD
INPUT=$RESULT_FOLDER_OBSERVED

if [ ! -d "$INPUT" ]; then
    echo "input folder foes not exist:" $INPUT
    exit 1;
fi

if [ ! -d "$OUTPUT" ]; then
  echo "output folder does not exit, creating folder:" $OUTPUT
  mkdir -p $OUTPUT
else
  echo "results will be saved in " $OUTPUT
fi

for module in "${MODULES[@]}" ;do
  java -Xmx16g -jar $SANITIZER -d -i ${INPUT}/$NULLABLE-${module}.json -p ${PROJECT_FOLDER}/${module} -t gradle_multilang -o ${OUTPUT}/$NULLABLE-${module}.json -de ${OUTPUT}/deprecated-${module}.txt
done