const { teardownContainers } = require('./testcontainers-with-everything');

module.exports = async () => {
  console.log('Starting global teardown...');
  await teardownContainers();
  console.log('Containers stopped.');
};