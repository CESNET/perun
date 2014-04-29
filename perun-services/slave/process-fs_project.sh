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

	create_lock

	#At start there is no project path declared
	PROJECT_PATH_DECLARED=

	sort ${FROM_PERUN} > ${FROM_PERUN_SORTED}
	#Sort lines by project_path
	while IFS=`echo -e "\t"` read PROJECT_PATH PROJECT_NAME PERMISSIONS OWNER UNIX_GROUP_NAME; do
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
