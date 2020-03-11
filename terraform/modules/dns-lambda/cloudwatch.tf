resource "aws_cloudwatch_event_rule" "emr-trigger" {
  name        = "${var.name}-emr-trigger"
  description = "Trigger on EMR Change"

  event_pattern = <<PATTERN
{
  "source": [
    "aws.emr"
  ],
  "detail-type": [
    "EMR Cluster State Change"
  ],
  "detail": {
    "state": [
      "RUNNING",
      "TERMINATED_WITH_ERRORS",
      "TERMINATED"
    ]
  }
}
PATTERN
}

resource "aws_cloudwatch_log_group" "lamda_logs" {
  name              = "/aws/lambda/${aws_lambda_function.dns_lambda.function_name}"
  retention_in_days = 14
}


resource "aws_cloudwatch_event_target" "check_at_rate" {
  rule = aws_cloudwatch_event_rule.emr-trigger.name
  arn  = aws_lambda_function.dns_lambda.arn
}
