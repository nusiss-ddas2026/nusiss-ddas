#!/bin/sh
set -eu

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
CERT_DIR="$SCRIPT_DIR/certs"
CRT="$CERT_DIR/server.crt"
KEY="$CERT_DIR/server.key"

mkdir -p "$CERT_DIR"

if [ -f "$CRT" ] && [ -f "$KEY" ]; then
  echo "Certs already exist at $CERT_DIR — skipping generation."
  exit 0
fi

openssl req -x509 -newkey rsa:2048 -nodes -days 825 \
  -keyout "$KEY" -out "$CRT" \
  -subj "/CN=postgres" \
  -addext "subjectAltName=DNS:postgres,DNS:localhost"

chmod 600 "$KEY"

echo "Generated self-signed dev cert/key in $CERT_DIR"
