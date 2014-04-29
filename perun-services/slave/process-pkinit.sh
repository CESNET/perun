#!/bin/bash

PROTOCOL_VERSION='3.0.0'

function process {
	DEST=/var/lib/heimdal-kdc
	VAR_RUN=/var/run/

	for FROM_PERUN in `ls ${WORK_DIR}/pkinit.*`; do
		REALM=`head -n1 $FROM_PERUN`
		KDC_DIR=$DEST/$REALM

		REALM_LC=`echo $REALM | awk '{print tolower($0)}'`

		tail -n +2 $FROM_PERUN/pkinit.$REALM_LC $KDC_DIR/pki-mapping
		chmod 0644 $KDC_DIR/pki-mapping
	done

	# Reload KDC
	cd $VAR_RUN
	kill -HUP `cat $VAR_RUN/heimdal-kdc.pid`
}

