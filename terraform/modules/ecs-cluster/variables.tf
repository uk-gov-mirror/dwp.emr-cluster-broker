# ---------------------------------------------------------------------------------------------------------------------
# AWS CREDENTIALS AND REGION
# ---------------------------------------------------------------------------------------------------------------------

variable "region" {
  type        = string
  description = "(Required) AWS Region where the infrastructure is hosted in"
}

# ---------------------------------------------------------------------------------------------------------------------
# ECS CLUSTER
# ---------------------------------------------------------------------------------------------------------------------
variable "name" {
  type        = string
  description = "(Required) The name of the cluster (up to 255 letters, numbers, hyphens, and underscores)"
}

variable "role_arn" {
  type        = string
  description = "(Required) The role to assume when doing an apply, defaults to ci"
}

variable "common_tags" {
  type        = map(string)
  description = "(Required) Common tags to apply to aws resources"
}
