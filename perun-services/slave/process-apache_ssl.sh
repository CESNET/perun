#!/bin/bash

PROTOCOL_VERSION='3.0.0'

FILENAME="authz"

function process {
	### Status codes
	I_CHANGED=(0 '${DST_FILE} updated')
	I_NOT_CHANGED=(0 '${DST_FILE} has not changed')
	I_APACHE_RELOAD=(0 'reloading apache')
	E_WRONG_CONFIG=(1 'syntax error in configuration file')

	FROM_PERUN_DIR="${WORK_DIR}/apache_ssl/"

	create_lock

	RELOAD_APACHE=0
	for DIR in `ls -d $FROM_PERUN_DIR/*/`; do
		DST_FILE=`cat $DIR/path`
		SRC_FILE="$DIR/$FILENAME"
		if diff_mv "$SRC_FILE" "$DST_FILE" ;then
			RELOAD_APACHE=1
			log_msg I_CHANGED
		else
			log_msg I_NOT_CHANGED
		fi
	done

	if [ "$RELOAD_APACHE" -eq "1" ]; then
		catch_error E_WRONG_CONFIG /usr/sbin/apache2ctl configtest
		log_msg I_APACHE_RELOAD
		/etc/init.d/apache2 reload
	fi
}
