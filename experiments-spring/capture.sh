#!/bin/bash

## observe nullability while exercising a program running its tests
## @author jens dietrich

. ./spring.env
if [ ! -d "$PROJECT_FOLDER" ]; then
    echo "Project folder does not exist, fetch projects first: $PROJECT_FOLDER"
    exit 1;
fi

if [ ! -d "$INSTRUMENTED_GRADLE_FOLDER" ]; then
    echo "Folder with instrumented gradle scripts does not exist, create this first: $INSTRUMENTED_GRADLE_FOLDER"
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

for module in "${MODULES[@]}" ;do
  echo "Copying instrumented gradle build script: " $INSTRUMENTED_GRADLE_FOLDER/$module.gradle
  cp $INSTRUMENTED_GRADLE_FOLDER/$module.gradle $PROJECT_FOLDER_INSTRUMENTED/$module
  echo "removing old instrumention results"
  rm $PROJECT_FOLDER_INSTRUMENTED/$module/null-*.json
  echo "Copying agent(s) into modules"
  cp $AGENT $PROJECT_FOLDER_INSTRUMENTED/$module
  cp $AGENT2 $PROJECT_FOLDER_INSTRUMENTED/$module
done

# run tests with instrumentation
cd $PROJECT_FOLDER_INSTRUMENTED_ROOT/$NAME
start=`date +%s`
./gradlew clean build --continue --no-build-cache
end=`date +%s`
runtime=$((end-start))
echo "$runtime" > $RESULT_FOLDER_OBSERVED/capture-runtime-spring.log


# merge issues collected into a single file, copy in result folder
for module in "${MODULES[@]}" ;do
  echo "Merging results collected in: " $PROJECT_FOLDER_INSTRUMENTED/${module}
  java -Xmx20g -jar $MERGER -i $PROJECT_FOLDER_INSTRUMENTED/${module} -o $RESULT_FOLDER_OBSERVED/$NULLABLE-${module}.json
done



