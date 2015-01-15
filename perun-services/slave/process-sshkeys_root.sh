#!/bin/bash

PROTOCOL_VERSION='3.0.0'


function process {
	DST_FILE="/root/.ssh/authorized_keys"

	### Status codes
	I_CHANGED=(0 "${DST_FILE} updated")
	I_NOT_CHANGED=(0 "${DST_FILE} has not changed")
	E_CHOWN=(50 'Cannot chown on ${FROM_PERUN}')
	E_CHMOD=(51 'Cannot chmod on ${FROM_PERUN}')

	FROM_PERUN="${WORK_DIR}/sshkeys_root"

	# Add perun ssh keys
	echo -e "\nfrom=\"perun.ics.muni.cz\",command=\"/opt/perun/bin/perun\" ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC26+QiDtZ3bnLiLllySgsImSPUX0/sFBmo//3PmqOsuJIBdWB5BLU5Ws+pTRxefqC8SHfI92ZQoGXe7aJniTXxbRPa0FZJ3fskAHwpbiJfstGVZ1hddBcHIvial3v5Rd++zRiKslDVTkXLlb+b1pTnjyTVbD/6kGILgnUz7RKY5DnXADVnmTdPliQCabhE41AhkWdcuWpHBNwvxONKoZJJpbuouDbcviX4lJu9TF9Ij62rZjcoNzg5/JiIKTcMVi8L04FTjyCMxKRzlo00IjSuapFnXQNNZUL5u/mfPA/HpyIkSAOiPXLhWy9UuBNo7xdrCmfTh1qUvzbuWXJZN3d9 perunv3@perun.ics.muni.cz" >> $FROM_PERUN

	create_lock

	# Destination file doesn't exist
	if [ ! -f ${DST_FILE} ]; then
		catch_error E_CHOWN chown root.root $FROM_PERUN
		catch_error E_CHMOD chmod 0644 $FROM_PERUN
	fi

	# Create diff between old.perun and .new
	diff_mv "${FROM_PERUN}" "${DST_FILE}"

	if [ $? -eq 0 ]; then
		log_msg I_CHANGED
	else
		log_msg I_NOT_CHANGED
	fi
}
