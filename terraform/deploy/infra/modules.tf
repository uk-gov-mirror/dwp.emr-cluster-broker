module cluster_broker_vpc {
  source  = "dwp/vpc/aws"
  version = "2.0.1"

  common_tags                                = local.common_tags
  gateway_vpce_route_table_ids               = module.networking.outputs.aws_route_table_private_ids
  interface_vpce_source_security_group_count = 0
  interface_vpce_source_security_group_ids   = []
  interface_vpce_subnet_ids                  = module.networking.outputs.aws_subnets_private[*].id
  region                                     = var.vpc_region
  vpc_cidr_block                             = local.cidr_block[local.environment].emr-cluster-broker-vpc
  vpc_name                                   = local.name

  dynamodb_endpoint    = false
  ecrapi_endpoint      = false
  ecrdkr_endpoint      = true
  ec2_endpoint         = true
  ec2messages_endpoint = true
  glue_endpoint        = true
  kms_endpoint         = true
  logs_endpoint        = true
  monitoring_endpoint  = true
  s3_endpoint          = true
  emr_endpoint         = true
  ssm_endpoint         = false
  ssmmessages_endpoint = false
}

module networking {
  source = "../../modules/networking"

  common_tags = local.common_tags
  name        = local.name

  region = var.vpc_region

  role_arn = {
    cluster-broker = "arn:aws:iam::${lookup(local.account, lookup(local.management_account, local.environment))}:role/${var.assume_role}"
  }

  vpc = {
    cidr_block          = module.cluster_broker_vpc.vpc.cidr_block
    id                  = module.cluster_broker_vpc.vpc.id,
    main_route_table_id = module.cluster_broker_vpc.vpc.main_route_table_id
  }

  internet_proxy_service_name = data.terraform_remote_state.internet_egress.outputs.internet_proxy_service.service_name
}
