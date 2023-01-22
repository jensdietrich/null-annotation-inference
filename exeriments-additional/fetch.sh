#!/bin/bash

## script to fetch additional data sets
## tested with Java 11 (guava does not build with Java 17) -- https://github.com/google/guava/issues/5801 
## @author jens dietrich

. ./additional.env


length=${#REPOS[@]}
for (( i=0; i<${length}; i++ ));
do
    cd ${ROOT}
    if [ -d ${PROJECT_FOLDERS[$i]} ]; then
        echo "Using existing project: ${PROJECT_FOLDERS[$i]}"
    else
      echo "cloning ${REPOS[$i]} into ${PROJECT_FOLDERS[$i]}"
      git clone ${REPOS[$i]} ${PROJECT_FOLDERS[$i]}
      cd ${PROJECT_FOLDERS[$i]}
      
      echo "switching to version tags/${VERSIONS[$i]}"
      git checkout tags/${VERSIONS[$i]}
    fi
done



