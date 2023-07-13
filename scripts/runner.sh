#!/usr/bin/env bash

PROJ_NAME=virtual-device
START_DIR=${PWD}

if [[ $START_DIR != *$PROJ_NAME* ]]; then
  echo $0 must be run within the project $PROJ_NAME
  exit 1
fi

PATH_END=${PWD##*/}

while [[ $PATH_END != $PROJ_NAME ]];
do
  cd ..
  PATH_END=${PWD##*/}
done

source scripts/env.sh "${START_DIR}"

VIRDEV_JAR=$(find target -name "virtual-device-*.jar" -printf "%f")

# echo "VIRDEV_JAR ${VIRDEV_JAR}"

if [[ -z ${VIRDEV_JAR}  ]]; then
  echo "Could not locate virtual-device-*.jar in target directory."
  echo "Perhaps $ mvn package should be run."
  error_exit "No Jar File to run."
fi

RUNCMD="java"

CONFIG=$1

if [[ -n ${CONFIG} ]]; then
  test -f ${CONFIG} || error_exit "${CONFIG} does not exist or is not a normal file".
  CONFIG_HEAD=$(head -1 ${CONFIG})
#  echo "DEBUG CONFIG_HEAD ${CONFIG_HEAD}"
  test "$CONFIG_HEAD" == "---" ||  error_exit "${CONFIG} does not appear to be a yaml file."
  RUNCMD="${RUNCMD} -Drunner.conf=${CONFIG}"
fi

RUNCMD="${RUNCMD} -jar target/${VIRDEV_JAR}"

# echo "DEBUG RUNCMD ${RUNCMD}"

${RUNCMD}

do_exit 0
