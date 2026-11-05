#!/bin/sh
# Runs once on first start. The app gets its own non-superuser that owns only the ddas database.
set -e
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<SQL
CREATE ROLE ddas LOGIN PASSWORD '${DB_PASSWORD}' NOSUPERUSER NOCREATEDB NOCREATEROLE;
CREATE DATABASE ddas OWNER ddas;
REVOKE ALL ON DATABASE ddas FROM PUBLIC;
SQL
