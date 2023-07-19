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
  echo "======= [Running All Tests] ======="
  unit_tests
  integration_tests
  envar_tests
}

function unit_tests(){
  echo "========= [Running Unit Tests] ====="
  time mvn --batch-mode test -D groups=unit -DsurefireReportsDirectory=${REPORTS_DIR}/unit
}

function integration_tests(){
  echo "========= [Running Integration Tests ] ====="
  time mvn --batch-mode test -D groups=intg -DsurefireReportsDirectory=${REPORTS_DIR}/integration
}

function envar_tests(){

  echo "========= [Running Envar Tests] ====="

  export VD_TRUSTSTORE=envarStore.jks
  export VD_TRUSTSTORE_PASSWORD=envarPassword
  env | grep "^VD"

  time mvn --batch-mode test -D groups=envars -DsurefireReportsDirectory=${REPORTS_DIR}/envars

  unset VD_TRUSTSTORE
  unset VD_TRUSTSTORE_PASSWORD

}

function failure_check(){
  FAILURES=$(grep 'FAILURE!$' $1)
  FAILURE_COUNT=0
  if [[ -n $FAILURES  ]]; then
    FAILURE_COUNT=$(echo "$FAILURES" | wc -l)
  fi
  FAILURE_FILES=$(grep -l 'FAILURE!$' $1)
  echo "Failures $FAILURE_COUNT"
  if [[ $FAILURE_COUNT -gt 0 ]]; then
    for FILE in $FAILURE_FILES
    do
      cat $FILE
    done
  fi
}

function clean(){
  rm -rdf ${REPORTS_DIR}
}

case $1 in
   "-a" | "--all")
   clean
   time all_tests
   failure_check "${REPORTS_DIR}/**/*.txt"
   ;;
   "-u" | "--unit")
   unit_tests
   failure_check "${REPORTS_DIR}/unit/*.txt"
   ;;
   "-i" | "--integration" | "--intg")
   integration_tests
   failure_check "${REPORTS_DIR}/integration/*.txt"
   ;;
   "-e" | "-v" | "--envars")
   envar_tests
   failure_check "${REPORTS_DIR}/envars/*.txt"
   ;;
   "-c" | "--clean")
   clean
   ;;
   "*" | "")
   default_test
   ;;
esac

cd "${START_DIR}" || exit 1

