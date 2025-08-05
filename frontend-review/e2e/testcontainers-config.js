const path = require('path');
const { DockerComposeEnvironment, Wait, getContainerRuntimeClient } = require('testcontainers');

const composeFile = path.resolve(__dirname, '../docker-compose-frontend-development.yml');
let testContainersRuntime;

async function setupContainers() {
  const containerRuntimeClient = await getContainerRuntimeClient();

  let dockerComposeEnvironment = new DockerComposeEnvironment(
      path.dirname(composeFile),
      path.basename(composeFile)
    )
      .withWaitStrategy('review-collector', Wait.forHttp('/actuator/health').forStatusCode(200))
      .withWaitStrategy('review-analyzer', Wait.forHttp('/actuator/health').forStatusCode(200));


      dockerComposeEnvironment.up();
  // testContainersRuntime = await containerRuntimeClient.compose.up({
  //   environment: dockerComposeEnvironment
  // });

  console.log('Containers started successfully.');
  const containers = await containerRuntimeClient.container.list();

  // Find the localstack container by name or image
  const localstackContainerInfo = containers.find(c => {
    // Try by name (may include /localstack) or by image
    return (c.Names && c.Names.some(name => name.includes('localstack'))) || (c.Image && c.Image.includes('localstack'));
  });

  if (localstackContainerInfo) {
    const localstackContainer = containerRuntimeClient.container.getById(localstackContainerInfo.Id);
    // Create S3 bucket
    await containerRuntimeClient.container.exec(localstackContainer, ['awslocal', 's3', 'mb', 's3://review-analysis-bucket']);
    // Create SQS queue
    await containerRuntimeClient.container.exec(localstackContainer, ['awslocal', 'sqs', 'create-queue', '--queue-name', 'review-analysis-queue']);
    console.log('Created S3 Bucket and SQS Queue.');
  } else {
    console.warn('Localstack container not found for exec commands.');
  }

  return testContainersRuntime;
}

async function teardownContainers() {
  if (testContainersRuntime) await testContainersRuntime.down();
}

module.exports = { setupContainers, teardownContainers };
