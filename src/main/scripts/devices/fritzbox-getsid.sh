#!/bin/bash
#
export CLASSPATH=/home/patrick/rosi/source
#
x=`java org.rosi.drivers.fritzbox.FbDriver http://fritz.box getsid patrick ..elchy12`
echo $x | awk -F= '{ print $2 }' >/tmp/SID
#
exit 0
