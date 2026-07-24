const path = require('path');
const { execSync } = require('child_process');
const { DockerComposeEnvironment, Wait } = require('testcontainers');

const composeFile = path.resolve(__dirname, '../docker-compose-frontend-development.yml');
const terraformDir = path.resolve(__dirname, '../../terraform');
let testContainersRuntime;

function provisionAwsResourcesWithTerraform() {
  execSync('terraform init', {
    cwd: terraformDir,
    stdio: 'inherit',
  });

  execSync('terraform apply -var-file=local.tfvars -auto-approve', {
    cwd: terraformDir,
    stdio: 'inherit',
  });
}

async function setupContainers() {

  testContainersRuntime = await new DockerComposeEnvironment(
      path.dirname(composeFile),
      path.basename(composeFile)
    )
      .withWaitStrategy('localstack', Wait.forLogMessage('Ready'))
      .withWaitStrategy('review-collector', Wait.forHttp("/actuator/health", 8080).forStatusCode(200))
      .withWaitStrategy('review-analyzer', Wait.forHttp("/actuator/health", 8081).forStatusCode(200))
      .up();

  console.log('Containers started successfully.');

  provisionAwsResourcesWithTerraform();

  return testContainersRuntime;
}

async function teardownContainers() {
  if (testContainersRuntime) {
    await testContainersRuntime.down();
  }
}

module.exports = { setupContainers, teardownContainers };
