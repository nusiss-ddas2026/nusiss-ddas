#!/bin/bash
# One-time Vault bootstrap. Run on the server as root: sudo ./setup.sh
# Initialises Vault, enables transit + audit log, creates a least-privilege app token,
# stores it in deploy/.env, restarts the app, then revokes the root token.
set -euo pipefail
cd "$(dirname "$0")/.."
[ "$(id -u)" -eq 0 ] || { echo "Run with sudo." >&2; exit 1; }
command -v jq >/dev/null || { echo "jq is required." >&2; exit 1; }

dc() { docker compose -f docker-compose.prod.yml "$@"; }
# vault <token> <args...>: run the vault CLI inside the container
vault() { local t="$1"; shift; dc exec -T -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN="$t" vault vault "$@"; }

echo "Waiting for Vault..."
for _ in $(seq 1 30); do
  # status exits 0 (unsealed), 2 (sealed/uninitialised) and 1 only on connection errors
  rc=0; vault "" status >/dev/null 2>&1 || rc=$?
  [ "$rc" -ne 1 ] && break
  sleep 2
done

if vault "" status -format=json | jq -e '.initialized' >/dev/null; then
  echo "Vault is already initialised; refusing to run setup again." >&2
  exit 1
fi

INIT=$(vault "" operator init -recovery-shares=1 -recovery-threshold=1 -format=json)
ROOT=$(echo "$INIT" | jq -r .root_token)
RECOVERY=$(echo "$INIT" | jq -r '.recovery_keys_b64[0]')

# Auto-unseal via KMS may take a moment to finish
for _ in $(seq 1 20); do
  vault "" status -format=json | jq -e '.sealed == false' >/dev/null && break
  sleep 1
done

vault "$ROOT" secrets enable transit
vault "$ROOT" audit enable file file_path=/vault/logs/audit.log

# Least privilege: only per-approver signing keys under the document-signing- prefix.
dc exec -T -e VAULT_ADDR=http://127.0.0.1:8200 -e VAULT_TOKEN="$ROOT" vault vault policy write ddas-app - <<'POLICY'
path "transit/keys/document-signing-*"   { capabilities = ["create", "read", "update"] }
path "transit/sign/document-signing-*"   { capabilities = ["update"] }
path "transit/verify/document-signing-*" { capabilities = ["update"] }
# read-only, lets the app confirm transit is mounted at startup
path "sys/mounts"                        { capabilities = ["read"] }
POLICY

APP_TOKEN=$(vault "$ROOT" token create -policy=ddas-app -orphan -period=768h \
  -display-name=ddas-app -format=json | jq -r .auth.client_token)

sed -i "s|^VAULT_APP_TOKEN=.*|VAULT_APP_TOKEN=${APP_TOKEN}|" .env

# Periodic tokens must be renewed or they expire; do it daily.
cat > /etc/cron.d/ddas-vault-renew <<CRON
0 3 * * * root $(pwd)/vault/renew-token.sh >> /var/log/ddas-vault-renew.log 2>&1
CRON
chmod 644 /etc/cron.d/ddas-vault-renew

vault "$ROOT" token revoke -self
dc up -d --force-recreate app

cat <<MSG

Vault is initialised, transit is enabled, the audit log is on and the root token is revoked.
The app token is stored in deploy/.env and renewed daily by cron.

Save this RECOVERY KEY offline now (it is not stored anywhere on the server).
It is only needed to generate a new root token in an emergency:

  $RECOVERY
MSG
