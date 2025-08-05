const path = require('path');
const { DockerComposeEnvironment, Wait, getContainerRuntimeClient } = require('testcontainers');

const composeFile = path.resolve(__dirname, '../docker-compose-frontend-development.yml');
let environment;

async function setupContainers() {

  const containerRuntimeClient = await getContainerRuntimeClient();

  //console.log(containerRuntimeClient.info);

  const environment = await containerRuntimeClient.compose.up({environment: new DockerComposeEnvironment(
    path.dirname(composeFile),
    path.basename(composeFile),
  ).withWaitStrategy('review-collector', Wait.forHttp('/actuator/health').forStatusCode(200))
    .withWaitStrategy('review-analyzer', Wait.forHttp('/actuator/health').forStatusCode(200))});

  /*environment = await new DockerComposeEnvironment(
    path.dirname(composeFile),
    path.basename(composeFile),
  )
    .withBuild()
    .withStartupTimeout(20000) // Increase timeout to 20 seconds
    .withProjectName('e2e-tests')
    .withWaitStrategy('review-collector', Wait.forHttp('/actuator/health').forStatusCode(200))
    .withWaitStrategy('review-analyzer', Wait.forHttp('/actuator/health').forStatusCode(200))
    .up(['review-collector', 'review-analyzer']);
*/
  // Print available container names for debugging
  //const containers = environment.getContainers();

  // Use the exact names as in the compose file (no _1 suffix)
  //const reviewCollectorPort = environment.getContainer('review-collector').getMappedPort(8080);
  //const reviewAnalyzerPort = environment.getContainer('review-analyzer').getMappedPort(8081);
  //const localstackPort = environment.getContainer('localstack').getMappedPort(4566);

  return {
    environment
  };
}

async function teardownContainers() {
  if (environment) await environment.down();
}

module.exports = { setupContainers, teardownContainers };
