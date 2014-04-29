#!/bin/bash

PROTOCOL_VERSION='3.0.0'

function process {
	PASSWD_DST_FILE="/etc/passwd"
	SHADOW_DST_FILE="/etc/shadow"

	### Status codes
	I_P_CHANGED=(0 "${PASSWD_DST_FILE} updated")
	I_P_NOT_CHANGED=(0 "${PASSWD_DST_FILE} has not changed")
	I_S_CHANGED=(0 "${SHADOW_DST_FILE} updated")
	I_S_NOT_CHANGED=(0 "${SHADOW_DST_FILE} has not changed")

	E_PASSWD_FILTER=(50 'Error in /etc/passwd filter')
	E_PASSWD_WRONG_MIN_UID=(51 'Invalid min_uid parameter')
	E_PASSWD_WRONG_MAX_UID=(52 'Invalid max_uid parameter')
	E_PASSWD_MERGE=(53 'Error during passwd file merge')
	E_PASSWD_DUPLICATES=(54 'Lognames in passwd are not uniq: ${DUPLICATE_LOGNAMES}')
	E_PASSWD_NO_ROOT=(55 'Missing user "root"')
	E_PASSWD_GET_USERS=(56 'Cannot get users from passwd')
	E_PASSWD_NO_ROOT=(57 'User root missing in new passwd file')

	E_SHADOW_FILTER=(58 'Error in /etc/shadow filter')
	E_SHADOW_NO_ROOT=(59 'User root missing in new shadow file')
	E_PASSWD_MERGE=(60 'Error during shadow file merge')
	E_SHADOW_DUPLICATES=(61 'Lognames in shadow are not uniq: ${DUPLICATE_LOGNAMES}')

	PASSWD_FROM_PERUN="${WORK_DIR}/passwd"
	SHADOW_FROM_PERUN="${WORK_DIR}/shadow"

	OLD_PASSWD='/etc/passwd'
	OLD_SHADOW='/etc/shadow'
	#NEW_PASSWD="/etc/passwd.new" # file must be on same mountpoint for atomic switch
	NEW_PASSWD="${WORK_DIR}/passwd.new"
	NEW_SHADOW="${WORK_DIR}/shadow.new"
	USERS_REGEXP_FILE="${WORK_DIR}/users.regexp"
	MIN_PERUN_UID=`head -n 1 "${WORK_DIR}/min_uid"`
	MAX_PERUN_UID=`head -n 1 "${WORK_DIR}/max_uid"`
	[ "${MIN_PERUN_UID}" -gt 0 ] || log_msg E_PASSWD_WRONG_MIN_UID
	[ "${MAX_PERUN_UID}" -gt 0 ] || log_msg E_PASSWD_WRONG_MAX_UID

	create_lock

	# grep lines from /etc/passwd which uid is NOT between min a max uid
	catch_error E_PASSWD_FILTER perl -F':' -ane "print if(\$F[2] < $MIN_PERUN_UID || \$F[2] > $MAX_PERUN_UID)" "${OLD_PASSWD}" >"${NEW_PASSWD}"
	[ -s "${NEW_PASSWD}" ] || log_msg E_PASSWD_FILTER

	#delete empty lines and then convert lines to format ^USER:
	catch_error E_PASSWD_GET_USERS sed -e '/^\s*$/d ; s/^\([^:]*:\).*$/^\1/' ${NEW_PASSWD} > ${USERS_REGEXP_FILE}

	#filter users from /etc/shadow
	catch_error E_SHADOW_FILTER grep -f ${USERS_REGEXP_FILE} ${OLD_SHADOW} > ${NEW_SHADOW}

	catch_error E_PASSWD_NO_ROOT egrep -q '^root:' ${NEW_PASSWD}
	catch_error E_SHADOW_NO_ROOT egrep -q '^root:' ${NEW_SHADOW}

	# merge received passwd into striped old one
	catch_error E_PASSWD_MERGE cat "${PASSWD_FROM_PERUN}" >>"${NEW_PASSWD}"

	# merge received shadow into striped old one
	catch_error E_SHADOW_MERGE cat "${SHADOW_FROM_PERUN}" >> "${NEW_SHADOW}"

	# check for duplicate lognames in passwd
	DUPLICATE_LOGNAMES=`cut -d: -f1 "${NEW_PASSWD}" | sort | uniq -d`
	[ "x${DUPLICATE_LOGNAMES}" == 'x' ] || log_msg E_PASSWD_DUPLICATES

	# check for duplicate lognames in shadow
	DUPLICATE_LOGNAMES=`cut -d: -f1 "${NEW_SHADOW}" | sort | uniq -d`
	[ "x${DUPLICATE_LOGNAMES}" == 'x' ] || log_msg E_SHADOW_DUPLICATES

	# move only in case there are any changes
	diff_mv "${NEW_PASSWD}" "${PASSWD_DST_FILE}" \
		&& log_msg I_P_CHANGED \
		|| log_msg I_P_NOT_CHANGED

	# process shadow
	diff_mv "${NEW_SHADOW}" "${SHADOW_DST_FILE}" \
		&& log_msg I_S_CHANGED \
		|| log_msg I_S_NOT_CHANGED
}
