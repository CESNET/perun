#!/bin/bash

# List of logins who have to have directory in the /scratch
PROTOCOL_VERSION='3.3.0'


function process {
	FROM_PERUN="${WORK_DIR}/fs_scratch_global"
	SCRATCH_MOUNTPOINT=`cat ${WORK_DIR}/scratch_mountpoint`

	I_DIR_CREATED=(0 'Scratch directory ${SCRATCH_MOUNTPOINT}/${LOGIN} ($U_UID.$U_GID) for ${LOGIN} created.')

	E_CANNOT_CREATE_DIR=(50 'Cannot create directory ${SCRATCH_MOUNTPOINT}/${LOGIN}.')
	E_CANNOT_SET_OWNERSHIP=(51 'Cannot set ownership ${U_UID}.${U_GID} for directory ${SCRATCH_MOUNTPOINT}/${LOGIN}')
	E_CANNOT_SET_PERMISSIONS=(52 'Cannot set permissions 0755 for directory ${SCRATCH_MOUNTPOINT}/${LOGIN}.')
	E_SCRATCH_DIR_NOT_EXISTS=(53 'Scratch directory ${SCRATCH_MOUNTPOINT} does not exist.')

	create_lock

	if [ -z "${UMASK}" ]; then
		UMASK=0755   #default pemissions
	fi

	# Check if the top-level scratch dir exists
	if [ ! -d "${SCRATCH_MOUNTPOINT}" ]; then
		log_msg E_SCRATCH_DIR_NOT_EXISTS
	fi

	# lines contains login\tUID\tGID\t...
	while IFS=`echo -e "\t"` read LOGIN U_UID U_GID SOFT_QUOTA_DATA HARD_QUOTA_DATA SOFT_QUOTA_FILES HARD_QUOTA_FILES; do

		if [ ! -d "${SCRATCH_MOUNTPOINT}/${LOGIN}" ]; then
			catch_error E_CANNOT_CREATE_DIR  mkdir "${SCRATCH_MOUNTPOINT}/${LOGIN}"

			catch_error E_CANNOT_SET_OWNERSHIP chown "${U_UID}"."${U_GID}" "${SCRATCH_MOUNTPOINT}/${LOGIN}"
			catch_error E_CANNOT_SET_PERMISSIONS chmod "${UMASK}" "${SCRATCH_MOUNTPOINT}/${LOGIN}"

			# Set quota
			# setquota name block-softlimit block-hardlimit inode-softlimit inode-hardlimit filesystem
			#      if [ -x /usr/sbin/setquota ]; then
			#					# Get $QUOTA_FS
			#         /usr/sbin/setquota $U_UID $SOFT_QUOTA_DATA $HARD_QUOTA_DATA $SOFT_QUOTA_FILES $HARD_QUOTA_FILES $QUOTA_FS
			#      fi

			log_msg I_DIR_CREATED
		fi
	done < "${FROM_PERUN}"
}
