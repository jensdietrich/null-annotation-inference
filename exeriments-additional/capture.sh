#!/bin/bash

## observe nullability while exercising a program running its tests
## @author jens dietrich

. ./additional.env
if [ ! -d "$PROJECT_FOLDER" ]; then
    echo "Project folder does not exist, fetch projects first: $PROJECT_FOLDER"
    exit 1;
fi

if [ ! -d "$INSTRUMENTED_BUILD_SCRIPT_FOLDER" ]; then
    echo "Folder with instrumented build scripts does not exist, create this first: $INSTRUMENTED_BUILD_SCRIPT_FOLDER"
    exit 1;
fi

if [ ! -d "$RESULT_FOLDER_OBSERVED" ]; then
  echo "Result folder does not exit, creating folder: " $RESULT_FOLDER_OBSERVED
  mkdir -p $RESULT_FOLDER_OBSERVED
else
  echo "Issues observed will be saved in " $RESULT_FOLDER_OBSERVED
fi

# copy instrumented gradle build files into projects
if [ ! -d "$PROJECT_FOLDER_INSTRUMENTED_ROOT" ]; then
  echo "Instrumented project does not exit, creating folder: " $PROJECT_FOLDER_INSTRUMENTED_ROOT
  mkdir -p $PROJECT_FOLDER_INSTRUMENTED_ROOT
  cp -r $PROJECT_FOLDER $PROJECT_FOLDER_INSTRUMENTED_ROOT
fi


length=${#REPOS[@]}
for (( i=0; i<${length}; i++ ));
do
  echo "Copying instrumented build script: ${INSTRUMENTED_BUILD_SCRIPT_FOLDER}/${INSTRUMENTED_BUILD_SCRIPTS[$i]}"
  echo " to ${PROJECT_FOLDER_INSTRUMENTED}${INSTRUMENTED_BUILD_SCRIPTS[$i]}"
  cp ${INSTRUMENTED_BUILD_SCRIPT_FOLDER}/${INSTRUMENTED_BUILD_SCRIPTS[$i]} $PROJECT_FOLDER_INSTRUMENTED${INSTRUMENTED_BUILD_SCRIPTS[$i]}
  
  echo "Copying agent(s) into ${INSTRUMENTED_BUILD_SCRIPTS_DIR[$i]}"
  cp $AGENT $PROJECT_FOLDER_INSTRUMENTED${INSTRUMENTED_BUILD_SCRIPTS_DIR[$i]}
  cp $AGENT2 $PROJECT_FOLDER_INSTRUMENTED${INSTRUMENTED_BUILD_SCRIPTS_DIR[$i]}
  
  cd $PROJECT_FOLDER_INSTRUMENTED${INSTRUMENTED_BUILD_SCRIPTS_DIR[$i]}
  echo "cleaning old issues"
  rm null-*.json
  echo "Running instrumented tests"
  start=`date +%s`
  ${BUILD_CMDS[$i]}
  end=`date +%s`
  runtime=$((end-start))
  echo "$runtime" > capture-runtime-${NAMES[$i]}.log
  
  cd ${ROOT}
  echo "Merging results collected into $PROJECT_FOLDER_INSTRUMENTED${INSTRUMENTED_BUILD_SCRIPTS_DIR[$i]}/$NULLABLE-${NAMES[$i]}.json"
  java -Xmx20g -jar $MERGER -i $PROJECT_FOLDER_INSTRUMENTED${INSTRUMENTED_BUILD_SCRIPTS_DIR[$i]} -o $PROJECT_FOLDER_INSTRUMENTED${INSTRUMENTED_BUILD_SCRIPTS_DIR[$i]}/$NULLABLE-${NAMES[$i]}.json
done



#for module in "${MODULES[@]}" ;do
#  echo "Copying instrumented gradle build script: " $INSTRUMENTED_GRADLE_FOLDER/$module.gradle
#  cp $INSTRUMENTED_GRADLE_FOLDER/$module.gradle $PROJECT_FOLDER_INSTRUMENTED/$module
#  echo "removing old instrumention results"
#  rm $PROJECT_FOLDER_INSTRUMENTED/$module/null-*.json
#  echo "Copying agent(s) into modules"
#  cp $AGENT $PROJECT_FOLDER_INSTRUMENTED/$module
#  cp $AGENT2 $PROJECT_FOLDER_INSTRUMENTED/$module
#done
#
## run tests with instrumentation
#cd $PROJECT_FOLDER_INSTRUMENTED_ROOT/$NAME
#./gradlew clean build --continue --no-build-cache
#
## merge issues collected into a single file, copy in result folder
#for module in "${MODULES[@]}" ;do
#  echo "Merging results collected in: " $PROJECT_FOLDER_INSTRUMENTED/${module}
#  java -Xmx20g -jar $MERGER -i $PROJECT_FOLDER_INSTRUMENTED/${module} -o $RESULT_FOLDER_OBSERVED/$NULLABLE-${module}.json
#done



