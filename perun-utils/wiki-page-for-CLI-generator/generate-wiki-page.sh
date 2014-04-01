#!/bin/bash

################################################################
# Generates wiki page from CLI tools.                          #
# Output is printed to stdout.                                 #
#                                                              #
#    generate_wiki_page DIR                                    #
#                                                              #
#    DIR     directory where CLI tools are located             #
#                                                              #
################################################################

CLI_DIR=$1;
[ -d $CLI_DIR ] || exit 1;

#set perl environment
export PERL5LIB=$CLI_DIR;

echo '=== CLI ==='
echo "* ''' tools for listing '''"
for FILE in `ls $CLI_DIR/list*`; do
	echo "** [[#$FILE|$FILE]]"
done

echo "* ''' tools for creating, updating a validation of entities '''"
for FILE in `ls $CLI_DIR/{create,update,validate}*`; do
	echo "** [[#$FILE|$FILE]]"
done

echo "* ''' tools for adding '''"
for FILE in `ls $CLI_DIR/add*`; do
	echo "** [[#$FILE|$FILE]]"
done

echo "* ''' tools for deleting '''"
for FILE in `ls $CLI_DIR/delete*`; do
	echo "** [[#$FILE|$FILE]]"
done

echo "* ''' tools for assigning setting and removing '''"
for FILE in `ls $CLI_DIR/{assign,set,remove}*`; do
	echo "** [[#$FILE|$FILE]]"
done

echo "* ''' tools for propagations '''"
for FILE in `ls {propagate}*`; do
	echo "** [[#$FILE|$FILE]]"
done

echo -e "\n\n\n"

#generate helps
for FILE in `ls $CLI_DIR/{add,assign,create,delete,list,remove,set,update,validate,propagate}*`
do echo -n "==== $FILE ===="
	[ -x $FILE ] && perl $FILE --help | ./wiki-formater.pl
	echo -e "\n\n"
done
