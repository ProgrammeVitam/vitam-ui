// Karma configuration file, see link for more information
// https://karma-runner.github.io/1.0/config/configuration-file.html
const path = require('path');

module.exports = function (config) {
  const projectPath = config.buildWebpack.webpackConfig.context;
  const projectName = path.basename(projectPath);

  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],
    plugins: [
      require('karma-jasmine'),
      require('karma-chrome-launcher'),
      require('puppeteer'),
      require('karma-spec-reporter'),
      require('karma-junit-reporter'),
      require('karma-coverage-istanbul-reporter'),
      require('@angular-devkit/build-angular/plugins/karma')
    ],
    client:{
      clearContext: false, // leave  Spec Runner output visible in browser
      captureConsole: false,
    },
    coverageIstanbulReporter: {
      reports: [ 'html', 'lcovonly' ],
      dir: path.join(projectPath, 'target/coverage'),
      fixWebpackSourcePaths: true
    },
    angularCli: {
      environment: 'dev'
    },
    reporters : ['spec', 'junit', 'coverage-istanbul'],
    specReporter: {
      maxLogLines: 5,             // limit number of lines logged per test
      suppressSummary: false,      // do not print summary
      suppressErrorSummary: false, // do not print error summary
      suppressFailed: false,      // do not print information about failed tests
      suppressPassed: false,      // do not print information about passed tests
      suppressSkipped: true,      // do not print information about skipped tests
      showBrowser: false,        // print the browser for each spec
      showSpecTiming: true,      // print the time elapsed for each spec
      failFast: false,           // test would finish with error when a first
    },
    junitReporter: {
      outputDir: path.join(projectPath, 'target/junit'),
      suite: projectName,
      classNameFormatter: (browser, result) => `${projectName}.${result.suite[0]}`,
      useBrowserName: false,
    },
    port: 9876,
    colors: true,
    logLevel: config.LOG_INFO,
    autoWatch: false,
    browsers: ['Chrome'],
    singleRun: true
  });
};
