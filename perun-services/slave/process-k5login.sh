#!/bin/bash

PROTOCOL_VERSION='3.0.0'


function process {
	### Status codes
	I_K5LOGIN_CREATED=(0 '${K5LOGIN} with entries ${PRINCIPALS} created.')
	I_K5LOGIN_UPDATED=(0 '${K5LOGIN} updated. Added entries are ${ADDED_PRINCIPALS}.')
	I_K5LOGIN_NOT_UPDATED=(0 '${K5LOGIN} not updated. Nothing to add.')

	E_DIR_NOT_EXISTS=(50 'Home directory ${HOME_DIR} does not exits.')
	E_CHANGE_OWNER=(51 'Change owner on ${TMP_K5LOGIN} failed.')
	E_K5LOGIN_NOT_CREATED=(52 'Move diff from ${TMP_K5LOGIN} to ${HOME_DIR}/.k5login failed.')
	E_CANNOT_COPY_K5LOGIN=(53 'Cannot create copy of ${HOME_DIR}/.k5login to ${TMP_K5LOGIN}')
	E_CANNOT_REMOVE_TMP_K5LOGIN=(54 'Cannot remove ${TMP_K5LOGIN} file.')
	E_CANNOT_CREATE_TMP_K5LOGIN=(55 'Cannot create temporary ${TMP_K5LOGIN} file.')

	FROM_PERUN="${WORK_DIR}/k5login"

	create_lock

	# lines contains login\tprincipal#1\tprincipal#2\t...
	while read line
	do
		HOME_DIR=`echo "${line}" | awk '{ print $1 };'`
		PRINCIPALS=`echo "${line}" | sed -e 's/^[^\t]*[\t]//'`
		K5LOGIN="${HOME_DIR}/.k5login"
		TMP_K5LOGIN="${HOME_DIR}/.k5login-from-perun-$$.tmp"

		add_on_exit 'rm -f "${TMP_K5LOGIN}"'

		catch_error E_DIR_NOT_EXISTS cd "${HOME_DIR}"

		#create new tmp file
		if [ ! -f "${K5LOGIN}" ]; then
			catch_error E_CANNOT_CREATE_TMP_K5LOGIN touch ${TMP_K5LOGIN}
			K5LOGIN_EXISTS=true
		else
			catch_error E_CANNOT_COPY_K5LOGIN cp -p "${K5LOGIN}" "${TMP_K5LOGIN}"
			K5LOGIN_EXISTS=false
		fi

		#add not existing principals
		ADDED_PRINCIPALS=""
		for PRINCIPAL in ${PRINCIPALS}; do
			grep "^${PRINCIPAL}\\s*\$" "${TMP_K5LOGIN}" > /dev/null
			if [ $? -eq 1 ]; then
				ADDED_PRINCIPALS="$ADDED_PRINCIPALS ${PRINCIPAL}"
				echo "${PRINCIPAL}" >> "${TMP_K5LOGIN}"
			fi
		done 
		
		# Setup rights, the k5login temp file will have the same owner and group as user's home directory
		F_USER=`ls -ld "${HOME_DIR}" | awk '{ print $3; }'`
		F_GROUP=`ls -ld "${HOME_DIR}" | awk '{ print $4; }'`
		catch_error E_CHANGE_OWNER chown ${F_USER}.${F_GROUP} "${TMP_K5LOGIN}"

		F_USER_REAL=`ls -l "${TMP_K5LOGIN}" | awk '{ print $3; }'`
		F_GROUP_REAL=`ls -l "${TMP_K5LOGIN}" | awk '{ print $4; }'`
		log_debug "Owner was changed for file ${TMP_K5LOGIN}. New owner is ${F_USER_REAL}:${F_GROUP_REAL} (expected ${F_USER}:${F_GROUP})"

		#if something to add, create or update k5login
		if [ -n "$ADDED_PRINCIPALS" ]; then
			catch_error E_K5LOGIN_NOT_UPDATED diff_mv "${TMP_K5LOGIN}" "${K5LOGIN}"
			if [ ${K5LOGIN_EXISTS} == true ]; then
				log_msg I_K5LOGIN_UPDATED
			else
				log_msg I_K5LOGIN_CREATED
			fi
		else
			catch_error E_CANNOT_REMOVE_TMP_K5LOGIN rm -f "${TMP_K5LOGIN}"
			log_msg I_K5LOGIN_NOT_UPDATED
		fi
	done < "${FROM_PERUN}"
}
