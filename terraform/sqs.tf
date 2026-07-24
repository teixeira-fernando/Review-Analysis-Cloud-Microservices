resource "aws_sqs_queue" "review_analysis_queue" {
  name                       = var.queue_name
  visibility_timeout_seconds = var.queue_visibility_timeout
  message_retention_seconds  = var.queue_message_retention_period

  tags = {
    Name        = var.queue_name
    Environment = var.aws_endpoint != "" ? "development" : "production"
    ManagedBy   = "Terraform"
  }
}
