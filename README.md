# Review Analysis Cloud Microservices - Test Containers and Localstack in practice


## Microservices 

* Review Collector
* Review Analyzer

![alt text](images/ReviewAnalysisProject.drawio.png "Review Analysis Microservices Flow")

## Stack

* Java 21
* Spring Boot
* Gradle
* Test Containers
* Localstack
* JUnit 5
* AWS (S3, SQS)

## Requirements to run it locally

* Docker
* Gradle and Java
* An AWS account if you want to run it using real services (you can run the setup with LocalStack which does not require an AWS Account)

## Instructions to run the project

There are different options to run the project:

#### 1 - Docker - using Localstack

You can simply run this docker-compose command to run the 2 services, together with the E2E tests and Localstack:

```Shell
docker-compose up --abort-on-container-exit
```

#### 2 - Docker - using real AWS services

For this option, you need to have a created AWS account and set 2 environment variables, AWS_ACCESSKEY and AWS_SECRETKEY. Depending on your machine OS, you will have a different command to set those environment variables. If you using linux, you can simply run the following:

```Shell
export AWS_ACCESSKEY=YOUR_ACCESSKEY_HERE
```

```Shell
export AWS_SECRETKEY=YOUR_SECRETKEY_HERE
```

## QA Strategy

* Unit Tests: <b>Junit5 and Mockito</b>
* Integration tests: <b>Spring Boot Test, TestContainers, Localstack</b> 
* Quality Metrics:
    * Mutation Tests/Mutation Coverage: <b>PITest</b>
    * Code Coverage: <b>Jacoco</b>
    * Technical Debt, Code Smells and other complementary metrics : <b>Sonar Cloud</b>
* Contract tests: <b>Pact framework</b>
* Continuous Integration: This project uses Github Action for Continuous Integration, where it executes all the tests and Sonar Cloud Analysis for every pull request, making easier the process of integration of every new code, also facilitating the process of Code Review.

## Pipeline configuration

[TODO]


## Development info

### Running E2E tests using Docker alone

From root folder, run the following commands:
"docker build -t my-e2e-tests . -f Dockerfile_e2e_tests"

"docker run --rm -it -e REVIEW_COLLECTOR_BASE_URL=$REVIEW_COLLECTOR_BASE_URL -e REVIEW_ANALYZER_BASE_URL=$REVIEW_ANALYZER_BASE_URL -v /var/run/docker.sock:/var/run/docker.sock my-e2e-tests"

