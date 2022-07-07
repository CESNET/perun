#!/bin/bash

GENERATOR_VERSION=6.0.1
if [ ! -f  "openapi-generator-cli-$GENERATOR_VERSION.jar" ] ; then
  wget https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/$GENERATOR_VERSION/openapi-generator-cli-$GENERATOR_VERSION.jar
fi

rm -rf perun_openapi
# see https://openapi-generator.tech/docs/usage#generate
# and https://openapi-generator.tech/docs/generators/python
java \
 -DapiDocs=false \
 -DapiTests=false \
 -DmodelDocs=false \
 -DmodelTests=false \
 -jar openapi-generator-cli-$GENERATOR_VERSION.jar generate \
 --generator-name python \
 --input-spec ../perun-openapi/openapi.yml \
 --model-package model \
 --additional-properties=generateSourceCodeOnly=true,packageName=perun_openapi
