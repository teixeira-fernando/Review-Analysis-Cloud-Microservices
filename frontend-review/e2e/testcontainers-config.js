const path = require('path');
const { default: test } = require('playwright/test');
const { DockerComposeEnvironment, Wait, getContainerRuntimeClient } = require('testcontainers');

const composeFile = path.resolve(__dirname, '../docker-compose-frontend-development.yml');
let testContainersRuntime;

async function setupContainers() {
  const containerRuntimeClient = await getContainerRuntimeClient();

  let dockerComposeEnvironment = new DockerComposeEnvironment(
      path.dirname(composeFile),
      path.basename(composeFile)
    )
      .withPullPolicy(PullPolicy.alwaysPull)
      .withWaitStrategy('localstack', Wait.forLogMessage('Ready'))
      .withWaitStrategy('review-collector', Wait.forLogMessage('Started ReviewCollectorApplication'))
      .withWaitStrategy('review-analyzer', Wait.forLogMessage('Started ReviewAnalyzerApplication'));


  testContainersRuntime = await dockerComposeEnvironment.up();

  console.log('Containers started successfully.');
  const containers = await containerRuntimeClient.container.list();

  console.log('Available containers:', containers.map(c => ({
    id: c.Id,
    names: c.Names,
    image: c.Image,
    status: c.Status
  })));

   testContainersRuntime.getContainer('localstack').exec(['awslocal', 's3', 'mb', 's3://review-analysis-bucket']);
   testContainersRuntime.getContainer('localstack').exec(['awslocal', 'sqs', 'create-queue', '--queue-name', 'review-analysis-queue']);

  return testContainersRuntime;
}

async function teardownContainers() {
  if (testContainersRuntime) await testContainersRuntime.stop();
}

function sleep(ms) {
  return new Promise(resolve => setTimeout(resolve, ms));
}


module.exports = { setupContainers, teardownContainers };
