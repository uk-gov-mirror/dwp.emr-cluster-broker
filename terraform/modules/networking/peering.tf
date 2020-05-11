
data aws_vpc_peering_connection frontend {
  count       = length(regexall("^vpc-", var.frontend_vpc)) > 0 ? 1 : 0
  vpc_id      = var.frontend_vpc
  peer_vpc_id = var.vpc.id
}
