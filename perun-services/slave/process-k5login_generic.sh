#!/bin/bash

PROTOCOL_VERSION='3.0.0'

function process {
	#Status msgs
	I_EVERYTHING_OK=(0 'All files has been loaded.')

	#Error msgs
	E_PARSING_FILE_NAME=(50 'Error when parsing filename')
	E_HOME_DIR_NOT_EXISTS=(51 'Home for ${DST_HOME_DIR} not found.')
	E_PARSING_GID=(52 'Cannot get GID for user ${USER_NAME}')
	E_FINISHED_WITH_ERRORS=(53 'Slave script finished with errors!')

	TMP_ERROR_FILE="${WORK_DIR}/error.tmp"
	FROM_PERUN_DIR="${WORK_DIR}/k5login/"
	PASSWD="/etc/passwd"	

	ERROR=0
	
	create_lock

	# For all files from perun do diff
	for FROM_PERUN_FILE in $FROM_PERUN_DIR/*
	do
		# Get name of file only
		USER_NAME=`basename $FROM_PERUN_FILE`;
		# If error ocured, log it
		if [ $? -ne 0 ]; then
			log_msg_without_exit E_PARSING_FILE_NAME
			ERROR=1
			continue
		fi

		# Get information about existence of home directory of this user
		DST_HOME_DIR=`eval echo "~$USER_NAME"`;
		if [ ! -d "$DST_HOME_DIR" ]; then
			log_msg_without_exit E_HOME_DIR_NOT_EXISTS
			ERROR=1
			continue
		fi

		# If .k5login not exists, create new one, in other case use diff on them
		DST_FILE="$DST_HOME_DIR/.k5login"
		if [ ! -f "$DST_FILE" ]; then
			cp "$FROM_PERUN_FILE" "$DST_FILE"
			
			#separate GID for user from passwd file (4th column)
			GID=`id -g $USER_NAME`
			if [ $? -ne 0 ]; then
				log_msg_without_exit E_PARSING_GID
				ERROR=1
				continue	
			fi
			
			#set permissions	
			chown $USER_NAME:$GID $DST_FILE
			chmod 644 $DST_FILE
		else
			diff_mv "${FROM_PERUN_FILE}" "${DST_FILE}"
			# If diff is ok, than set also permissions 
			if [ $? -eq 0 ]; then
				#separate GID for user from passwd file (4th column)
				GID=`id -g $USER_NAME`
				if [ $? -ne 0 ]; then
					log_msg_without_exit E_PARSING_GID
					ERROR=1
	        			continue
				fi

				#set permissions
				chown $USER_NAME:$GID $DST_FILE
				chmod 644 $DST_FILE
			fi
		fi	
	done

	if [ $ERROR -ne 0 ]; then
		log_msg E_FINISHED_WITH_ERRORS
	else
		log_msg I_EVERYTHING_OK
	fi
}
