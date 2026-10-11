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
  default     = "https://github.com/sigmund-c/nusiss-ddas.git"
}

variable "repo_branch" {
  type    = string
  default = "main"
}
