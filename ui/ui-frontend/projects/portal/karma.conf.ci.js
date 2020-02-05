var baseConfig = require('../../karma.conf.ci.js');

module.exports = function(config){
    // Load base config
    baseConfig(config);

    // Override base config dir
    config.set({
        coverageIstanbulReporter: {
            dir: 'target/coverage/portal',
            fixWebpackSourcePaths: true
        }
    });
};
