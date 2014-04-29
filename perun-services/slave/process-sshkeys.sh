#!/bin/bash

PROTOCOL_VERSION='3.1.0'


function process {
	### Status codes

	I_CHANGED=(0 "${SSH_AUTHORIZED_KEYS} updated")
	I_NOT_CHANGED=(0 "${SSH_AUTHORIZED_KEYS} has not changed")


	E_NO_HOME=(50 'Cannot found home for user ${USER}.')
	E_CREATE_SSH_DIR=(51 'Cannot create $SSH_DIR')
	E_MERGE_SSH_KEYS=(52 'Cannot merge ssh keys')
	E_IO=(53 'IO error')

	E_DIR_NOT_EXISTS=(50 'Home directory ${HOME_DIR} does not exits.')

	FROM_PERUN_DIR="${WORK_DIR}/sshkeys/"
	TMP_FILE="${WORK_DIR}/tmp"

	create_lock


	for USER in `ls $FROM_PERUN_DIR` ; do
		FROM_PERUN_AUTHORIZED_KEYS="$FROM_PERUN_DIR/$USER"

		HOME_DIR=`eval "echo ~$USER"`
		catch_error E_NO_HOME [ -d "$HOME_DIR" ]

		SSH_DIR="$HOME_DIR/.ssh/"
		SSH_AUTHORIZED_KEYS="$SSH_DIR/authorized_keys"

		catch_error E_CREATE_SSH_DIR [ -d "$SSH_DIR" ] || mkdir "$SSH_DIR"

		if [ -f "$SSH_AUTHORIZED_KEYS" ]; then
			catch_error E_MERGE_SSH_KEYS sort -u "$SSH_AUTHORIZED_KEYS" "$FROM_PERUN_AUTHORIZED_KEYS" > "$TMP_FILE"
			catch_error E_IO mv "$TMP_FILE" "$FROM_PERUN_AUTHORIZED_KEYS"
		fi

		diff_mv "$FROM_PERUN_AUTHORIZED_KEYS" "$SSH_AUTHORIZED_KEYS" && log_msg I_CHANGED || log_msg I_NOT_CHANGED

	done
}
