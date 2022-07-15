#!/bin/bash

## extract existing Nullable annotations
## @author jens dietrich

. ./spring.env
if [ ! -d "$PROJECT_FOLDER" ]; then
    echo "Project folder does not exist, fetch projects first: $PROJECT_FOLDER"
    exit 1;
fi

for module in "${MODULES[@]}" ;do
    echo "analysing module: $module"
    java -jar $EXTRACTOR -p ${PROJECT_FOLDER}/${module} -i nullable-annotats-found-${module}.json -t gradle_multilang

    # java -jar $REFINER -i $ROOT/issues-collected/$module -a -s $ISSUE_INFERRED/$module -n $NEGATIVE_TEST_LIST -p $PROJECT_FOLDER/$module -o $SUMMARY -x $prefix -t gradle_multilang
done





