# Elastic IP first, so the sslip.io hostname is known before the instance boots.
resource "aws_eip" "server" {
  domain = "vpc"
}

locals {
  domain = "${replace(aws_eip.server.public_ip, ".", "-")}.sslip.io"
}

resource "aws_instance" "server" {
  ami                    = data.aws_ami.ubuntu.id
  instance_type          = var.instance_type
  iam_instance_profile   = aws_iam_instance_profile.server.name
  vpc_security_group_ids = [aws_security_group.web.id]

  root_block_device {
    volume_size = 20
    volume_type = "gp3"
    encrypted   = true
  }

  metadata_options {
    http_endpoint = "enabled"
    http_tokens   = "required" # IMDSv2 only
    # 2 so containers (one extra network hop) can reach the instance role for KMS unseal
    http_put_response_hop_limit = 2
  }

  user_data = templatefile("${path.module}/user_data.sh", {
    repo_url    = var.repo_url
    repo_branch = var.repo_branch
    domain      = local.domain
    kms_key_id  = aws_kms_key.vault_unseal.key_id
    region      = var.region
  })

  tags = { Name = "ddas" }
}

resource "aws_eip_association" "server" {
  instance_id   = aws_instance.server.id
  allocation_id = aws_eip.server.id
}
