Schema Registry
================

Schema Registry provides a serving layer for your metadata. It provides a
RESTful interface for storing and retrieving Avro schemas. It stores a versioned
history of all schemas, provides multiple compatibility settings and allows
evolution of schemas according to the configured compatibility setting. It
provides serializers that plug into Kafka clients that handle schema storage and
retrieval for Kafka messages that are sent in the Avro format.

Quickstart
----------

The following assumes you have Kafka and an instance of the Schema Registry running using the default settings.

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

License
-------

The project is licensed under the [Confluent Community License](LICENSE-ConfluentCommunity), except for client libs,
which is under the [Apache 2.0 license](LICENSE-Apache).
See LICENSE file in each subfolder for detailed license agreement.
