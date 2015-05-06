#!/bin/bash

# Original update script created by (C) funny from ZCU
# 2007-08-05 handle bug in ldapsearch
# 2011-08-10 generification of script
# 2013-06-20 prevent concurrent run

PROTOCOL_VERSION='3.0.0'

function process {

	E_CANNOT_READ_LDAP=(50 'Cannot read current state of LDAP.')
	E_WHEN_UPDATING_LDAP=(51 'Error when updating LDAP.')

	# sort & diff scripts from CPAN
	LDIFDIFF="${SCRIPTS_DIR}/ldap/ldifdiff.pl"
	LDIFSORT="${SCRIPTS_DIR}/ldap/ldifsort.pl"

	# work files location
	INFILE="${WORK_DIR}/ldap.ldif"
	ACFILE="${WORK_DIR}/actual.ldif"

	# sorted work files
	SINFILE="${WORK_DIR}/sorted-new.ldif"
	SACFILE="${WORK_DIR}/sorted-actual.ldif"

	# diff file used to modify ldap
	MODFILE="${WORK_DIR}/mod"

	BASE_DN=`head -n 1 "${WORK_DIR}/baseDN"`

	# list only entries that service should manage
	LDAP_FILTER="(&(|(objectclass=organizationalunit)(objectclass=perunEduroamUser)(objectclass=dcObject))(!(ou=authz))(!(ou=users))(!(ou=policies))(!(ou=groups)))"

	# Create lock
	create_lock

	# get actual state of ldap
	catch_error E_CANNOT_READ_LDAP ldapsearch -x -H "$LDAP_URL" -D "$LDAP_LOGIN" -w "$LDAP_PASSWORD" -LLL -b "$BASE_DN" "$LDAP_FILTER" '*' > $ACFILE

	sed '/^[^ ].*/N; s/\n //g' $ACFILE > $ACFILE.2

	if test -s $ACFILE.2; then
	# LDAP is not empty under base DN

		# SORT LDIFs
		$LDIFSORT -k dn $ACFILE.2 >$SACFILE
		$LDIFSORT -k dn $INFILE >$SINFILE

		# DIFF LDIFs
		$LDIFDIFF -k dn $SINFILE $SACFILE | sed '/^[^ ].*/N; s/\n //g' > MODFILE

		# Update LDAP based on changes
		catch_error E_WHEN_UPDATING_LDAP ldapmodify -x -H "$LDAP_URL" -D "$LDAP_LOGIN" -w "$LDAP_PASSWORD" -c < MODFILE

	else
	# LDAP is empty under base DN

		$LDIFSORT -k dn $INFILE >$SINFILE

		# All entries are new, use ldapadd
		catch_error E_WHEN_UPDATING_LDAP ldapadd -x -H "$LDAP_URL" -D "$LDAP_LOGIN" -w "$LDAP_PASSWORD" -c < $SINFILE

	fi

}