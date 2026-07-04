#!/bin/bash
set -e
MODULE=$1
VERSION=$2
export VERSION=$VERSION
./gradlew :${MODULE}:publish
