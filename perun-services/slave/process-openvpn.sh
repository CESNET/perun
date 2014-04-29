#!/bin/bash

PROTOCOL_VERSION='3.0.0'

function process {
	### Status codes
	I_CHANGED=(0 "${DST_FILE} updated")
	I_NOT_CHANGED=(0 "${DST_FILE} has not changed")

	FROM_PERUN="${WORK_DIR}/openvpn"
	DST_FILE=/opt/openvpn/etc/pki-perun
	STATE=${DST_FILE}.perun-state

	create_lock

	# Create diff between .old and .new
	diff_update "${FROM_PERUN}" "${DST_FILE}" "$STATE" \
		&& log_msg I_CHANGED \
		|| log_msg I_NOT_CHANGED
}
