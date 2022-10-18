#!/bin/bash

## re-annotate projects where @Nullable annotations have been removed
## annotations can be removed from spring projects by renaming @Nullable (in org.springframework.lang) using the IntelliJ refactoring browser
## @author jens dietrich


# java -jar analyses/annotator.jar -p projects/null-removed/spring-framework/spring-core/ -o projects/reannotated/spring-framework/spring-core/ -i results/extracted/nullable-spring-core.json -t gradle  -a spring


. ./spring.env
if [ ! -d "$PROJECT_FOLDER" ]; then
    echo "Project folder does not exist, fetch projects first: $PROJECT_FOLDER"
    exit 1;
fi

if [ ! -d "$PROJECT_FOLDER" ]; then
    echo "Project folder does not exist, fetch projects first: $PROJECT_FOLDER"
    exit 1;
fi

if [ ! -d "$PROJECT_FOLDER_EXISTING_ANNOTATION_REMOVED" ]; then
  echo "Folder containing unannotated projects does not exist: " $PROJECT_FOLDER_EXISTING_ANNOTATION_REMOVED
  echo "Folder must be created by cloning " $PROJECT_FOLDER_ORIGINAL " into " $PROJECT_FOLDER_EXISTING_ANNOTATION_REMOVED " and removing @Nullable annotation"
  echo "This can be done by renaming org.springframework.lang.Nullable using IDE (tested with IntelliJ) refactoring, e.g to Foo"
  exit 1;
fi

if [ ! -d "$RESULT_FOLDER_EXTRACTED" ]; then
  echo "Folder with extracted issues does not exit, run extraction first to create: " $RESULT_FOLDER_EXTRACTED
  exit 1;
fi

## copy original projects into re-annotated folder, annotation will then override certain files
echo "copying original projects from $PROJECT_FOLDER into $PROJECT_FOLDER_REANNOTATED (annotation will override some files)"
cp -r $PROJECT_FOLDER/* $PROJECT_FOLDER_REANNOTATED

for module in "${MODULES[@]}" ;do
    echo "re-annotating: $module"
    # java -jar $EXTRACTOR -p ${PROJECT_FOLDER}/${module} -t gradle_multilang -o $RESULT_FOLDER_EXTRACTED/$NULLABLE-${module}.json
    java -jar $ANNOTATOR -p $PROJECT_FOLDER_EXISTING_ANNOTATION_REMOVED/$module -o $PROJECT_FOLDER_REANNOTATED/$module -i $RESULT_FOLDER_EXTRACTED/nullable-$module.json -t gradle  -a spring
done





