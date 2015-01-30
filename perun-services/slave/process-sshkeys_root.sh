#!/bin/bash

PROTOCOL_VERSION='3.0.0'


function process {
	DST_FILE="/root/.ssh/authorized_keys"

	### Status codes
	I_CHANGED=(0 "${DST_FILE} updated")
	I_NOT_CHANGED=(0 "${DST_FILE} has not changed")

	FROM_PERUN="${WORK_DIR}/sshkeys_root"

	# Add perun ssh keys
	grep "command=\"/opt/perun/bin/perun\" " "$DST_FILE" >> "$FROM_PERUN"

	create_lock

	diff_mv "${FROM_PERUN}" "${DST_FILE}"

	if [ $? -eq 0 ]; then
		chown root.root $DST_FILE
		chmod 0644 $DST_FILE
		log_msg I_CHANGED
	else
		log_msg I_NOT_CHANGED
	fi
}
