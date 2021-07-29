#!/bin/bash

sed -E -i "s/^  version: [0-9]+\.[0-9]+\.[0-9]+$/  version: $1/" perun-openapi/openapi.yml
mvn versions:set -DnewVersion=$1
