#!/bin/sh
echo "Initializing localstack s3"

awslocal --region eu-central-1 s3api create-bucket --bucket review-analysis-bucket
awslocal --region eu-central-1 sqs create-queue --queue-name review-analysis-queue

echo "Executed commands to setup localstack s3 and sqs"
