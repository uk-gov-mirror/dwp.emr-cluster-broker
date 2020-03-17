resource "aws_lambda_permission" "allow_cloudwatch" {
  statement_id  = "AllowExecutionFromCloudWatch"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.dns_lambda.function_name
  principal     = "events.amazonaws.com"
  source_arn    = aws_cloudwatch_event_rule.emr-trigger.arn
}

resource "aws_lambda_alias" "dns_lambda" {
  name             = "${var.name}-dns-lambda-alias"
  description      = "EMR DNS Creation Lambda"
  function_name    = aws_lambda_function.dns_lambda.function_name
  function_version = "$LATEST"
}

resource "aws_lambda_function" "dns_lambda" {
  filename      = "${path.module}/emrAliasLambda.zip"
  function_name = "${var.name}-dns-lambda"
  role          = aws_iam_role.lambda_execution_role.arn
  handler       = "index.handler"
  runtime       = "nodejs12.x"
  publish       = false
}

resource "aws_iam_role" "lambda_execution_role" {
  name               = "${var.name}-lambda"
  assume_role_policy = data.aws_iam_policy_document.lambda.json
}

resource "aws_iam_role_policy_attachment" "lambda-attach" {
  role       = aws_iam_role.lambda_execution_role.name
  policy_arn = aws_iam_policy.iam_policy_for_dns-lambda.arn
}

data "aws_iam_policy_document" "lambda" {
  statement {
    actions = [
      "sts:AssumeRole",
    ]

    principals {
      type        = "Service"
      identifiers = ["lambda.amazonaws.com"]
    }
  }
}

resource "aws_iam_policy" "iam_policy_for_dns-lambda" {
  name   = "${var.name}-dns-lamda-role"
  policy = data.aws_iam_policy_document.iam_for_dns-lambda.json
}

data "aws_iam_policy_document" "iam_for_dns-lambda" {
  statement {
    sid = "1"

    effect = "Allow"

    actions = [
      "logs:CreateLogStream",
      "elasticmapreduce:DescribeCluster",
      "route53:ChangeResourceRecordSets",
      "logs:PutLogEvents",
      "route53:ListHostedZonesByName",
      "logs:CreateLogGroup"
    ]

    resources = [
      "*"
    ]

  }
}
