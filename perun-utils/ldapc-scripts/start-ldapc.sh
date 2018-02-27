#!/bin/bash

LAST_PROCESSED_ID=$1

PIDFILE=/home/perun/perun-ldapc/pid
JAR=/home/perun/perun-ldapc/perun-ldapc-3.0.1-SNAPSHOT-production.jar
LOGFILE=/var/log/perun/perun-ldapc-out.log

#we don't want to use default kerberos ticket (for user perun-engine)
KRB5CCNAME="/dev/null"
export KRB5CCNAME

# Kill running ldapc
if [ -s $PIDFILE ]; then
  PID=`cat $PIDFILE`
  if [ -d "/proc/$PID" ]; then
    kill $PID
  fi
fi

cd /home/perun/perun-ldapc

if [ $LAST_PROCESSED_ID ]; then
  java -jar $JAR $LAST_PROCESSED_ID >> $LOGFILE 2>&1  &
else
  java -jar $JAR >> $LOGFILE 2>&1  &
fi

# Save the PID
echo $! > $PIDFILE
