resource "aws_iam_role" "ecs_autoscale_role" {
  assume_role_policy = data.aws_iam_policy_document.ecs_assume_role.json
}

data "aws_iam_policy_document" "ecs_assume_role" {
  statement {
    effect = "Allow"

    actions = ["sts:AssumeRole"]

    principals {
      identifiers = ["AWSServiceRoleForApplicationAutoScaling_ECSService"]
      type        = "serivce"
    }

  }
}

resource "aws_iam_role_policy" "ecs_autoscaling" {
  role   = aws_iam_role.ecs_autoscale_role.id
  policy = data.aws_iam_policy_document.ecs_autoscaling.json
}

data "aws_iam_policy_document" "ecs_autoscaling" {
  statement {
    effect = "Allow"

    actions = [
      "ecs:DescribeServices",
      "ecs:UpdateService",
      "cloudwatch:DeleteAlarms",
      "cloudwatch:DescribeAlarms",
      "cloudwatch:PutMetricAlarm"
    ]

    resources = ["*"]
  }
}
