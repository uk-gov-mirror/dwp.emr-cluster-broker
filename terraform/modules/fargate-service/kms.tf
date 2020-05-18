resource "aws_kms_key" "encryption_key" {
  description             = "${var.name_prefix} ECS Encryption Key"
  is_enabled              = true
  enable_key_rotation     = true
  deletion_window_in_days = 7
  tags                    = merge(var.common_tags, { Name = "${var.name_prefix}-kms-key" })
}

resource "aws_kms_alias" "encryption_key" {
  name          = "alias/emr-cluster-broker/ecs_encryption_key"
  target_key_id = aws_kms_key.encryption_key.id

  lifecycle {
    prevent_destroy = true
  }
}
