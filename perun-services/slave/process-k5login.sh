#!/bin/bash

PROTOCOL_VERSION='3.0.0'


function process {
	### Status codes
	I_K5LOGIN_CREATED=(0 '${HOME_DIR}/.k5login with entries ${PRINCIPALS} created.')
	I_K5LOGIN_UPDATED=(0 '${HOME_DIR}/.k5login updated. Added entries are ${ADDED_PRINCIPALS}.')

	E_DIR_NOT_EXISTS=(50 'Home directory ${HOME_DIR} does not exits.')
	E_CHANGE_OWNER=(51 'Change owner on ${HOME_DIR}/.k5login failed.')

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
			log_debug "File ${HOME_DIR}/.k5login not exists and need to be created."
			for PRINCIPAL in ${PRINCIPALS}; do
				echo "${PRINCIPAL}" >> "${HOME_DIR}/.k5login"
			done
			log_debug "If not exists, file ${HOME_DIR}/.k5login was created and ${PRINCIPALS} were added into it."

			# Setup rights, the .k5login will have the same owner and group as user's home directory
			F_USER=`ls -ld "${HOME_DIR}" | awk '{ print $3; }'`
			F_GROUP=`ls -ld "${HOME_DIR}" | awk '{ print $4; }'`
			catch_error E_CHANGE_OWNER chown ${F_USER}.${F_GROUP} "${HOME_DIR}/.k5login"

			F_USER_REAL=`ls -ld "${HOME_DIR}" | awk '{ print $3; }'`
			F_GROUP_REAL=`ls -ld "${HOME_DIR}" | awk '{ print $4; }'`
			log_debug "Owner was changed for file ${HOME_DIR}/.k5login. New owner is ${F_USER_REAL}:${F_GROUP_REAL} (expected ${F_USER}:${F_GROUP})"

			catch_error E_PERMISSIONS chmod 0644 "${HOME_DIR}/.k5login"
			K5LOGIN_PERMISSIONS=`stat -L -c %a "${HOME_DIR}/.k5login"`
			log_debug "Permissions was set to ${K5LOGIN_PERMISSIONS} for file ${HOME_DIR}/.k5login (expected 0644)"

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
