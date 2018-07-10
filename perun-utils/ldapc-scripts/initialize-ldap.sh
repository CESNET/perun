#!/bin/sh

PERUN_LDAP_INITIALIZATOR=/home/perun/perun-ldapc/perun-ldapc-initializer.jar
LDIF_DIFF_LIBRARY=/home/perun/perun-ldapc/ldifdiff.pl
LDIF_SORT_LIBRARY=/home/perun/perun-ldapc/ldifsort.pl
LDIF_DIFF_SORT_LIBRARY=/home/perun/perun-ldapc/ldifdiff-sort.pl
LDAP_PROPERTIES_FILE=/etc/perun/perun-ldapc.properties

LDAP_INIT_FILE=/tmp/init-perun.ldif
LDAP_CONTENT_FILE=/tmp/ldap-content.ldif
LDAP_CONTENT_FILE_SORTED=/tmp/ldap-content-sorted.ldif
PERUN_PRE_CONTENT_FILE=/tmp/perun-pre-content.ldif
PERUN_CONTENT_FILE=/tmp/perun-content.ldif
PERUN_CONTENT_FILE_SORTED=/tmp/perun-content-sorted.ldif
LDAP_PERUN_DIFF=/tmp/ldap-perun-diff.ldif
LDAP_PERUN_DIFF_SORTED=/tmp/ldap-perun-diff-sorted.ldif
AUDITER_INFO=/tmp/auditer_info.tmp
TEST_OUTPUT=/tmp/TEST-ldap-perun-diff-sorted.ldif

#Check if ldap properties file exists and it is readable
if [ ! -r "$LDAP_PROPERTIES_FILE" ]; then
	echo "Can't find ldap properties file!" 1>&2
	exit 1
fi

LDAP_BASE=`grep '^ldap\.base=' "$LDAP_PROPERTIES_FILE" | sed -e 's/^ldap\.base=//'`
LDAP_DC=`echo $LDAP_BASE | sed -e 's/^dc=//' | sed -e 's/,.*$//'`
LDAP_ADMIN=`grep '^ldap\.userDn=' "$LDAP_PROPERTIES_FILE" | sed -e 's/^ldap\.userDn=//'`
LDAP_ADMIN_PASSWORD=`grep '^ldap\.password=' "$LDAP_PROPERTIES_FILE" | sed -e 's/^ldap\.password=//'`
LDAP_PORT="389"

#Check emptiness of variables
if [ -z "$LDAP_BASE" -o -z "$LDAP_ADMIN" -o -z "$LDAP_ADMIN_PASSWORD" ]; then
	echo "Can't read one of the mandatory variables for LDAP inicialization!" 1>&2
	exit 2
fi


#Option test is only for generating diff between perun and LDAP without any action
while getopts "tf" opt; do
		case "$opt" in
		t)
				TEST=1
				echo "Initialization executed in the test mode - without any changes!"
				;;
		f)
				USE_EXISTING_LDIF=1
				echo "Initialization will read data from $LDAP_PERUN_DIFF_SORTED file instead of from Perun!"
				;;
		esac
done

if [ ! ${TEST} ]; then
	#If LDAPc is running, inform us and exit without processing
	LDAPC_PID_FILE=/var/run/perun/perun-ldapc.pid
	if [ -f "$LDAPC_PID_FILE" ]; then
		echo "LDAPc is still running, please stop it first before start initializing data!" 1>&2
		exit 3
	fi
fi

if [ ! ${USE_EXISTING_LDIF} ]; then 
	JAVA_OPTIONS=" -Dspring.profiles.default=production "
	INITIALIZER_OPTIONS=" -f $PERUN_PRE_CONTENT_FILE "
	if [ ! ${TEST} ]; then
		INITIALIZER_OPTIONS=" $INITIALIZER_OPTIONS -c "
	fi

	#Check if ldif diff library exists and is executable
	if [ ! -x "$LDIF_DIFF_LIBRARY" -o ! -x "$LDIF_DIFF_SORT_LIBRARY" -o ! -x "$LDIF_SORT_LIBRARY" ]; then
		echo "Can't find all expected libraries: '$LDIF_DIFF_LIBRARY', '$LDIF_DIFF_SORT_LIBRARY', '$LDIF_SORT_LIBRARY' or there are not executable." 1>&2
		exit 4
	fi

	# Set removing temp files on exit
	trap 'rm -r -f "$LDAP_CONTENT_FILE" "$PERUN_PRE_CONTENT_FILE" "$PERUN_CONTENT_FILE" "$LDAP_PERUN_DIFF" "$LDAP_INIT_FILE" "$LDAP_CONTENT_FILE_SORTED" "$PERUN_CONTENT_FILE_SORTED"' EXIT

	#create init.ldif file
	cat > $LDAP_INIT_FILE <<EOF

