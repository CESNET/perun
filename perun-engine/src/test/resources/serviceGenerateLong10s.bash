#!/bin/bash

##########################################################
#                                                        #
# Author: Michal Karm Babacek <michal.babacek@gmail.com> #
#                                                        # 
# This script serves as a dummy for TaskExecutor tests   #
##########################################################

FACILITY=$2
sleep 10
#StdOut: Message from the remote machine.
echo "Message:Facility:$FACILITY"

echo "0"
#StdErr:
# 0 - DONE
# 1 - ERROR
echo Err message >&2
echo Return:0 >&2
