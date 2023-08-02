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

OPENSSL_MAJ_VERSION=$(openssl version | awk '{print $2}' | sed 's/\..*//')

if [[ $OPENSSL_MAJ_VERSION -lt 3 ]]; then
  error_exit "This script requires $OPENSSL_CMD version 3 or higher.  Detected $OPENSSL_MAJ_VERSION.\nExiting"
fi

if [[ ! -d ${KEYS_DIR}  ]]; then
  mkdir ${KEYS_DIR}
fi

VD_SUBJ_CA_BASE=${VD_SUBJ_CA_BASE:-"/C=CZ/ST=Praha/L=Harfa/O=bonitoo/OU=qa"}
VD_SUBJ_SVR_BASE=${VD_SUBJ_SVR_BASE:-"/C=CZ/ST=Praha/L=Harfa/O=bonitoo/OU=server"}
VD_SUBJ_CLIENT_BASE=${VD_SUBJ_CLIENT_BASE:-"/C=CZ/ST=Praha/L=Harfa/O=bonitoo/OU=client"}
CN=${VD_HOST_IP:-""}

function help(){
  echo "${0##*/} - generate self signed certificate and add it to truststore"
  echo "    intended for demoing and testing purposes only."
  echo ""
  echo "Envars (N.B. have precedence over args):"
  echo "VD_HOST_IP          Sets a value for the host IP which will be used as the CN value"
  echo "                    for certificate subjects.  If not specified the script will attempt"
  echo "                    to get the ipV4 address of a running ethernet or wifi interface"
  echo "                    Current Value \"$VIRDEV_HOST_IP\""
  echo "VD_SUBJ_CA_BASE     Sets the base string for the CA certificate."
  echo "                    Current value \"$VD_SUBJ_CA_BASE\""
  echo "VD_SUBJ_SVR_BASE    sets the base string for the server certificate."
  echo "                    Current value \"$VD_SUBJ_SVR_BASE\""
  echo "VD_SUBJ_CLIENT_BASE sets the base string for the client certificate."
  echo "                    Current value \"${VD_SUBJ_CLIENT_BASE}\""
  echo ""
  echo "Arguments:"
  echo "-c --cn       Value for final CN field of certificate.  Used in the event that"
  echo "              the envar VIRDEV_HOST_IP is not defined.  It should be the"
  echo "              IP address or hostname of the machine, where the mosquitto server"
  echo "              configured to use TLS will be running.  If not specified"
  echo "              the script will attempt to get the ipV4 address of a running"
  echo "              ethernet or wifi interface and use it as the CN."
  echo "-h -? --help  This message."
  echo ""
  exit 1
}

SUBJECT_MATCH="^/C=.*/ST=.*/L=.*/O=.*/OU=.*/CN=[0-9A-Za-z.]*$"

function validate_subject(){
  echo "$1" | grep "$SUBJECT_MATCH"
  if [[ $? -ne 0 ]]; then
    echo "Detected invalid subject: $1"
    echo "Subject needs to match this regex: $SUBJECT_MATCH"
    error_exit "Invalid certificate subject.\nExiting"
  fi
}

case $1 in
   "-c" | "--cn")
   if [[ -z $CN ]]; then
     CN=$2
   fi
   ;;
   "-h" | "-?" | "--help")
   help
   ;;
esac

IP=""

