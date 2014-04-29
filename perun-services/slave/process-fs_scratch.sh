#!/bin/bash

# List of logins who have to have directory in the /scratch
PROTOCOL_VERSION='3.4.0'


function process {
	FROM_PERUN="${WORK_DIR}/fs_scratch"
	UMASK_FILE="${WORK_DIR}/umask"

	I_DIR_CREATED=(0 'Scratch directory ${SCRATCH_DIR} ($U_UID.$U_GID) for ${U_LOGNAME} created.')

	E_CANNOT_CREATE_DIR=(50 'Cannot create directory ${SCRATCH_DIR}/${LOGIN}.')
	E_CANNOT_SET_OWNERSHIP=(51 'Cannot set ownership ${U_UID}.${U_GID} for directory ${SCRATCH_DIR}')
	E_CANNOT_SET_PERMISSIONS=(52 'Cannot set permissions 0755 for directory ${SCRATCH_DIR}.')

	create_lock

	if [ -z "${UMASK}" ]; then
		UMASK=0700   #default pemissions
		[ -f "$UMASK_FILE" ] && UMASK=`head -n 1 "$UMASK_FILE"`
	fi

	unset SET_QUOTA_ENABLED
	if [ "${SET_QUOTA_PROGRAM}" ]; then
		if [ -x "${SET_QUOTA_PROGRAM}" ]; then
			SET_QUOTA="${SET_QUOTA_PROGRAM}"
			SET_QUOTA_ENABLED=1
		else
			echo "Can't set user quotas! ${SET_QUOTA_PROGRAM} is not executable" 1>&2
		fi
	else
		if [ -x /usr/sbin/setquota ]; then
			SET_QUOTA=/usr/sbin/setquota
			#setquota name block-softlimit block-hardlimit inode-softlimit inode-hardlimit filesystem
			SET_QUOTA_TEMPLATE='$U_UID $SOFT_QUOTA_DATA $HARD_QUOTA_DATA $SOFT_QUOTA_FILES $HARD_QUOTA_FILES $QUOTA_FS'
		else
			echo "Can't set user quotas! /usr/sbin/setquota is not available" 1>&2
		fi
	fi

	# lines contains login\tUID\tGID\t...
	while IFS=`echo -e "\t"` read U_SCRATCH_MNT_POINT U_LOGNAME U_UID U_GID SOFT_QUOTA_DATA HARD_QUOTA_DATA SOFT_QUOTA_FILES HARD_QUOTA_FILES USER_STATUS; do
		SCRATCH_DIR="${U_SCRATCH_MNT_POINT}/${U_LOGNAME}"
		if [ ! -d "${SCRATCH_DIR}" ]; then

			catch_error E_CANNOT_CREATE_DIR mkdir "${SCRATCH_DIR}"
			catch_error E_CANNOT_SET_OWNERSHIP chown "${U_UID}"."${U_GID}" "${SCRATCH_DIR}"
			catch_error E_CANNOT_SET_PERMISSIONS chmod "$UMASK" "${SCRATCH_DIR}"

			log_msg I_DIR_CREATED
		fi

		if [ "$SET_QUOTA_ENABLED" ]; then
			QUOTA_FS=`df -P  "$SCRATCH_DIR" | tail -n 1 | sed -e 's/^.*\s//'`
			[ $? -eq 0 ] || log_msg E_CANNOT_GET_QUOTAFS
			SET_QUOTA_PARAMS=`eval echo $SET_QUOTA_TEMPLATE`
			catch_error E_CANNOT_SET_QUOTA $SET_QUOTA $SET_QUOTA_PARAMS
		fi
	done < "${FROM_PERUN}"

}
