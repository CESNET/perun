#!/bin/bash

sed -E -i "s/^  version: [0-9]+\.[0-9]+\.[0-9]+$/  version: $1/" perun-openapi/openapi.yml
sed -E -i "s/[0-9]+\.[0-9]+\.[0-9]+/$1/" perun-cli/Perun/Agent.pm
mvn versions:set -DnewVersion=$1
