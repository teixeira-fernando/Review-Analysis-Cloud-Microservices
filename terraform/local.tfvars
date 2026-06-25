# LocalStack Development Environment Configuration
# Use this with: terraform apply -var-file=local.tfvars

aws_region   = "eu-central-1"
# Use localhost when running Terraform from your host machine.
# If Terraform runs inside a container on the same Docker network, use http://localstack:4566.
aws_endpoint = "http://localhost:4566"

# LocalStack default credentials (test/test)
aws_access_key = "test"
aws_secret_key = "test"

# Resource names
bucket_name = "review-analysis-bucket"
queue_name  = "review-analysis-queue"

# Queue configuration
queue_visibility_timeout       = 30
queue_message_retention_period = 345600 # 4 days

# S3 configuration
enable_s3_versioning = false
