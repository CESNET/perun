#!/bin/bash

# Original update script created by (C) funny from ZCU
# 2007-08-05 handle bug in ldapsearch
# 2011-08-10 generification of script
# 2013-06-20 prevent concurrent run

PROTOCOL_VERSION='3.0.1'

function process {

	# Available error codes
	E_CANNOT_READ_LDAP=(50 'Cannot read current state of LDAP.')
	E_WHEN_UPDATING_LDAP=(51 'Error when updating LDAP.')

	# sort & diff scripts from CPAN
	LDIFDIFF="${SCRIPTS_DIR}/ldap/ldifdiff.pl"
	LDIFSORT="${SCRIPTS_DIR}/ldap/ldifsort.pl"

	# work files location
	INFILE="${WORK_DIR}/${SERVICE}_users.ldif"
	ACFILE="${WORK_DIR}/actual_users.ldif"
	INFILE_GROUPS="${WORK_DIR}/${SERVICE}_groups.ldif"
	ACFILE_GROUPS="${WORK_DIR}/actual_groups.ldif"

	# sorted work files
	SINFILE="${WORK_DIR}/sorted-new_users.ldif"
	SACFILE="${WORK_DIR}/sorted-actual_users.ldif"
	SINFILE_GROUPS="${WORK_DIR}/sorted-new_groups.ldif"
	SACFILE_GROUPS="${WORK_DIR}/sorted-actual_groups.ldif"

	# diff file used to modify ldap
	MODFILE="${WORK_DIR}/mod"
	MODFILE_GROUPS="${WORK_DIR}/mod_groups"

	BASE_DN=`head -n 1 "${WORK_DIR}/baseDN"`
	BASE_DN_USERS="ou=perun,ou=users,$BASE_DN"
	BASE_DN_GROUPS="ou=perun,ou=groups,$BASE_DN"

	# Create lock
	create_lock

	# ============ process USERS ===============

	# get actual state of ldap
	catch_error E_CANNOT_READ_LDAP ldapsearch -x -H "$LDAP_URL" -D "$LDAP_LOGIN" -w "$LDAP_PASSWORD" -LLL -b "$BASE_DN_USERS" "$LDAP_FILTER_USERS" '*' > $ACFILE

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

	# ============ process GROUPS ===============

	# get actual state of ldap
	catch_error E_CANNOT_READ_LDAP ldapsearch -x -H "$LDAP_URL" -D "$LDAP_LOGIN" -w "$LDAP_PASSWORD" -LLL -b "$BASE_DN_GROUPS" "$LDAP_FILTER_GROUPS" '*' > $ACFILE_GROUPS

	sed '/^[^ ].*/N; s/\n //g' $ACFILE_GROUPS > $ACFILE_GROUPS.2

	if test -s $ACFILE_GROUPS.2; then
		# LDAP is not empty under base DN

		# SORT LDIFs
		$LDIFSORT -k dn $ACFILE_GROUPS.2 >$SACFILE_GROUPS
		$LDIFSORT -k dn $INFILE_GROUPS >$SINFILE_GROUPS

		# DIFF LDIFs
		$LDIFDIFF -k dn $SINFILE_GROUPS $SACFILE_GROUPS | sed '/^[^ ].*/N; s/\n //g' > MODFILE_GROUPS

		# Update LDAP based on changes
		catch_error E_WHEN_UPDATING_LDAP ldapmodify -x -H "$LDAP_URL" -D "$LDAP_LOGIN" -w "$LDAP_PASSWORD" -c < MODFILE_GROUPS

	else
		# LDAP is empty under base DN

		$LDIFSORT -k dn $INFILE_GROUPS >$SINFILE_GROUPS

		# All entries are new, use ldapadd
		catch_error E_WHEN_UPDATING_LDAP ldapadd -x -H "$LDAP_URL" -D "$LDAP_LOGIN" -w "$LDAP_PASSWORD" -c < $SINFILE_GROUPS

	fi

}