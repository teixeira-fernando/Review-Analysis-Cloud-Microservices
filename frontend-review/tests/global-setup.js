const { setupContainers } = require('./testcontainers-with-everything');
const fs = require('fs');
const path = require('path');

module.exports = async () => {
  console.log('Starting global setup...');
  const containers = await setupContainers();
};