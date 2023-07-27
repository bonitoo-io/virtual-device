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

if [[ ! -d ${KEYS_DIR}  ]]; then
  mkdir ${KEYS_DIR}
fi

IP=""

if [ -f /sys/hypervisor/uuid ]; then
  # check if ec2 - TODO update for other types of AWS vms
  if [ `head -c 3 /sys/hypervisor/uuid` == "ec2" ]; then
      echo "Detected current host is EC2"
      IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)
  fi
else
  TARGET_IFACE=$(ifconfig -s | awk '{print $1 " " $3}' | awk '$2+0 > 0' | awk '$1 !~ "lo|vir|wl|br|do|If"' | awk '{print $1}')
  echo "DEBUG TARGET_IFACE ${TARGET_IFACE}"
  IP=$(ifconfig $TARGET_IFACE | grep "inet " | awk '{print $2}')
  echo "DEBUG IP $IP"
  # IP=$(ip -br a | awk '$1 !~ "lo|vir|wl|br|do" { print $1 " " $2 " " $3}' | awk '$2 ~ "UP" { print $3}' | cut -d/ -f1)
fi

echo debug IP ${IP}

SUBJECT_CA="/C=CZ/ST=Praha/L=Harfa/O=bonitoo/OU=qa/CN=$IP"
SUBJECT_SERVER="/C=CZ/ST=Praha/L=Harfa/O=bonitoo/OU=server/CN=$IP"
SAN_SERVER="subjectAltName=IP:$IP,IP:127.0.0.1,DNS:localhost"
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
   $OPENSSL_CMD req -nodes -sha256 -new -subj "$SUBJECT_SERVER" -addext "$SAN_SERVER" -keyout $SERVER_KEY_FILE -out $SERVER_SIGN_REQ
   wait_file $SERVER_SIGN_REQ 10 || {
     echo "Generation of ${SERVER_SIGN_REQ} timed out. Exiting."
     exit 1;
   }
   $OPENSSL_CMD x509 -req -sha256 -in $SERVER_SIGN_REQ -CA $KEY_CERT -CAkey $KEY_FILE -CAcreateserial -out $SERVER_CERT  -days 365 -copy_extensions=copyall
}

function generate_client () {
   echo "Generating Client Cert"
   echo "$SUBJECT_CLIENT"
   openssl req -new -nodes -sha256 -subj "$SUBJECT_CLIENT" -out $CLIENT_SIGN_REQ -keyout $CLIENT_KEY_FILE
      wait_file $CLIENT_SIGN_REQ || {
        echo "Generation of ${CLIENT_SIGN_REQ} timed out. Exiting."
        exit 1;
      }
   openssl x509 -req -sha256 -in $CLIENT_SIGN_REQ -CA ${KEY_CERT} -CAkey ${KEY_FILE} -CAcreateserial -out ${CLIENT_CERT} -days 365
}

function generate_keystore () {
  ALIAS="ca$(date +%d%m)"
  $KEYTOOL_CMD -alias $ALIAS -importcert -keystore $DEFAULT_TRUSTSTORE -file $KEY_CERT -storepass $DEFAULT_TRUSTSTORE_PASSWORD -noprompt || \
     error_exit "Failed to add alias ${ALIAS} to keystore ${DEFAULT_TRUSTSTORE}."
  echo "Added ca.cert to keystore ${DEFAULT_TRUSTSTORE} as alias ${ALIAS}"
}

function wait_file() {
  local file="$1"; shift
  local timeout=${1:-10}; shift
  local count=0;

  until [[ $((count++)) -eq ${timeout} || -e "${file}" ]]
  do
    sleep 1;
  done

  test -e "${file}"
}

echo "Starting generation of self-signed Certificate"

generate_CA
generate_server
generate_client
generate_keystore

cd ${START_DIR} || exit 1