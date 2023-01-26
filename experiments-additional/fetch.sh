#!/bin/bash

## script to fetch additional data sets
## tested with Java 11 (guava does not build with Java 17) -- https://github.com/google/guava/issues/5801 
## @author jens dietrich

. ./additional.env

length=${#REPOS[@]}
for (( i=0; i<${length}; i++ ));
do
    cd ${ROOT}
    PROJECT_FOLDER=${PROJECT_FOLDERS}/${NAMES[$i]}
    if [ -d ${PROJECT_FOLDER} ]; then
        echo "Using existing project: ${PROJECT_FOLDER}"
    else
      echo "cloning ${REPOS[$i]} into ${PROJECT_FOLDER}"
      git clone ${REPOS[$i]} ${PROJECT_FOLDER}
      cd ${PROJECT_FOLDER}
      
      echo "switching to version tags/${VERSIONS[$i]}"
      git checkout tags/${VERSIONS[$i]}
    fi
done
