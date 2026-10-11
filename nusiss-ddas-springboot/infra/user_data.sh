#!/bin/bash
# Runs once as root on first boot (rendered by Terraform). Log: /var/log/cloud-init-output.log
set -euxo pipefail

# Swap: the Maven build + JVM + Postgres + Vault is tight on 2 GB
fallocate -l 2G /swapfile && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab

apt-get update -y
apt-get install -y docker.io docker-compose-v2 git jq
systemctl enable --now docker

git clone --branch ${repo_branch} ${repo_url} /opt/ddas
cd /opt/ddas/nusiss-ddas-springboot/deploy

DOMAIN=${domain} KMS_KEY_ID=${kms_key_id} AWS_REGION=${region} ./gen-env.sh
docker compose -f docker-compose.prod.yml up -d --build
