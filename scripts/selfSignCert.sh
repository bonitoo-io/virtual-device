#!/usr/bin/env bash

PROJ_NAME=virtual-device
START_DIR=${PWD}
OPENSSL_CMD="openssl";
KEYS_DIR="./scripts/keys";
KEY_FILE="${KEYS_DIR}/ca.key";
KEY_CERT="${KEYS_DIR}/ca.cert";
SERVER_KEY_FILE="${KEYS_DIR}/server.key"
SERVER_SIGN_REQ="${KEYS_DIR}/server.csr"
SERVER_CERT="${KEYS_DIR}/server.crt"
CLIENT_KEY_FILE="${KEYS_DIR}/client.key"
CLIENT_SIGN_REQ="${KEYS_DIR}/client.csr"
CLIENT_CERT="${KEYS_DIR}/client.crt"

if ! command -v $OPENSSL_CMD > /dev/null; then
  echo "This script requires $OPENSSL_CMD, but it was not found in the system."
  echo "Exiting"
  exit 1
fi

if [[ $START_DIR != *$PROJ_NAME* ]]; then
  echo $0 must be run within the project $PROJ_NAME
  exit 1
fi

while [[ $PATH_END != $PROJ_NAME ]];
do
  cd ..
  PATH_END=${PWD##*/}
done

if [[ ! -d ${KEYS_DIR}  ]]; then
  mkdir ${KEYS_DIR}
fi

IP=""

if [ -f /sys/hypervisor/uuid ]; then
  # check if ec2 - TODO update for other types of AWS vms
  if [ `head -c 3 /sys/hypervisor/uuid` == "ec2" ]; then
      IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)
  fi
else
  IP=$(ip -br a | awk '$1 !~ "lo|vir|wl|br|do" { print $1 " " $2 " " $3}' | awk '$2 ~ "UP" { print $3}' | cut -d/ -f1)
fi

echo debug IP ${IP}

SUBJECT_CA="/C=CZ/ST=Praha/L=Harfa/O=bonitoo/OU=qa/CN=$IP"
SUBJECT_SERVER="/C=CZ/ST=Praha/L=Harfa/O=bonitoo/OU=server/CN=$IP"
SUBJECT_CLIENT="/C=CZ/ST=Praha/L=Harfa/O=bonitoo/OU=client/CN=$IP"

function help_openssl () {
  $OPENSSL_CMD
}

function generate_CA () {
   echo "Generating CA"
   echo "$SUBJECT_CA"
   $OPENSSL_CMD req -x509 -nodes -sha256 -newkey rsa:2048 -subj "$SUBJECT_CA"  -days 365 -keyout ${KEY_FILE} -out ${KEY_CERT}
}

function generate_server () {
   echo "Generating Server Cert"
   echo "$SUBJECT_SERVER"
   $OPENSSL_CMD req -nodes -sha256 -new -subj "$SUBJECT_SERVER" -keyout $SERVER_KEY_FILE -out $SERVER_SIGN_REQ
   $OPENSSL_CMD x509 -req -sha256 -in $SERVER_SIGN_REQ -CA $KEY_CERT -CAkey $KEY_FILE -CAcreateserial -out $SERVER_CERT -days 365
}

function generate_client () {
   echo "Generating Client Cert"
   echo "$SUBJECT_CLIENT"
   openssl req -new -nodes -sha256 -subj "$SUBJECT_CLIENT" -out $CLIENT_SIGN_REQ -keyout $CLIENT_KEY_FILE
   openssl x509 -req -sha256 -in $CLIENT_SIGN_REQ -CA ${KEY_CERT} -CAkey ${KEY_FILE} -CAcreateserial -out ${CLIENT_CERT} -days 365
}

generate_CA
generate_server
generate_client

cd ${START_DIR} || exit 1