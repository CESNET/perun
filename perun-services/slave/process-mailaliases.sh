#!/bin/bash

PROTOCOL_VERSION='3.0.0'

### Status codes
I_CHANGED=(0 'Aliases updated')
I_NOT_CHANGED=(0 'Aliases has not changed')
RESET_ETC_ALIASES=(0 'Old perun settings in /etc/aliases was reseted from /etc/aliases.old.perun')
NOT_RESET_ETC_ALIASES=(0 'Old perun settings in /etc/aliases was not reseted.')
RESET_ETC_MAIL_ALIASES=(0 'Old perun settings in /etc/mail/aliases was reseted from /etc/aliases.old.perun')
NOT_RESET_ETC_MAIL_ALIASES=(0 'Old perun settings in /etc/mail/aliases was not reseted')
ETC_OLD_PERUN_FILE=(0 'Old perun file /etc/aliases.old.perun was deleted')
ETC_MAIL_OLD_PERUN_FILE=(0 'Old perun file /etc/mail/aliases.old.perun was deleted')
E_CHANGEMODE=(50, '${DST_FILE} mode bits cannot be changed')
E_NEWALIASES=(51 'Command newaliases failed')
E_DUPLICITS=(52 'Duplicits: "\"${DUPLICITS}\"" have been found.')
E_BACKUP_SAVE=(53 'Failed while saving backup files')
E_BACKUP_RECOVER=(54 'Failed while recovering backup files')
E_NOTEXISTING_ALIASES=(55 '/etc/aliases and /etc/mail/aliases not exist or both are symlinks')
E_BOTH_ALIASES_EXIST=(56 'Both files /etc/aliases and /etc/mail/aliases exist and they are not symlinks')

### Backup files
DST_BACKUP="/tmp/perun_dst_backup"
STATE_BACKUP="/tmp/perun_state_backup"
ALIASESD_BACKUP="/tmp/perun_aliasesd_backup"

ETC_ALIASES="/etc/aliases"
ETC_MAIL_ALIASES="/etc/mail/aliases"
FROM_PERUN="${WORK_DIR}/mailaliases"
LAST_STATE="/etc/aliases.perun-added"
ALIASESD_PERUN="/etc/aliases.d/perun"

function process {
	### Create lock
	create_lock

	### Looking which file with aliases exists
	if [ -f "${ETC_ALIASES}" -a ! -h "${ETC_ALIASES}" ]; then
		DST_FILE="${ETC_ALIASES}"
		### If both aliases exist and they are not symlinks, exit with error
		if [ -f "${ETC_MAIL_ALIASES}" -a ! -h "${ETC_MAIL_ALIASES}" ]; then
			log_msg E_BOTH_ALIASES_EXIST
		fi
	elif [ -f "${ETC_MAIL_ALIASES}" -a ! -h "${ETC_MAIL_ALIASES}" ]; then
		DST_FILE="${ETC_MAIL_ALIASES}"
	else
		log_msg E_NOTEXISTING_ALIASES
	fi

	### Removing old settings of perun if exist
	reset_settings_and_remove_old_perun_file

	### If last_state file not exists, create new empty one
	if [ ! -f "${LAST_STATE}" ]; then
		touch "${LAST_STATE}"
		chmod 644 "${LAST_STATE}"
	fi

	### Save backup files
	remove_backup_files
	save_backup_files

	### If there exists folder aliases.d, try to find conflicts
	ETC_ALIASESD="/etc/aliases.d/"

	if [ -d "$ETC_ALIASESD" ]; then
		### if aliases.d exists and last_state file exists, remove last_state from DST file
		if [ -f "${LAST_STATE}" ]; then
			diff_update "/dev/null" "${DST_FILE}" "${LAST_STATE}"
			rm -f "${LAST_STATE}"
		fi
		### Try to find duplicits in all files
		ALIASESD_FILES=`ls -d "$ETC_ALIASESD"/* | grep -v 'perun'`
		recover_if_duplicits_found "${FROM_PERUN}" "${DST_FILE}" $ALIASESD_FILES
		### If no duplicits found, move perun file to the destination
		if [ ! -f "${ALIASESD_PERUN}" ]; then
			touch "${ALIASESD_PERUN}"
			chmod 644 "${ALIASESD_PERUN}"
		fi
		diff_mv "${FROM_PERUN}" "${ALIASESD_PERUN}" && log_msg I_CHANGED || log_msg I_NOT_CHANGED
		### If there not exists folder aliases.d, use DST_FILE
	else
		diff_update "${FROM_PERUN}" "${DST_FILE}" "${LAST_STATE}" && log_msg I_CHANGED || log_msg I_NOT_CHANGED
		recover_if_duplicits_found "${DST_FILE}"
	fi

	### In the end, try to call newaliases
	newaliases
	if [ $? -ne 0 ]; then
		recover_and_remove_backup_files
		log_msg E_NEWALIASES
	fi
}

