# ---------------------------------------------------------------------------------------------------------------------
# ECS Task Definition
# ---------------------------------------------------------------------------------------------------------------------
module "ecs-fargate-task-definition" {
  source                       = "../../../modules/fargate-task-definition"
  name_prefix                  = var.name_prefix
  region                       = var.vpc_region
  container_name               = var.name_prefix
  container_image              = local.container_image
  container_port               = var.container_port
  container_cpu                = var.container_cpu
  container_memory             = var.container_memory
  container_memory_reservation = var.container_memory_reservation
  common_tags                  = local.common_tags
  role_arn                     = "arn:aws:iam::${local.account[local.environment]}:role/${var.assume_role}"
  account                      = lookup(local.account, local.environment)
  log_configuration = {
    secretOptions = []
    logDriver     = "awslogs"
    options = {
      "awslogs-group"         = "/ecs/${data.aws_ecs_cluster.ecs_main_cluster.cluster_name}/${var.name_prefix}"
      "awslogs-region"        = var.vpc_region
      "awslogs-stream-prefix" = "ecs"
    }
  }
  environment = [
    {
      name  = "clusterBroker_awsRegion"
      value = var.vpc_region
    },
    {
      name  = "clusterBroker_amiSearchPattern"
      value = "*-emr-ami*"
    },
    {
      name  = "clusterBroker_amiOwnerIds"
      value = local.account["management"]
    },
    {
      name  = "clusterBroker_emrReleaseLabel"
      value = "emr-5.28.0"
    },
    {
      name  = "clusterBroker_s3LogUri"
      value = "TBC"
    },
    {
      name  = "clusterBroker_securityConfiguration"
      value = var.name_prefix
    },
    {
      name  = "clusterBroker_jobFlowRoleBlacklist"
      value = ""
    },
    {
      name  = "clusterBroker_jobFlowRole"
      value = module.iam-emr.cb_job_flow_role
    },
    {
      name  = "clusterBroker_serviceRole"
      value = module.iam-emr.cb_service_role
    },
    {
      name  = "clusterBroker_autoScalingRole"
      value = module.iam-emr.cb_autoscaling_role
    }
  ]
}
#
## ---------------------------------------------------------------------------------------------------------------------
## ECS Service
## ---------------------------------------------------------------------------------------------------------------------
module "ecs-fargate-service" {
  source          = "../../../modules/fargate-service"
  name_prefix     = var.name_prefix
  region          = var.vpc_region
  vpc_id          = data.terraform_remote_state.aws_emr_infra.outputs.vpc.aws_vpc
  private_subnets = data.terraform_remote_state.aws_emr_infra.outputs.vpc.aws_subnets_private.*.id

  ecs_cluster_name        = data.aws_ecs_cluster.ecs_main_cluster.cluster_name
  ecs_cluster_arn         = data.aws_ecs_cluster.ecs_main_cluster.arn
  task_definition_arn     = module.ecs-fargate-task-definition.aws_ecs_task_definition_td.arn
  container_name          = module.ecs-fargate-task-definition.container_name
  container_port          = module.ecs-fargate-task-definition.container_port
  desired_count           = var.desired_count
  platform_version        = var.platform_version
  security_groups         = var.security_groups
  enable_ecs_managed_tags = var.enable_ecs_managed_tags
  role_arn = {
    management-dns = "arn:aws:iam::${local.account[local.management_account[local.environment]]}:role/${var.assume_role}"
  }
  interface_vpce_sg_id = data.terraform_remote_state.aws_emr_infra.outputs.interface_vpce_sg_id
  s3_prefixlist_id     = data.terraform_remote_state.aws_emr_infra.outputs.s3_prefix_list_id
  common_tags          = local.common_tags
  parent_domain_name   = local.parent_domain_name[local.environment]
  root_dns_prefix      = local.root_dns_prefix[local.environment]
  cert_authority_arn   = data.terraform_remote_state.aws_certificate_authority.outputs.cert_authority.arn
}

# ---------------------------------------------------------------------------------------------------------------------
# EMR Security Configuration to be used by broker
# ---------------------------------------------------------------------------------------------------------------------
module emr-security-config {
  source       = "../../../modules/emr-security-configuration"
  name         = var.name_prefix
  emp_provider = "s3://${local.emr_encryption_materials_bucket}/encryption_materials_provider_jar/encryption-materials-provider-${local.emr_encryption_materials_version}.jar"
  region       = var.vpc_region
  role_arn     = "arn:aws:iam::${local.account[local.environment]}:role/${var.assume_role}"
  common_tags  = local.common_tags
}

# ---------------------------------------------------------------------------------------------------------------------
# DNS Lambda
# ---------------------------------------------------------------------------------------------------------------------

module dns-lambda {
  source   = "../../../modules/dns-lambda"
  name     = var.name_prefix
  region   = var.vpc_region
  role_arn = "arn:aws:iam::${local.account[local.environment]}:role/${var.assume_role}"
}

# ---------------------------------------------------------------------------------------------------------------------
# IAM EMR
# ---------------------------------------------------------------------------------------------------------------------
module iam-emr {
  source        = "../../../modules/iam-emr"
  name          = var.name_prefix
  region        = var.vpc_region
  role_arn      = "arn:aws:iam::${local.account[local.environment]}:role/${var.assume_role}"
  common_tags   = local.common_tags
  ebs_cmk       = data.terraform_remote_state.security-tools.outputs.ebs_cmk.arn
  ingest_bucket = data.terraform_remote_state.ingestion.outputs.s3_buckets.input_bucket
}

