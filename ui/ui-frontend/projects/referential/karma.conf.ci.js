const baseConfig = require('../../karma.conf.ci.js');

module.exports = function (config) {
  // Load base config
  baseConfig(config);

  // Override base config dir
  config.set({
    coverageIstanbulReporter: {
      dir: 'target/coverage/referential',
      fixWebpackSourcePaths: true,
    },
    junitReporter: {
      outputDir: 'target/junit', // results will be saved as $outputDir/$browserName.xml
      outputFile: 'referential', // if included, results will be saved as $outputDir/$browserName/$outputFile
    },
  });
};
