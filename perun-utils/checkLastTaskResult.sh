#!/bin/bash

# define connection to Perun
export PERUN_URL=
export PERUN_USER=
export PERUN_COOKIE=/var/lib/nagios/.perun-nagios-cookie.txt
# export perl libs
PERL5LIB="/opt/perun-cli/lib/${PERL5LIB+:}${PERL5LIB}";
export PERL5LIB;
export PERL5LIB=$PERL5LIB":."

# run tool
/opt/perun-cli/bin/checkLastTaskResult "$@" 2>&1 || exit 2
