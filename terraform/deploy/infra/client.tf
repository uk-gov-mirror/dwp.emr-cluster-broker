resource "aws_iam_role" "emr_cluster_broker_client" {
  name               = "emr_cluster_broker_client"
  assume_role_policy = data.aws_iam_policy_document.lambda_assume_role.json
  tags               = local.common_tags
}

resource "aws_iam_role_policy_attachment" "emr_cluster_broker_client-attach" {
  role       = aws_iam_role.emr_cluster_broker_client.name
  policy_arn = aws_iam_policy.iam_policy_for_emr_cluster_broker_client.arn
}

resource "aws_iam_role_policy_attachment" "vpc_lambda" {
  role       = aws_iam_role.emr_cluster_broker_client.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"
}

data "aws_iam_policy_document" "lambda_assume_role" {
  statement {
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }

    actions = ["sts:AssumeRole"]
  }
}

data "aws_iam_policy_document" "iam_for_emr_cluster_broker_client" {
  statement {
    sid = "1"

    effect = "Allow"

    actions = [
      "logs:CreateLogStream",
      "logs:PutLogEvents",
      "logs:CreateLogGroup",
      "ec2:DescribeImages",
      "elasticmapreduce:RunJobFlow",
      "elasticmapreduce:DescribeCluster",
      "iam:PassRole"
    ]

    resources = [
      "*"
    ]

  }
}

resource "aws_iam_policy" "iam_policy_for_emr_cluster_broker_client" {
  name   = "iam_policy_for_emr_cluster_broker_client"
  policy = data.aws_iam_policy_document.iam_for_emr_cluster_broker_client.json
}

resource "aws_lambda_function" "emr_cluster_broker_client" {
  filename         = "${var.emr_cluster_broker_client_zip["base_path"]}/emr-cluster-broker-client-${var.emr_cluster_broker_client_zip["version"]}.zip"
  source_code_hash = filebase64sha256(format("%s/emr-cluster-broker-client-%s.zip", var.emr_cluster_broker_client_zip["base_path"], var.emr_cluster_broker_client_zip["version"]))
  function_name    = "emr_cluster_broker_client"
  role             = aws_iam_role.emr_cluster_broker_client.arn
  handler          = "main.handler"
  runtime          = "python3.7"
  publish          = false
  timeout          = 60
  depends_on       = [aws_cloudwatch_log_group.emr_cluster_broker_client]

  vpc_config {
    subnet_ids         = module.networking.outputs.aws_subnets_private.*.id
    security_group_ids = [aws_security_group.emr_cluster_broker_client.id]
  }

  environment {
    variables = {
      LOG_LEVEL = "INFO"
    }
  }

  tags = merge(
    local.common_tags,
    {
      Name                    = "emr_cluster_broker_client"
      contains-sensitive-info = "False"
    }
  )
}

resource "aws_security_group" "emr_cluster_broker_client" {
  name                   = "EMR Cluster Broker Client"
  description            = "Client for EMR Cluster Broker"
  revoke_rules_on_delete = true
  vpc_id                 = module.networking.outputs.aws_vpc.id
}


resource "aws_security_group_rule" "emr_cluster_broker_client_egress" {
  description       = "Allow outbound requests"
  type              = "egress"
  from_port         = 0
  to_port           = 443
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.emr_cluster_broker_client.id
}

resource "aws_cloudwatch_log_group" "emr_cluster_broker_client" {
  name              = "/aws/lambda/emr_cluster_broker_client"
  retention_in_days = 14
}
