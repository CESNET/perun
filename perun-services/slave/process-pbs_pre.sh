#!/bin/bash

PROTOCOL_VERSION='3.0.0'


function process {
	DST_FILE="/var/local/perun-pbs/pbs_pre"

	### Status codes
	I_CHANGED=(0 "${DST_FILE} updated")
	I_NOT_CHANGED=(0 "${DST_FILE} has not changed")

	OLD="${DST_FILE}.old.perun"
	FROM_PERUN="${WORK_DIR}/pbs_pre"

	create_lock

	# Create diff between old.perun and .new
	diff_mv "${FROM_PERUN}" "${DST_FILE}" \
		&&  log_msg I_CHANGED \
		|| log_msg I_NOT_CHANGED
}
