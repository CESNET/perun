#!/bin/bash

PROTOCOL_VERSION='3.0.1'


function process {
	### Status codes
	I_K5LOGIN_CREATED=(0 '${HOME_DIR}/.k5login with entries ${PRINCIPALS} created.')
	I_K5LOGIN_UPDATED=(0 '${HOME_DIR}/.k5login updated. Added entries are ${ADDED_PRINCIPALS}.')

	E_DIR_NOT_EXISTS=(50 'Home directory ${HOME_DIR} does not exits.')
	E_CHANGE_OWNER=(51 'Change owner on ${TEMP_FILE} failed.')
	E_CANNOT_CREATE_TEMP=(52 'Cannot create temp file ${TEMP_FILE}.')
	E_CANNOT_COPY_K5LOGIN=(53 'Cannot copy content of ${HOME_DIR}/.k5login to ${TEMP_FILE}.')
	E_CANNOT_MOVE_TEMP=(54 'Cannot move ${TEMP_FILE} to ${HOME_DIR}/.k5login.')

	FROM_PERUN="${WORK_DIR}/k5login"

	create_lock

	# lines contains login\tprincipal#1\tprincipal#2\t...
	while read line
	do
		HOME_DIR=`echo "${line}" | awk '{ print $1 };'`
		PRINCIPALS=`echo "${line}" | sed -e 's/^[^\t]*[\t]//'`

		catch_error E_DIR_NOT_EXISTS cd "${HOME_DIR}"

		# If .k5login doesn't exist, create it and fill it with proper principals
		if [ ! -f "${HOME_DIR}/.k5login" ]; then
			log_debug "File ${HOME_DIR}/.k5login not exists and needs to be created."
			for PRINCIPAL in ${PRINCIPALS}; do
				echo "${PRINCIPAL}" >> "${HOME_DIR}/.k5login"
			done
			log_debug "If not exists, file ${HOME_DIR}/.k5login was created and ${PRINCIPALS} were added into it."

			# Setup rights, the .k5login will have the same owner and group as user's home directory
			F_USER=`ls -ld "${HOME_DIR}" | awk '{ print $3; }'`
			F_GROUP=`ls -ld "${HOME_DIR}" | awk '{ print $4; }'`
			catch_error E_CANNOT_CREATE_TEMP TEMP_FILE="$(mktemp ${HOME_DIR}/.tmp-perun-k5login-${F_USER}.XXXX)"
			catch_error E_CHANGE_OWNER chown ${F_USER}.${F_GROUP} "${TEMP_FILE}"

			F_USER_REAL=`ls -ld "${HOME_DIR}" | awk '{ print $3; }'`
			F_GROUP_REAL=`ls -ld "${HOME_DIR}" | awk '{ print $4; }'`
			log_debug "Owner was changed for file ${TEMP_FILE}. New owner is ${F_USER_REAL}:${F_GROUP_REAL} (expected ${F_USER}:${F_GROUP})"

			catch_error E_PERMISSIONS chmod 0644 "${TEMP_FILE}"
			K5LOGIN_PERMISSIONS=`stat -L -c %a "${TEMP_FILE}"`
			log_debug "Permissions was set to ${K5LOGIN_PERMISSIONS} for file ${TEMP_FILE} (expected 0644)"

			catch_error E_CANNOT_COPY_K5LOGIN cp "${HOME_DIR}/.k5login" "${TEMP_FILE}"
			log_debug "Content of ${HOME_DIR}/.k5login was copied to file ${TEMP_FILE}"

			catch_error E_CANNOT_MOVE_TEMP mv "${TEMP_FILE}" "${HOME_DIR}/.k5login"
			log_debug "File ${TEMP_FILE} was moved to ${HOME_DIR}/.k5login"

			log_msg I_K5LOGIN_CREATED
		else
			# .k5login exists. So only add missing entries.
			ADDED_PRINCIPALS=""
			for principal in ${PRINCIPALS}; do
				grep "^${principal}\\s*\$" "${HOME_DIR}/.k5login" > /dev/null
				if [ $? -eq 1 ]; then
					ADDED_PRINCIPALS="$ADDED_PRINCIPALS ${principal}"
					echo "${principal}" >> "${HOME_DIR}/.k5login"
				fi
			done
			if [ -n "$ADDED_PRINCIPALS" ]; then
				log_msg I_K5LOGIN_UPDATED
			fi
		fi
	done < "${FROM_PERUN}"
}
