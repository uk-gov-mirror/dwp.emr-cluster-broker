output outputs {
  value = {
    aws_availability_zones      = data.aws_availability_zones.current
    aws_subnets_private         = aws_subnet.private
    aws_vpc                     = var.vpc
    aws_route_table_private_ids = aws_route_table.private[*].id
    internet_proxy_vpce_sg_id   = aws_security_group.internet_proxy_endpoint.id
  }
}
