# ---------------------------------------------------------------------------------------------------------------------
# Misc
# ---------------------------------------------------------------------------------------------------------------------
variable "name" {
  type        = string
  description = "(Required) Name prefix for resources we create, defaults to repository name"
}

# ---------------------------------------------------------------------------------------------------------------------
# AWS CREDENTIALS AND REGION
# ---------------------------------------------------------------------------------------------------------------------

variable "region" {
  type        = string
  description = "(Required) The region to deploy into, defaults to eu-west-2 (London)"
}

variable "ebs_cmk" {
  type        = string
  description = "(Required) The region to deploy into, defaults to eu-west-2 (London)"
}

variable "common_tags" {
  type        = map(string)
  description = "(Required) common tags to apply to aws resources"
}

variable "role_arn" {
  type        = string
  description = "(Required) The role to assume when doing an apply, defaults to ci"
}

variable "ingest_bucket" {
  type        = string
  description = "(Required) The role to assume when doing an apply, defaults to ci"
}
