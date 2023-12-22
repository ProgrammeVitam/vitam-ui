const fs = require('fs');

const PROXY_CONFIG = [
  {
    // Portal to IAM External Backend
    context: ['/portal-api'],
    target: {
      protocol: 'https:',
      host: 'localhost',
      port: 8083,
      pfx: fs.readFileSync('../../dev-deployment/environments/certs/server/hosts/localhost/ui-portal.p12'),
      passphrase: 'changeme',
    },
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    pathRewrite: {
      '^/portal-api/ui': '/iam/v1',
      '^/portal-api': '/iam/v1',
    },
  },
  {
    // Identity to IAM External Backend
    context: ['/identity-api'],
    target: {
      protocol: 'https:',
      host: 'localhost',
      port: 8083,
      pfx: fs.readFileSync('../../dev-deployment/environments/certs/server/hosts/localhost/ui-identity-admin.p12'),
      passphrase: 'changeme',
    },
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    pathRewrite: {
      '^/identity-api/ui': '/iam/v1',
      '^/identity-api/logbooks': '/v1/logbooks',
      '^/identity-api/accesscontracts': '/v1/accesscontracts',
      '^/identity-api': '/iam/v1',
    },
  },
  {
    context: ['/referential-api/security-profile'],
    target: {
      protocol: 'https:',
      host: 'localhost',
      port: 8087,
      pfx: fs.readFileSync('../../dev-deployment/environments/certs/server/hosts/localhost/ui-referential.p12'),
      passphrase: 'changeme',
    },
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    pathRewrite: {
      '^/referential-api': '/referential/v1',
    },
  },
  {
    // Referential to IAM External Backend
    context: [
      '/referential-api/ui',
      '/referential-api/externalparameters',
      '/referential-api/security',
      '/referential-api/tenants',
      '/referential-api/customers',
      '/referential-api/userinfos/me',
      '/referential-api/users/analytics',
      '/referential-api/logbooks/operations',
    ],
    target: {
      protocol: 'https:',
      host: 'localhost',
      port: 8083,
      pfx: fs.readFileSync('../../dev-deployment/environments/certs/server/hosts/localhost/ui-referential.p12'),
      passphrase: 'changeme',
    },
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    pathRewrite: {
      '^/referential-api/ui': '/iam/v1',
      '^/referential-api/externalparameters': '/iam/v1/externalparameters/me',
      '^/referential-api/logbooks/operations': '/v1/logbooks/operations',
      '^/referential-api': '/iam/v1',
    },
  },
  {
    context: ['/referential-api'],
    target: {
      protocol: 'https:',
      host: 'localhost',
      port: 8087,
      pfx: fs.readFileSync('../../dev-deployment/environments/certs/server/hosts/localhost/ui-referential.p12'),
      passphrase: 'changeme',
    },
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    pathRewrite: {
      '^/referential-api/fileformat': '/referential/v1/fileformats',
      '^/referential-api/fileFormat': '/referential/v1/fileformats',
      '^/referential-api/operation': '/referential/v1/operations',
      '^/referential-api/search/filingplan': '/units/filingplan',
      '^/referential-api/search/units': '/units',
      '^/referential-api/static': '',
      '^/referential-api': '/referential/v1',
    },
  },
];

module.exports = PROXY_CONFIG;
