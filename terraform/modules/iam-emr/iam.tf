resource "aws_iam_role" "emr_cb_iam" {
  name               = "${var.name}_emr_cb_iam"
  assume_role_policy = data.aws_iam_policy_document.ec2_assume_role.json
  tags               = var.common_tags
}

resource "aws_iam_instance_profile" "emr_cb_iam" {
  name = "${var.name}_emr_cb_iam"
  role = aws_iam_role.emr_cb_iam.id
}

data "aws_iam_policy_document" "ec2_assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role_policy_attachment" "emr_for_ec2_attachment" {
  role       = aws_iam_role.emr_cb_iam.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonElasticMapReduceforEC2Role"
}

resource "aws_iam_role_policy_attachment" "ec2_for_ssm_attachment" {
  role       = aws_iam_role.emr_cb_iam.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

data "aws_iam_policy_document" "write_s3_cb" {
  statement {
    effect = "Allow"

    actions = [
      "s3:ListBucket",
    ]

    resources = [
      "arn:aws:s3:::${var.ingest_bucket}",
      "arn:aws:s3:::${var.s3_log_bucket}"
    ]
  }

  statement {
    effect = "Allow"

    actions = [
      "s3:GetObject*",
      "s3:DeleteObject*",
      "s3:PutObject*",
    ]

    resources = [
      "arn:aws:s3:::${var.ingest_bucket}/business-data/hbase/*",
      "arn:aws:s3:::${var.s3_log_bucket}/emr/*"
    ]
  }

  statement {
    effect = "Allow"

    actions = [
      "kms:Encrypt",
      "kms:Decrypt",
      "kms:ReEncrypt*",
      "kms:GenerateDataKey*",
      "kms:DescribeKey",
    ]

    resources = [
      "${var.ebs_cmk}",
    ]
  }

  statement {
    sid    = "AllowUseDefaultEbsCmk"
    effect = "Allow"

    actions = [
      "kms:Encrypt",
      "kms:Decrypt",
      "kms:ReEncrypt*",
      "kms:GenerateDataKey*",
      "kms:DescribeKey",
    ]

    resources = ["${var.ebs_cmk}"]
  }
}

resource "aws_iam_policy" "write_s3_cb" {
  name        = "${var.name}_EmrWriteIngestHBaseS3"
  description = "Allow Ingestion EMR cluster to write HBase data to the input bucket"
  policy      = data.aws_iam_policy_document.write_s3_cb.json
}

resource "aws_iam_role_policy_attachment" "emr_write_s3_cb" {
  role       = aws_iam_role.emr_cb_iam.name
  policy_arn = aws_iam_policy.write_s3_cb.arn
}

resource "aws_iam_role" "emr_service" {
  name               = "${var.name}_emr_service_role"
  assume_role_policy = data.aws_iam_policy_document.emr_assume_role.json
  tags               = var.common_tags
}

data "aws_iam_policy_document" "emr_assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["elasticmapreduce.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

resource "aws_iam_role_policy_attachment" "emr_attachment" {
  role       = aws_iam_role.emr_service.id
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonElasticMapReduceRole"
}

data "aws_iam_policy_document" "emr_ebs_cmk" {
  statement {
    effect = "Allow"

    actions = [
      "kms:Encrypt",
      "kms:Decrypt",
      "kms:ReEncrypt*",
      "kms:GenerateDataKey*",
      "kms:DescribeKey",
    ]

    resources = ["${var.ebs_cmk}"]
  }

  statement {
    effect = "Allow"

    actions = ["kms:CreateGrant"]

    resources = ["${var.ebs_cmk}"]

    condition {
      test     = "Bool"
      variable = "kms:GrantIsForAWSResource"
      values   = ["true"]
    }
  }
}

resource "aws_iam_policy" "emr_ebs_cmk" {
  name        = "${var.name}_EmrUseEbsCmk"
  description = "Allow EMR cluster to use EB CMK for encryption"
  policy      = data.aws_iam_policy_document.emr_ebs_cmk.json
}

resource "aws_iam_role_policy_attachment" "emr_ebs_cmk" {
  role       = aws_iam_role.emr_service.id
  policy_arn = aws_iam_policy.emr_ebs_cmk.arn
}

resource "aws_iam_role" "emr_autoscaling_role" {
  name               = "${var.name}_EMR_AutoScaling_Role"
  assume_role_policy = data.aws_iam_policy_document.emr_autoscaling_role_assume_role.json
  tags               = var.common_tags
}

data "aws_iam_policy_document" "emr_autoscaling_role_assume_role" {
  statement {
    effect  = "Allow"
    actions = ["sts:AssumeRole"]

    principals {
      type = "Service"
      identifiers = [
        "elasticmapreduce.amazonaws.com",
        "application-autoscaling.amazonaws.com"
      ]
    }
  }
}

resource "aws_iam_role_policy" "elastic_map_reduce_for_auto_scaling_role" {
  name   = "${var.name}_ElasticMapReduceforAutoScalingRole"
  role   = aws_iam_role.emr_autoscaling_role.id
  policy = data.aws_iam_policy_document.elastic_map_reduce_for_auto_scaling_role.json
}

data "aws_iam_policy_document" "elastic_map_reduce_for_auto_scaling_role" {
  statement {
    effect = "Allow"
    actions = [
      "cloudwatch:DescribeAlarms",
      "elasticmapreduce:ListInstanceGroups",
      "elasticmapreduce:ModifyInstanceGroups"
    ]
    resources = ["*"]
  }
}
