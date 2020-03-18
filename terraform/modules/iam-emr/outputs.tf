output "cb_service_role" {
  description = "The Service Role."
  value       = aws_iam_role.emr_service
}

output "cb_job_flow_role" {
  description = "The Job Flow Role."
  value       = aws_iam_role.emr_cb_iam
}

output "cb_autoscaling_role" {
  description = "The AutoScaling Role."
  value       = aws_iam_role.emr_autoscaling_role
}
