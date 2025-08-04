const { GenericContainer, Network, Wait, PullPolicy } = require('testcontainers');
const {LocalstackContainer} = require('@testcontainers/localstack');

const LOCALSTACK_IMAGE = 'localstack/localstack:4.0.3';
const REVIEW_COLLECTOR_IMAGE = process.env.REVIEW_COLLECTOR_DOCKER_IMAGE || 'teixeirafernando/review-collector:latest';
const REVIEW_ANALYZER_IMAGE = process.env.REVIEW_ANALYZER_DOCKER_IMAGE || 'teixeirafernando/review-analyzer:latest';

const BUCKET_NAME = 'review-analysis-bucket';
const QUEUE_NAME = 'review-analysis-queue';

let network, localstack, reviewCollector, reviewAnalyzer;

async function setupContainers() {
  network = await new Network().start();

  localstack = await new LocalstackContainer(LOCALSTACK_IMAGE)
  .withNetworkMode(network.getName())
    .withNetworkAliases('localstack')
    .withEnvironment({ 
      LOCALSTACK_SSL_CERT_DOWNLOAD: '0'
    })
    .withExposedPorts(4566)
    .start();

    
  // Create S3 bucket and SQS queue
  await localstack.exec(["awslocal", "s3", "mb", `s3://${BUCKET_NAME}`]);
  await localstack.exec(["awslocal", "sqs", "create-queue", "--queue-name", QUEUE_NAME]);

  reviewCollector = await new GenericContainer(REVIEW_COLLECTOR_IMAGE)
    .withPullPolicy(PullPolicy.alwaysPull())    
    .withNetworkMode(network.getName())
    .withNetworkAliases('review-collector-service')
    .withEnvironment({ AWS_ENDPOINT: 'http://localstack:4566'})
    .withExposedPorts(8080)
    .withWaitStrategy(Wait.forHttp("/actuator/health", 8080).forStatusCode(200))
    .start();

  reviewAnalyzer = await new GenericContainer(REVIEW_ANALYZER_IMAGE)
    .withPullPolicy(PullPolicy.alwaysPull())  
    .withNetworkMode(network.getName())
    .withNetworkAliases('review-analyzer-service')
    .withEnvironment({ AWS_ENDPOINT: 'http://localstack:4566'})
    .withExposedPorts(8081)
    .withWaitStrategy(Wait.forHttp("/actuator/health", 8081).forStatusCode(200))
    .start();

  return {
    localstack,
    reviewCollector,
    reviewAnalyzer,
    network,
    ports: {
      localstack: localstack.getMappedPort(4566),
      reviewCollector: reviewCollector.getMappedPort(8080),
      reviewAnalyzer: reviewAnalyzer.getMappedPort(8081)
    },
  };
}

async function teardownContainers() {
  if (localstack) await localstack.stop();
  if (network) await network.stop();
}

module.exports = { setupContainers, teardownContainers };
