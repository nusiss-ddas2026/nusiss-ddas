#!/bin/bash
# Renews the app's periodic Vault token (run daily by /etc/cron.d/ddas-vault-renew).
set -euo pipefail
cd "$(dirname "$0")/.."
TOKEN=$(grep '^VAULT_APP_TOKEN=' .env | cut -d= -f2-)
docker compose -f docker-compose.prod.yml exec -T \
  -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN="$TOKEN" vault vault token renew-self >/dev/null
echo "$(date -u +%FT%TZ) renewed"
