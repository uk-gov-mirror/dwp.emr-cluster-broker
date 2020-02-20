variable "name" {
  description = "(Required) common name"
  type        = string
}

variable "emp_provider" {
  description = "(Required) Path to encrpytion material provider in S3 (including bucket)"
  type        = string
}

variable "role_arn" {
  description = "(Required) role arns for in module providers"
  type        = string
}

variable "region" {
  description = "(Required) The region"
  type        = string
}

variable "common_tags" {
  description = "(Required) Common tags to apply to aws resources"
  type        = map(string)
}
