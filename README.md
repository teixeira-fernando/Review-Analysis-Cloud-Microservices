# Review Analysis Cloud Microservices - Test Containers and Localstack in practice

[![E2E tests - Release Process](https://github.com/teixeira-fernando/Review-Analysis-Cloud-Microservices/actions/workflows/e2e-tests-release.yml/badge.svg)](https://github.com/teixeira-fernando/Review-Analysis-Cloud-Microservices/actions/workflows/e2e-tests-release.yml)
![Coverage](https://img.shields.io/codecov/c/github/teixeira-fernando/Review-Analysis-Cloud-Microservices)

## Table of Contents
- [Microservices](#microservices)
- [Stack](#stack)
- [Requirements to run it locally](#requirements-to-run-it-locally)
- [Instructions to run the project](#instructions-to-run-the-project)
  - [Docker - using Localstack](#1---docker---using-localstack)
  - [Docker - using real AWS services](#2---docker---using-real-aws-services)
  - [Using your favorite IDE](#3---using-your-favorite-ide)
- [QA Strategy](#qa-strategy)
- [Pipeline Configuration](#pipeline-configuration)
- [Development Info](#development-info)
  - [Running E2E tests using Docker alone](#running-e2e-tests-using-docker-alone)

## Microservices 

- **Review Collector**
- **Review Analyzer**

![Review Analysis Microservices Flow](images/ReviewAnalysisProject.drawio.png)

## Stack

- **Java 21**
- **Spring Boot**
- **Gradle**
- **Test Containers**
- **Localstack**
- **JUnit 5**
- **AWS (S3, SQS)**

## Requirements to run it locally

- **Docker**
- **Gradle and Java**
- **An AWS account** (if you want to run it using real services; you can use LocalStack which does not require an AWS Account)


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

Now, we can run this single docker command to run the 2 services and the E2E tests using real AWS services from your account.

```Shell
docker-compose -f docker-compose-real-AWS-services.yml up --abort-on-container-exit
```

#### 3 - Using your favorite IDE

You can also run the project using your favorite IDE. As mentioned, you just need the Java JDK and Gradle properly installed and configured on your machine. Let me show you how to easily run the 2 services from the project in that way.

 <b>Run review-collector service:</b>
```Gradle
./gradlew :review-collector:bootRun
```

 <b>Run review-analyzer service:</b>
```Gradle
./gradlew :review-analyzer:bootRun
```

 <b>Run E2E tests:</b>

```Gradle
./gradlew :e2e-tests:test
```

## QA Strategy

* Unit Tests: <b>Junit5 and Mockito</b>
* Integration tests: <b>Spring Boot Test, TestContainers, Localstack</b>
* E2E tests:  <b>Rest Assured, Localstack</b>
* Quality Metrics:
    * Mutation Tests/Mutation Coverage: <b>PITest</b> (TODO)
    * Code Coverage: <b>Jacoco</b> (TODO)
    * Technical Debt, Code Smells and other complementary metrics : <b>Sonar Cloud</b>
* Contract tests: <b>Pact framework</b> (TODO)
* Continuous Integration: This project uses Github Action for Continuous Integration, where it executes all the tests and Sonar Cloud Analysis for every pull request, making easier the process of integration of every new code, also facilitating the process of Code Review.

## Pipeline Configuration

Even though this project is using a single repository, it is still a microservices project. The CI/CD process is organized to make each service modular and independent. Below is an overview of the pipelines created for every pull request to the main branch:

### Pull Request Pipelines

* **review-analyzer-pull-request**
  * Builds and runs the unit and integration tests for the `review-analyzer` service.
* **review-collector-pull-request**
  * Builds and runs the unit and integration tests for the `review-collector` service.
* **e2e-tests-pull-request**
  * Runs the E2E tests using the `docker-compose.yml` configuration.
  * Builds Docker images for both services and the E2E tests, running them with the latest changes from the PR.
  * Uses LocalStack to simulate AWS services, avoiding extra costs.

### Release Pipelines

After changes are merged into the main branch, the following pipelines are used:

* **review-analyzer-release**
  * Builds the Docker image for the `review-analyzer` service and pushes it to the Docker registry.
* **review-collector-release**
  * Builds the Docker image for the `review-collector` service and pushes it to the Docker registry.
* **e2e-tests-release**
  * Runs the E2E tests using the `docker-compose-real-AWS-services.yml` configuration.
  * Uses the latest Docker images for both services and the E2E tests built in the release pipelines.
  * Utilizes real AWS services for test execution, requiring `AWS_ACCESSKEY` and `AWS_SECRETKEY`.

Note: While it would be more efficient to run the `docker-compose.yml` configuration during the release process, this project demonstrates different possibilities with LocalStack and real AWS services.

## Development info

### Running E2E tests using Docker alone

From root folder, run the following commands:
"docker build -t my-e2e-tests . -f Dockerfile_e2e_tests"

"docker run --rm -it -e REVIEW_COLLECTOR_BASE_URL=$REVIEW_COLLECTOR_BASE_URL -e REVIEW_ANALYZER_BASE_URL=$REVIEW_ANALYZER_BASE_URL -v /var/run/docker.sock:/var/run/docker.sock my-e2e-tests"

