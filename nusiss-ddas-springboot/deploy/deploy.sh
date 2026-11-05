#!/bin/bash
# Deploys one specific commit that CI has already tested. Run as root on the server:
#   ./deploy.sh <40-char commit sha>
# The GitHub "Deploy" workflow calls this through AWS Systems Manager (no SSH).
set -euo pipefail

SHA="${1:?usage: deploy.sh <commit sha>}"
[[ "$SHA" =~ ^[0-9a-f]{40}$ ]] || { echo "not a full commit sha: $SHA" >&2; exit 1; }

REPO=/opt/ddas
DEPLOY_DIR=$REPO/nusiss-ddas-springboot/deploy

# One deploy at a time (the lock is kept across the re-exec below)
exec 9>/var/lock/ddas-deploy.lock
flock -n 9 || { echo "another deploy is already running" >&2; exit 1; }

cd "$REPO"

if [ "${DDAS_STAGE:-1}" = 1 ]; then
  PREV=$(git rev-parse HEAD)
  git fetch --quiet origin main
  # Only ever deploy code that is on main
  git merge-base --is-ancestor "$SHA" origin/main \
    || { echo "refusing: $SHA is not on origin/main" >&2; exit 1; }
  git reset --hard "$SHA"
  # Re-run the freshly checked-out copy so changes to this script apply immediately
  DDAS_STAGE=2 DDAS_PREV="$PREV" exec bash "$DEPLOY_DIR/deploy.sh" "$SHA"
fi

PREV="${DDAS_PREV:?}"
cd "$DEPLOY_DIR"
dc() { docker compose -f docker-compose.prod.yml "$@"; }
DOMAIN=$(grep '^DOMAIN=' .env | cut -d= -f2-)

# Ask Caddy for the login page locally (resolves the domain to this machine, so no outside round trip)
healthy() {
  for _ in $(seq 1 36); do
    curl -fsS --max-time 5 --resolve "$DOMAIN:443:127.0.0.1" "https://$DOMAIN/login" -o /dev/null && return 0
    sleep 5
  done
  return 1
}

echo "Deploying $SHA (previous: $PREV)"
if dc up -d --build && healthy; then
  docker image prune -f >/dev/null
  echo "Deploy OK: $SHA"
else
  echo "Deploy FAILED, rolling back to $PREV" >&2
  git -C "$REPO" reset --hard "$PREV"
  dc up -d --build
  healthy || echo "WARNING: the rollback is also unhealthy" >&2
  exit 1
fi
