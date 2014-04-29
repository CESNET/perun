#!/bin/bash

PROTOCOL_VERSION='3.0.0'

function process {
	DST_FILE="/var/spool/docdb"
	FROM_PERUN="${WORK_DIR}/docdb"

	### Status codes
	I_CHANGED=(0 "${DST_FILE} updated")
	I_NOT_CHANGED=(0 "${DST_FILE} has not changed")

	create_lock

	CHANGED=0
	# Create diff between old and new
	diff_mv "$FROM_PERUN" "${DST_FILE}" \
		&&  log_msg I_CHANGED && CHANGED=1 \
		|| log_msg I_NOT_CHANGED

	if [ "$CHANGED" -eq 1 ] ; then
		#parse and synchronise
		/root/docdbsync/docdb_perun.py "${DST_FILE}"
	fi
}
