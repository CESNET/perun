#!/bin/bash

PROTOCOL_VERSION='3.0.0'

function process {
	DST_FILE="/etc/raddb/eduroam-radius-huntgroups.perun"

	### Status codes
	I_CHANGED=(0 "${DST_FILE} updated")
	I_NOT_CHANGED=(0 "${DST_FILE} has not changed")

	#NEW_HUNTGROUP="/etc/passwd.new" # file must be on same mountpoint for atomic switch
	FROM_PERUN="${WORK_DIR}/eduroam_radius"
	TMP_FILE=`mktemp`

	create_lock

	FIRST=1
	IFSTMP=$IFS
	while IFS= read -r line <&3; do
		if [ $FIRST -eq 1 ]; then
			FIRST=0
			echo -n "  User-Name == \"$line\"" >> "${TMP_FILE}"
		else
			echo "," >> "${TMP_FILE}"
			echo -n "  User-Name == \"$line\"" >> "${TMP_FILE}"
		fi
	done 3< "${FROM_PERUN}"
	IFS=$IFSTMP

	diff_mv "${TMP_FILE}" "${DST_FILE}" \
		&& (log_msg I_CHANGED && /etc/init.d/radius restart) \
		|| log_msg I_NOT_CHANGED
}
