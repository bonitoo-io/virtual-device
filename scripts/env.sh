#!/usr/bin/env bash

PROJ_NAME=virtual-device
START_DIR=$1
OPENSSL_CMD="openssl";
KEYTOOL_CMD="keytool";
KEYS_DIR="./scripts/keys";
KEY_FILE="${KEYS_DIR}/ca.key";
KEY_CERT="${KEYS_DIR}/ca.cert";
SERVER_KEY_FILE="${KEYS_DIR}/server.key"
SERVER_SIGN_REQ="${KEYS_DIR}/server.csr"
SERVER_CERT="${KEYS_DIR}/server.crt"
CLIENT_KEY_FILE="${KEYS_DIR}/client.key"
CLIENT_SIGN_REQ="${KEYS_DIR}/client.csr"
CLIENT_CERT="${KEYS_DIR}/client.crt"
DEFAULT_TRUSTSTORE=${VD_TRUSTSTORE:-"${KEYS_DIR}/brokerTrust.jks"}
DEFAULT_TRUSTSTORE_PASSWORD=${VD_TRUSTSTORE_PASSWORD:-"changeit"}

# echo "DEBUG env.sh START_DIR ${START_DIR}"

function error_exit(){
  echo "ERROR: $1";
  do_exit 1
}

function do_exit(){
  cd "${START_DIR}" || exit 1
  exit "$1"
}

if ! command -v $OPENSSL_CMD > /dev/null; then
  error_exit "This script requires $OPENSSL_CMD, but it was not found in the system.\nExiting."
fi

OPENSSL_MAJ_VERSION=$(openssl version | awk '{print $2}' | sed 's/\..*//')
echo "OPENSSL_MAJ_VERSION"

if [[ $OPENSSL_MAJ_VERSION -lt 3 ]]; then
  error_exit "This script requires $OPENSSL_CMD version 3 or higher.  Detected $OPENSSL_MAJ_VERSION.\nExiting"
fi

if ! command -v $KEYTOOL_CMD > /dev/null; then
  error_exit "This script requires $KEYTOOL_CMD, but it was not found in the system.\nExiting."
fi

