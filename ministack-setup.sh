#!/bin/sh
echo "Initializing ministack s3 and sqs"

aws --endpoint-url=http://ministack:4566 s3api create-bucket --bucket review-analysis-bucket
aws --endpoint-url=http://ministack:4566 sqs create-queue --queue-name review-analysis-queue

echo "Executed commands to setup ministack s3 and sqs"