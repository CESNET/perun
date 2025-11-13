#!/bin/bash

#apt install php-cli composer phpunit php-xml php-mbstring php-curl

GENERATOR_VERSION=5.1.0
if [ ! -f  "openapi-generator-cli-$GENERATOR_VERSION.jar" ] ; then
  wget https://repo1.maven.org/maven2/org/openapitools/openapi-generator-cli/$GENERATOR_VERSION/openapi-generator-cli-$GENERATOR_VERSION.jar
fi

rm -rf composer.json composer.lock git_push.sh lib phpunit.xml.dist vendor
# see https://openapi-generator.tech/docs/usage#generate
# and https://openapi-generator.tech/docs/generators/php
java \
 -DapiDocs=false \
 -DapiTests=false \
 -DmodelDocs=false \
 -DmodelTests=false \
 -jar openapi-generator-cli-$GENERATOR_VERSION.jar generate \
 --generator-name php \
 --input-spec ../perun-openapi/openapi.yml \
 --api-package Api \
 --model-package Model \
 --package-name Perun \
 --additional-properties=generateSourceCodeOnly=true,variableNamingConvention=camelCase,legacyDiscriminatorBehavior=false

composer install
