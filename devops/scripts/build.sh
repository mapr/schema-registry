#!/bin/bash
set -ex

SCRIPT_DIR=$(dirname "${BASH_SOURCE[0]}")
. "${SCRIPT_DIR}/_initialize_package_variables.sh"
. "${SCRIPT_DIR}/_utils.sh"

build_schema_registry() {
  mvn ${KAFKA_MAVEN_ARGS}
  tgz_name="./package-schema-registry/target/kafka-schema-registry-package-*-package.tar.gz"
  mkdir -p "${BUILD_ROOT}/build"
  tar xvf ${tgz_name} -C "${BUILD_ROOT}/build"
}

main() {
  echo "Cleaning '${BUILD_ROOT}' dir..."
  rm -rf "$BUILD_ROOT"

  if [ "$DO_DEPLOY" = "true" ] && [ "$OS" = "redhat" ]; then
    echo "Deploy is enabled"
    export KAFKA_MAVEN_ARGS="-DaltDeploymentRepository=${REPOSITORY_ID}::default::${MAPR_MAVEN_REPO} ${KAFKA_MAVEN_ARGS} deploy"
  fi

  echo "Building project..."
  build_schema_registry

  echo "Preparing directory structure..."
  setup_role "mapr-schema-registry"

  setup_package "mapr-schema-registry-internal"

  echo "Building packages..."
  build_package "mapr-schema-registry-internal"
  build_package "mapr-schema-registry"

  echo "Resulting packages:"
  find "$DIST_DIR" -exec readlink -f {} \;
}

main
