#!/bin/bash

## script to fetch program, analyse it and add null annotations
## @author jens dietrich

## TODO pass variables as parameters, using this as template for other experiments
REPO="https://github.com/apache/commons-codec.git"
VERSION="rel/commons-codec-1.15"
NAME="commons-codec"
AGENT="nullannoinference-agent/target/nullannoinference-agent.jar"

if [ -f "$NAME" ]; then
    echo "Using existing project: $NAME"
else
    echo "Cloning project from $REPO"
    git clone $REPO $NAME
    cd $NAME
    git checkout tags/$VERSION
    cd ..
fi

echo "rebuilding project in $NAME"
cd $NAME
mvn clean test

echo "building instrumented tests"
cd ..
cd ..
if [ -f "AGENT" ]; then
    echo "Using existing agent: $AGENT"
else
    echo "Building $AGENT"
    cd nullannoinference-agent
    mvn clean package
fi
cd ..
cp $AGENT experiments/$NAME
cd experiments
cp instrumented-poms/$NAME.xml $NAME/pom-instrumented.xml

echo "collecting nullability by running instrumented tests"
cd $NAME
# note: rat plugin may cause problems, just disable it
mvn clean test -f pom-instrumented.xml -Drat.skip=true


