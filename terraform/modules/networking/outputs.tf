output outputs {
  value = {
    aws_availability_zones      = data.aws_availability_zones.current
    aws_subnets_private         = aws_subnet.private
    aws_vpc                     = var.vpc
    id                          = var.vpc
    aws_route_table_private_ids = aws_route_table.private[*].id

    internet_proxy_vpce = {
      sg_id    = aws_security_group.internet_proxy_endpoint.id
      dns_name = aws_vpc_endpoint.internet_proxy.dns_entry[0].dns_name
    }
  }
}
