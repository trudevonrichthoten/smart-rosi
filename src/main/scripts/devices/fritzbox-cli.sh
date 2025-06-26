#!/bin/bash
#
export CLASSPATH=/home/patrick/rosi/source
#
SID=`cat /tmp/SID`
if [ $# -lt 1 ] ; then
   echo "cli <command> <options ...>" >&2 
   echo "  commands: getdevicenames" >&2 
   echo "            getattributes <devicename>" >&2 
   echo "            setattributes <devicename> <key> <value>" >&2 
   exit 1;
fi
command=$1
shift 1
java org.rosi.drivers.fritzbox.FbDriver http://fritz.box ${command} ${SID} $* 
#
exit 0
