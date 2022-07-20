#!/bin/bash

## script to fetch spring
## @author jens dietrich

. ./spring.env
if [ -d "$PROJECT_FOLDER" ]; then
    echo "Using existing project: $PROJECT_FOLDER"
else
    echo "Cloning project from $REPO"
    git clone $REPO $PROJECT_FOLDER
    cd $PROJECT_FOLDER
    git checkout tags/$VERSION
    echo "Project fetch complete -- project now needs to be build, see README for details"
    cd ..
fi



