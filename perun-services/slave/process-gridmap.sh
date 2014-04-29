#!/bin/bash

PROTOCOL_VERSION='3.0.0'


function process {
	DST_FILE="/etc/grid-security/grid-mapfile"

	### Status codes
	I_CHANGED=(0 '${DST_FILE} updated')
	I_NOT_CHANGED=(0 '${DST_FILE} has not changed')

	E_CHMOD_ERROR=(1 'Cannot set 0644 privileges on ${FROM_PERUN}')

	FROM_PERUN="${WORK_DIR}/gridmap"

	chmod 0644 "$FROM_PERUN" || log_msg E_CHMOD_ERROR

	create_lock

	# Create diff between old.perun and .new
	diff_mv "${FROM_PERUN}" "${DST_FILE}" \
		&&  log_msg I_CHANGED \
		|| log_msg I_NOT_CHANGED
}
