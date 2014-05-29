#!/bin/bash

# List of projects
PROTOCOL_VERSION='3.0.0'


function process {
	FROM_PERUN="${WORK_DIR}/fs_project"
	FROM_PERUN_SORTED="${WORK_DIR}/fs_project_sorted"

	I_PROJECT_GENERATING_SUCCESS=(0 'All projects were generate right.');

	E_PROJECT_DIR_NOT_EXISTS=(50 'Cannot find directory ${PROJECT_PATH}')
	E_CANNOT_CREATE_DIR=(51 'Cannot create directory ${PROJECT_PATH}/${PROJECT_NAME}')
	E_CANNOT_CHANGE_PERMISSIONS=(52 'Cannot change permissions on directory ${PROJECT_PATH}/${PROJECT_NAME} to 2${PERMISSIONS}')
	E_CANNOT_CHANGE_PERMISSIONS_TO_NOBODY=(53 'Cannot change permissions on directory ${PREVIOUS_PROJECT_PATH}/${LINE} to 2000')
	E_CANNOT_CHANGE_OWNER=(54 'Cannot change owner of directory ${PROJECT_PATH}/${PROJECT_NAME} to ${OWNER}:${UNIX_GROUP_NAME}')
	E_CANNOT_CHANGE_OWNER_TO_NOBODY=(55 'Cannot change owner of directory ${PREVIOUS_PROJECT_PATH}/${LINE} to nobody:nogroup')
	E_CANNOT_GET_QUOTAFS=(56 'Cannot get filesystem with set quota on')
	E_CANNOT_SET_QUOTA=(57 'Cannot set quota on ${QUOTA_FS} for group ${GID}')

	create_lock

	#At start there is no project path declared
	PROJECT_PATH_DECLARED=

	#Load quota_enabled or set it to default 0 - quota not enabled
	[ -z "${QUOTA_ENABLED}" ] && QUOTA_ENABLED=0

	sort ${FROM_PERUN} > ${FROM_PERUN_SORTED}
	#Sort lines by project_path
	while IFS=`echo -e "\t"` read PROJECT_PATH PROJECT_NAME PERMISSIONS OWNER UNIX_GROUP_NAME GID SOFT_QUOTA_DATA HARD_QUOTA_DATA SOFT_QUOTA_FILES HARD_QUOTA_FILES QUOTA_ENABLED REST_OF_LINE; do
		#If there is no directory project_path, end with error
		if [ -d "$PROJECT_PATH" ]; then
			#Project path is not declared on first start
			if [ -z $PROJECT_PATH_DECLARED ]; then
				EXISTING_DIRECTORIES=`ls $PROJECT_PATH`
				PROJECT_PATH_DECLARED=true
			else
				#Test if the previous projectPath is not the same like the new one
				if [ "$PROJECT_PATH" != "$PREVIOUS_PROJECT_PATH" ]; then
					#If projectPaths are not same, set all not managed existing directories to nobady
					for LINE in $EXISTING_DIRECTORIES; do
						#Set permission to 2000 and owners to nobody:nogroup
						catch_error E_CANNOT_CHANGE_PERMISSIONS_TO_NOBODY chmod 2000 "$PREVIOUS_PROJECT_PATH"/"$LINE"
						catch_error E_CANNOT_CHANGE_OWNER_TO_NOBODY chown nobody:nogroup "$PREVIOUS_PROJECT_PATH"/"$LINE"
					done
					#Reload existing directories for new path
					EXISTING_DIRECTORIES=`ls $PROJECT_PATH`
				fi
			fi

			#If directory not exists, create new one
			if [ ! -d "$PROJECT_PATH/$PROJECT_NAME" ]; then
				catch_error E_CANNOT_CREATE_DIR mkdir "$PROJECT_PATH/$PROJECT_NAME"
			else
				#If directory exists, remove it from EXISTING_DIRECTORIES
				EXISTING_DIRECTORIES=`echo "$EXISTING_DIRECTORIES" | sed -e "/^${PROJECT_NAME}\$/d"`
			fi

			#Set permissions and owners on this directory
			catch_error E_CANNOT_CHANGE_PERMISSIONS chmod 2"$PERMISSIONS" "$PROJECT_PATH"/"$PROJECT_NAME"
			catch_error E_CANNOT_CHANGE_OWNER chown "$OWNER":"$UNIX_GROUP_NAME" "$PROJECT_PATH"/"$PROJECT_NAME"

			#Set settings for quota if enabled
			if [ "${QUOTA_ENABLED}" -gt 0 ]; then
				#If there is another program (not the default one) to set quota, use it, in other case use default one
				if [ ! -z "${SET_QUOTA_PROGRAM}" ]; then
					#If another program for set quota is executable, set it, in other set quota enabled on 0
					if [ -x "${SET_QUOTA_PROGRAM}" ]; then
						SET_QUOTA="${SET_QUOTA_PROGRAM}"
						QUOTA_ENABLED=1
						#If set quota template is empty, set quota enabled on 0
						if [ -z "${SET_QUOTA_TEMPLATE}" ]; then
							echo "Can't set group quotas by ${SET_QUOTA_PROGRAM}! Template for parameters is not set." 1>&2
							QUOTA_ENABLED=0
						fi
					else
						echo "Can't set group quotas! ${SET_QUOTA_PROGRAM} is not executable" 1>&2
						QUOTA_ENABLED=0
					fi
				else
					if [ -x /usr/sbin/setquota ]; then
						SET_QUOTA=/usr/sbin/setquota
						#setquota name block-softlimit block-hardlimit inode-softlimit inode-hardlimit filesystem
						SET_QUOTA_TEMPLATE='--group $GID $SOFT_QUOTA_DATA $HARD_QUOTA_DATA $SOFT_QUOTA_FILES $HARD_QUOTA_FILES $QUOTA_FS'
						QUOTA_ENABLED=1
					else
						echo "Can't set group quotas! /usr/sbin/setquota is not available" 1>&2
						QUOTA_ENABLED=0
					fi
				fi
			fi

			#Set quota on this directory if quota enabled (use settings above)
			if [ "${QUOTA_ENABLED}" -gt 0 ]; then
				#Get file system information about project directory
				QUOTA_FS=`df -P "$PROJECT_PATH"/"$PROJECT_NAME"`
				[ $? -eq 0 ] || log_msg E_CANNOT_GET_QUOTAFS
				QUOTA_FS=`echo $QUOTA_FS | tail -n 1 | sed -e 's/^.*\s//'`
				#Set quota on directory
				SET_QUOTA_PARAMS=`eval echo $SET_QUOTA_TEMPLATE`
				catch_error E_CANNOT_SET_QUOTA $SET_QUOTA $SET_QUOTA_PARAMS
			fi

			#Set the last used projectPath for equaling in next step
			PREVIOUS_PROJECT_PATH="$PROJECT_PATH"
		else
			log_msg E_PROJECT_DIR_NOT_EXISTS
		fi
	done < ${FROM_PERUN_SORTED}

	#Need to do it for the last time when while ends
	for LINE in $EXISTING_DIRECTORIES; do
		#Set permission to 2000 and owners to nobody:nogroup
		catch_error E_CANNOT_CHANGE_PERMISSIONS_TO_NOBODY chmod 2000 "$PREVIOUS_PROJECT_PATH"/"$LINE"
		catch_error E_CANNOT_CHANGE_OWNER_TO_NOBODY chown nobody:nogroup "$PREVIOUS_PROJECT_PATH"/"$LINE"
	done

	#Everything is ok
	log_msg I_PROJECT_GENERATING_SUCCESS
}
