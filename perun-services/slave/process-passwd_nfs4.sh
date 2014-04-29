#!/bin/bash

PROTOCOL_VERSION='3.0.0'


function process {
	PASSWD_DST_FILE="/etc/passwd-nfs4"

	### Status codes
	I_P_CHANGED=(0 "${PASSWD_DST_FILE} updated")
	I_P_NOT_CHANGED=(0 "${PASSWD_DST_FILE} has not changed")

	E_PASSWD_FILTER=(50 'Error in /etc/passwd filter')
	E_PASSWD_WRONG_MIN_UID=(51 'Invalid min_uid parameter')
	E_PASSWD_WRONG_MAX_UID=(52 'Invalid max_uid parameter')
	E_PASSWD_MERGE=(53 'Error during passwd file merge')
	E_PASSWD_DUPLICATES=(54 'Lognames in passwd are not uniq: ${DUPLICATE_LOGNAMES}')
	E_PASSWD_NO_ROOT=(55 'Missing user "root"')

	PASSWD_FROM_PERUN="${WORK_DIR}/passwd_nfs4"

	OLD_PASSWD="$PASSWD_DST_FILE"
	#NEW_PASSWD="/etc/passwd.new" # file must be on same mountpoint for atomic switch
	NEW_PASSWD="${WORK_DIR}/passwd.new"
	MIN_PERUN_UID=`head -n 1 "${WORK_DIR}/min_uid"`
	MAX_PERUN_UID=`head -n 1 "${WORK_DIR}/max_uid"`
	[ "${MIN_PERUN_UID}" -gt 0 ] || log_msg E_PASSWD_WRONG_MIN_UID
	[ "${MAX_PERUN_UID}" -gt 0 ] || log_msg E_PASSWD_WRONG_MAX_UID

	create_lock

	#if OLD_PASSWD doesn't exists, create it
	[ -e "$OLD_PASSWD" ] || touch "$OLD_PASSWD"

	# grep lines from /etc/passwd which uid is NOT between min a max uid
	catch_error E_PASSWD_FILTER perl -F':' -ane "print if(\$F[2] < $MIN_PERUN_UID || \$F[2] > $MAX_PERUN_UID)" "${OLD_PASSWD}" >"${NEW_PASSWD}"

	# merge received passwd into striped old one
	catch_error E_PASSWD_MERGE cat "${PASSWD_FROM_PERUN}" >>"${NEW_PASSWD}"

	# check for duplicate lognames in passwd
	DUPLICATE_LOGNAMES=`cut -d: -f1 "${NEW_PASSWD}" | sort | uniq -d`
	[ "x${DUPLICATE_LOGNAMES}" == 'x' ] || log_msg E_PASSWD_DUPLICATES

	# move only in case there are any changes
	diff_mv "${NEW_PASSWD}" "${PASSWD_DST_FILE}" \
		&& log_msg I_P_CHANGED \
		|| log_msg I_P_NOT_CHANGED
}
