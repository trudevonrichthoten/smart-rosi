#!/bin/bash
#
espeak-ng -a200  --stdout "$1"  2>/dev/null | aplay
exit 0
