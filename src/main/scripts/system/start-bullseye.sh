#!/bin/bash
#
. ./init-bullseye.sh

LOGS=/var/log/rosi

if [ -f ${LOGS}/pid.rosi ] ; then
  kill `cat ${LOGS}/pid.rosi` >/dev/null 2>/dev/null
  sleep 4
fi
nohup java -Duser.timezone="Europe/Berlin" -Xms500m -Xlog:gc:/var/log/rosi/gc.log  \
      org.rosi.execution.HomeSwitchboard      \
      /home/patrick/rosi/config/rosi.conf >${LOGS}/HomeSwitchboard.log 2>${LOGS}/HomeSwitchboard.elog &
ps -edf |grep "user.timezone" | grep -v grep | awk '{ print $2 }' >${LOGS}/pid.rosi
exit 0

