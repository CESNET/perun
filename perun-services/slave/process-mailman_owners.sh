#!/bin/bash

PROTOCOL_VERSION='3.0.0'

function process {
	DST_DIR="/var/spool/mailman_owners"
	FROM_PERUN_DIR="${WORK_DIR}/mailman_owners/"

	### Status codes
	I_CHANGED=(0 '${DST_FILE} updated')
	I_NOT_CHANGED=(0 '${DST_FILE} has not changed')

	create_lock

	for FILE in `ls $FROM_PERUN_DIR/` ; do
		DST_FILE=${DST_DIR}/$FILE
		CHANGED=0
		# Create diff between old and new
		diff_mv "${FROM_PERUN_DIR}/$FILE" "${DST_FILE}" \
			&&  log_msg I_CHANGED && CHANGED=1 \
			|| log_msg I_NOT_CHANGED

		if [ $CHANGED -eq 1 ] ; then
			# create Python file with code setting the owners and sso flag
			python -c 'print "owner=",[line.strip() for line in open("'${DST_FILE}'")],"\nmlist.sso=True"' >>/tmp/conf$$.py || exit 1
			# use the file
			/usr/lib/mailman/bin/config_list -v -i /tmp/conf$$.py $FILE
			# clean up
			rm /tmp/conf$$.py
		fi

	done
}
