#!/bin/bash

## refine (filter + infer) nullability issues in all programs
## @author jens dietrich

./refine-collected-issues-commons-codec.sh
./refine-collected-issues-commons-collections.sh
./refine-collected-issues-commons-configuration.sh
./refine-collected-issues-commons-csv.sh
./refine-collected-issues-commons-io.sh
./refine-collected-issues-commons-lang.sh
./refine-collected-issues-commons-text.sh

