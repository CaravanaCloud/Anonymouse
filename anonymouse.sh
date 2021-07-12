#!/bin/sh

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )"
export MAVEN_OPTS="-Xms10G -Xmx25G"
mvn -f $SCRIPT_DIR/pom.xml -Dquarkus-profile=prod clean compile exec:java
