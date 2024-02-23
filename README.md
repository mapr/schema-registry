Schema Registry
================

Confluent Schema Registry provides a serving layer for your metadata. It provides a RESTful interface for storing and retrieving your Avro®, JSON Schema, and Protobuf schemas. It stores a versioned history of all schemas based on a specified subject name strategy, provides multiple compatibility settings and allows evolution of schemas according to the configured compatibility settings and expanded support for these schema types. It provides serializers that plug into Apache Kafka® clients that handle schema storage and retrieval for Kafka messages that are sent in any of the supported formats.

This README includes the following sections:

<!-- START doctoc generated TOC please keep comment here to allow auto update -->
<!-- DON'T EDIT THIS SECTION, INSTEAD RE-RUN doctoc TO UPDATE -->
<!-- generated with [DocToc](https://github.com/thlorenz/doctoc), see the link for install and instructions for use -->

- [Documentation](#documentation)
- [Quickstart API Usage examples](#quickstart-api-usage-examples)
- [Installation](#installation)
- [Deployment](#deployment)
- [Development](#development)
- [OpenAPI Spec](#openapi-spec)
- [Contribute](#contribute)
- [License](#license)

<!-- END doctoc generated TOC please keep comment here to allow auto update -->

Documentation
-------------

Here are a few links to Schema Registry pages in the Confluent Documentation.

- [Installing and Configuring Schema Registry](https://docs.confluent.io/current/schema-registry/installation/index.html)
- [Schema Management overview](https://docs.confluent.io/current/schema-registry/index.html)
- [Schema Registry Tutorial](https://docs.confluent.io/current/schema-registry/schema_registry_tutorial.html)
- [Schema Registry API reference](https://docs.confluent.io/current/schema-registry/develop/api.html)
- [Serializers, Deserializers for supported schema types](https://docs.confluent.io/current/schema-registry/serializer-formatter.html)
- [Kafka Clients](https://docs.confluent.io/current/clients/index.html#kafka-clients)
- [Schema Registry on Confluent Cloud](https://docs.confluent.io/current/quickstart/cloud-quickstart/schema-registry.html)

Quickstart API Usage examples
-----------------------------

The following assumes you have Kafka and an [instance of the Schema Registry](https://docs.confluent.io/current/schema-registry/installation/index.html)
running using the default settings. These examples, and more, are also available at [API Usage examples](https://docs.confluent.io/current/schema-registry/using.html) on [docs.confluent.io](https://docs.confluent.io/current/).

#### Secure cluster (Basic Auth):
- ensure your URL has `https` protocol and correct domain (FQDN): `https://$(hostname):8087/subjects`
- add `-u <user> ...` to enter password by prompt or `-u <user>:<password>` to avoid prompt
- specify certificate by `--cacert <pem_certificate>`, e.g. `--cacert /opt/mapr/conf/ssl_truststore.pem`.

```bash
$ curl -u mapr --cacert /opt/mapr/conf/ssl_truststore.pem -X GET "https://$(hostname):8087/subjects"
Enter host password for user 'mapr': # mapr
  ["Kafka-value","Kafka-key"]
  
$ curl -u mapr:mapr --cacert /opt/mapr/conf/ssl_truststore.pem -X GET "https://$(hostname):8087/subjects"
  ["Kafka-value","Kafka-key"]
```

#### Secure cluster (Maprsasl Auth):
- ensure your URL has `https` protocol and correct domain (FQDN): `https://$(hostname):8087/subjects`
- specify certificate by `--cacert <pem_certificate>`, e.g. `--cacert /opt/mapr/conf/ssl_truststore.pem`.
- obtain Maprsasl challenge string
```bash
$ mapr com.mapr.security.client.examples.MapRClient gettoken -cluster cyber.mapr.cluster -url "https://$(hostname):8090/ws/"
  Using cluster name cyber.mapr.cluster
  Obtained challenge string CihbshT+2Av5Rx5nUsnk1Gx2C9nrDz6BFHpjbnwrSh6n2tacNbMMcSg4EnMCCAFCGpsBxNOolozsuIPhwgfkEl8rA0lEwLbCQEmyu+W6ldM3wBaPvnmw0H0zC0bM/DhSrkKCdO04FY4fffRozIZ8eAigw7Z9jHv+3wozJjy/b4HsR/9I/BG0EYMzTSwK/XUpcLafFl+O5ZQkSVQWVPId
  Sending challenge to server
  Obtained Base-64 encoded response ... # ignore
  Extracting token from response message
  Found token in response message
  Obtained token: ... # ignore

$ curl --cacert /opt/mapr/conf/ssl_truststore.pem \
    -H "Authorization: MAPR-Negotiate CihbshT+2Av5Rx5nUsnk1Gx2C9nrDz6BFHpjbnwrSh6n2tacNbMMcSg4EnMCCAFCGpsBxNOolozsuIPhwgfkEl8rA0lEwLbCQEmyu+W6ldM3wBaPvnmw0H0zC0bM/DhSrkKCdO04FY4fffRozIZ8eAigw7Z9jHv+3wozJjy/b4HsR/9I/BG0EYMzTSwK/XUpcLafFl+O5ZQkSVQWVPId" \
    -X GET "https://$(hostname):8087/subjects"
  ["Kafka-value","Kafka-key"]
```

#### Secure cluster (reusing Cookie):
- ensure your URL has `https` protocol and correct domain (FQDN): `https://$(hostname):8087/subjects`
- specify certificate by `--cacert <pem_certificate>`, e.g. `--cacert /opt/mapr/conf/ssl_truststore.pem`.
- make a secure request by Basic or Maprsasl Auth to save cookies in a file and then use the file in next call
```bash
# -c, --cookie-jar  stores cookies to file
# -b, --cookie      adds cookies from file or text
$ curl -u mapr:mapr --cacert /opt/mapr/conf/ssl_truststore.pem -X GET "https://$(hostname):8087/subjects" -c /tmp/mapr-auth-cookies.txt
  ["Kafka-value","Kafka-key"]
$ curl --cacert /opt/mapr/conf/ssl_truststore.pem -X GET "https://$(hostname):8087/subjects" -b /tmp/mapr-auth-cookies.txt
  ["Kafka-value","Kafka-key"]
```
- or make a request with `--verbose, -v` option, copy and paste cookie manually
```bash
$ curl -u mapr:mapr --cacert /opt/mapr/conf/ssl_truststore.pem -X GET "https://$(hostname):8087/subjects" -v
  ... # <curl output>
  Set-Cookie: hadoop.auth="u=mapr&p=mapr&t=multiauth&e=1560883274071&s=LqjBljNg1TePAcwRQDe5bnUUwJ4="; # ignore rest of the line
  ... # <curl output>
  
$ curl --cacert /opt/mapr/conf/ssl_truststore.pem \
    --cookie 'hadoop.auth="u=mapr&p=mapr&t=multiauth&e=1560883274071&s=LqjBljNg1TePAcwRQDe5bnUUwJ4="' \
    -X GET "https://$(hostname):8087/subjects"
  ["Kafka-value","Kafka-key"]
```

#### Insecure cluster:
- use `http` protocol, e.g. `http://localhost:8087/subjects`.
```bash
$ curl -X GET http://localhost:8087/subjects
  ["Kafka-value","Kafka-key"]
```


#### More examples:
The examples below work for secure cluster.
Changing `https` protocol to `http` would be enough to use on insecure cluster.
```bash
# Register a new version of a schema under the subject "Kafka-key"
$ curl -u mapr:mapr --cacert /opt/mapr/conf/ssl_truststore.pem \
    -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"schema": "{\"type\": \"string\"}"}' \
    "https://$(hostname):8087/subjects/Kafka-key/versions"
  {"id":1}

# Register a new version of a schema under the subject "Kafka-value"
$ curl -u mapr:mapr --cacert /opt/mapr/conf/ssl_truststore.pem \
    -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"schema": "{\"type\": \"string\"}"}' \
     "https://$(hostname):8087/subjects/Kafka-value/versions"
  {"id":1}

# List all subjects
$ curl -u mapr:mapr --cacert /opt/mapr/conf/ssl_truststore.pem \
    -X GET "https://$(hostname):8087/subjects"
  ["Kafka-value","Kafka-key"]

# List all schema versions registered under the subject "Kafka-value"
$ curl -u mapr:mapr --cacert /opt/mapr/conf/ssl_truststore.pem \
    -X GET "https://$(hostname):8087/subjects/Kafka-value/versions"
  [1]

# Fetch a schema by globally unique id 1
$ curl -u mapr:mapr --cacert /opt/mapr/conf/ssl_truststore.pem \
    -X GET "https://$(hostname):8087/schemas/ids/1"
  {"schema":"\"string\""}

# Fetch version 1 of the schema registered under subject "Kafka-value"
$ curl -u mapr:mapr --cacert /opt/mapr/conf/ssl_truststore.pem \
    -X GET "https://$(hostname):8087/subjects/Kafka-value/versions/1"
  {"subject":"Kafka-value","version":1,"id":1,"schema":"\"string\""}

# Fetch the most recently registered schema under subject "Kafka-value"
$ curl -u mapr:mapr --cacert /opt/mapr/conf/ssl_truststore.pem \
    -X GET "https://$(hostname):8087/subjects/Kafka-value/versions/latest"
  {"subject":"Kafka-value","version":1,"id":1,"schema":"\"string\""}

# Delete version 3 of the schema registered under subject "Kafka-value"
$ curl -u mapr:mapr --cacert /opt/mapr/conf/ssl_truststore.pem \
    -X DELETE "https://$(hostname):8087/subjects/Kafka-value/versions/3"
  3

# Delete all versions of the schema registered under subject "Kafka-value"
$ curl -u mapr:mapr --cacert /opt/mapr/conf/ssl_truststore.pem \
    -X DELETE "https://$(hostname):8087/subjects/Kafka-value"
  [1, 2, 3, 4, 5]

# Check whether a schema has been registered under subject "Kafka-key"
$ curl -u mapr:mapr --cacert /opt/mapr/conf/ssl_truststore.pem \
    -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"schema": "{\"type\": \"string\"}"}' \
    "https://$(hostname):8087/subjects/Kafka-key"
  {"subject":"Kafka-key","version":1,"id":1,"schema":"\"string\""}

# Test compatibility of a schema with the latest schema under subject "Kafka-value"
$ curl -u mapr:mapr --cacert /opt/mapr/conf/ssl_truststore.pem \
    -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"schema": "{\"type\": \"string\"}"}' \
    "https://$(hostname):8087/compatibility/subjects/Kafka-value/versions/latest"
  {"is_compatible":true}

# Get top level config
$ curl -u mapr:mapr --cacert /opt/mapr/conf/ssl_truststore.pem \
    -X GET "https://$(hostname):8087/config"
  {"compatibilityLevel":"BACKWARD"}

# Update compatibility requirements globally
$ curl -u mapr:mapr --cacert /opt/mapr/conf/ssl_truststore.pem \
    -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"compatibility": "NONE"}' \
    "https://$(hostname):8087/config"
  {"compatibility":"NONE"}

# Update compatibility requirements under the subject "Kafka-value"
$ curl -u mapr:mapr --cacert /opt/mapr/conf/ssl_truststore.pem \
    -X PUT -H "Content-Type: application/vnd.schemaregistry.v1+json" \
    --data '{"compatibility": "BACKWARD"}' \
    "https://$(hostname):8087/config/Kafka-value"
  {"compatibility":"BACKWARD"}
```

Installation
------------

You can download prebuilt versions of the schema registry as part of the
[Confluent Platform](http://confluent.io/downloads/). To install from source,
follow the instructions in the Development section.

Deployment
----------

The REST interface to schema registry includes a built-in Jetty server. The
wrapper scripts ``bin/schema-registry-start`` and ``bin/schema-registry-stop``
are the recommended method of starting and stopping the service.

Development
-----------

To build a development version, you may need a development versions of
[common](https://github.com/confluentinc/common) and
[rest-utils](https://github.com/confluentinc/rest-utils).  After
installing these, you can build the Schema Registry
with Maven.

This project uses the [Google Java code style](https://google.github.io/styleguide/javaguide.html)
to keep code clean and consistent.

To build:

```bash
mvn compile
```

To run the unit and integration tests:

```bash
mvn test
```

To run an instance of Schema Registry against a local Kafka cluster (using the default configuration included with Kafka):

```bash
mvn exec:java -pl :kafka-schema-registry -Dexec.args="config/schema-registry.properties"
```

To create a packaged version, optionally skipping the tests:

```bash
mvn package [-DskipTests]
```

It produces:

- Schema registry in `package-schema-registry/target/kafka-schema-registry-package-$VERSION-package`
- Serde tools for avro/json/protobuf in `package-kafka-serde-tools/target/kafka-serde-tools-package-$VERSION-package`

Each of the produced contains a directory layout similar to the packaged binary versions.

You can also produce a standalone fat JAR of schema registry using the `standalone` profile:

```bash
mvn package -P standalone [-DskipTests]
```

This generates `package-schema-registry/target/kafka-schema-registry-package-$VERSION-standalone.jar`, which includes all the dependencies as well.

OpenAPI Spec
------------

OpenAPI (formerly known as Swagger) specifications are built automatically using `swagger-maven-plugin`
on `compile` phase.


Contribute
----------

Thanks for helping us to make Schema Registry even better!

- Source Code: https://github.com/confluentinc/schema-registry
- Issue Tracker: https://github.com/confluentinc/schema-registry/issues

License
-------

The project is licensed under the [Confluent Community License](LICENSE-ConfluentCommunity), except for the `client-*` and `avro-*` libs,
which are under the [Apache 2.0 license](LICENSE-Apache).
See LICENSE file in each subfolder for detailed license agreement.
