#!/bin/sh
set -eu

mkdir -p /var/lib/postgresql/certs
cp /certs-ro/server.crt /certs-ro/server.key /var/lib/postgresql/certs/
chown postgres:postgres /var/lib/postgresql/certs/server.crt /var/lib/postgresql/certs/server.key
chmod 644 /var/lib/postgresql/certs/server.crt
chmod 600 /var/lib/postgresql/certs/server.key

exec docker-entrypoint.sh postgres \
  -c ssl=on \
  -c ssl_cert_file=/var/lib/postgresql/certs/server.crt \
  -c ssl_key_file=/var/lib/postgresql/certs/server.key
