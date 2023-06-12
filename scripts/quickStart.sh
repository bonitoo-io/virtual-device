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
  echo "$MY_NAME help           - returns this message"
}

function build(){
  mvn -DskipTests clean package
}

function setup(){
  if [[ ! -d ${TARGET_DIR}  ]]; then
    echo "$TARGET_DIR not found.  Building project"
    build
  fi
  VIRDEV_JAR=$(find target -name "virtual-device-*.jar" -printf "%f")
  if [[ "${VIRDEV_JAR}x" == "x" ]]; then
    echo "$VIRDEV_JAR not found.  Building project"
    build
  fi

  nc -vz 127.0.0.1 1883
  if [[ $? -ne 0 ]]; then
    echo "local Mosquitto not started.  Starting it."
    scripts/broker.sh start > /dev/null 2>&1 &
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

  mvn exec:java -Dmain.class="io.bonitoo.qa.Mqtt5Subscriber" > ${SUBSCRIBER_LOG} &
  MQTT5_PID=$!
  if [[ -z "$MQTT5_PID" ]]; then
    echo "ERROR: no PID for Mqtt5Subscriber"
    shutdown
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

    java -jar target/${VIRDEV_JAR}

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

  java -Drunner.conf=plugins/examples/accelerator/sampleRunnerConfig.yml -jar target/${VIRDEV_JAR};

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

  java -Drunner.conf=plugins/examples/simpleMovingAvg/sampleRunnerConfig.yml -jar target/${VIRDEV_JAR};

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

  java -Drunner.conf=plugins/examples/lpFileReader/sampleRunnerConfig.yml -jar target/${VIRDEV_JAR};

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

  java -Drunner.conf=examples/nrf9160/thingy91.yml -jar target/${VIRDEV_JAR};

  printf "\n\nDONE PUBLISHING NRF9160 THINGY EXAMPLE\n"
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
  "")
     base_example
     ;;
  *)
     help;
     ;;
esac

cd ${START_DIR} || exit