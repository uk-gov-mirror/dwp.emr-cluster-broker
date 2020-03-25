# ---------------------------------------------------------------------------------------------------------------------
# AWS ROLES AND REGION
# ---------------------------------------------------------------------------------------------------------------------

variable "assume_role" {
  type        = string
  description = "(Required) The role to assume when doing an apply, defaults to ci"
}

variable "region" {
  type        = string
  description = "(Required) The region to deploy into, defaults to eu-west-2 (London)"
}

variable "vpc_region" {
  type        = string
  description = "(Required) The region the VPC we are deploying into is in, defaults to eu-west-2 (London)"
}

variable "emr_cluster_broker_client_zip" {
  type = object({
    base_path = string
    version   = string
  })
  description = "(Required) the zipped Lambda package containing the EMR Cluster Broker client"
}
