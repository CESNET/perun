#!/bin/bash

# Get version and release from debian changelog
VERSION=`head -n 1 debian/changelog | sed -e 's/^perun-slave (\([0-9]*.[0-9]*.[0-9]*\)-\([0-9]*.[0-9]*.[0-9]*\)) stable; urgency=.*$/\1/'`
RELEASE=`head -n 1 debian/changelog | sed -e 's/^perun-slave (\([0-9]*.[0-9]*.[0-9]*\)-\([0-9]*.[0-9]*.[0-9]*\)) stable; urgency=.*$/\2/'`

TOPDIR=/tmp/perun-slave-rpm-build

mkdir -p ${TOPDIR}/{BUILD,RPMS,SOURCES,SPECS,SRPMS}

# create perun-slave.tar.gz
tar -czf ${TOPDIR}/SOURCES/perun-slave.tar.gz ../slave

cat > perun-slave.spec <<EOF
Summary: Perun slave scripts
Name: perun-slave
Version: $VERSION
Release: $RELEASE
License: Apache License
Group: Applications/System
BuildArch: noarch

Source0: perun-slave.tar.gz

BuildRoot: %{_tmppath}/%{name}-%{version}-root
%define perun_home /opt/perun/bin

%description
Perun slave scripts

%prep
%setup -q -nslave

%clean
rm -rf %{buildroot}

%install
mkdir -p %{buildroot}%{perun_home}
mkdir -p %{buildroot}%{perun_home}/ldap
install perun *.sh %{buildroot}%{perun_home}
install ldap/* %{buildroot}%{perun_home}/ldap

rsync -arvz *.d %{buildroot}%{perun_home} --exclude=".svn"

%files
%exclude %{perun_home}/prepare_rpm_spec_file.sh
%defattr(-,root,root)
%config(noreplace) %{perun_home}/*.d/*
%{perun_home}
EOF

rpmbuild --define "_topdir ${TOPDIR}" -ba perun-slave.spec

cp ${TOPDIR}/RPMS/noarch/*.rpm ../
rm -rf ${TOPDIR}

rm perun-slave.spec

#EOF
#
#for dir in `ls -d *.d`; do
#  echo install -d %{buildroot}%{perun_home}/$dir >> perun-slave.spec
#  echo install $dir/* %{buildroot}%{perun_home}/$dir/ >> perun-slave.spec
#done
#
#cat >> perun-slave.spec <<EOF

