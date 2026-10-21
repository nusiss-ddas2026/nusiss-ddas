variable "region" {
  type    = string
  default = "ap-southeast-1"
}

variable "instance_type" {
  type    = string
  default = "t4g.small"
}

variable "repo_url" {
  type        = string
  description = "Git URL the server clones. Must be publicly cloneable (or embed a read-only token)."
  default     = "https://github.com/nusiss-ddas2026/nusiss-ddas.git"
}

variable "repo_branch" {
  type    = string
  default = "main"
}

variable "github_repo" {
  type        = string
  description = "GitHub repo (owner/name) whose 'production' environment may deploy"
  default     = "nusiss-ddas2026/nusiss-ddas"
}

variable "create_github_oidc_provider" {
  type        = bool
  description = "Set to false if this AWS account already has a GitHub OIDC provider (only one is allowed per account)"
  default     = true
}
