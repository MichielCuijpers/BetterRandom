#!/bin/bash
sudo renice -10 $$
cd betterrandom
set -eo pipefail # TODO: Find a pipefail method that's not Bash-specific
mvn -B -Ppit clean test-compile org.pitest:pitest-maven:mutationCoverage 2>&1 | tee >(grep -qv "BUILD FAILURE")
