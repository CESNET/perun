#!/bin/bash

##########################################################
#                                                        #
# Author: Michal Karm Babacek <michal.babacek@gmail.com> #
#                                                        # 
# This script serves as a dummy for TaskExecutor tests   #
##########################################################

DESTINATION=$1

#StdOut: Message from the remote machine.
echo "Message:Destination:$DESTINATION"
sleep 1
echo "0"
#StdErr:
# 0 - DONE
# 1 - ERROR
# 2 - FATAL_ERROR
echo Err message >&2
echo Return:0 >&2
