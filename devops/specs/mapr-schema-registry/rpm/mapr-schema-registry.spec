%undefine __check_files

summary:     Ezmeral Ecosystem Pack: Kafka Schema Registry
license:     Hewlett Packard Enterprise, CopyRight
Vendor:      Hewlett Packard Enterprise, <ezmeral_software_support@hpe.com>
name:        mapr-schema-registry
version:     __RELEASE_VERSION__
release:     1
prefix:      /
group:       MapR
buildarch:   noarch
requires:    mapr-schema-registry-internal = __RELEASE_VERSION__
AutoReqProv: no

%description
Ezmeral Ecosystem Pack: Kafka Schema Registry package
Tag: __RELEASE_BRANCH__
Commit: __GIT_COMMIT__


%clean
echo "NOOP"


%files
__PREFIX__/roles

%post
##
## If this is an UPGRADE, ...
##
if [ "$1" = "2" ]; then
    echo "POSTINST upgrade"
    # Tell posttrans that it is ok to reinstall the warden conf file
    touch %{_localstatedir}/lib/rpm-state/upgrade
fi

#remove old warden conf
if [ -f __PREFIX__/conf/conf.d/warden.schemaregistry.conf ]; then
    rm -f __PREFIX__/conf/conf.d/warden.schemaregistry.conf
fi

if [ -h __PREFIX__/pid/schemaregistry.pid ]; then
    rm -f __PREFIX__/pid/schemaregistry.pid
fi

mkdir -p "__INSTALL_3DIGIT__"/etc/schema-registry/
if [ "$1" = "1" ]; then
  touch "__INSTALL_3DIGIT__/etc/schema-registry/.not_configured_yet"
fi


%preun
PID_FILE=__PREFIX__/pid/schemaregistry.pid
if [ ! -z "$PID_FILE" ] && [ -f "$PID_FILE" ] && [ -s "$PID_FILE" ] && [ -r "$PID_FILE" ]; then
    PID=`cat "$PID_FILE"`
    ps -p $PID >/dev/null 2>&1
    if [ $? -eq 0 ] ; then
        kill $PID >/dev/null 2>&1
        rm -f $PID_FILE >/dev/null 2>&1
    fi
fi


%postun

#
# If this is an uninstall, ....
#
if [ "$1" = "0" ]; then
    if [ -f __PREFIX__/conf/conf.d/warden.schemaregistry.conf ]; then
        rm -Rf __PREFIX__/conf/conf.d/warden.schemaregistry.conf
    fi
fi

if [ -h __PREFIX__/pid/schemaregistry.pid ]; then
    rm -f __PREFIX__/pid/schemaregistry.pid
fi


%posttrans

# On an upgrade install new warden file into /opt/mapr/conf/conf.d.new
# Customer can merge when convenient
if [ -d __PREFIX__/conf/conf.d.new -a -f %{_localstatedir}/lib/rpm-state/upgrade ]; then
    cp -Rp __INSTALL_3DIGIT__/conf/warden.schemaregistry.conf __PREFIX__/conf/conf.d.new/.
    rm %{_localstatedir}/lib/rpm-state/upgrade
fi

