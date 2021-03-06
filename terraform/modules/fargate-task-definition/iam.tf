data "aws_iam_policy_document" "ecs-tasks" {
  statement {
    actions = [
      "sts:AssumeRole",
    ]

    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

data "aws_iam_policy_document" "task_role" {
  statement {
    sid = "1"

    actions = [
      "elasticmapreduce:DescribeCluster"
    ]

    resources = [
      "arn:aws:elasticmapreduce:${var.region}:${var.account}:cluster/cb-*",
    ]
  }

  statement {
    actions = [
      "ec2:DescribeImages",
      "elasticmapreduce:RunJobFlow",
      "iam:PassRole"
    ]

    resources = [
      "*",
    ]
  }
}

resource "aws_iam_policy" "ecs_task_role" {
  name   = "${var.name_prefix}-task_role"
  policy = data.aws_iam_policy_document.task_role.json
}

resource "aws_iam_role" "ecs_task_execution_role" {
  name               = "${var.name_prefix}-ecs-task-execution-role"
  assume_role_policy = data.aws_iam_policy_document.ecs-tasks.json
}

resource "aws_iam_role" "ecs_task_role" {
  name               = "${var.name_prefix}-ecs-task-role"
  assume_role_policy = data.aws_iam_policy_document.ecs-tasks.json
}

resource "aws_iam_role_policy_attachment" "ecs_task_execution_role_policy_attach" {
  role       = aws_iam_role.ecs_task_execution_role.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

resource "aws_iam_role_policy_attachment" "ecs_task_role_policy_attach" {
  role       = aws_iam_role.ecs_task_role.name
  policy_arn = aws_iam_policy.ecs_task_role.arn
}
