#!/bin/bash

PROTOCOL_VERSION='3.0.0'


function process {
	DST_FILE="/etc/group-nfs4"

	### Status codes
	I_CHANGED=(0 "${DST_FILE} updated")
	I_NOT_CHANGED=(0 "${DST_FILE} has not changed")
	I_PREVIOUSLY_ADDED_USERS_CHANGED=(0 '${PREVIOUSLY_ADDED_USERS} updated')
	I_PREVIOUSLY_ADDED_USERS_NOT_CHANGED=(0 '${PREVIOUSLY_ADDED_USERS} has not changed')


	E_GROUP_WRONG_MIN_GID=(51 'Invalid min_gid parameter')
	E_GROUP_WRONG_MAX_GID=(52 'Invalid max_gid parameter')
	E_GROUP_MERGE=(53 'Error during group file merge')
	E_GROUP_DUPLICATES=(54 'Group names in group file are not uniq: ${DUPLICATE_GROUP_NAMES}')
	#E_GROUP_NO_ROOT=(55 'Missing group "root"')
	E_CREATE_FILE=(56 'Cannot create /etc/group_nfs4')
	E_CHMOD=(57 'Cannot chmod on /etc/group_nfs4')


	FROM_PERUN="${WORK_DIR}/group_nfs4"

	OLD_GROUP="$DST_FILE"
	PREVIOUSLY_ADDED_USERS='/etc/group-nfs4.perun-added-system-users'
	NOW_ADDED_USERS="${WORK_DIR}/group-nfs4.perun-added-system-users"
	#NEW_GROUP="/etc/passwd.new" # file must be on same mountpoint for atomic switch
	NEW_GROUP="${WORK_DIR}/group.new"
	MIN_PERUN_GID=`head -n 1 "${WORK_DIR}/min_gid"`
	MAX_PERUN_GID=`head -n 1 "${WORK_DIR}/max_gid"`
	[ "${MIN_PERUN_GID}" -gt 0 ] || log_msg E_GROUP_WRONG_MIN_GID
	[ "${MAX_PERUN_GID}" -gt 0 ] || log_msg E_GROUP_WRONG_MAX_GID

	create_lock


	#This skripts works in 3 steps:
	# 1. remove groups which are from perun's namepsace from /etc/group (first param)
	# 2. remove all previsously added users from system groups (file PREVIOUSLY_ADDED_USERS)
	# 3. adds groups received from perun
	#       Prints users added to system groups to NOW_ADDED_USERS file
	#
	#
	# usage:  perl -e ${PERL_UPDATE_GROUP} "${OLD_GROUP}" ${FROM_PERUN} >"${NEW_GROUP}"
	#
	# OLD_GROUP - /etc/group
	# FROM_PERUN - group file received from perun
	# prints new group file to STDOUT
	PERL_UPDATE_GROUP_SCRIPT='
	# Script suppose that it get uniqe group names from perun.
	###########################################################

	local $" = ",";
	local $, = ":";
	local $\ = "\n";


	my %groups;
	my %gids;
	open GROUP, shift @ARGV or die "Cannot open file: $!";
	foreach (<GROUP>) {
	chomp;
	next unless $_; #skip empty lines
	my ($group, $password, $gid, $users) = split ":";
	next unless ($gid < '$MIN_PERUN_GID' || $gid > '$MAX_PERUN_GID');
	my %users = map {$_ => 1 }  split ",", $users;
	if(defined $groups{$group}) { die "Duplicate group name: $group\n"; }
		if(defined $gids{$gid}) { die "Duplicate group GID: $gid\n"; }
			$groups{$group} = {
			password => $password,
			gid => $gid,
			users => \%users
		};
		$gids{$gid} = 1;
	}
	close GROUP;

	#delete previously added users from system groups
	if( -e "'$PREVIOUSLY_ADDED_USERS'" ) {
		open PREVIOUSLY_ADDED, "<", "'$PREVIOUSLY_ADDED_USERS'" or die "Cant open '$PREVIOUSLY_ADDED_USERS': $!";
		while(<PREVIOUSLY_ADDED>) {
			chomp;
			next unless $_; #skip empty lines
			my ($group, $password, $gid, $users) = split ":";
			if(defined $groups{$group}) {
				if($groups{$group}->{"gid"} != $gid) { die "Consistency error: Gid of group $group changed since last update from $groups{$group}->{qw(gid)} to $gid"; }
					delete @{$groups{$group}->{"users"}}{split ",", $users};
					unless(%{$groups{$group}->{"users"}})  { delete $groups{$group}; delete $gids{$gid};  }
				}
			}
			close PREVIOUSLY_ADDED;
		}


		open NOW_ADDED, ">", "'$NOW_ADDED_USERS'" or die "Cant open '$NOW_ADDED_USERS': $!";
		open FROM_PERUN, shift @ARGV || die "Missing argument." or die "Cant open '$NOW_ADDED_USERS': $!";
		while(<FROM_PERUN>) {
			chomp;
			next unless $_; #skip empty lines
			my ($group, $password, $gid, $users) = split ":";
			my %users = map {$_ => 1 }  split ",", $users;
			unless(defined $groups{$group}) {
			if(defined $gids{$gid}) { die "Duplicate group GID: $gid\n" };
				$groups{$group} = {
				password => $password,
				gid => $gid,
				users => \%users
			};
			$gids{$gid} = 1;
			if ($gid < '$MIN_PERUN_GID' || $gid > '$MAX_PERUN_GID') {
				my @addUsers = keys %users;
				print NOW_ADDED $group, $password, $gid, "@addUsers";
			}
		} else {
		if($gid != $groups{$group}->{qw(gid)}) { die "Group with same name have different gids. Name $group   group GIDs: $gid $groups{$group}->{qw(gid)}\n"; }
			my @addUsers = grep { ! exists $groups{$group}->{"users"}->{$_} } keys %users;
			print NOW_ADDED $group, $password, $gid, "@addUsers";
			@{$groups{$group}->{"users"}}{keys %users} = values %users;  #merge %users and %{$groups{$group}->{qw(users)}} into %{$groups{$group}->{"users"}}
		}
	}
	close NOW_ADDED;
	close FROM_PERUN;

	for my $group (sort { $groups{$a}->{qw(gid)} <=> $groups{$b}->{qw(gid)} } keys %groups) {
		my @users = sort keys %{$groups{$group}->{"users"}};
		print $group, $groups{$group}->{"password"}, $groups{$group}->{"gid"}, "@users";
	}
	'

	#if OLD_GROUP doesn't exist, create it
	if [ ! -e "$OLD_GROUP" ]; then
		catch_error E_CREATE_FILE touch "$OLD_GROUP"
		catch_error E_CHMOD chmod 0755 "$OLD_GROUP"
	fi

	catch_error E_GROUP_MERGE perl -e "${PERL_UPDATE_GROUP_SCRIPT}" "${OLD_GROUP}" ${FROM_PERUN} >"${NEW_GROUP}"

	# check for duplicate group names in /etc/group_nfs4
	DUPLICATE_GROUP_NAMES=`cut -d: -f1 "${NEW_GROUP}" | sort | uniq -d`
	[ "x${DUPLICATE_GROUP_NAMES}" == 'x' ] || log_msg E_GROUP_DUPLICATES

	# check for duplicate group GIDs in new /etc/group_nfs4
	DUPLICATE_GROUP_GIDS=`cut -d: -f3 "${NEW_GROUP}" | sort | uniq -d`
	[ "x${DUPLICATE_GROUP_GIDS}" == 'x' ] || log_msg E_GID_DUPLICATES

	diff_mv "${NOW_ADDED_USERS}" "${PREVIOUSLY_ADDED_USERS}" \
		&& log_msg I_PREVIOUSLY_ADDED_USERS_CHANGED \
		|| log_msg I_PREVIOUSLY_ADDED_USERS_NOT_CHANGED

	# move only in case there are any changes
	diff_mv "${NEW_GROUP}" "${DST_FILE}" \
		&& log_msg I_CHANGED \
		|| log_msg I_NOT_CHANGED
}
