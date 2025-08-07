const path = require('path');
const { default: test } = require('playwright/test');
const { DockerComposeEnvironment, Wait, PullPolicy } = require('testcontainers');

const composeFile = path.resolve(__dirname, '../docker-compose-frontend-development.yml');
let testContainersRuntime;

async function setupContainers() {

  let dockerComposeEnvironment = new DockerComposeEnvironment(
      path.dirname(composeFile),
      path.basename(composeFile)
    )
      .withWaitStrategy('localstack', Wait.forLogMessage('Ready'))
      .withWaitStrategy('review-collector', Wait.forHttp("/actuator/health", 8080).forStatusCode(200))
      .withWaitStrategy('review-analyzer', Wait.forHttp("/actuator/health", 8081).forStatusCode(200));


  testContainersRuntime = await dockerComposeEnvironment.up();

  console.log('Containers started successfully.');

  testContainersRuntime.getContainer('localstack').exec(['awslocal', 's3', 'mb', 's3://review-analysis-bucket']);
  testContainersRuntime.getContainer('localstack').exec(['awslocal', 'sqs', 'create-queue', '--queue-name', 'review-analysis-queue']);

  return testContainersRuntime;
}

async function teardownContainers() {
  if (testContainersRuntime) await testContainersRuntime.stop();
}


module.exports = { setupContainers, teardownContainers };
