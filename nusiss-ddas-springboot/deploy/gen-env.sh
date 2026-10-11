#!/bin/bash
# Generates deploy/.env with random secrets. Usage: DOMAIN=... KMS_KEY_ID=... AWS_REGION=... ./gen-env.sh
set -euo pipefail
cd "$(dirname "$0")"
[ -e .env ] && { echo ".env already exists; refusing to overwrite." >&2; exit 1; }
: "${DOMAIN:?}" "${KMS_KEY_ID:?}" "${AWS_REGION:?}"

rand() { openssl rand -base64 36 | tr -d '/+=\n' | cut -c1-32; }

umask 077
cat > .env <<ENV
DOMAIN=$DOMAIN
AWS_REGION=$AWS_REGION
VAULT_AWSKMS_SEAL_KEY_ID=$KMS_KEY_ID
POSTGRES_ADMIN_PASSWORD=$(rand)
DB_PASSWORD=$(rand)
JWT_SECRET=$(openssl rand -base64 48 | tr -d '\n')
SEED_ADMIN_PASSWORD=$(rand)
SEED_USER_PASSWORD=$(rand)
SEED_MANAGER_PASSWORD=$(rand)
VAULT_APP_TOKEN=
ENV
echo "Wrote $(pwd)/.env (mode 600). Seeded logins (admin/user/manager) use the SEED_* passwords in that file."
