#!/bin/bash

MAPR_HOME=${MAPR_HOME:-/opt/mapr}
SR_VERSION="5.1.2"
SR_HOME="$MAPR_HOME"/schema-registry/schema-registry-"$SR_VERSION"
SR_BIN="$SR_HOME"/bin
SR_TEMPLATE_CONF_DIR="$SR_HOME/conf.new/"
SR_CONF_DIR="$SR_HOME/etc/schema-registry/"
MAPR_CONF_DIR="${MAPR_HOME}/conf/"
MAPR_WARDEN_CONF_DIR="${MAPR_HOME}/conf/conf.d"
DAEMON_CONF="$MAPR_HOME/conf/daemon.conf"
WARDEN_SR_CONF="$SR_HOME"/warden/warden.schemaregistry.conf
WARDEN_SR_DEST="$MAPR_WARDEN_CONF_DIR/warden.schemaregistry.conf"
HADOOP_VER=$(cat "$MAPR_HOME/hadoop/hadoopversion")
secureCluster=0
MAPR_USER=""
MAPR_GROUP=""

if [ -f "$DAEMON_CONF" ]; then
  MAPR_USER=$( awk -F = '$1 == "mapr.daemon.user" { print $2 }' "$DAEMON_CONF")
  MAPR_GROUP=$( awk -F = '$1 == "mapr.daemon.group" { print $2 }' "$DAEMON_CONF")
else
  MAPR_USER=`logname`
  MAPR_GROUP="$MAPR_USER"
fi

# isSecure is set in server/configure.sh
if [ -n "$isSecure" ]; then
  if [ "$isSecure" == "true" ]; then
    secureCluster=1
  fi
fi

changeSrPermission() {
  #
  # change permissions
  #
  if [ ! -z "$MAPR_USER" ]; then
    chown -R "$MAPR_USER" "$MAPR_HOME/schema-registry"
  fi
  if [ ! -z "$MAPR_GROUP" ]; then
    chgrp -R "$MAPR_GROUP" "$MAPR_HOME/schema-registry"
  fi
}

createRestartFile(){
  if ! [ -d ${MAPR_CONF_DIR}/restart ]; then
    mkdir -p ${MAPR_CONF_DIR}/restart
  fi

cat > "${MAPR_CONF_DIR}/restart/schemaregistry-${SR_VERSION}.restart" <<'EOF'
  #!/bin/bash
  isSecured="false"
  if [ -f "${MAPR_HOME}/conf/mapr-clusters.conf" ]; then
    isSecured=$(head -1 ${MAPR_HOME}/conf/mapr-clusters.conf | grep -o 'secure=\w*' | cut -d= -f2)
  fi
  if [ "${isSecured}" = "true" ] && [ -f "${MAPR_HOME}/conf/mapruserticket" ]; then
    export MAPR_TICKETFILE_LOCATION="${MAPR_HOME}/conf/mapruserticket"
    fi
  maprcli node services -action restart -name schemaregistry -nodes $(hostname)
EOF

  chmod +x "${MAPR_CONF_DIR}/restart/schemaregistry-$SR_VERSION.restart"
  chown -R $MAPR_USER:$MAPR_GROUP "${MAPR_CONF_DIR}/restart/schemaregistry-$SR_VERSION.restart"
}

#
# Copying the warden service config file
#
setupWardenConfFile() {
  if ! [ -d ${MAPR_WARDEN_CONF_DIR} ]; then
    mkdir -p ${MAPR_WARDEN_CONF_DIR} > /dev/null 2>&1
  fi

  # Install warden file
  cp ${WARDEN_SR_CONF} ${MAPR_WARDEN_CONF_DIR}
  chown $MAPR_USER:$MAPR_GROUP $WARDEN_SR_DEST
}

function getProperty() {
   PROPERTY_FILE=$1
   PROP_KEY=$2
   PROP_VALUE=`cat $PROPERTY_FILE | grep "$PROP_KEY" | cut -d'=' -f2`
   echo $PROP_VALUE
}

createInternalStreamIfNotExists() {
   CONF_FILE="$SR_CONF_DIR/schema-registry.properties"
   INTERNAL_STREAM_NAME=$(getProperty $CONF_FILE "kafkastore.stream")
   runuser -l $MAPR_USER -c "bash $SR_BIN/create-internal-stream-if-not-exists.sh $secureCluster $INTERNAL_STREAM_NAME $MAPR_USER"
   if [ $? == 1 ]; then
      exit 1
   fi
}

copyFilesToTargetConfigDir() {
    mkdir -p $SR_CONF_DIR
    cp -n $SR_TEMPLATE_CONF_DIR/*.properties $SR_CONF_DIR

    if [ $secureCluster == 1 ]; then
        cp -n $SR_TEMPLATE_CONF_DIR/unsecure/schema-registry.properties $SR_CONF_DIR/schema-registry.properties
        # cp -n $SR_TEMPLATE_CONF_DIR/secure/schema-registry-secure.properties $SR_CONF_DIR/schema-registry.properties
    else
        cp -n $SR_TEMPLATE_CONF_DIR/unsecure/schema-registry.properties $SR_CONF_DIR/schema-registry.properties
    fi
}

setZookeeperProperty() {
    KAFKASTORE_CONNECTION_URL=""
    while read -r line; do
        if [[ $line == kafkastore.connection.url* ]]; then
            KAFKASTORE_CONNECTION_URL=("${line#kafkastore.connection.url=}")
            break
        fi
    done < $SR_CONF_DIR/schema-registry.properties

    if [ $KAFKASTORE_CONNECTION_URL == "localhost:5181" ]; then

        ZOOKEEPER_NODES=""
        while read -r line; do
            if [[ $line != Zookeepers ]]; then
                ZOOKEEPER_NODES=("$line")
                break
            fi
        done <<< "$(su - mapr -c 'maprcli node listzookeepers')"

        if [ $ZOOKEEPER_NODES != "" ]; then
            sed -i -e "s/localhost:5181/$ZOOKEEPER_NODES/g" $SR_CONF_DIR/schema-registry.properties
        else
            echo "ERROR: Zookeeper is not installed on this cluster. Schema Registry configuration process is interrupted."
            exit 1
        fi
    fi
}

#
# main
#
# typically called from core configure.sh
#

USAGE="usage: $0 [--secure|--customSecure|--unsecure|-EC|-R|--help"
if [ ${#} -gt 1 ]; then
  for i in "$@" ; do
    case "$i" in
      --secure)
        secureCluster=1
        shift
        ;;
      --customSecure|-cs)
        secureCluster=1
        shift
        ;;
      --unsecure)
        secureCluster=0
        shift
        ;;
      --help)
        echo "$USAGE"
        return 0 2>/dev/null || exit 0
        ;;
      -EC|--EC)
         shift
         ;;
       -R|--R)
         shift
         ;;
       --)
        echo "$USAGE"
        return 1 2>/dev/null || exit 1
        ;;
    esac
  done
else
    echo "$USAGE"
    return 1 2>/dev/null || exit 1
fi

if [ ! -f "$SR_CONF_DIR/.not_configured_yet" ]; then
    createRestartFile
fi
copyFilesToTargetConfigDir
setZookeeperProperty
changeSrPermission
createInternalStreamIfNotExists
setupWardenConfFile

# remove state file and start files
if [ -f "$SR_CONF_DIR/.not_configured_yet" ]; then
    rm -f "$SR_CONF_DIR/.not_configured_yet"
fi

true
