# Terraform Infrastructure Guide

This guide explains how to use Terraform to provision AWS resources (S3 bucket and SQS queue) for the Review Analysis microservices application.

## Overview

The Terraform configuration in the `./terraform/` directory defines:
- **S3 Bucket**: `review-analysis-bucket` (configurable, environment-specific)
- **SQS Queue**: `review-analysis-queue` (configurable, environment-specific)

This setup supports both **LocalStack** (development/testing) and **real AWS** (production) through environment-specific `.tfvars` files.

## Prerequisites

- **Terraform** >= 1.0 ([Install Terraform](https://learn.hashicorp.com/tutorials/terraform/install-cli))
- **AWS CLI** (optional, but recommended for credentials management)
- **Docker & Docker Compose** (for LocalStack development setup)

## Project Structure

```
terraform/
├── main.tf                  # AWS provider configuration with LocalStack support
├── variables.tf             # Input variables (region, endpoint, resource names, etc.)
├── outputs.tf              # Output values (bucket name, queue URL, ARNs)
├── s3.tf                   # S3 bucket resource definition
├── sqs.tf                  # SQS queue resource definition
├── terraform.tfvars        # Default values (committed to repo)
├── local.tfvars            # LocalStack configuration (committed to repo)
└── prod.tfvars             # Production AWS configuration (committed to repo)
```

## Quick Start

### 1. Development with LocalStack

**Step 1: Start LocalStack**

```bash
docker-compose up -d localstack
# Wait for LocalStack to be ready (check logs)
docker-compose logs localstack
```

**Step 2: Initialize and apply Terraform**

```bash
cd terraform
terraform init
terraform plan -var-file=local.tfvars
terraform apply -var-file=local.tfvars
```

**Step 3: Verify resources were created**

```bash
# View Terraform outputs
terraform output

# Check resources in LocalStack
docker-compose exec localstack awslocal s3 ls
docker-compose exec localstack awslocal sqs list-queues
```

**Step 4: Start the application**

```bash
docker-compose up -d review-collector review-analyzer
```

### 2. Production Deployment

**Prerequisites:**
- AWS credentials configured via environment variables or AWS CLI profile:

```bash
export AWS_ACCESS_KEY_ID="your-access-key"
export AWS_SECRET_ACCESS_KEY="your-secret-key"
export AWS_REGION="eu-central-1"
```

Or use an AWS profile:

```bash
export AWS_PROFILE=production
```

**Deploy:**

```bash
cd terraform
terraform init
terraform plan -var-file=prod.tfvars
terraform apply -var-file=prod.tfvars
```

## Configuration Files Explained

### `local.tfvars` (LocalStack/Development)

```hcl
aws_region  = "eu-central-1"
aws_endpoint = "http://localstack:4566"  # LocalStack endpoint
aws_access_key = "test"                   # LocalStack default credentials
aws_secret_key = "test"

bucket_name = "review-analysis-bucket"
queue_name  = "review-analysis-queue"
```

**When to use:** Local development, testing, CI/CD with LocalStack.

### `prod.tfvars` (AWS Production)

```hcl
aws_region = "eu-central-1"
# aws_endpoint is omitted to use real AWS endpoints

bucket_name = "review-analysis-bucket-prod"
queue_name  = "review-analysis-queue-prod"

enable_s3_versioning = true  # Enable in production
```

**When to use:** Production deployments to real AWS.

**Note:** AWS credentials are **not** stored in this file. Instead:
- Use environment variables: `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`
- Or use AWS CLI profiles: `export AWS_PROFILE=production`
- Or pass inline: `terraform apply -var="aws_access_key=..." -var="aws_secret_key=..." -var-file=prod.tfvars`

### `terraform.tfvars` (Defaults)

This file contains default values that apply to all environments if not overridden.

## Common Terraform Commands

### Initialize (first time only)

```bash
terraform init
```

### Plan changes (dry run)

```bash
# LocalStack
terraform plan -var-file=local.tfvars

# Production
terraform plan -var-file=prod.tfvars
```

### Apply changes

```bash
# LocalStack
terraform apply -var-file=local.tfvars

# Production
terraform apply -var-file=prod.tfvars
```

### View current state

```bash
terraform show
terraform output
```

### Destroy resources

```bash
# LocalStack (careful!)
terraform destroy -var-file=local.tfvars

# Production (very careful!)
terraform destroy -var-file=prod.tfvars
```

### Format code

```bash
terraform fmt -recursive
```

### Validate configuration

```bash
terraform validate
```

## Variables Reference

| Variable | Type | Default | Description |
|----------|------|---------|-------------|
| `aws_region` | string | `eu-central-1` | AWS region |
| `aws_endpoint` | string | `` | Custom endpoint (e.g., `http://localstack:4566` for LocalStack, empty for AWS) |
| `aws_access_key` | string | `test` | AWS access key (sensitive) |
| `aws_secret_key` | string | `test` | AWS secret key (sensitive) |
| `bucket_name` | string | `review-analysis-bucket` | S3 bucket name |
| `queue_name` | string | `review-analysis-queue` | SQS queue name |
| `queue_visibility_timeout` | number | `30` | SQS message visibility timeout (seconds) |
| `queue_message_retention_period` | number | `345600` | SQS message retention period (4 days in seconds) |
| `enable_s3_versioning` | bool | `false` | Enable S3 bucket versioning |

## Outputs

After applying Terraform, these values are available:

```bash
terraform output
```

| Output | Description |
|--------|-------------|
| `s3_bucket_name` | S3 bucket name |
| `s3_bucket_arn` | S3 bucket ARN |
| `sqs_queue_name` | SQS queue name |
| `sqs_queue_url` | SQS queue URL |
| `sqs_queue_arn` | SQS queue ARN |

To retrieve a specific output:

```bash
terraform output s3_bucket_name
terraform output sqs_queue_url
```

## Terraform State Management

### Local State (Current Setup)

By default, Terraform stores state locally in:
- `terraform/.terraform/` — Terraform metadata
- `terraform/terraform.tfstate` — State file (JSON)
- `terraform/terraform.tfstate.backup` — Previous state backup

**Security Consideration:** Local state files are unencrypted. Do **not** commit `.tfstate` files to Git (already in `.gitignore`).

### Remote State (Recommended for Teams/CI-CD)

For production and team environments, consider using a remote backend:

**S3 Backend Example** (create `terraform/backend.tf`):

```hcl
terraform {
  backend "s3" {
    bucket         = "my-terraform-state"
    key            = "review-analysis/terraform.tfstate"
    region         = "eu-central-1"
    encrypt        = true
    dynamodb_table = "terraform-locks"
  }
}
```

Then migrate state:

```bash
terraform init  # Terraform will ask to copy state to remote backend
```

## Troubleshooting

### LocalStack Connection Failed

**Error:** `Error: error creating S3 bucket: InvalidAccessKeyId`

**Solution:**
- Ensure LocalStack is running: `docker-compose ps localstack`
- Check LocalStack logs: `docker-compose logs localstack`
- Verify endpoint is correct in `local.tfvars`: `http://localstack:4566`
- Wait for LocalStack to be fully ready (check for `Ready.` in logs)

### Permission Denied (AWS)

**Error:** `Error: user is not authorized to perform: s3:CreateBucket`

**Solution:**
- Verify AWS credentials: `aws sts get-caller-identity`
- Ensure credentials have S3 and SQS permissions
- Check AWS IAM policy for the user/role

### State Lock Timeout

**Error:** `Error acquiring the state lock`

**Solution:**
- If using remote state, check for stuck locks: `terraform force-unlock <LOCK_ID>`
- For local state, ensure no other Terraform process is running

### Resource Already Exists

**Error:** `BucketAlreadyExists` or `QueueNameExists`

**Solution:**
- Use a different bucket/queue name in `.tfvars`
- Or destroy existing resources: `terraform destroy -var-file=local.tfvars`
- Check for naming collisions with other environments

## Integration with Application

The application reads S3 and SQS configuration from environment variables or `application.properties`:

**Key Properties:**
- `app.bucket` — S3 bucket name (matches `bucket_name` Terraform variable)
- `app.queue` — SQS queue name (matches `queue_name` Terraform variable)
- `spring.cloud.aws.s3.endpoint` — S3 endpoint (LocalStack: `http://localstack:4566`)
- `spring.cloud.aws.sqs.endpoint` — SQS endpoint (LocalStack: `http://localstack:4566`)

After running Terraform, verify bucket and queue names match your `application.properties` files in `review-analyzer` and `review-collector`.

## Next Steps

1. ✅ Terraform files created → `./terraform/`
2. ✅ Environment configs created → `local.tfvars`, `prod.tfvars`
3. ⏭️ **Next:** Run `terraform init && terraform apply -var-file=local.tfvars` with LocalStack running
4. ⏭️ **Verify:** Check S3 and SQS in LocalStack: `awslocal s3 ls`, `awslocal sqs list-queues`
5. ⏭️ **Test:** Start application: `docker-compose up -d review-collector review-analyzer`

## References

- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS S3 Bucket Resource](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/s3_bucket)
- [AWS SQS Queue Resource](https://registry.terraform.io/providers/hashicorp/aws/latest/docs/resources/sqs_queue)
- [LocalStack Getting Started](https://docs.localstack.cloud/getting-started/)
- [Terraform Best Practices](https://www.terraform.io/docs/language)

---

**Questions?** Check the Terraform state with `terraform show` or review the generated `.terraform/` metadata.
