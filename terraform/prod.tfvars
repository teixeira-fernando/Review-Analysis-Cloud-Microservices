# AWS Production Environment Configuration
# Use this with: terraform apply -var-file=prod.tfvars
#
# IMPORTANT: Set AWS credentials via environment variables:
#   export AWS_ACCESS_KEY_ID="your-prod-key"
#   export AWS_SECRET_ACCESS_KEY="your-prod-secret"
# OR use AWS CLI profiles:
#   export AWS_PROFILE=production
# OR pass credentials in this file (NOT recommended for security):
#   aws_access_key = "..."
#   aws_secret_key = "..."

aws_region = "eu-central-1"

# Leave aws_endpoint empty for real AWS (commented out below)
# aws_endpoint = ""

# Resource names (environment-specific to avoid collisions)
bucket_name = "review-analysis-bucket-prod"
queue_name  = "review-analysis-queue-prod"

# Queue configuration
queue_visibility_timeout       = 30
queue_message_retention_period = 345600 # 4 days

# S3 configuration
enable_s3_versioning = true
