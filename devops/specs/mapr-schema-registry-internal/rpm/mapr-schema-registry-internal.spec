%undefine __check_files

summary:     Ezmeral Ecosystem Pack: Kafka Schema Registry internal
license:     Hewlett Packard Enterprise, CopyRight
Vendor:      Hewlett Packard Enterprise, <ezmeral_software_support@hpe.com>
name:        mapr-schema-registry-internal
version:     __RELEASE_VERSION__
release:     1
prefix:      /
group:       MapR
buildarch:   noarch
requires:    mapr-kafka >= 2.6.0
conflicts:   mapr-core < 6.2.0, mapr-kafka < 2.6.0
AutoReqProv: no


%description
Ezmeral Ecosystem Pack: Kafka Schema Registry internal package
Tag: __RELEASE_BRANCH__
Commit: __GIT_COMMIT__


%clean
echo "NOOP"


%files
__PREFIX__/schema-registry

%pre
# $1 -eq 1 install
# $1 -eq 2 upgrade
# N/A     uninstall
[ -n "$VERBOSE" ] && echo "pre install called with argument \`$1'" >&2
[ -n "$VERBOSE" ] && set -x ; :

if [ "$1" = "2" ]; then
   if [ -f  __PREFIX__/schema-registry/schema-registryversion ]; then
      bash __PREFIX__/schema-registry/schema-registry-__VERSION_3DIGIT__/bin/schema-registry-stop-service
   fi

   if [ -d __PREFIX__/schema-registry/schema-registry-__VERSION_3DIGIT__/logs ]; then
      rm -rf __PREFIX__/schema-registry/schema-registry-__VERSION_3DIGIT__/logs
   fi
   #Saving of old configurations
   OLD_TIMESTAMP=$(rpm -qi mapr-schema-registry-internal | awk -F': ' '/Version/ {print $2}')
   OLD_VERSION=$( echo $OLD_TIMESTAMP | cut -d'.' -f1-3 )
   mkdir -p __PREFIX__/schema-registry/schema-registry-${OLD_TIMESTAMP}/etc/schema-registry
   cp __PREFIX__/schema-registry/schema-registry-${OLD_VERSION}/etc/schema-registry/* __PREFIX__/schema-registry/schema-registry-${OLD_TIMESTAMP}/etc/schema-registry

   DAEMON_CONF=__PREFIX__/conf/daemon.conf
   if [ -f "$DAEMON_CONF" ]; then
       MAPR_USER=$( awk -F = '$1 == "mapr.daemon.user" { print $2 }' $DAEMON_CONF)
       MAPR_GROUP=$( awk -F = '$1 == "mapr.daemon.group" { print $2 }' $DAEMON_CONF)
       if [ ! -z "$MAPR_USER" ]; then
           chown -R ${MAPR_USER}:${MAPR_GROUP} __PREFIX__/schema-registry/schema-registry-${OLD_TIMESTAMP}
       fi
   fi
fi


%post
# $1 -eq 1 install
# $1 -eq 2 upgrade
# N/A     uninstall
[ -n "$VERBOSE" ] && echo "post install called with argument \`$1'" >&2
[ -n "$VERBOSE" ] && set -x ; :

rm -f __PREFIX__/lib/kafka-schema-registry-client*.jar
rm -f __PREFIX__/lib/kafka-connect-avro-converter*.jar
rm -f __PREFIX__/lib/kafka-avro-serializer*.jar
newJar=$(find __INSTALL_3DIGIT__/share/java/schema-registry/   -printf '%T+ %p\n'   | sort -r | grep kafka-schema-registry-client |  head -n 1 |  awk '{ print $2 }')
ln -sf $newJar  __PREFIX__/lib/.
newJarAvroConverter=$(find __INSTALL_3DIGIT__/share/java/schema-registry/   -printf '%T+ %p\n'   | sort -r | grep kafka-connect-avro-converter |  head -n 1 |  awk '{ print $2 }')
ln -sf $newJarAvroConverter  __PREFIX__/lib/.
newJarAvroSerializer=$(find __INSTALL_3DIGIT__/share/java/schema-registry/   -printf '%T+ %p\n'   | sort -r | grep kafka-avro-serializer |  head -n 1 |  awk '{ print $2 }')
ln -sf $newJarAvroSerializer  __PREFIX__/lib/.

#
# change permissions
#
DAEMON_CONF=__PREFIX__/conf/daemon.conf
if [ -f "$DAEMON_CONF" ]; then
    MAPR_USER=$( awk -F = '$1 == "mapr.daemon.user" { print $2 }' $DAEMON_CONF)
    MAPR_GROUP=$( awk -F = '$1 == "mapr.daemon.group" { print $2 }' $DAEMON_CONF)
    if [ ! -z "$MAPR_USER" ]; then
        chown -R ${MAPR_USER} __PREFIX__/schema-registry/
    fi
    if [ ! -z "$MAPR_GROUP" ]; then
        chgrp -R ${MAPR_GROUP} __PREFIX__/schema-registry/
    fi
fi

mkdir -p "__INSTALL_3DIGIT__"/logs
chmod 1777 "__INSTALL_3DIGIT__"/logs


%preun
# N/A     install
# $1 -eq 1 upgrade
# $1 -eq 0 uninstall
[ -n "$VERBOSE" ] && echo "preun install called with argument \`$1'" >&2
[ -n "$VERBOSE" ] && set -x ; :

if [ -f  __PREFIX__/schema-registry/schema-registryversion ]; then
    bash __PREFIX__/schema-registry/schema-registry-__VERSION_3DIGIT__/bin/schema-registry-stop-service
    if [ -d __PREFIX__/schema-registry/schema-registry-__VERSION_3DIGIT__/logs ]; then
        rm -rf __PREFIX__/schema-registry/schema-registry-__VERSION_3DIGIT__/logs
    fi
fi

if [ "$1" -eq "0" ]; then
  rm -f __PREFIX__/lib/kafka-schema-registry-client*.jar
  rm -f __PREFIX__/lib/kafka-connect-avro-converter*.jar
  rm -f __PREFIX__/lib/kafka-avro-serializer*.jar
fi

%postun
# N/A     install
# $1 -eq 1 upgrade
# $1 -eq 0 uninstall
[ -n "$VERBOSE" ] && echo "postun install called with argument \`$1'" >&2
[ -n "$VERBOSE" ] && set -x ; :

if [ "$1" = "0" ]; then
    rm -rf __PREFIX__/schema-registry
fi

%posttrans
# $1 -eq 0 install
# $1 -eq 0 upgrade
# N/A     uninstall
[ -n "$VERBOSE" ] && echo "posttrans install called with argument \`$1'" >&2
[ -n "$VERBOSE" ] && set -x ; :