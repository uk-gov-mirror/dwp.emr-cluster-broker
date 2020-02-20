resource "aws_ecs_cluster" "cluster" {
  name = var.name
  tags = merge(var.common_tags, { Name = "${var.name}-ecs-cluster" })
}

