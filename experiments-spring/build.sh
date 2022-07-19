#!/bin/bash

## script to build the project and copy the executables into this folder
## @author jens dietrich

. ./spring.env

if [ ! -d "$ANALYSES_FOLDER" ]; then
  echo "Analyses folder does not exit, creating folder: " $ANALYSES_FOLDER
  mkdir -p $ANALYSES_FOLDER
else
  echo "Built analyses will be copied " $ANALYSES_FOLDER
fi

cd ..
mvn clean package
cp nullannoinference-propagator/target/propagator.jar $PROPAGATOR
cp nullannoinference-sanitizer/target/sanitizer.jar $SANITIZER
cp nullannoinference-scoper/target/scoper.jar $SCOPER
cp nullannoinference-extractor/target/extractor.jar $EXTRACTOR



