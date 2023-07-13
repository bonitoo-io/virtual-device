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

HOST="127.0.0.1"
PORT=8883
TEMPCERT="tempCert.pem"
ALIAS="test"
FORCE="false"

function help(){
  echo ""
  echo "$0 gets a certificate from a server and adds it to a truststore."
  echo ""
  echo "Arguments"
  echo "-h|--host        Target hostname. Default ${HOST}."
  echo "-p|--port        Target port. Default ${PORT}."
  echo "-s|--store       Target truststore.  Default ${DEFAULT_TRUSTSTORE}."
  echo "-pw|-password    Target truststore password.  Default ${DEFAULT_TRUSTSTORE_PASSWORD}"
  echo "-a|--alias       Alias under which the certificate will be stored.  Default ${ALIAS}"
  echo "-f|--force       If Alias already exists, current certificate will be deleted from"
  echo "                 store and attempt to be replaced.  Default ${FORCE}."
  echo ""
}

function get_server_cert(){
   openssl s_client -connect ${HOST}:${PORT} 2>/dev/null </dev/null |  sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > ${TEMPCERT}
   RESULT=$?
   CERT_HEAD=$(head -1 $TEMPCERT)
   if [[  $RESULT -ne 0  || "$CERT_HEAD" != "-----BEGIN CERTIFICATE-----" ]]; then
     error_exit "FAILED TO GET CERTIFICATE FROM ${HOST}:${PORT}.  Exiting."
   fi
}

function import_cert_to_store(){

   if [[  "$FORCE" == "true" ]]; then
     echo "Forced update.  Deleting alias <${ALIAS}>";
     delete_alias
   fi

   echo "Attempting to add alias <${ALIAS}> to store ${DEFAULT_TRUSTSTORE}";
   keytool -importcert -alias $ALIAS -keystore $DEFAULT_TRUSTSTORE -storepass $DEFAULT_TRUSTSTORE_PASSWORD -noprompt -file tempCert.pem || \
        error_exit "FAILED TO ADD CERTIFICATE TO DEFAULT_TRUSTSTORE ${DEFAULT_TRUSTSTORE}. Exiting."
}

function delete_alias(){
  keytool -delete -alias $ALIAS -keystore $DEFAULT_TRUSTSTORE -storePass $DEFAULT_TRUSTSTORE_PASSWORD -noprompt
}

#function error_exit(){
#  echo "ERROR: $1";
#  do_exit 1
#}

#function do_exit(){
#  cd "${START_DIR}" || exit 1
#  exit "$1"
#}

while [[ $# -gt 0 ]]; do
  case $1 in
    "-h" | "--host")
       HOST=$2
       shift;
       shift;
       ;;
    "-p" | "--port")
      PORT=$2
      shift;
      shift;
      ;;
    "-s" | "--store")
      DEFAULT_TRUSTSTORE=$2
      shift;
      shift;
      ;;
    "-pw" | "--password")
      DEFAULT_TRUSTSTORE_PASSWORD=$2
      shift;
      shift;
      ;;
    "-a" | "--alias")
      ALIAS=$2;
      shift;
      shift;
      ;;
    "-f" | "--force")
      FORCE="true";
      shift;
      ;;
    "-?" | "--help")
      help
      do_exit 0;
      ;;
    *)
      echo "Unknown argument $1"
      help
      do_exit 1
      ;;
  esac
done

get_server_cert
import_cert_to_store
do_exit 0
