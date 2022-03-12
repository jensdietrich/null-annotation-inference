#!/bin/bash

## script to fetch program, run instrumented tests and collect nullability issues
## @author jens dietrich

REPO=$1
VERSION=$2
NAME=$3
AGENT="nullannoinference-agent/target/nullannoinference-agent.jar"
AGENT2="nullannoinference-agent2/target/nullannoinference-agent2.jar"
SCOPER="nullannoinference-scoper/target/nullannoinference-scoper.jar"
SCOPERL="nullannoinference-scoper.jar"
ROOT="$(pwd)"
PROJECT_FOLDER=$ROOT/projects/original/$NAME

if [ -d "$PROJECT_FOLDER" ]; then
    echo "Using existing project: $PROJECT_FOLDER"
else
    echo "Cloning project from $REPO"
    git clone $REPO $PROJECT_FOLDER
    cd $PROJECT_FOLDER
    git checkout tags/$VERSION
    cd ..
fi

echo "rebuilding project in $PROJECT_FOLDER"
cd $PROJECT_FOLDER
mvn clean compile

echo "building instrumented tests"
cd $ROOT
cd ..
if [ ! -f "$AGENT" ]; then
    echo "Agent not found - must build entire project first: $AGENT"
    exit 1
fi

cp $AGENT $PROJECT_FOLDER
if [ ! -f "$AGENT2" ]; then
    echo "Agent not found - must build entire project first: $AGENT2"
    exit 1
fi
cp $AGENT2 $PROJECT_FOLDER

if [ ! -f "$SCOPER" ]; then
    echo "Scoper not found - must build entire project first: $SCOPER"
    exit 1
fi
cp $SCOPER $PROJECT_FOLDER

cd $ROOT
cp instrumented-poms/$NAME.xml $PROJECT_FOLDER/pom-instrumented.xml

echo "collecting nullability by running instrumented tests"
cd $PROJECT_FOLDER
# note: rat plugin may cause problems, just disable it
mvn clean test -f pom-instrumented.xml -Drat.skip=true

# move issues into issues-collected folder
cd $ROOT
if [ ! -d issues-collected/$NAME ]
then
    mkdir -p issues-collected/$NAME
fi
# cull old issues as files have timestamps and will not be overridden
rm issues-collected/$NAME/*
mv $PROJECT_FOLDER/null-issues-*.json issues-collected/$NAME

echo "adding scope to collected issues"
cd $PROJECT_FOLDER
java -jar $SCOPERL -i $ROOT/issues-collected/$NAME -p $PROJECT_FOLDER
