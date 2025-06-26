#!/bin/bash
cd ../source/org/rosi
find . -name "*.java" | xargs javac -target 1.7 -source 1.7 
exit $?
