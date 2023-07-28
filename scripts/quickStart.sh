#!/usr/bin/env bash

PROJ_NAME=virtual-device
START_DIR=${PWD}
PATH_END=${START_DIR##*/}
TARGET_DIR=target
MQTT5_PID=""
SUBSCRIBER_LOG=scripts/mqtt5subscriber.log
ITEM_PLUGIN_JAR=accelerator-0.1-SNAPSHOT.jar
ITEM_PLUGIN_PATH=plugins/examples/accelerator/${ITEM_PLUGIN_JAR}
ITEM_PLUGIN_AVG_JAR=simpleMovingAverage-0.1-SNAPSHOT.jar
ITEM_PLUGIN_AVG_PATH=plugins/examples/simpleMovingAvg/${ITEM_PLUGIN_AVG_JAR}
SAMPLE_PLUGIN_JAR=lpFileReader-0.1-SNAPSHOT.jar
SAMPLE_PLUGIN_PATH=plugins/examples/lpFileReader/${SAMPLE_PLUGIN_JAR}
MOSQUITTO_LOG=mosquitto.log

if [[ $START_DIR != *$PROJ_NAME* ]]; then
  echo $0 must be run within the project $PROJ_NAME
  exit 1
fi

while [[ $PATH_END != "$PROJ_NAME" ]];
do
  cd ..
  PATH_END=${PWD##*/}
done

PROJ_DIR=${PWD}

VIRDEV_JAR=""
MSQTT_STARTED_HERE=false

function help(){
  MY_NAME=${0##*/}
  echo "$MY_NAME - a runner for Virtual Device examples"
  echo " "
  echo "run one of the following scenarios"
  echo " "
  echo "$MY_NAME                - runs the basic configuration without plugins"
  echo "$MY_NAME itemPlugin     - runs a configuration with an item plugin"
  echo "$MY_NAME itemPluginRich - runs a configuration with item plugin - simpleMovingAvg"
  echo "$MY_NAME samplePlugin   - runs a configuration with a sample plugin"
  echo "$MY_NAME nrf9160        - runs without plugins but with example runner.conf"
  echo "$MY_NAME tlsBasic       - runs the basic configuration against mosquitto with TLS."
  echo "$MY_NAME help           - returns this message"
}

function build(){
  mvn -DskipTests clean package
}

function check_build(){

  if [[ ! -d ${TARGET_DIR}  ]]; then
    echo "$TARGET_DIR not found.  Building project"
    build
  fi
  VIRDEV_JAR=$(find target -name "virtual-device-*.jar" -printf "%f")
  if [[ "${VIRDEV_JAR}x" == "x" ]]; then
    echo "$VIRDEV_JAR not found.  Building project"
    build
  fi
}

function setup(){

  check_build

  nc -vz 127.0.0.1 8883
  if [[ $? -eq 0 ]]; then
    echo "Detected mosquitto listening at the TLS port (8883)."
    echo "Please shut it down and clean configurations"
    echo "before running non-tls quickStart examples."
    echo "e.g. scripts/broker.sh stop && scripts/broker.sh clean"
    exit 1
  fi

  nc -vz 127.0.0.1 1883
  if [[ $? -ne 0 ]]; then
    echo "local Mosquitto not started.  Starting it."
    scripts/broker.sh start > $MOSQUITTO_LOG 2>&1 &
    timeout 22 sh -c 'until nc -z $0 $1; do sleep 1; done' 127.0.0.1 1883
    sleep 1
    cat $MOSQUITTO_LOG
    grep "mosquitto.*running" $MOSQUITTO_LOG
    if [[ $? -gt 0 ]]; then
      echo "failed to start mosquitto broker with docker.  Check $MOSQUITTO_LOG.  Exiting"
      exit 1
    fi
    now=$(date +%s)
    ttl=$(($now + 10))
    nc -vz 127.0.0.1 1883
    rStatus=$?
    sleep 1
    until [[ $rStatus -eq 0 || $(date +%s) -gt ttl ]]; do
      echo "waiting for Mosquitto"
      sleep 1
      nc -vz 127.0.0.1 1883
      rStatus=$?
    done

  echo "Mosquitto started"

  MSQTT_STARTED_HERE=true
  else
    echo "Found Mosquitto at 1883"
  fi

  echo "Starting Subscriber"
  mvn exec:java -Dmain.class="io.bonitoo.qa.Mqtt5Subscriber" > ${SUBSCRIBER_LOG} &
  MQTT5_PID=$!
  if [[ -z "$MQTT5_PID" ]]; then
    echo "ERROR: no PID for Mqtt5Subscriber"
    shutdown
    exit 1
  fi
  timeout 30 bash -c 'until grep -q \"Subscribed to topic\" $0 > /dev/null 2>&1; do sleep 1; done' $SUBSCRIBER_LOG
  echo "SUBSCRIBER_LOG"
  cat $SUBSCRIBER_LOG
  grep "Subscribed to topic" $SUBSCRIBER_LOG
  if [[ $? -gt 0 ]]; then
    echo "Failed to start subscriber.  See $SUBSCRIBER_LOG. Exiting"
    exit 1
  fi

  echo "Mqtt5Subscriber started with PID $MQTT5_PID.  Output piped to ${SUBSCRIBER_LOG}"

}

function setup_tls(){
    check_build

     nc -vz 127.0.0.1 1883
     if [[ $? -eq 0 ]]; then
       echo "Mosquitto is already listening in non-TLS mode."
       echo "Please shut it down and clean up any config files,"
       echo "before continuing with TLS."
       echo "e.g. scripts/broker.sh stop && scripts/broker.sh clean"
       exit 1
     fi

     nc -vz 127.0.0.1 8883
     if [[ $? -ne 0 ]]; then
       echo "Local mosquitto not listening at traditional TLS port (8883)."
       echo "starting mosquitto in TLS mode."
       scripts/broker.sh start -tls > $MOSQUITTO_LOG 2>&1 &
       timeout 22 sh -c 'until nc -z $0 $1; do sleep 1; done' 127.0.0.1 8883
       sleep 1
       cat $MOSQUITTO_LOG
       grep "mosquitto.*running" $MOSQUITTO_LOG
       if [[ $? -gt 0 ]]; then
         echo "failed to start mosquitto broker with docker. Check $MOSQUITTO_LOG  Exiting"
         exit 1
       fi
       now=$(date +%s)
       ttl=$(($now + 10))
       nc -vz 127.0.0.1 8883
       rStatus=$?
       sleep 1
       until [[ $rStatus -eq 0 || $(date +%s) -gt ttl ]]; do
         echo "waiting for Mosquitto"
         sleep 1
          nc -vz 127.0.0.1 8883
          rStatus=$?
       done

       echo "Mosquitto started"

       MSQTT_STARTED_HERE=true

     else
       echo "Found mosquitto already listening at traditional TLS port (8883)."
       echo "No need to start."
     fi

     echo "Starting Subscriber"
     mvn exec:java -Dmain.class="io.bonitoo.qa.Mqtt5Subscriber" -Dsub.tls=true > ${SUBSCRIBER_LOG} &

     MQTT5_PID=$!
     if [[ -z "$MQTT5_PID" ]]; then
       echo "ERROR: no PID for Mqtt5Subscriber"
       shutdown
       exit 1
     fi
     timeout 30 bash -c 'until grep -q \"Subscribed to topic\" $0 > /dev/null 2>&1; do sleep 1; done' $SUBSCRIBER_LOG
     echo "SUBSCRIBER_LOG"
     cat $SUBSCRIBER_LOG
     grep "Subscribed to topic" $SUBSCRIBER_LOG
     if [[ $? -gt 0 ]]; then
       echo "Failed to start subscriber.  See $SUBSCRIBER_LOG. Existing"
       exit 1
     fi

     echo "Mqtt5Subscriber started with PID $MQTT5_PID.  Output piped to ${SUBSCRIBER_LOG}"

}

function shutdown(){
  if [[ "$MSQTT_STARTED_HERE" == "true" ]]; then
    echo "SHUTTING DOWN local Mosquitto";
    scripts/broker.sh stop
  else
    echo "Mosquitto not started by this script.  Leaving it running"
  fi

  if [[ -n "$MQTT5_PID" ]]; then
    echo "Killing Mqtt5Subscriber with PID $MQTT5_PID"
    kill $MQTT5_PID
  fi

  scripts/broker.sh clean
}

function read_log(){
  printf "\n\nREADING SUBSCRIBER LOG\n"
  printf "=======================\n"
  count=0
  while read line; do
    echo $line
    count=$((count + 1))
    if [[ count -gt 9 ]]; then
      count=0
      sleep 1
    fi
  done < ${SUBSCRIBER_LOG}
  printf "\n\nDONE READING SUBSCRIBER LOG\n"
  printf "===============================\n"
}

function base_example(){
    setup
    printf "\n\nRUNNING BASIC EXAMPLE\n"
    printf "=======================\n"

    scripts/runner.sh

    printf "\n\nDONE PUBLISHING BASIC EXAMPLE\n"
    printf "=============================\n"

    read_log
    shutdown
}

function item_plugin_example(){
  setup

  printf "\nCopying ${ITEM_PLUGIN_PATH} to ./plugins/${ITEM_PLUGIN_JAR}\n"
  cp ${ITEM_PLUGIN_PATH} ./plugins/${ITEM_PLUGIN_JAR}

  printf "\n\nRUNNING ITEM PLUGIN EXAMPLE\n"
  printf "=======================\n"

  scripts/runner.sh plugins/examples/accelerator/sampleRunnerConfig.yml

  printf "\n\nDONE PUBLISHING ITEM PLUGIN EXAMPLE\n"
  printf "=============================\n"

  read_log

  printf "\nRemoving ./plugins/${ITEM_PLUGIN_JAR}\n"
  rm ./plugins/${ITEM_PLUGIN_JAR}

  shutdown
}

function item_plugin_avg_example(){
  setup

  printf "\nCopying ${ITEM_PLUGIN_AVG_PATH} to ./plugins/${ITEM_PLUGIN_AVG_JAR}\n"
  cp ${ITEM_PLUGIN_AVG_PATH} ./plugins/${ITEM_PLUGIN_AVG_JAR}

  printf "\n\nRUNNING ITEM PLUGIN RICH EXAMPLE\n"
  printf "=======================\n"

  scripts/runner.sh plugins/examples/simpleMovingAvg/sampleRunnerConfig.yml

  printf "\n\nDONE PUBLISHING ITEM RICH EXAMPLE\n"
  printf "=============================\n"

  read_log

  printf "\nRemoving ./plugins/${ITEM_PLUGIN_AVG_JAR}\n"
  rm ./plugins/${ITEM_PLUGIN_AVG_JAR}

  shutdown
}

function sample_plugin_example(){
  setup

  printf "\nCopying ${SAMPLE_PLUGIN_PATH} to ./plugins/${SAMPLE_PLUGIN_JAR}\n"
  cp ${SAMPLE_PLUGIN_PATH} ./plugins/${SAMPLE_PLUGIN_JAR}

  printf "\n\nRUNNING SAMPLE PLUGIN EXAMPLE\n"
  printf "=======================\n"

  scripts/runner.sh plugins/examples/lpFileReader/sampleRunnerConfig.yml

  printf "\n\nDONE PUBLISHING SAMPLE PLUGIN EXAMPLE\n"
  printf "=============================\n"

  read_log

  printf "\nRemoving ./plugins/${SAMPLE_PLUGIN_JAR}\n"
  rm ./plugins/${SAMPLE_PLUGIN_JAR}

  shutdown

}

function sample_nrf9160(){
  setup
  printf "\n\nRUNNING NRF9160 THINGY EXAMPLE\n"
  printf "=======================\n"

  scripts/runner.sh examples/nrf9160/thingy91.yml

  printf "\n\nDONE PUBLISHING NRF9160 THINGY EXAMPLE\n"
  printf "=============================\n"

  read_log
  shutdown
}

function tlsBasic(){
  setup_tls
  printf "\n\nRUNNING TLS BASIC EXAMPLE\n"
  printf "=======================\n"

  scripts/runner.sh src/test/resources/testRunnerTlsConfig.yml

  printf "\n\nDONE PUBLISHING TLS BASIC EXAMPLE\n"
  printf "=============================\n"

  read_log

  shutdown
}

# TODO use case with special runner config only

case $1 in
  "itemPlugin")
     item_plugin_example
     ;;
  "itemPluginRich")
     item_plugin_avg_example
     ;;
  "samplePlugin")
     sample_plugin_example
     ;;
  "nrf9160")
     sample_nrf9160
     ;;
  "tlsBasic")
     tlsBasic
     ;;
  "")
     base_example
     ;;
  *)
     help;
     ;;
esac

cd ${START_DIR} || exit