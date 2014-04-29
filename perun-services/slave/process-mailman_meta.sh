#!/bin/bash

PROTOCOL_VERSION='3.1.0'

function process {
	DST_DIR="/var/spool/mailinglists"
	FROM_PERUN_DIR="${WORK_DIR}/mailinglists/"

	### Errors
	E_CANNOT_CREATE_DIR=(1, "Cannot create destination directory ${DST_DIR}")

	### Status codes
	I_CHANGED=(0 '${DST_FILE} updated')
	I_NOT_CHANGED=(0 '${DST_FILE} has not changed')

	create_lock

	if [ ! -d ${DST_DIR} ]; then
		catch_error E_CANNOT_CREATE_DIR mkdir -p ${DST_DIR}
	fi

	for FILE in `ls $FROM_PERUN_DIR/` ; do
		DST_FILE=${DST_DIR}/$FILE
		CHANGED=0
		# Create diff between old.perun and .new
		diff_mv "${FROM_PERUN_DIR}/$FILE" "${DST_FILE}" \
			&&  log_msg I_CHANGED && CHANGED=1 \
			|| log_msg I_NOT_CHANGED

		if [ $CHANGED -eq 1 ] ; then
			#set list members
			/usr/sbin/sync_members --welcome-msg=no --goodbye-msg=no -f "${DST_FILE}" $FILE
			# create Python file with code setting the sso flag
			echo 'mlist.sso=True' >>/tmp/conf$$.py
			# use the file
			/usr/lib/mailman/bin/config_list -v -i /tmp/conf$$.py $FILE
			# clean up
			rm /tmp/conf$$.py
		fi

	done
}
