#!/bin/bash
PROTOCOL_VERSION='3.1.0'

function process {

	E_FINISHED_WITH_ERRORS=(50 'AFS slave script finished with errors')

	FROM_PERUN="${WORK_DIR}/afs"
	ERROR=0

	create_lock

	VOLUMES_TO_RELEASE="" # release at the end of the script.  SYNTAX: volume:cell\nvol2:cel2\n

	while IFS=`echo -e "\t"` read AFS_SERVER AFS_CELL AFS_PARTITION AFS_DEFAULT_USERS_REALM AFS_USERS_MOUNT_POINT AFS_USERS_VOLUME AFS_VOLUME USER_LOGIN USER_QUOTA TARGET_AFS_CELL; do

		#pts listentries -users -cell "$AFS_CELL" | grep -q -F "$USER_LOGIN@$AFS_DEFAULT_USERS_REALM"
		OUT=`pts examine -nameorid "$USER_LOGIN@$AFS_DEFAULT_USERS_REALM" -cell "$AFS_CELL" 2>&1 >/dev/null`

		if [ $? -ne 0 ]; then
			if `echo $OUT | grep -q "pts: User or group doesn't exist so couldn't look up id for "`; then
				pts createuser -name "$USER_LOGIN@$AFS_DEFAULT_USERS_REALM" -cell "$AFS_CELL"
				if [ $? -ne 0 ]; then
					echo "Command failed: pts createuser -name $AFS_DEFAULT_USERS_REALM -cell $AFS_CELL" >&2
					ERROR=1
				fi
			else
				echo $OUT >&2
				echo "Command failed: pts examine -nameorid "$USER_LOGIN@$AFS_DEFAULT_USERS_REALM" -cell "$AFS_CELL"  >/dev/null" >&2
			fi
		fi

		[ "$TARGET_AFS_CELL" = 'ics.muni.cz' -o $TARGET_AFS_CELL = 'ruk.cuni.cz' ] || continue;

		OUT=`vos exa -id "$AFS_USERS_VOLUME.$USER_LOGIN" -cell "$AFS_CELL" 2>&1 >/dev/null`
		if [ $? -ne 0 ]; then
			if [ "$OUT" = "VLDB: no such entry" ]; then
				vos create -server "$AFS_SERVER" -partition "$AFS_PARTITION" -name "$AFS_USERS_VOLUME.$USER_LOGIN" -maxquota "$USER_QUOTA" -cell "$AFS_CELL"
				[ $? -eq 0 ] || echo "Command failed: vos create -server $AFS_SERVER -partition $AFS_PARTITION -name $AFS_USERS_VOLUME.$USER_LOGIN -maxquota $USER_QUOTA -cell $AFS_CELL" >&2
			else
				echo $OUT >&2
				echo "Command failed: vos exa -id $AFS_USERS_VOLUME.$USER_LOGIN -cell $AFS_CELL  >/dev/null" >&2
			fi
		fi


		if [ ! -d "/afs/.$AFS_CELL/$AFS_USERS_MOUNT_POINT/$USER_LOGIN" ]; then
			DIR="/afs/.$AFS_CELL/$AFS_USERS_MOUNT_POINT/$USER_LOGIN"
			OUT=`fs lsmount -dir "$DIR" 2>&1 >/dev/null`
			if [ $? -ne 0 ]; then
				if [ "$OUT" = "fs: File '$DIR' doesn't exist" ]; then
					fs mkmount -dir "$DIR" -vol "$AFS_USERS_MOUNT_POINT.$USER_LOGIN" -cell "$AFS_CELL"
					if [ $? -ne 0 ]; then
						echo "Command failed: fs mkmount -dir $DIR -vol $AFS_USERS_MOUNT_POINT.$USER_LOGIN -cell $AFS_CELL" >&2
						ERROR=1
					else
						#add volume:cell to VOLUMES_TO_RELEASE if it doesn't containt then yet
						echo $VOLUMES_TO_RELEASE | grep -q '\(^\|\\n\)'"$AFS_VOLUME:$AFS_CELL"'\($\|\\n\)' || VOLUMES_TO_RELEASE="$VOLUMES_TO_RELEASE$AFS_VOLUME:$AFS_CELL\\n"
					fi
					# Set user's rights
					fs sa -dir $DIR -acl $USER_LOGIN@$AFS_DEFAULT_USERS_REALM all
					if [ $? -ne 0 ]; then
						echo "Command failed: fs sa -dir $DIR -acl $USER_LOGIN@$AFS_DEFAULT_USERS_REALM all" >&2
						ERROR=1
					fi
					# Set root's rights
					fs sa -dir $DIR -acl roots rl
					if [ $? -ne 0 ]; then
						echo "Command failed: fs sa -dir $DIR -acl roots rl" >&2
						ERROR=1
					fi
				else
					echo $OUT >&2
					echo "Command failed: fs lsmount -dir "$DIR" >/dev/null" >&2
				fi
			fi
		fi


		fs sq -path "/afs/.$AFS_CELL/$AFS_USERS_MOUNT_POINT/$USER_LOGIN" -max "$USER_QUOTA"
		if [ $? -ne 0 ]; then
			echo "Command failed: fs sq -path /afs/.$AFS_CELL/$AFS_USERS_MOUNT_POINT/$USER_LOGIN -max $USER_QUOTA" >&2
			ERROR=1
		fi


	done < "${FROM_PERUN}"


	#Release the volumes
	echo -e -n "$VOLUMES_TO_RELEASE" | while IFS=":" read VOL CELL ; do
	vos rel "$VOL" -cell "$CELL" >/dev/null
	if [ $? -ne 0 ]; then
		echo "Command failed: vos rel $VOL -cell $CELL" >&2
		ERROR=1
	fi
			done

			[ $ERROR -eq 0 ] || log_msg E_FINISHED_WITH_ERRORS

}
