const { teardownContainers } = require('./testcontainers-config');

module.exports = async () => {
  console.log('Starting global teardown...');
  await teardownContainers();
  console.log('Containers stopped.');
};