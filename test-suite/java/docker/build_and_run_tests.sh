#!/bin/sh

# Build and run djinni tests. Intended to be run
# from inside a Docker container

set -e
set -x

cd /opt/djinni/test-suite/java
ant -v clean compile test 
ant -v jar run-jar
ant clean

