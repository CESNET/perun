#!/bin/bash

PROTOCOL_VERSION='3.0.0'

function process {
	FROM_PERUN_DIR="${WORK_DIR}/mailinglists/"

	# Format of the file:
	# Name of file = name of mailing list
	# [Email address] [name of user for this email]
	# Example:
	# c4e@ics.muni.cz Tomas Koutny
	# kypo@ics.muni.cz Mgr. Zdenek Horava
	SYMPA_MAILINGLISTS_DIR="/var/spool/mailinglists_sympa/"
	# ACL settings
	UMASK="640"
	GROUP="sympa"
	USER="root"

	if [ ! -d "$SYMPA_MAILINGLISTS_DIR" ]; then
		# Create new directory for sympa mailinglists
		mkdir -p "$SYMPA_MAILINGLISTS_DIR"
	fi

	I_MAILING_LIST_UPDATED=(0 '${MAILING_LIST_NAME} successfully updated.')
	E_PERMISSIONS=(50 'Cannot set permissions ${UMASK} for ${MAILING_LIST_NAME} mailing list.')
	E_CHANGEOWNER=(51 'Cannot change owner to ${USER}:${GROUP} for ${MAILING_LIST_NAME} mailing list.')

	create_lock

	for MAILING_LIST_NAME in `ls $FROM_PERUN_DIR/` ; do
		catch_error E_PERMISSIONS chmod $UMASK "${FROM_PERUN_DIR}/${MAILING_LIST_NAME}"
		catch_error E_CHANGEOWNER chown ${USER}:${GROUP} "${FROM_PERUN_DIR}/${MAILING_LIST_NAME}"
		mv -f "${FROM_PERUN_DIR}/${MAILING_LIST_NAME}" "${SYMPA_MAILINGLISTS_DIR}/${MAILING_LIST_NAME}" 
		log_msg I_MAILING_LIST_UPDATED
	done
}
