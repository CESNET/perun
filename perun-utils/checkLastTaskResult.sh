#!/bin/bash

# source ENGINE identity and API URL
. /etc/perun/perun-engine
# overwrite cookie to ours
export PERUN_COOKIE=/var/lib/nagios/.perun-nagios-cookie.txt
# run tool
/opt/perun-cli/bin/checkLastTaskResult "$@" 2>&1 || exit 2
