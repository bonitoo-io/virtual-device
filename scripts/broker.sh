#!/usr/bin/env bash

PROJ_NAME=virtual-device
MQTT_DIR=mqtt
START_DIR=${PWD}
PATH_END=${START_DIR##*/}
KEYS_DIR=keys
CA_CERT="ca.cert"
SERVER_CERT="server.crt"
SERVER_KEY="server.key"
SERVER_CA_FILE="${KEYS_DIR}/${CA_CERT}"
SERVER_CERT_FILE="${KEYS_DIR}/${SERVER_CERT}"
SERVER_KEY_FILE="${KEYS_DIR}/${SERVER_KEY}"


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
  echo "   start    - start the container."
  echo "                -tls - with this argument enables tls"
  echo "   stop     - stop the container."
  echo "   clean    - clean remove the container and clean local directories."
  echo "                -certs - with this arg cleans generated certificates."
  echo "                N.B. requires sudo."
  echo "   up       - check if container is currently running."
  echo "   down     - check if container is currently down."
  echo "   addUser  - adds a new user to the password file."
  echo "                N.B. followed by <username> <password>"
  echo "   add-user - alias for addUser."
  echo "   add      - alias for addUser."
}

function setup_mosquitto(){

  if [[ ! -d ${MQTT_DIR}  ]]; then
    mkdir ${MQTT_DIR}
  fi

  cd ${MQTT_DIR} || error_exit "Failed to change to ${MQTT_DIR}"

  # Check if script was previously run with -tls enabled
  grep "^listener 8883" mosquitto/config/mosquitto.conf
  PAST_TLS=$?
  if [[ "$PAST_TLS" == 0 && "$1" != "-tls" ]]; then
   echo "Found -TLS enabled in current mosquitto.conf, but now running without tls.  Resetting config."
   clean_mosquitto
  elif [[ "$PAST_TLS" == 1 && "$1" == "-tls" ]]; then
   echo "Starting with TLS but found existing config without it.  Resetting config."
   clean_mosquitto
  fi

  mkdir -p mosquitto/config
  mkdir -p mosquitto/data
  mkdir -p mosquitto/log

  if [[ ! -f mosquitto/config/mosquitto.conf ]]; then
    cp -f ${PROJ_DIR}/scripts/mosquitto/mosquitto.conf mosquitto/config || error_exit "Failed to copy config"
  fi


  if [[ ! -f mosquitto/data/password-file ]]; then
    cp ${PROJ_DIR}/scripts/mosquitto/password-file mosquitto/data || error_exit "Failed to copy password file"
  fi

  if  [[ "$1" == "-tls" ]]; then

    if [[ ! -f ${PROJ_DIR}/scripts/${SERVER_CA_FILE} ||
          ! -f ${PROJ_DIR}/scripts/${SERVER_CERT_FILE} ||
          ! -f ${PROJ_DIR}/scripts/${SERVER_KEY_FILE}  ]]; then
      ../scripts/selfSignCert.sh || error_exit "Failed to generate new self signed certificates"
    fi

    mkdir -p mosquitto/etc

    if [[ ! -f mosquitto/etc/${CA_CERT} ]]; then
      cp ${PROJ_DIR}/scripts/${SERVER_CA_FILE} mosquitto/etc || error_exit "Failed to copy ${CA_CERT}"
      sed -i -E "s/#cafile/cafile \/mosquitto\/etc\/${CA_CERT}/" mosquitto/config/mosquitto.conf
    fi

    if [[ ! -f mosquitto/etc/${SERVER_CERT} ]]; then
      cp ${PROJ_DIR}/scripts/${SERVER_CERT_FILE} mosquitto/etc || error_exit "Failed to copy ${SERVER_CERT}"
    sed -i -E "s/#certfile/certfile \/mosquitto\/etc\/${SERVER_CERT}/" mosquitto/config/mosquitto.conf
    fi

    if [[ ! -f mosquitto/etc/${SERVER_KEY} ]]; then
      cp ${PROJ_DIR}/scripts/${SERVER_KEY_FILE} mosquitto/etc || error_exit "Failed to copy ${SERVER_KEY}"
      sed -i -E "s/#keyfile/keyfile \/mosquitto\/etc\/${SERVER_KEY}/" mosquitto/config/mosquitto.conf
    fi

    sed -i -E "s/^listener 1883/listener 8883 0.0.0.0/" mosquitto/config/mosquitto.conf

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

  docker rm mosquito-mq > /dev/null 2>&1

  if [[ "$1" == "-tls" ]]; then
    docker run --name mosquito-mq -p 8883:8883 -p 9001:9001 -v $(pwd)/mosquitto/config/mosquitto.conf:/mosquitto/config/mosquitto.conf -v $(pwd)/mosquitto/data:/mosquitto/data -v $(pwd)/mosquitto/etc:/mosquitto/etc eclipse-mosquitto
  else
    docker run --name mosquito-mq -p 1883:1883 -p 9001:9001 -v $(pwd)/mosquitto/config/mosquitto.conf:/mosquitto/config/mosquitto.conf -v $(pwd)/mosquitto/data:/mosquitto/data eclipse-mosquitto
  fi

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

  if [[  "$1" == "-certs" ]]; then
    cd scripts || error_exit "Failed to open directory ./scripts"
    rm -rdf keys
    cd ${PROJ_DIR} || error_exit "Failed to return to ${PROJ_DIR}"
  fi

}

function add_user_mosquitto(){
  STOPPED=$(mosquitto_stopped)

  if [[ $STOPPED == "true" ]]; then
    error_exit "Mosquitto needs to be running to add user.  Please run broker.sh start"
  fi

  if [[ ! $1 ]]; then
    error_exit "add user requires a username parameter. None found. Exiting."
  fi

  if [[ ! $2 ]]; then
    error_exit "add user requires a password parameter.  None found.  Exiting."
  fi

  docker exec -it mosquito-mq mosquitto_passwd -b mosquitto/data/password-file $1 $2

}

case "$1" in
   "start")
     if [[ "$2" == "-tls" ]]; then
       echo "Starting with tls enabled"
       setup_mosquitto $2
       start_mosquitto $2
       exit 0
     else
       echo "Starting without tls"
       setup_mosquitto
       start_mosquitto
     fi
     ;;
   "stop")
     stop_mosquitto
     ;;
   "clean")
     clean_mosquitto $2
     ;;
   "up")
     mosquitto_started
     ;;
   "down")
     mosquitto_stopped
     ;;
   "addUser"|"add-user"|"add")
      add_user_mosquitto $2 $3
      ;;
   *)
     help;
     exit 1;;
esac

cd ${START_DIR} || exit

