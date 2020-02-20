resource "aws_kms_key" "emr_sec_config" {
  description             = "${var.name}_key_for_ebs_encryption"
  deletion_window_in_days = 7

  tags = merge({ "Name" = var.name }, var.common_tags)
}

resource "aws_kms_alias" "emr_sec_config" {
  name          = "alias/${var.name}/kms"
  target_key_id = aws_kms_key.emr_sec_config.id
}