dn: ou=People,$LDAP_BASE
ou: People
objectClass: organizationalUnit

EOF

	if [ ! -r "$LDAP_INIT_FILE" ]; then
		echo "Can't find ldap init file!" 1>&2
		exit 5
	fi

	#we don't want to use kerberos
	export KRB5CCNAME="/dev/null"

	# Generate content ldif file from Perun
	echo "Generating LDIF content file from Perun..."
	java $JAVA_OPTIONS -jar $PERUN_LDAP_INITIALIZATOR $INITIALIZER_OPTIONS 2>$AUDITER_INFO
	if [ $? -ne 0 ]; then
		echo "Generating content file from Perun failed. More info in $AUDITER_INFO" 1>&2
		exit 6
	fi

	# Test if pre-content ldif file exists
	if [ ! -r "$PERUN_PRE_CONTENT_FILE" ]; then
		echo "Expected file $PERUN_PRE_CONTENT_FILE not exists!" 1>&2
		exit 7
	fi

	# Add init part to the perun content file (it is also in ldap content file)
	cat "$LDAP_INIT_FILE" "$PERUN_PRE_CONTENT_FILE" > $PERUN_CONTENT_FILE

	# Test if resulted content ldif file exists
	if [ ! -r "$PERUN_CONTENT_FILE" ]; then
		echo "Expected file $PERUN_CONTENT_FILE not exists!" 1>&2
		exit 8
	fi

	#Create content ldif file from LDAP
	echo "Generating LDIF content file from LDAP..."
	ldapsearch -LLL -x -h localhost -p $LDAP_PORT -b "$LDAP_BASE" -D "$LDAP_ADMIN" -w "$LDAP_ADMIN_PASSWORD" "(!(dc=$LDAP_DC))" > $LDAP_CONTENT_FILE

	#Create diff between actual state in LDAP and Perun
	echo "Content files will be sorted now..."
	$LDIF_SORT_LIBRARY -k dn $PERUN_CONTENT_FILE > $PERUN_CONTENT_FILE_SORTED
	$LDIF_SORT_LIBRARY -k dn $LDAP_CONTENT_FILE > $LDAP_CONTENT_FILE_SORTED

	echo "LDIF diff executed on content files..."
	$LDIF_DIFF_LIBRARY -k dn $PERUN_CONTENT_FILE_SORTED $LDAP_CONTENT_FILE_SORTED > $LDAP_PERUN_DIFF

	if [ $? -ne 0 ]; then
		echo "Generating LDIF-diff from PERUN and LDAP content files ends with error!" 1>&2
		exit 9
	fi

	# Test if resulted content ldif file exists
	if [ ! -r "$LDAP_PERUN_DIFF" ]; then
		echo "Expected file $LDAP_PERUN_DIFF not exists!" 1>&2
		exit 10
	fi

	# Test if resulted content ldif file is not empty
	if [ ! -s "$LDAP_PERUN_DIFF" ]; then
		echo "There is no difference between content file from LDAP and from Perun."
		exit 0
	fi

	#Sort not empty ldif diff
	echo "LDIF diff sort started..."
	$LDIF_DIFF_SORT_LIBRARY $LDAP_PERUN_DIFF > $LDAP_PERUN_DIFF_SORTED

fi

# Test if resulted content ldif file exists
if [ ! -r "$LDAP_PERUN_DIFF_SORTED" ]; then
	echo "Expected file $LDAP_PERUN_DIFF_SORTED not exists!" 1>&2
	exit 11
fi

#If this is just test, copy output file and exit with return code 0
if [ $TEST ]; then
	cp $LDAP_PERUN_DIFF_SORTED $TEST_OUTPUT
	if [ $? -ne 0 ]; then
		echo "Can't copy file $LDAP_PERUN_DIFF_SORTED to $TEST_OUTPUT!" 1>&2
	fi
	echo "This was only test - nothing was changed and output should be in file $TEST_OUTPUT."
	exit 0
fi

echo "STARTING ON TIME: " >> $AUDITER_INFO
date >> $AUDITER_INFO

#insert all data from ldif diff to ldap
echo "Modifying data in LDAP by ldif diff file..."
cat "$LDAP_PERUN_DIFF_SORTED" | ldapmodify -x -h localhost -D "$LDAP_ADMIN" -w "$LDAP_ADMIN_PASSWORD"
if [ $? -ne 0 ]; then
	echo "Data import failded!" 1>&2
	exit 12
fi

echo "FINISHED ON TIME: " >> $AUDITER_INFO
date >> $AUDITER_INFO

echo "LDAP was modified ok. (Information about messages are in file ${AUDITER_INFO})"

