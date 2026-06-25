variable "aws_region" {
  description = "AWS region"
  type        = string
  default     = "eu-central-1"
}

variable "aws_access_key" {
  description = "AWS access key"
  type        = string
  sensitive   = true
  default     = "test"
}

variable "aws_secret_key" {
  description = "AWS secret key"
  type        = string
  sensitive   = true
  default     = "test"
}

variable "aws_endpoint" {
  description = "AWS endpoint URL (e.g., http://localhost:4566 for LocalStack, empty string for real AWS)"
  type        = string
  default     = ""
}

variable "bucket_name" {
  description = "S3 bucket name for review analysis"
  type        = string
  default     = "review-analysis-bucket"
}

variable "queue_name" {
  description = "SQS queue name for review analysis"
  type        = string
  default     = "review-analysis-queue"
}

variable "queue_visibility_timeout" {
  description = "SQS message visibility timeout in seconds"
  type        = number
  default     = 30
}

variable "queue_message_retention_period" {
  description = "SQS message retention period in seconds (default 4 days)"
  type        = number
  default     = 345600
}

variable "enable_s3_versioning" {
  description = "Enable S3 bucket versioning"
  type        = bool
  default     = false
}
