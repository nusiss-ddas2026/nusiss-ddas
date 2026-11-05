# KMS key used ONLY by Vault to auto-unseal.
resource "aws_kms_key" "vault_unseal" {
  description             = "DDAS Vault auto-unseal"
  enable_key_rotation     = true
  deletion_window_in_days = 7
}

resource "aws_kms_alias" "vault_unseal" {
  name          = "alias/ddas-vault-unseal"
  target_key_id = aws_kms_key.vault_unseal.key_id
}

# Role assumed by the EC2 instance itself (never by people).
resource "aws_iam_role" "server" {
  name = "ddas-server"
  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect    = "Allow"
      Action    = "sts:AssumeRole"
      Principal = { Service = "ec2.amazonaws.com" }
    }]
  })
}

# Lets the owner open a shell through Session Manager (no SSH, no key pairs).
resource "aws_iam_role_policy_attachment" "ssm" {
  role       = aws_iam_role.server.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# Least privilege: the server may use exactly one KMS key, and only for unsealing.
resource "aws_iam_role_policy" "vault_unseal" {
  name = "vault-unseal-kms"
  role = aws_iam_role.server.id
  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Effect   = "Allow"
      Action   = ["kms:Encrypt", "kms:Decrypt", "kms:DescribeKey"]
      Resource = aws_kms_key.vault_unseal.arn
    }]
  })
}

resource "aws_iam_instance_profile" "server" {
  name = "ddas-server"
  role = aws_iam_role.server.name
}

data "aws_vpc" "default" {
  default = true
}

# Only web traffic in. No SSH, no database or Vault ports.
resource "aws_security_group" "web" {
  name        = "ddas-web"
  description = "HTTP/HTTPS only"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    description = "HTTP (redirects to HTTPS, used for certificate issuance)"
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  ingress {
    description = "HTTPS"
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }
  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }
}
