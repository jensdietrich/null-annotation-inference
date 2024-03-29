#!/bin/bash

## sanitise issues by removing issue in deprecated elements
## @author jens dietrich

. ./additional.env

OUTPUT=$RESULT_FOLDER_SANITIZEDN
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
mvn test-compile # this needs test binaries
cd ${ROOT}
start=`date +%s`
java -Xmx16g -jar $SANITIZER -n -i ${INPUT}/$NULLABLE-guava.json -p ${PROJECT_FOLDER}/guava -t special.guava -o ${OUTPUT}/$NULLABLE-guava.json -nt ${OUTPUT}/negtests-guava.csv
end=`date +%s`
runtime=$((end-start))
echo "$runtime" > ${OUTPUT}/runtime-guava.log

# analyse guava
cd ${PROJECT_FOLDER}/error-prone
mvn test-compile  # this needs test binaries
cd ${ROOT}
start=`date +%s`
java -Xmx16g -jar $SANITIZER -n -i ${INPUT}/$NULLABLE-error-prone.json -p ${PROJECT_FOLDER}/error-prone -t special.errorprone -o ${OUTPUT}/$NULLABLE-error-prone.json -nt ${OUTPUT}/negtests-error-prone.csv
end=`date +%s`
runtime=$((end-start))
echo "$runtime" > ${OUTPUT}/runtime-error-prone.log

cd ${ROOT}
