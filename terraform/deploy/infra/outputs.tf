output vpc {
  value = module.networking.outputs
}

output interface_vpce_sg_id {
  value = module.cluster_broker_vpc.interface_vpce_sg_id
}

output s3_prefix_list_id {
  value = module.cluster_broker_vpc.s3_prefix_list_id
}

output dynamodb_prefix_list_id {
  value = module.cluster_broker_vpc.dynamodb_prefix_list_id
}
