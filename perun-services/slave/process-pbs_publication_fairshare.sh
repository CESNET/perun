#!/bin/bash

PROTOCOL_VERSION='3.0.0'


function process {
	DST_FILE="/var/spool/torque/sched_priv/resource_group"

	### Status codes
	I_CHANGED=(0 "${DST_FILE} updated")
	I_NOT_CHANGED=(0 "${DST_FILE} has not changed")

	FROM_PERUN="${WORK_DIR}/pbs_publication_fairshare"

	create_lock

	# Create diff between old.perun and .new
	diff_mv "${FROM_PERUN}" "${DST_FILE}"

	if [ $? -eq 0 ]; then
		log_msg I_CHANGED
	else
		log_msg I_NOT_CHANGED
	fi
}
