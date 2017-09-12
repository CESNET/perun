#!/bin/bash

source /etc/perun/perun-engine
KRB5CCNAME=/tmp/krb5cc_perun-engine-nagios
/opt/perun-cli/bin/checkLastTaskResult $* 2>&1 || exit 2
