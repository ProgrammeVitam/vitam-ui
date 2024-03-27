var baseConfig = require('../../karma.conf.ci.js');

module.exports = function(config){
    // Load base config
    baseConfig(config);

    // Override base config dir
    config.set({
        files: [
          'src/test.ts'
        ],
        coverageIstanbulReporter: {
            dir: 'target/coverage/identity',
            fixWebpackSourcePaths: true
        }
    });
};