# This function looking for any non comment duplicit names of aliases 'name:email'
# If any duplicit found, call recover_and_remove_backup_files and end with error
# Return 0 if no duplicit found
function recover_if_duplicits_found {
	FILES="$@"

	DUPLICITS=`sed -e 's/^\s*#.*//g' -e 's/\s\+//g' -e 's/:.*$//g' ${FILES} | sort | uniq -d`
	if [ -n "${DUPLICITS}" ]; then
		recover_and_remove_backup_files
		log_msg E_DUPLICITS
	fi

	return 0
}

# Try to save all needed files for backup
# DST_FILE, LAST_STATE_FILE and ALIASESD_PERUN_FILE
# Only DST_FILE must exist, other files not need to
# Return 0 if everything is correct
function save_backup_files {
	### DST_FILE must exist
	if [ -f "${DST_FILE}" ]; then
		catch_error E_BACKUP_SAVE cp -p "${DST_FILE}" "${DST_BACKUP}"
	else
		log_msg E_BACKUP_SAVE
	fi

	### LAST_STATE file not need to exist
	if [ -f "${LAST_STATE}" ]; then
		catch_error E_BACKUP_SAVE cp -p "${LAST_STATE}" "${STATE_BACKUP}"
	fi

	### ALIASESD_PERUN file not need to exist
	if [ -f "${ALIASESD_PERUN}" ]; then
		catch_error E_BACKUP_SAVE cp -p "${ALIASESD_PERUN}" "${ALIASESD_BACKUP}"
	fi

	return 0
}

# Try to recover files from backup files
# DST_FILE, LAST_STATE_FILE and ALIEASESD_PERUN_FILE
# When recovering, DST_FILE_BACKUP must exist, other not need to
# Return 0 if everything is correct
function recover_backup_files {
	### DST_BACKUP must exist
	if [ -f "${DST_BACKUP}" ]; then
		diff_mv "${DST_BACKUP}" "${DST_FILE}"
	else
		log_msg E_BACKUP_RECOVER
	fi

	### STATE_BACKUP not need to exist
	if [ -f "${STATE_BACKUP}" ]; then
		diff_mv "${STATE_BACKUP}" "${LAST_STATE}"
	fi

	### ALIASESD_BACKUP not need to exist
	if [ -f "${ALIASESD_BACKUP}" ]; then
		diff_mv "${ALIASESD_BACKUP}" "${ALIASESD_PERUN}"
	fi

	return 0
}

# Try to remove backupfiles
# return 0 if files were removed, even if some files not exist
function remove_backup_files {
	rm -f "${DST_BACKUP}" "${STATE_BACKUP}" "${ALIASESD_BACKUP}"
	return 0
}

# Call methods recover_backup_files and remove_backup_files
function recover_and_remove_backup_files {
	recover_backup_files
	remove_backup_files
}

# This function is temporary for removing old perun settings
function reset_settings_and_remove_old_perun_file {
	OLD_PERUN_ETC="${ETC_ALIASES}.old.perun"
	OLD_PERUN_ETC_MAIL="${ETC_MAIL_ALIASES}.old.perun"

	### Looking for /etc/aliases.old.perun file
	if [ -f "${OLD_PERUN_ETC}" ]; then
		if [ -f "${ETC_ALIASES}" -a ! -h "${ETC_ALIASES}" ]; then
			diff_update "/dev/null" "${ETC_ALIASES}" "${OLD_PERUN_ETC}" && log_msg RESET_ETC_ALIASES || log_msg NOT_RESET_ETC_ALIASES
		fi
		rm -f "${OLD_PERUN_ETC}"
		log_msg ETC_OLD_PERUN_FILE
	fi

	### Looking for /etc/mail/aliases.old.perun file
	if [ -f "${OLD_PERUN_ETC_MAIL}" ]; then
		if [ -f "${ETC_MAIL_ALIASES}" -a ! -h "${ETC_MAIL_ALIASES}" ]; then
			diff_update "/dev/null" "${ETC_MAIL_ALIASES}" "${OLD_PERUN_ETC_MAIL}" && log_msg RESET_ETC_MAIL_ALIASES || log_msg NOT_RESET_ETC_MAIL_ALIASES
		fi
		rm -f "${OLD_PERUN_ETC_MAIL}"
		log_msg ETC_MAIL_OLD_PERUN_FILE
	fi

	return 0
}
