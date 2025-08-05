const { setupContainers } = require('./testcontainers-config');

module.exports = async () => {
  console.log('Starting global setup...');
  const containers = await setupContainers();
};