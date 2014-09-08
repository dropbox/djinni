#!/bin/sh

# See http://scottmcpeak.com/autodepend/autodepend.html

if [ $# -ne 1 ]
then
  echo "Usage: $0 depfile.d"
  exit 1
fi

sed -e 's|.*:|'"${1%.d}"'.o:|' < "$1"
sed -e 's/.*://' -e 's/\\$//' < "$1" | fmt -1 | sed -e 's/^ *//' -e 's/$/:/'
