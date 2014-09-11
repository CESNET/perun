#!/bin/bash
PROTOCOL_VERSION='3.0.0'

function process {

	E_FINISHED_WITH_ERRORS=(50 'afs_group slave script finished with errors')
	E_CANNOT_CREATE_FILTERED_DIRECTORY=(51 'Cannot create directory $FILTERED_GROUPS')

	FROM_PERUN_DIR="${WORK_DIR}/groups"
	FILTERED_GROUPS="${WORK_DIR}/filter"
	AFS_GROUP_USERS_FILE="${WORK_DIR}/afs_group_users.tmp"
	TMP_ERROR_FILE="${WORK_DIR}/error.tmp"
	ERROR=0

	create_lock

	#create directory for filtered groups
	catch_error E_CANNOT_CREATE_FILTERED_DIRECTORY mkdir -p "$FILTERED_GROUPS"
	
	for GROUP_NAME in `ls $FROM_PERUN_DIR/` ; do

		USERS_FROM_PERUN_FILE="$FROM_PERUN_DIR/$GROUP_NAME"
		USERS_FROM_PERUN_FILE_FILTERED="$FILTERED_GROUPS/$GROUP_NAME"

		#Create group if not exists
		pts examine -nameorid "$GROUP_NAME" >/dev/null
		#if not null return code, it means not exists and need to be created
		if [ $? -ne 0 ]; then
			pts creategroup -name "$GROUP_NAME" >/dev/null 2>"$TMP_ERROR_FILE"
			if [ $? -ne 0 ]; then
				MSG="Command failed: pts creategroup $GROUP_NAME Reason: `cat $TMP_ERROR_FILE`"
				echo "$MSG" >&2
				logger -t "${NAME}" -p daemon.error "${SERVICE}: ${MSG}" &>/dev/null
				ERROR=1
				continue	
			fi
		fi

		MEMBERSHIP=`pts membership -nameorid "$GROUP_NAME" | tail -n "+2" | sed -e 's/^\s*//' 2> "$TMP_ERROR_FILE"`
		if [ -s "$TMP_ERROR_FILE" ]; then
			MSG="Command failed: pts membership -nameorid $GROUP_NAME | tail -n +2 | sed -e 's/^\s*//' Reason: `cat $TMP_ERROR_FILE`"
			echo "$MSG" >&2
			logger -t "${NAME}" -p daemon.error "${SERVICE}: ${MSG}" &>/dev/null
			ERROR=1
			continue
		fi

		#Use filter on members from afs
		if [ -n "${AFS_MEMBERSHIP_FILTER}" ]; then
			echo "$MEMBERSHIP" | grep "$AFS_MEMBERSHIP_FILTER" > "$AFS_GROUP_USERS_FILE" 2> "$TMP_ERROR_FILE"
		else
			echo "$MEMBERSHIP" > "$AFS_GROUP_USERS_FILE" 2> "$TMP_ERROR_FILE"
		fi
		if [ -s "$TMP_ERROR_FILE" ]; then
			MSG="Command failed: $MEMBERSHIP | grep $AFS_MEMBERSHIP_FILTER > $AFS_GROUP_USERS_FILE Reason: `cat $TMP_ERROR_FILE`"
			echo "$MSG" >&2
			logger -t "${NAME}" -p daemon.error "${SERVICE}: ${MSG}" &>/dev/null
			ERROR=1
			continue
		fi

		#Use filter on members from perun (create temp file with filter)
		if [ -n "${AFS_MEMBERSHIP_FILTER}" ]; then	
			grep "$AFS_MEMBERHIP_FILTER" "$USERS_FROM_PERUN_FILE" > "$USERS_FROM_PERUN_FILE_FILTERED" 2> "$TMP_ERROR_FILE"
			if [ -s "$TMP_ERROR_FILE" ]; then
				MSG="Command failed: grep $AFS_MEMBERHIP_FILTER $USERS_FROM_PERUN_FILE > $USERS_FROM_PERUN_FILE_FILTERED Reason: `cat $TMP_ERROR_FILE`"
				echo "$MSG" >&2
				logger -t "${NAME}" -p daemon.error "${SERVICE}: ${MSG}" &>/dev/null
				ERROR=1
				continue
			fi
		else
			USERS_FROM_PERUN_FILE_FILTERED="$USERS_FROM_PERUN_FILE"
		fi

		USERS=`grep -v -F -f "$USERS_FROM_PERUN_FILE_FILTERED" "$AFS_GROUP_USERS_FILE"`
		#if there are no users for removing, skip this part
		if [ -n "$USERS" ]; then
			pts removeuser -user $USERS -group $GROUP_NAME
			if [ $? -ne 0 ]; then
				MSG="Command failed: pts removeuser -user \`grep -v -F -f $USERS_FROM_PERUN_FILE $AFS_GROUP_USERS_FILE \` -group $GROUP_NAME"
				echo "$MSG" >&2
				logger -t "${NAME}" -p daemon.error "${SERVICE}: ${MSG}" &>/dev/null
				ERROR=1
				continue
			fi
		fi

		USERS=`grep -v -F -f "$AFS_GROUP_USERS_FILE" "$USERS_FROM_PERUN_FILE_FILTERED"`
		#if there are no users for adding, skip this part
		if [ -n "$USERS" ]; then
			pts adduser -user $USERS -group $GROUP_NAME
			if [ $? -ne 0 ]; then
				MSG="Command failed: pts adduser -user \`grep -v -F -f $AFS_GROUP_USERS_FILE $USERS_FROM_PERUN_FILE \` -group $GROUP_NAME"
				echo "$MSG" >&2
				logger -t "${NAME}" -p daemon.error "${SERVICE}: ${MSG}" &>/dev/null
				ERROR=1
				continue
			fi
		fi

	done

	[ $ERROR -eq 0 ] || log_msg E_FINISHED_WITH_ERRORS

}
