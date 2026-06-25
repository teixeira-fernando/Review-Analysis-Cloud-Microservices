resource "aws_s3_bucket" "review_analysis_bucket" {
  bucket = var.bucket_name

  tags = {
    Name        = var.bucket_name
    Environment = var.aws_endpoint != "" ? "development" : "production"
    ManagedBy   = "Terraform"
  }
}

resource "aws_s3_bucket_versioning" "review_analysis_bucket_versioning" {
  bucket = aws_s3_bucket.review_analysis_bucket.id

  versioning_configuration {
    status = var.enable_s3_versioning ? "Enabled" : "Suspended"
  }
}

resource "aws_s3_bucket_public_access_block" "review_analysis_bucket_pab" {
  bucket = aws_s3_bucket.review_analysis_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}
