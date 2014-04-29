#!/bin/bash

PROTOCOL_VERSION='3.1.0'

function process {
	FROM_PERUN_DIR="${WORK_DIR}/mailinglists/"

	# Format of the file:
	# [Mailing list email address] [name of the mailing list within mailman]
	# Example:
	# c4e@ics.muni.cz c4e_ics
	# kypo@ics.muni.cz kypo
	EXISTING_MAILING_LISTS="/etc/perun/services.d/mailman.existingMailingLists"

	I_MAILING_LIST_IS_NOT_MANAGED_BY_PERUN=(0 '${MAILING_LIST_NAME} is not managed by Perun.')
	I_MAILING_LIST_UPDATED=(0 '${MAILING_LIST_NAME} successfully updated.')

	create_lock

	for MAILING_LIST_NAME in `ls $FROM_PERUN_DIR/` ; do
		# check if the mailing lists is managed by perun
		if [ `grep -c "^${MAILING_LIST_NAME} " ${EXISTING_MAILING_LISTS}` -eq 1 ]; then
			# extract the mailman mailing list name from $EXISTING_MAILING_LISTS
			MAILMAN_MAILING_LIST_NAME=`grep "^${MAILING_LIST_NAME}" ${EXISTING_MAILING_LISTS} | awk '{ print $2; }'`

			# set list members
			cat "${FROM_PERUN_DIR}/${MAILING_LIST_NAME}" | grep -v "^#" | sudo /usr/local/mailman/bin/sync_members --welcome-msg=no --goodbye-msg=no -f - $MAILMAN_MAILING_LIST_NAME
			log_msg I_MAILING_LIST_UPDATED
		else
			log_msg I_MAILING_LIST_IS_NOT_MANAGED_BY_PERUN
		fi
	done
}
