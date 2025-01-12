#!/bin/sh
echo "Initializing localstack s3"

awslocal s3api create-bucket --bucket review-analysis-bucket
awslocal sqs create-queue --queue-name review-analysis-queue

echo "Executed commands to setup localstack s3 and sqs"