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
cp nullannoinference-extractor/target/extractor.jar $EXTRACTOR
cp nullannoinference-agent/target/nullannoinference-agent.jar $AGENT
cp nullannoinference-agent2/target/nullannoinference-agent2.jar $AGENT2
cp nullannoinference-merger/target/merger.jar $MERGER

