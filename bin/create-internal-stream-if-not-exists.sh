#!/bin/bash

#
# Check permissions of internal stream and create it if needed
#

secureCluster=$1
INTERNAL_STREAM_NAME=$2
MAPR_USER=$3

if [ $INTERNAL_STREAM_NAME == "/apps/schema-registry/schema-registry-internal-stream" ]; then
   if ! hadoop fs -test -e $INTERNAL_STREAM_NAME; then
      if ! hadoop fs -test -e /apps; then
           hadoop fs -mkdir /apps
      fi
      if ! hadoop fs -test -e /apps/schema-registry; then
           hadoop fs -mkdir /apps/schema-registry
      fi

      if [ $secureCluster == 1 ]; then
          maprcli stream create -path $INTERNAL_STREAM_NAME -produceperm u:$MAPR_USER -consumeperm p -topicperm u:$MAPR_USER
      else
          maprcli stream create -path $INTERNAL_STREAM_NAME -produceperm p -consumeperm p -topicperm p
      fi
   else
      arr=()
      while read -r line; do
         arr+=("$line")
      done <<< "$(maprcli stream info -path $INTERNAL_STREAM_NAME)"

      read -r -a keys <<< ${arr[0]}
      read -r -a values <<< ${arr[1]}

      count=0
      declare -A hashmap
      for element in ${keys[@]}
      do
          hashmap[$element]=${values[$count]}
          count=$(( $count + 1 ))
      done

      if [ $secureCluster == 1 ]; then
          if [ "${hashmap[produceperm]}" != "u:$MAPR_USER" ] || [ "${hashmap[consumeperm]}" != "p" ] || [ "${hashmap[topicperm]}" != "u:$MAPR_USER" ]; then
             echo "WARNING: Schema Registry internal stream doesn't have appropriate permissions."
          fi
      else
          if [ "${hashmap[produceperm]}" != "p" ] || [ "${hashmap[consumeperm]}" != "p" ] || [ "${hashmap[topicperm]}" != "p" ]; then
             echo "WARNING: Schema Registry internal stream doesn't have appropriate permissions."
          fi
      fi
   fi
else
   if ! hadoop fs -test -e $INTERNAL_STREAM_NAME; then
      echo "ERROR: Schema Registry internal stream differs from the default one and does not exist. Schema Registry configuration process is interrupted."
      exit 1
   fi
fi
