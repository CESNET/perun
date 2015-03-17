#!/bin/bash

PROTOCOL_VERSION='3.0.0'

### Status codes
I_CHANGED=(0 'Aliases updated')
I_NOT_CHANGED=(0 'Aliases has not changed')
E_DUPLICITS=(50 'Duplicits: "\"${DUPLICITS}\"" have been found in "\"${FILES}\"".')
E_NOTEXISTING_ALIASESD_DIR=(51 'Directory "\"${ETC_ALIASESD_DIR}\"" not exists.')
E_PERMISSIONS=(52 'Cannot set permissions "\"${PERMISSIONS}\"" to file "\"${FROM_PERUN}\"".')
E_NOTEXISTING_ALIASES_FILE=(53 'File "\"${ETC_ALIASES}\"" not exists.')
E_NEWALIASES=(54 'Command newaliases failed.')

GENERIC_FILE="perun_generic"
FROM_PERUN="${WORK_DIR}/${GENERIC_FILE}"

ETC_ALIASES="/etc/aliases"
ETC_ALIASESD_DIR="/etc/aliases.d/"
PERMISSIONS="644"

function process {
	### Create lock
	create_lock

	### Set permisson for special case if destination file not exists
	catch_error E_PERMISSIONS chmod "${PERMISSIONS}" "${FROM_PERUN}"

	### try if /etc/aliases.d/ exists, if not, end with error
	if [ ! -d "${ETC_ALIASESD_DIR}" ]; then
		log_msg E_NOTEXISTING_ALIASESD_DIR
	fi

	### try if file /etc/aliases exists, if not, end with error
	if [ ! -f "${ETC_ALIASES}" ]; then
		log_msg E_NOTEXISTING_ALIASES_FILE
	fi
	
	### Looking for duplicits in possible places (except perun_generic_file)
	### take all files from /etc/aliases.d/ except old generic_file and all files with extension .db
	fail_if_duplicits_found "${ETC_ALIASES}" "${FROM_PERUN}" `find "${ETC_ALIASESD_DIR}" -type f -not -name ${GENERIC_FILE} -not -name "*.db" -print`

	### If no duplicits found, move new generic_file to /etc/aliases.d/
	diff_mv "${FROM_PERUN}" "${ETC_ALIASESD_DIR}/${GENERIC_FILE}" && log_msg I_CHANGED || log_msg I_NOT_CHANGED

	### call new aliases
	newaliases
	if [ $? -ne 0 ]; then
    		log_msg E_NEWALIASES
	fi
}



# This function looking for any non comment duplicit names of aliases 'name:email'
# If any duplicit found, then end with error
# Return 0 if no duplicit found
function fail_if_duplicits_found {
	FILES="$@"

	DUPLICITS=`sed -e 's/^\s*#.*//g' -e 's/\s\+//g' -e 's/:.*$//g' ${FILES} | sort | uniq -d`
	if [ -n "${DUPLICITS}" ]; then
		log_msg E_DUPLICITS
	fi

	return 0
}
