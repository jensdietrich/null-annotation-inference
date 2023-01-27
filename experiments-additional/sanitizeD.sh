#!/bin/bash

## sanitise issues by removing issue in deprecated elements
## @author jens dietrich

. ./additional.env

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

# analyse guava
cd ${PROJECT_FOLDER}/guava
mvn compile
cd ${ROOT}
start=`date +%s`
java -Xmx16g -jar $SANITIZER -d -i ${INPUT}/$NULLABLE-guava.json -p ${PROJECT_FOLDER}/guava -t special.guava -o ${OUTPUT}/$NULLABLE-guava.json -de ${OUTPUT}/deprecated-guava.txt
end=`date +%s`
runtime=$((end-start))
echo "$runtime" > ${OUTPUT}//runtime-guava.log

# analyse guava
cd ${PROJECT_FOLDER}/error-prone
mvn compile
cd ${ROOT}
start=`date +%s`
java -Xmx16g -jar $SANITIZER -d -i ${INPUT}/$NULLABLE-error-prone.json -p ${PROJECT_FOLDER}/error-prone -t special.errorprone -o ${OUTPUT}/$NULLABLE-error-prone.json -de ${OUTPUT}/deprecated-error-prone.txt
end=`date +%s`
runtime=$((end-start))
echo "$runtime" > ${OUTPUT}//runtime-error-prone.log

cd ${ROOT}
