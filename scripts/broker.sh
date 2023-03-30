#!/usr/bin/env bash

PROJ_NAME=virtual-device
MQTT_DIR=mqtt
START_DIR=${PWD}
PATH_END=${START_DIR##*/}

if [[ $START_DIR != *$PROJ_NAME* ]]; then
  echo $0 must be run within the project $PROJ_NAME
  exit 1
fi

while [[ $PATH_END != $PROJ_NAME ]];
do
  cd ..
  PATH_END=${PWD##*/}
done

PROJ_DIR=${PWD}

if [[ ! -d ${MQTT_DIR}  ]]; then
  mkdir ${MQTT_DIR}
fi

function help(){
  echo "$0 - A simple script for starting a mosquitto container"
  echo .
  echo "Commands"
  echo "   start - start the container."
  echo "   stop  - stop the container."
  echo "   clean - clean remove the container and clean local directories."
  echo "             N.B. requires sudo."
  echo "   up    - check if container is currently running."
  echo "   down  - check if container is currently down."
}

function setup_mosquitto(){
  if [[ ! -d ${MQTT_DIR}  ]]; then
    mkdir ${MQTT_DIR}
  fi

  cd ${MQTT_DIR} || error_exit "Failed to change to ${MQTT_DIR}"

  mkdir -p mosquitto/config
  mkdir -p mosquitto/data
  mkdir -p mosquitto/log

  if [[ ! -f mosquitto/config/mosquitto.conf ]]; then
    cp ${PROJ_DIR}/scripts/mosquitto/mosquitto.conf mosquitto/config || error_exit "Failed to copy config"
  fi

  if [[ ! -f mosquitto/data/password-file ]]; then
    cp ${PROJ_DIR}/scripts/mosquitto/password-file mosquitto/data || error_exit "Failed to copy password file"
  fi
}

function error_exit(){
  echo $1
  cd ${START_DIR} || echo "cannot return to ${START_DIR}"
  exit 1
}

function start_mosquitto(){

  STARTED=$(mosquitto_started)

  if [[ ${STARTED} == "true"  ]]; then
    echo Container mosquit-mq is already running.  Nothing to do.
    return
  fi

  docker rm mosquito-mq > /dev/null
  docker run --name mosquito-mq -p 1883:1883 -p 9001:9001 -v ./mosquitto/config/mosquitto.conf:/mosquitto/config/mosquitto.conf -v ./mosquitto/data:/mosquitto/data eclipse-mosquitto

}

function stop_mosquitto(){
  STOPPED=$(mosquitto_stopped)

  if [[ $STOPPED == "true" ]]; then
    echo Nothing to do.
    return
  fi

  docker stop mosquito-mq

  COUNT=0
  until [[ COUNT -eq 30 ]]; do
    STOPPED=$(mosquitto_stopped);
    if [[ ! $STOPPED ]]; then
      COUNT++
      sleep 1
    else
      break
    fi
  done

  if [[ COUNT -eq 30 ]]; then
    echo Failed to shutdown docker mosquito-mq
    cd $START_DIR || error_exit "Failed to return to $START_DIR"
    exit 1
  fi

}

function mosquitto_started(){
  ID=$(docker container ls -f name=^mosquito-mq$ -q)
  if [[ ${ID}x != "x"  ]]; then
    echo true
  else
    echo false
  fi
}

function mosquitto_stopped(){
  ID=$(docker container ls -f name=^mosquito-mq$ -q)
    if [[ ${ID}x == "x"  ]]; then
      echo true
    else
      echo false
    fi
}

function clean_mosquitto(){

  STOPPED=$(mosquitto_stopped)
  if [[ ! $STOPPED == "true" ]]; then
    echo Mosquitto broker is currently running.
    echo Please stop it before running clean.
    exit 1
  fi

  docker rm mosquito-mq

  cd ${PROJ_DIR} || error_exit "Failed to return to ${PROJ_DIR}"
  sudo rm -rdf ${MQTT_DIR}

}

case "$1" in
   "start")
     setup_mosquitto
     start_mosquitto
     ;;
   "stop")
     stop_mosquitto
     ;;
   "clean")
     clean_mosquitto
     ;;
   "up")
     mosquitto_started
     ;;
   "down")
     mosquitto_stopped
     ;;
   *)
     help;
     exit 1;;
esac

cd ${START_DIR} || exit

