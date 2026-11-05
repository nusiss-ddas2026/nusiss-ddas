ui            = false
api_addr      = "http://vault:8200"

storage "file" {
  path = "/vault/file"
}

# Plain HTTP is acceptable only because this port is never published: it is reachable
# solely from other containers on the internal Docker network.
listener "tcp" {
  address     = "0.0.0.0:8200"
  tls_disable = true
}

# Auto-unseal: Vault asks AWS KMS (using the EC2 instance role) to unwrap its master key,
# so no unseal key is stored on disk. The key id comes from VAULT_AWSKMS_SEAL_KEY_ID
# and the region from AWS_REGION.
seal "awskms" {}
