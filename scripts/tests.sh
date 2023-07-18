#!/usr/bin/env bash

PROJ_NAME=virtual-device
START_DIR=${PWD}
REPORTS_DIR=target/test-reports

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

function default_test(){
  mvn --batch-mode test
}

function all_tests(){
  unit_tests
  integration_tests
  envar_tests
}

function unit_tests(){
  time mvn --batch-mode test -D groups=unit -DsurefireReportsDirectory=${REPORTS_DIR}/unit
}

function integration_tests(){
  time mvn --batch-mode test -D groups=intg -DsurefireReportsDirectory=${REPORTS_DIR}/integration
}

function envar_tests(){

  export VD_TRUSTSTORE=envarStore.jks
  export VD_TRUSTSTORE_PASSWORD=envarPassword

  time mvn --batch-mode test -D groups=envars -DsurefireReportsDirectory=${REPORTS_DIR}/envars

  unset VD_TRUSTSTORE
  unset VD_TRUSTSTORE_PASSWORD

}

function clean(){
  rm -rdf ${REPORTS_DIR}
}

case $1 in
   "-a" | "--all")
   clean
   time all_tests
   ;;
   "-u" | "--unit")
   unit_tests
   ;;
   "-i" | "--integration" | "--intg")
   integration_tests
   ;;
   "-e" | "-v" | "--envars")
   envar_tests
   ;;
   "-c" | "--clean")
   clean
   ;;
   "*" | "")
   default_test
   ;;
esac

cd "${START_DIR}" || exit 1

