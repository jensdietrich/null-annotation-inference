# Experiments

This experiments run the tool(s) on a set up Apache `commons-` libraries. Analysis is performed in steps, and for each step sh scripts are provided to run the analysis 
for a single program, or for all programs (`*-all.sh`).

## Prerequisites

1. this has been tested with Java 11 (Oracle JVM 11.0.2). Other versions may also work, but it is recommended to use Java 11. Both `mvn` (tested with version 3.8.1) and `git` are required.
2. ths sh scripts in this folder must be made executable (`chmod +x *.sh`)

## Prepare

1. to build the project, run `mvn clean package` in the project root folder (one up). This will build all components.
2. to run everything from scratch, remove or empty the following folders:
    1. `projects/` -- holds the checkout original and annotated projects
    2. `issues-collected/` -- contains the issues collected from original test runs 
    3. `issues-inferred/` -- contains refined issues
    4. `infer-results/` -- results of the infer analysis
    
## Step 1: Fetch Projects and Collect Issues

Use the `collect-nullability-issues-*.sh` . If the projects are not found in `projects/`, they will be fetched (cloned) from github and the appropriate version will be checked out. 
Check for the program-specific scripts for repo URLs and tags (versions). 

The instrumentation is hardcoded in a special pom that can be found in `instrumented-poms/`. For instance, when you want to analyse another program of version using this setup, 
the pom needs to be modified. In general, those poms only modify the `<argLine>` argument in the Maven surefire plugin to instrument the tests. 

Part of this script is an additional post-processing step called *scoping* where each issue is associated with a `scope` attribute indicating whether the issues is located in a main project class, a test classe or another class located in a dependency.

The script will collect all issues in `issues-collected/`. 

## Step 2: Refinement Static Analysis

The `refine-collected-issues-*.sh` scripts perform two static analysis which remove / add issues by performing a negatuve test analysis (leading to removal) and propagating issues to overidden / overwriting methods in order to comply with Liskov's Subtitution Principle. The refined issues will be saved in `issued-inferred/`. This folder will contain a list of negative tests (part 1 of the analyisis is based on this), and a file containing newly inferred issues `additional-issues.json`.

## Step 3: Annotate Projects

The `annotate-*.sh` scripts are used to annotate the original projects in `projects/original` and store the annotated copies in `projects/annotated`.

## Step 4: Run Infer-Eradicate

The scripts `run-infer-*.sh` are used for this purpose. We note that the infer analysis can be brittle and sensitive to the environment used. The infer builds failed with Java 11, we used the following configuration successfully:

1. infer v1.1.0-63a78acdd built from sources MacOs 12.2.1 on Apple M1 Pro
2. Java(TM) SE Runtime Environment (build 17.0.2+8-LTS-86), Java HotSpot(TM) 64-Bit Server VM (build 17.0.2+8-LTS-86, mixed mode, sharing)

Results will be stored in `infer-results/` , verify that each project-specific folder contains infer results in `report.json`. If not, the respective projects can be manually rebuild with `infer run -- mvn compile -Drat.skip=true` and the `infer-out` folder copied.

## Step 5: Creating Latex Tables with Results

A couple of Java classes was used for this purpose, see `nullannoinference-experiments-analysis` module for details. 


## Spring Experiments

Some additional experiments are located in `spring-framework`. The purpose of these experiments is to compare the captured and refined nullability issues with actual nullability annotation found in several spring projects. 

TODO add details



