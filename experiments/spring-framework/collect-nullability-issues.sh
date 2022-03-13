#!/bin/bash

## script to fetch the spring famework, and collect issues in selected modules
## @author jens dietrich

REPO=git@github.com:spring-projects/spring-framework.git
VERSION=v5.3.9
AGENT="nullannoinference-agent/target/nullannoinference-agent.jar"
AGENT2="nullannoinference-agent2/target/nullannoinference-agent2.jar"
SCOPER="nullannoinference-scoper/target/nullannoinference-scoper.jar"
SCOPERL="nullannoinference-scoper.jar"
ROOT="$(pwd)"
PROJECT_FOLDER=$ROOT/spring-framework
ISSUES_COLLECTED=$ROOT/issues-collected
# modules=('spring-core' 'spring-beans')
modules=('spring-core' 'spring-beans' 'spring-orm' 'spring-oxm' 'spring-context' 'spring-web' 'spring-webmvc')


if [ -d "$PROJECT_FOLDER" ]; then
    echo "Using existing project: $PROJECT_FOLDER"
else
    echo "Cloning project from $REPO"
    git clone $REPO $PROJECT_FOLDER
    cd $PROJECT_FOLDER
    git checkout tags/$VERSION
    ./gradlew build
    cd ..
fi

echo "preparing projects for instrumented test runs"
cd $ROOT
cd ..
cd ..
if [ ! -f "$AGENT" ]; then
    echo "Agent not found - must build entire project first: $AGENT"
    exit 1
fi
for module in "${modules[@]}" ;do
    mkdir -p $PROJECT_FOLDER/$module
    cp $AGENT $PROJECT_FOLDER/$module
done

if [ ! -f "$AGENT2" ]; then
    echo "Agent not found - must build entire project first: $AGENT2"
    exit 1
fi
for module in "${modules[@]}" ;do
    cp $AGENT2 $PROJECT_FOLDER/$module
done

if [ ! -f "$SCOPER" ]; then
    echo "Scoper not found - must build entire project first: $SCOPER"
    exit 1
fi
for module in "${modules[@]}" ;do
    cp $SCOPER $PROJECT_FOLDER/$module
done

cd $ROOT
for module in "${modules[@]}" ;do
    cp instrumented-gradle/$module.gradle $PROJECT_FOLDER/$module
done

#echo "run instrumented tests"
cd $PROJECT_FOLDER
for module in "${modules[@]}" ;do
    ./gradlew $module:build
    mkdir -p $ISSUES_COLLECTED/$module
    rm $ISSUES_COLLECTED/$module/*.json
    mv $module/null*.json $ISSUES_COLLECTED/$module
done
cd $ROOT

#echo "adding scope to collected issues"
cd $ROOT
for module in "${modules[@]}" ;do
    #echo "adding scope to collected issues for $module"
    java -jar $PROJECT_FOLDER/$module/$SCOPERL -i $ISSUES_COLLECTED/$module -p $PROJECT_FOLDER/$module -t gradle_multilang
done

