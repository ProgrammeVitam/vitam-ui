let baseConfig = require('./karma.conf.js');

process.env.CHROME_BIN = require('puppeteer').executablePath();

module.exports = function (config) {
  // Load base config
  baseConfig(config);

  // Override base config
  config.set({
    singleRun: true,
    autoWatch: false,
    captureTimeout: 210000,
    browserDisconnectTolerance: 3,
    browserDisconnectTimeout : 210000,
    browserNoActivityTimeout : 210000,
    browsers: ['ChromeHeadless_custom'],
    customLaunchers: {
      'ChromeHeadless_custom': {
        base: 'ChromeHeadless',
        flags: [
          '--disable-setuid-sandbox',
          '--no-sandbox',
          '--disable-gpu',
          '--enable-logging',
          '--no-default-browser-check',
          '--no-first-run',
          '--disable-default-apps',
          '--disable-popup-blocking',
          '--disable-translate',
          '--disable-background-timer-throttling',
          '--disable-renderer-backgrounding',
          '--disable-device-discovery-notifications',
          '--remote-debugging-port=9222',
          '--disable-web-security'
        ],
      },
    }
  });
};
