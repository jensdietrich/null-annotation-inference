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
    
## Step 1 -- Fetch Projects and Collect Issues

Use the `collect-nullability-issues-*.sh` . If the projects are not found in `projects/`, they will be fetched (cloned) from github and the appropriate version will be checked out. 
Check for the program-specific scripts for repo URLs and tags (versions). 

The instrumentation is hardcoded in a special pom that can be found in `instrumented-poms/`. For instance, when you want to analyse another program of version using this setup, 
the pom needs to be modified. In general, those poms only modify the `<argLine>` argument in the Maven surefire plugin to instrument the tests. 

## Step 2 -- Static Analysis

TODO -- Integrate existing code, provide scripts

## Step 3 -- Annotate Projects

TODO -- Integrate existing code, provide scripts

