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

function help(){
  echo "$0 [path/to/config.yml]"
  echo ""
  echo "This script simply wraps running a locally built virtual-dev jar package."
  do_exit 0
}

if [[ "${1}" == "-?" || "${1}" == "--help" ]]; then
   help
fi

VIRDEV_JAR=$(find target -name "virtual-device-*.jar" -printf "%f")

if [[ -z ${VIRDEV_JAR}  ]]; then
  echo "Could not locate virtual-device-*.jar in target directory."
  echo "Perhaps $ mvn package should be run."
  error_exit "No Jar File to run."
fi

RUN_CMD="java"

CONFIG=$1

if [[ -n ${CONFIG} ]]; then
  test -f ${CONFIG} || error_exit "${CONFIG} does not exist or is not a normal file".
  CONFIG_HEAD=$(head -1 ${CONFIG})
  test "$CONFIG_HEAD" == "---" ||  error_exit "${CONFIG} does not appear to be a yaml file."
  RUN_CMD="${RUN_CMD} -Drunner.conf=${CONFIG}"
fi

RUN_CMD="${RUN_CMD} -jar target/${VIRDEV_JAR}"

${RUN_CMD}

do_exit 0
