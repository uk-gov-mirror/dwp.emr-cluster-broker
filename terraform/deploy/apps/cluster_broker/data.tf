data "aws_availability_zones" "available" {}

data "aws_ecs_cluster" "ecs_main_cluster" {
  cluster_name = "main"
}

data "aws_route53_zone" "main" {
  provider = aws
  name = local.parent_domain_name[local.environment]
}
