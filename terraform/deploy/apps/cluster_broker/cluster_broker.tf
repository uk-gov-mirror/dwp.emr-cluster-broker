# ---------------------------------------------------------------------------------------------------------------------
# ECS Cluster
# ---------------------------------------------------------------------------------------------------------------------
module ecs-cluster {
  source      = "../../../modules/ecs-cluster"
  name        = var.name_prefix
  region      = var.vpc_region
  role_arn    = "arn:aws:iam::${lookup(local.account, local.environment)}:role/${var.assume_role}"
  common_tags = local.common_tags
}

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
  log_configuration            = var.log_configuration
  role_arn                     = "arn:aws:iam::${local.account[local.environment]}:role/${var.assume_role}"
  account                      = lookup(local.account, local.environment)
  environment = [
    {
      name  = "AWS_REGION"
      value = var.vpc_region
    },
    {
      name  = "AMI_SEARCH_PATTERN"
      value = "*-emr-ami*"
    },
    {
      name  = "AMI_OWNER_IDS"
      value = "${lookup(local.account, "management")}"
    },
    {
      name  = "EMR_RELEASE_LABEL"
      value = "emr-5.28.0"
    },
    {
      name  = "S3_LOG_URI"
      value = "TBC"
    },
    {
      name  = "SECURITY_CONFIGURATION"
      value = var.name_prefix
    },
    {
      name  = "JOB_FLOW_ROLE_BLACKLIST"
      value = ""
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

  ecs_cluster_name    = module.ecs-cluster.aws_ecs_cluster_cluster.name
  ecs_cluster_arn     = module.ecs-cluster.aws_ecs_cluster_cluster.arn
  task_definition_arn = module.ecs-fargate-task-definition.aws_ecs_task_definition_td.arn
  container_name      = module.ecs-fargate-task-definition.container_name
  container_port      = module.ecs-fargate-task-definition.container_port
  desired_count       = var.desired_count
  platform_version    = var.platform_version
  security_groups     = var.security_groups
  // lb_health_check_path               = var.lb_health_check_path
  enable_ecs_managed_tags = var.enable_ecs_managed_tags
  // health_check_grace_period_seconds  = var.health_check_grace_period_seconds
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
  role_arn     = "arn:aws:iam::${lookup(local.account, local.environment)}:role/${var.assume_role}"
  common_tags  = local.common_tags
}
