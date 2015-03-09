#!/bin/bash

SRC_PATH=../slave
DST_PATH=/opt/perun/bin

VERSION=`head -n 1 $SRC_PATH/debian/changelog | sed -e 's/^perun-slave (\([0-9]*.[0-9]*.[0-9]*\)-\([0-9]*.[0-9]*.[0-9]*\)) stable; urgency=.*$/\1/'`
RELEASE=`head -n 1 $SRC_PATH/debian/changelog | sed -e 's/^perun-slave (\([0-9]*.[0-9]*.[0-9]*\)-\([0-9]*.[0-9]*.[0-9]*\)) stable; urgency=.*$/\2/'`

# will be filled by all available services
META_DEPS="-d 'perun-slave-base >= $VERSION.$RELEASE'";

# PACKAGE MAIN SCRIPT

if [[ $1 == "deb" ]]; then
	fpm -s dir -t deb -n perun-slave-base -v $VERSION.$RELEASE -a all --deb-meta-file ../slave/debian/ --description 'Main slave script, which is used to start service configuration on your machine by Perun.' -m 'Pavel Zlámal <zlamal@cesnet.cz>' --vendor 'CESNET z.s.p.o.' --license 'MIT' -f -d 'bash >= 2.05a-11' -d 'sed >= 3.02-8' -d 'grep >= 2.4.2.-3' -d 'coreutils >= 5.0-5' -d 'perl-base' --deb-priority 'optional' --url 'http://perun.cesnet.cz/web/' --deb-changelog $SRC_PATH/debian/changelog -C $SRC_PATH ./perun=$DST_PATH/perun
fi

if [[ $1 == "rpm" ]]; then
	fpm -s dir -t rpm -n perun-slave-base -v $VERSION.$RELEASE -a noarch --description 'Main slave script, which is used to start service configuration on your machine by Perun.' -m 'Pavel Zlámal <zlamal@cesnet.cz>' --vendor 'CESNET z.s.p.o.' --license 'MIT' -f -d 'bash >= 2.05a-11' -d 'sed >= 3.02-8' -d 'grep >= 2.4.2.-3' -d 'coreutils >= 5.0-5' -d 'perl' --url 'http://perun.cesnet.cz/web/' -C $SRC_PATH ./perun=$DST_PATH/perun
fi

# PACKAGE EACH SERVICE

for ENTRY in "$SRC_PATH"/*
do
	INDEX=`expr index "$ENTRY" -`
	SERVICE=${ENTRY:$INDEX: -3}
	PACKAGE_NAME="perun-slave-process-$SERVICE"
	if [[ $ENTRY == $SRC_PATH/process-*.sh ]]; then

		SRV_DIR="$SRC_PATH/$SERVICE"
		SRV_D_DIR="$SRV_DIR.d"

		# package also [service-name] and [service-name.d] folder but only if present
		FPM_SRV_FOLDER="$SRV_DIR=$DST_PATH/"
		FPM_SRV_FOLDER_D="$SRV_D_DIR=$DST_PATH/"

		if [ ! -d "$SRV_DIR" ]; then
			FPM_SRV_FOLDER=""
		fi

		if [ ! -d "$SRV_D_DIR" ]; then
			FPM_SRV_FOLDER_D=""
		fi

		if [[ $1 == "deb" ]]; then
			fpm -s dir -t deb -n $PACKAGE_NAME -v $VERSION.$RELEASE -a all --deb-meta-file ../slave/debian/ --description "Perun slave script, used to manage $SERVICE service on your machine by Perun." -m 'Pavel Zlámal <zlamal@cesnet.cz>' --vendor 'CESNET z.s.p.o.' --license 'MIT' -f -d "perun-slave-base >= $VERSION.$RELEASE" --deb-priority 'optional' --url 'http://perun.cesnet.cz/web/' --deb-changelog $SRC_PATH/debian/changelog $ENTRY=$DST_PATH/process-$SERVICE.sh $FPM_SRV_FOLDER $FPM_SRV_FOLDER_D
			META_DEPS="$META_DEPS -d '$PACKAGE_NAME => $VERSION.$RELEASE' "
		fi

		if [[ $1 == "rpm" ]]; then
			fpm -s dir -t rpm -n $PACKAGE_NAME -v $VERSION.$RELEASE -a noarch --description "Perun slave script, used to manage $SERVICE service on your machine by Perun." -m 'Pavel Zlámal <zlamal@cesnet.cz>' --vendor 'CESNET z.s.p.o.' --license 'MIT' -f -d "perun-slave-base >= $VERSION.$RELEASE" --url 'http://perun.cesnet.cz/web/' $ENTRY=$DST_PATH/process-$SERVICE.sh $FPM_SRV_FOLDER $FPM_SRV_FOLDER_D
		fi

	fi
done

# PACKAGE META PACKAGE
if [[ $1 == "deb" ]]; then
	eval "fpm -s empty -t deb -n 'perun-slave-full' -v $VERSION.$RELEASE -a all --deb-meta-file ../slave/debian/ --description 'Metapackage of Perun slave scripts, used to manage all kind of services on your machine by Perun. This package install support for all services.' -m 'Pavel Zlámal <zlamal@cesnet.cz>' --vendor 'CESNET z.s.p.o.' --license 'MIT' -f $META_DEPS --deb-priority 'optional' --url 'http://perun.cesnet.cz/web/' --deb-changelog $SRC_PATH/debian/changelog"
fi

if [[ $1 == "rpm" ]]; then
	eval "fpm -s empty -t rpm -n 'perun-slave-full' -v $VERSION.$RELEASE -a noarch --description 'Metapackage of Perun slave scripts, used to manage all kind of services on your machine by Perun. This package install support for all services.' -m 'Pavel Zlámal <zlamal@cesnet.cz>' --vendor 'CESNET z.s.p.o.' --license 'MIT' -f $META_DEPS --url 'http://perun.cesnet.cz/web/'"
fi