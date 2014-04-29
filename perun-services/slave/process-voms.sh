#!/bin/bash

PROTOCOL_VERSION='3.0.0'

function process {
	### Status codes
	I_CHANGED=(0 "${DST_FILE} updated")
	I_NOT_CHANGED=(0 "${DST_FILE} has not changed")

	### Error codes ###
	E_VOMS_DELETE=(50 'Error during VOMS delete-user operation')
	E_VOMS_CREATE=(50 'Error during VOMS create-user operation')

	FROM_PERUN="${WORK_DIR}/voms"

	create_lock

	VO_USERS=`mktemp`
	CURRENT_VO_USERS=`mktemp`
	CAS=`mktemp`

	# Get list of VOs
	VOS=`cat ${FROM_PERUN} | sed 's/^\([[:alnum:]_.-]*\)\t.*/\1/' | uniq`

	# Iterate through every VO and check which user will be added or removed
	echo -e "$VOS" | while read VO; do
		# Get users from the VO, VOMS doesn't accept emailAddress in the DN, it must be converted to Email
		cat ${FROM_PERUN} | grep -P "^${VO}\t.*" | sed 's/emailAddress/Email/' | sort > ${VO_USERS}

		# Get current users stored in VOMS and convert lines into PERUN format. VOMS also doesn't accept emailAddress in the DN, it must be converted to Email
		voms-admin --vo "${VO}" list-users | sed "s/\(.*\),\(.*\),\(.*\)/${VO}\t\1\t\2\t\3/" | sed 's/emailAddress/Email/' | sort > ${CURRENT_VO_USERS}

		# Get list of accepted CAs
		voms-admin --vo "${VO}" list-cas > ${CAS}

		# Check who should be deleted
		cat $CURRENT_VO_USERS | while read CURRENT_VO_USER; do
			if [ `grep -c "$CURRENT_VO_USER" $VO_USERS` -eq 0 ]; then
				# User is not in VO anymore, so remove him
				echo -e "$CURRENT_VO_USER" | while IFS=`echo -ne "\t"` read VO_SHORTNAME USER_DN CA_DN USER_EMAIL; do
					# Check if the user's certificate was issued by accepted CA
					if [ `grep -c "$CA_DN" $CAS` -gt 0 ]; then
						voms-admin --nousercert --vo "${VO_SHORTNAME}" delete-user "$USER_DN" "$CA_DN"
						logger Perun:VOMS user $USER_DN - $CA_DN removed from ${VO_SHORTNAME}
					fi
				done

			fi
		done

		####
		# Check who should be added
		cat $VO_USERS | while read VO_USER; do
			if  [ `grep -c "$VO_USER" $CURRENT_VO_USERS` -eq 0 ]; then
				# New user comming, so add him to the VO
				echo -e "$VO_USER" | while IFS=`echo -ne "\t"` read VO_SHORTNAME USER_DN CA_DN USER_EMAIL; do
					# Check if the user's certificate was issued by accepted CA
					if [ `grep -c "$CA_DN" $CAS` -gt 0 ]; then
						USER_CN=`echo $USER_DN | sed 's|^.*\/CN=\([^/]*\).*|\1|'`
						voms-admin --nousercert --vo "${VO_SHORTNAME}" create-user "$USER_DN" "$CA_DN" "$USER_CN" "$USER_EMAIL"
						logger Perun:VOMS user $USER_DN - $CA_DN added to ${VO_SHORTNAME}
					fi
				done
			fi
		done
	done

	rm -f $VO_USERS
	rm -f $CURRENT_VO_USERS
	rm -f $CAS
}
