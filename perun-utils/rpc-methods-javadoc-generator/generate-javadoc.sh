#!/bin/bash

########################################################
#                                                      #
# Generates JavaDoc from RPC methods enums.            #
# Paths are specified below.                           #
#                                                      #
########################################################

test -z $PERUN_RPC_SOURCE_DIR && PERUN_RPC_SOURCE_DIR=./methods
test -z $PERUN_API_JAVADOC_URL && echo URL targeting to JavaDoc of Perun API must be specified >&2 && exit
test -z $PEUNR_RPC_JAVADOC_DESTINATION && PERUN_RPC_JAVADOC_DESTINATION=./javadoc

# JavaDoc template
JAVADOC_TITLE="Perun RPC documentation"
JAVADOC_FOOTER="<strong>Links: <a target=\"_top\" href=\"http://meta.cesnet.cz/wiki/Perun\">Perun wiki</a></strong>"

# If empty, it won't be added to the code
#RPC_URL="https://perun.metacentrum.cz/perun-rpc-fed/jsonp/"
RPC_URL=""

# Fictional Java package
PACKAGE=rpc

# Temp
TMP_DIR=`mktemp -d`

# Preparing tmp, creates dir with the specified package
mkdir $TMP_DIR/$PACKAGE

# Generating interfaces
for f in $PERUN_RPC_SOURCE_DIR/*
do
    echo "Found file $f"
    basef=$(basename $f)
    basef=`echo $basef | sed -r 's/([a-zA-Z]+)\Method.java/\1/g'`.java
    php RpcMethodsParser.php -s $f -d $TMP_DIR/$PACKAGE/$basef -r $RPC_URL
done

# Generating JavaDoc
echo "Genarating JavaDoc ..."
javadoc -quiet -link $PERUN_API_JAVADOC_URL -d $PERUN_RPC_JAVADOC_DESTINATION -sourcepath $TMP_DIR -package $PACKAGE -noqualifier "java.lang:java.util" -windowtitle "${JAVADOC_TITLE}" -bottom "${JAVADOC_FOOTER}"
echo "JavaDoc generated, the \"cannot find symbol\" warnings can be ignored."

# Delete TMP_DIR
rm -r $TMP_DIR
echo "Temporary dir deleted."
