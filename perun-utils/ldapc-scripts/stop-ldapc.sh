#!/bin/bash

PIDFILE=/home/perun/perun-ldapc/pid

# Kill running ldapc
if [ -s $PIDFILE ]; then
  PID=`cat $PIDFILE`
  if [ -d "/proc/$PID" ]; then
    kill $PID
  fi
  rm $PIDFILE
fi
