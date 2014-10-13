#!/bin/bash

##########################################################
#                                                        #
# Author: Michal Karm Babacek <michal.babacek@gmail.com> #
#                                                        # 
# This script serves as a dummy for TaskExecutor tests   #
##########################################################

FACILITY=$2

#StdOut: Message from the remote machine.
echo "Message:Facility:$FACILITY"
sleep 1
echo "0"
#StdErr:
# 0 - DONE
# 1 - ERROR
echo Err message >&2
echo Return:0 >&2
