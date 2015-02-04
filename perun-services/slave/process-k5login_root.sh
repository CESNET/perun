#!/bin/bash

PROTOCOL_VERSION='3.0.0'


function process {
	DST_FILE="/root/.k5login"

	### Status codes
	I_CHANGED=(0 "${DST_FILE} updated")
	I_NOT_CHANGED=(0 "${DST_FILE} has not changed")
	E_CHOWN=(50 'Cannot chown on ${FROM_PERUN}')
	E_CHMOD=(51 'Cannot chmod on ${FROM_PERUN}')

	FROM_PERUN="${WORK_DIR}/k5login_root"

	create_lock

	# Destination file doesn't exist
	if [ ! -f ${DST_FILE} ]; then
		catch_error E_CHOWN chown root.root $FROM_PERUN
		catch_error E_CHMOD chmod 0644 $FROM_PERUN
	fi

	# Create diff between old.perun and .new
	diff_mv "${FROM_PERUN}" "${DST_FILE}"

	if [ $? -eq 0 ]; then
		log_msg I_CHANGED
	else
		log_msg I_NOT_CHANGED
	fi
}
