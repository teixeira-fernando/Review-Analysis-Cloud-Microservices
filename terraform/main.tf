terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region

  # Configure AWS credentials
  access_key = var.aws_access_key
  secret_key = var.aws_secret_key

  # Skip credentials validation for LocalStack compatibility
  skip_credentials_validation = true
  skip_metadata_api_check     = true
  skip_requesting_account_id  = true

  # LocalStack works more reliably with path-style S3 addressing.
  s3_use_path_style = true

  # Use custom endpoint for LocalStack
  # For AWS, this remains empty (uses default AWS endpoints)
  dynamic "endpoints" {
    for_each = var.aws_endpoint != "" ? [1] : []
    content {
      s3  = var.aws_endpoint
      sqs = var.aws_endpoint
    }
  }
}
