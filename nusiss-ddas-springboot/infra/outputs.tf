output "url" {
  value = "https://${local.domain}"
}

output "instance_id" {
  value = aws_instance.server.id
}

output "shell" {
  description = "Open a shell on the server (needs the AWS CLI + Session Manager plugin)"
  value       = "aws ssm start-session --region ${var.region} --target ${aws_instance.server.id}"
}

output "kms_key_id" {
  value = aws_kms_key.vault_unseal.key_id
}
