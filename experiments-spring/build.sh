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
mvn clean install
cp nullannoinference-propagator/target/propagator.jar $PROPAGATOR
cp nullannoinference-sanitizer/target/sanitizer.jar $SANITIZER
cp nullannoinference-extractor/target/extractor.jar $EXTRACTOR
cp nullannoinference-agent-jar-with-dependencies.jar $AGENT
cp nullannoinference-agent-nobb.jar $AGENT_NOBB
cp nullannoinference-agent2/target/nullannoinference-agent2.jar $AGENT2
cp nullannoinference-merger/target/merger.jar $MERGER
cp nullannoinference-annotator/target/annotator.jar $ANNOTATOR
