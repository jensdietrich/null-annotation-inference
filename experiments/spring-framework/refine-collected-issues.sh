#!/bin/bash

## script to refine fetched nullability issues, rejecting some and inferring additional ones
## @author jens dietrich

ROOT="$(pwd)"
PROJECT_FOLDER=$ROOT/spring-framework
ISSUE_INFERRED=$ROOT/issues-inferred
ISSUES_COLLECTED=$ROOT/issues-collected
REFINER=nullannoinference-refiner.jar
REFINER_PATH=nullannoinference-refiner/target/$REFINER
modules=('spring-core' 'spring-beans' 'spring-orm' 'spring-oxm' 'spring-context' 'spring-web' 'spring-webmvc')

# copy refiner
cd ..
cd ..
if [ ! -f "$REFINER_PATH" ]; then
    echo "Refiner not found, must build project first: $REFINER_PATH"
    exit 1
fi
cp $REFINER_PATH $ROOT

cd $ROOT
for module in "${modules[@]}" ;do
    mkdir -p $ISSUE_INFERRED/$module
    rm $ISSUE_INFERRED/$module/*.json
    NEGATIVE_TEST_LIST=$ISSUE_INFERRED/$module/negative-tests.csv
    SUMMARY=$ISSUE_INFERRED/$module/summary.csv
    echo "running inference for module $module"
    cd $ROOT

    # pick prefix
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
    echo "module: $module"
    echo "prefix: $prefix"
    java -jar $REFINER -i $ROOT/issues-collected/$module -a -s $ISSUE_INFERRED/$module -n $NEGATIVE_TEST_LIST -p $PROJECT_FOLDER/$module -o $SUMMARY -x $prefix -t gradle_multilang
done


cd $ROOT




