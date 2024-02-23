#!/bin/bash

MAPR_HOME=${MAPR_HOME:-/opt/mapr}
SR_VERSION="7.6.0"
SR_HOME="$MAPR_HOME"/schema-registry/schema-registry-"$SR_VERSION"
SR_BIN="$SR_HOME"/bin
SR_TEMPLATE_CONF_DIR="$SR_HOME/conf.new/"
SR_CONF_DIR="$SR_HOME/etc/schema-registry/"
MAPR_CONF_DIR="${MAPR_HOME}/conf/"
MAPR_WARDEN_CONF_DIR="${MAPR_HOME}/conf/conf.d"
DAEMON_CONF="$MAPR_HOME/conf/daemon.conf"
WARDEN_SR_CONF="$SR_HOME"/warden/warden.schemaregistry.conf
WARDEN_SR_DEST="$MAPR_WARDEN_CONF_DIR/warden.schemaregistry.conf"

WARDEN_HEAPSIZE_MIN_KEY="service.heapsize.min"
WARDEN_HEAPSIZE_MAX_KEY="service.heapsize.max"
WARDEN_HEAPSIZE_PERCENT_KEY="service.heapsize.percent"
WARDEN_RUNSTATE_KEY="service.runstate"

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

conf_get_property() {
  local conf_file="$1"
  local property_name="$2"
  local delim="="
  grep "^\s*${property_name}" "${conf_file}" | sed "s|^\s*${property_name}\s*${delim}\s*||"
}

conf_set_property() {
  local conf_file="$1"
  local property_name="$2"
  local property_value="$3"
  local delim="="
  if grep -q "^\s*${property_name}\s*${delim}" "${conf_file}"; then
    # modify property
    sed -i -r "s|^\s*${property_name}\s*${delim}.*$|${property_name}${delim}${property_value}|" "${conf_file}"
  else
    echo "${property_name}${delim}${property_value}" >> "${conf_file}"
  fi
}

setupWardenConfFile() {
  local curr_heapsize_min
  local curr_heapsize_max
  local curr_heapsize_percent
  local curr_runstate

  if [ -f "$WARDEN_SR_DEST" ]; then
    curr_heapsize_min=$(conf_get_property "$WARDEN_SR_DEST" "$WARDEN_HEAPSIZE_MIN_KEY")
    curr_heapsize_max=$(conf_get_property "$WARDEN_SR_DEST" "$WARDEN_HEAPSIZE_MAX_KEY")
    curr_heapsize_percent=$(conf_get_property "$WARDEN_SR_DEST" "$WARDEN_HEAPSIZE_PERCENT_KEY")
    curr_runstate=$(conf_get_property "$WARDEN_SR_DEST" "$WARDEN_RUNSTATE_KEY")
  fi

  cp ${WARDEN_SR_CONF} ${WARDEN_SR_DEST}

  [ -n "$curr_heapsize_min" ] && conf_set_property "$WARDEN_SR_DEST" "$WARDEN_HEAPSIZE_MIN_KEY" "$curr_heapsize_min"
  [ -n "$curr_heapsize_max" ] && conf_set_property "$WARDEN_SR_DEST" "$WARDEN_HEAPSIZE_MAX_KEY" "$curr_heapsize_max"
  [ -n "$curr_heapsize_percent" ] && conf_set_property "$WARDEN_SR_DEST" "$WARDEN_HEAPSIZE_PERCENT_KEY" "$curr_heapsize_percent"
  [ -n "$curr_runstate" ] && conf_set_property "$WARDEN_SR_DEST" "$WARDEN_RUNSTATE_KEY" "$curr_runstate"

  chown $MAPR_USER:$MAPR_GROUP "$WARDEN_SR_DEST"
}

function getProperty() {
   PROPERTY_FILE=$1
   PROP_KEY=$2
   PROP_VALUE=`cat $PROPERTY_FILE | grep "$PROP_KEY" | cut -d'=' -f2`
   echo $PROP_VALUE
}

copyFilesToTargetConfigDir() {
    mkdir -p $SR_CONF_DIR
    cp -n $SR_TEMPLATE_CONF_DIR/*.properties $SR_CONF_DIR

    if [ $secureCluster == 1 ]; then
        cp -n $SR_TEMPLATE_CONF_DIR/secure/schema-registry-secure.properties $SR_CONF_DIR/schema-registry.properties
        cp -n $SR_TEMPLATE_CONF_DIR/secure/headers.xml $SR_CONF_DIR/headers.xml
    else
        cp -n $SR_TEMPLATE_CONF_DIR/unsecure/schema-registry.properties $SR_CONF_DIR/schema-registry.properties
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
changeSrPermission
setupWardenConfFile

# remove state file and start files
if [ -f "$SR_CONF_DIR/.not_configured_yet" ]; then
    rm -f "$SR_CONF_DIR/.not_configured_yet"
fi

true
