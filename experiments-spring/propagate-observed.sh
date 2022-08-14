#!/bin/bash

## propagate extracted annotations applying LSP
## @author jens dietrich

. ./spring.env
if [ ! -d "$PROJECT_FOLDER" ]; then
    echo "Project folder does not exist, fetch projects first: $PROJECT_FOLDER"
    exit 1;
fi

if [ ! -d "$RESULT_FOLDER_OBSERVED_PROPAGATED" ]; then
  echo "Result folder does not exit, creating folder: " $RESULT_FOLDER_OBSERVED_PROPAGATED
  mkdir -p $RESULT_FOLDER_OBSERVED_PROPAGATED
else
  echo "Results will be saved in " $RESULT_FOLDER_OBSERVED_PROPAGATED
fi

for module in "${MODULES[@]}" ;do
    echo "inferring additional issues from existing nullable annotations found in module: $module"

    prefix='org.springframework'
    if [ "$module"  = 'spring-beans' ]; then
       prefix='org.springframework.beans'
    elif [ "$module"  = 'spring-orm' ]; then
       prefix='org.springframework.orm'
    elif [ "$module"  = 'spring-oxm' ]; then
       prefix='org.springframework.oxm'
    elif [ "$module"  = 'spring-web' ]; then
       prefix='org.springframework'
    elif [ "$module"  = 'spring-webmvc' ]; then
         prefix='org.springframework.web'
    fi

    echo "using package prefix: " $prefix
    java -jar $PROPAGATOR -p ${PROJECT_FOLDER}/${module} -t gradle_multilang -i ${RESULT_FOLDER_SANITIZED}/$NULLABLE-${module}.json -o ${RESULT_FOLDER_OBSERVED_PROPAGATED}/$NULLABLE-${module}.json -x $prefix
done





