#!/bin/sh
echo "Initializing localstack s3"

awslocal s3 mb s3://review-analysis-bucket
awslocal s3api create-bucket --bucket review-analysis-bucket
awslocal sqs create-queue --queue-name review-analysis-queue

# List the contents of the S3 bucket
awslocal s3 ls s3://review-analysis-bucket

echo "Executed commands to setup localstack s3 and sqs"