if [[ -z $CN ]]; then
   if [ -f /sys/hypervisor/uuid ]; then
     # check if ec2 - TODO update for other types of AWS vms
     if [ `head -c 3 /sys/hypervisor/uuid` == "ec2" ]; then
         echo "Detected current host is EC2"
         IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)
         echo "Will use EC2 resolved IP address $IP for certificate CN"
     fi
   else
     TARGET_IFACE=$(ifconfig | grep "RUNNING" | awk '$1 ~ "e|wl"' | awk '{print $1}' | sed 's/://' | head -n 1)
     if [[ -z $TARGET_IFACE ]]; then
       error_exit "Failed to locate target interface for certificate CN value.\nExiting."
     else
       echo "Using IP address of interface ${TARGET_IFACE} for certificate CN"
     fi
     IP=$(ifconfig $TARGET_IFACE | grep "inet " | awk '{print $2}')
     echo "Resolved IP address: $IP"
   fi

   if [[ -z $IP  ]]; then
     echo "Failed to resolve IP address for any interface."
     echo "IP address would be used for certificate CN."
     error_exit "Exiting"
   fi

   CN=$IP
else
  echo "Using CN: $CN"
  echo $CN | grep -oE "^([0-9]{1,3}\.){3}[0-9]{1,3}$"
  if [[ $? -eq 0 ]]; then
    IP=$CN
  fi
fi

SUBJECT_CA="${VD_SUBJ_CA_BASE}/CN=$CN"
SUBJECT_SERVER="${VD_SUBJ_SVR_BASE}/CN=$CN"
SUBJECT_CLIENT="${VD_SUBJ_CLIENT_BASE}/CN=$CN"

# When demoing or testing localhost access will likely be needed
if [[ -n $IP ]]; then
  SAN_SERVER="subjectAltName=IP:$IP,IP:127.0.0.1,DNS:localhost"
else
  SAN_SERVER="subjectAltName=IP:127.0.0.1,DNS:localhost"
fi

function help_openssl () {
  $OPENSSL_CMD
}

function generate_CA () {
   echo "Generating CA"
   validate_subject "$SUBJECT_CA"

   $OPENSSL_CMD req -x509 -nodes -sha256 -newkey rsa:2048 -subj "$SUBJECT_CA"  -days 365 -keyout ${KEY_FILE} -out ${KEY_CERT}
   if [[ $? -gt 0 ]]; then
     echo "Failed to generate CA ${SUBJECT_CA}"
     exit 1
   fi
}

function generate_server () {
   echo "Generating Server Cert"
   validate_subject "$SUBJECT_SERVER"

   $OPENSSL_CMD req -nodes -sha256 -new -subj "$SUBJECT_SERVER" -addext "$SAN_SERVER" -keyout $SERVER_KEY_FILE -out $SERVER_SIGN_REQ
   if [[ $? -gt 0 ]]; then
     echo "Failed to generate server signing request for $SUBJECT_CA"
     exit 1
   fi
   wait_file $SERVER_SIGN_REQ 10 || {
     echo "Generation of ${SERVER_SIGN_REQ} timed out. Exiting."
     exit 1;
   }
   $OPENSSL_CMD x509 -req -sha256 -in $SERVER_SIGN_REQ -CA $KEY_CERT -CAkey $KEY_FILE -CAcreateserial -out $SERVER_CERT  -days 365 -copy_extensions=copyall
   if [[ $? -gt 0 ]]; then
     echo "Failed to generate cert $SERVER_CERT"
     exit 1;
   fi
}

function generate_client () {
   echo "Generating Client Cert"
   validate_subject "$SUBJECT_CLIENT"

   openssl req -new -nodes -sha256 -subj "$SUBJECT_CLIENT" -out $CLIENT_SIGN_REQ -keyout $CLIENT_KEY_FILE
   if [[ $? -gt 0 ]]; then
     echo "Failed to generate client signing request for $SUBJECT_CLIENT"
   fi
      wait_file $CLIENT_SIGN_REQ || {
        echo "Generation of ${CLIENT_SIGN_REQ} timed out. Exiting."
        exit 1;
      }
   openssl x509 -req -sha256 -in $CLIENT_SIGN_REQ -CA ${KEY_CERT} -CAkey ${KEY_FILE} -CAcreateserial -out ${CLIENT_CERT} -days 365
   if [[ $? -gt 0 ]]; then
     echo "Failed to generate certificated  $CLIENT_CERT"
   fi
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