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
      '^/referential-api': '/referential/v1',
    },
  },

  {
    // archive-search to Referential External Backend
    context: ['/archive-search-api/security-profile', '/archive-search-api/ontology', '/archive-search-api/schemas', '/archive-search-api/rules'],
    target: {
      protocol: 'https:',
      host: 'localhost',
      port: 8087,
      pfx: fs.readFileSync('../../dev-deployment/environments/certs/server/hosts/localhost/ui-archive-search.p12'),
      passphrase: 'changeme',
    },
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    pathRewrite: {
      '^/archive-search-api/ontology': '/referential/v1/ontology',
      '^/archive-search-api/schemas': '/schemas',
      '^/archive-search-api/rules': '/referential/v1/rules',
      '^/archive-search-api': '/archive-search/v1',
    },
  },
  {
    // archive-search to IAM External Backend
    context: [
      '/archive-search-api/ui',
      '/archive-search-api/externalparameters',
      '/archive-search-api/security',
      '/archive-search-api/tenants',
      '/archive-search-api/customers',
      '/archive-search-api/userinfos/me',
      '/archive-search-api/users/analytics',
      '/archive-search-api/logbooks/operations',
      '/archive-search-api/accesscontracts',
    ],
    target: {
      protocol: 'https:',
      host: 'localhost',
      port: 8083,
      pfx: fs.readFileSync('../../dev-deployment/environments/certs/server/hosts/localhost/ui-archive-search.p12'),
      passphrase: 'changeme',
    },
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    pathRewrite: {
      '^/archive-search-api/security': '/iam/v1/security',
      '^/archive-search-api/userinfos': '/iam/v1/userinfos',
      '^/archive-search-api/ui/applications': '/iam/v1/applications',
      '^/archive-search-api/users': '/iam/v1/users',
      '^/archive-search-api/externalparameters': '/iam/v1/externalparameters/me',
      '^/archive-search-api': '/v1',
    },
  },
  {
    context: ['/archive-search-api/archive-search', '/archive-search-api/archive-units'],
    target: {
      protocol: 'https:',
      host: 'localhost',
      port: 8089,
      pfx: fs.readFileSync('../../dev-deployment/environments/certs/server/hosts/localhost/ui-archive-search.p12'),
      passphrase: 'changeme',
    },
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    pathRewrite: {
      '^/archive-search-api/archive-search/searchcriteriahistory': '/searchcriteriahistory',
      '^/archive-search-api/archive-search/filingholdingscheme': '/archives-search/filling-holding-schema',
      '^/archive-search-api/archive-search/': '/archives-search/',
      '^/archive-search-api/archive-units': '/archive-units',
    },
  },
  {
    // Pastis to Referential External Backend
    context: ['/pastis-api/archival-profile', '/pastis-api/profile'],
    target: {
      protocol: 'https:',
      host: 'localhost',
      port: 8087,
      pfx: fs.readFileSync('../../dev-deployment/environments/certs/server/hosts/localhost/ui-pastis.p12'),
      passphrase: 'changeme',
    },
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    pathRewrite: {
      '^/pastis-api/archival-profile': '/referential/v1/archival-profile',
      '^/pastis-api/profile': '/referential/v1/profile',
    },
  },
  {
    // pastis to IAM External Backend
    context: [
      '/pastis-api/ui',
      '/pastis-api/security',
      '/pastis-api/userinfos/me',
      '/pastis-api/users/analytics',
      '/pastis-api/logbooks/operations',
      '/pastis-api/accesscontracts',
    ],
    target: {
      protocol: 'https:',
      host: 'localhost',
      port: 8083,
      pfx: fs.readFileSync('../../dev-deployment/environments/certs/server/hosts/localhost/ui-pastis.p12'),
      passphrase: 'changeme',
    },
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    pathRewrite: {
      '^/pastis-api/security': '/iam/v1/security',
      '^/pastis-api/userinfos': '/iam/v1/userinfos',
      '^/pastis-api/ui/applications': '/iam/v1/applications',
      '^/pastis-api/users': '/iam/v1/users',
      '^/pastis-api': '/v1',
    },
  },
  {
    context: ['/pastis-api/pastis/'],
    target: {
      protocol: 'https:',
      host: 'localhost',
      port: 8015,
      pfx: fs.readFileSync('../../dev-deployment/environments/certs/server/hosts/localhost/ui-pastis.p12'),
      passphrase: 'changeme',
    },
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    pathRewrite: {
      '^/pastis-api/': '/',
    },
  },
  {
    // collect to IAM External Backend
    context: [
      '/collect-api/ui',
      '/collect-api/externalparameters',
      '/collect-api/security',
      '/collect-api/tenants',
      '/collect-api/userinfos/me',
      '/collect-api/users/analytics',
      '/collect-api/logbooks/operations',
      '/collect-api/accesscontracts',
    ],
    target: {
      protocol: 'https:',
      host: 'localhost',
      port: 8083,
      pfx: fs.readFileSync('../../dev-deployment/environments/certs/server/hosts/localhost/ui-collect.p12'),
      passphrase: 'changeme',
    },
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    pathRewrite: {
      '^/collect-api/security': '/iam/v1/security',
      '^/collect-api/userinfos': '/iam/v1/userinfos',
      '^/collect-api/ui/applications': '/iam/v1/applications',
      '^/collect-api/users': '/iam/v1/users',
      '^/collect-api/externalparameters': '/iam/v1/externalparameters/me',
      '^/collect-api': '/v1',
    },
  },
  {
    // collect to Referential External Backend
    context: ['/collect-api/ontology', '/collect-api/search/filingplan', '/collect-api/schemas', '/collect-api/rules'],
    target: {
      protocol: 'https:',
      host: 'localhost',
      port: 8087,
      pfx: fs.readFileSync('../../dev-deployment/environments/certs/server/hosts/localhost/ui-collect.p12'),
      passphrase: 'changeme',
    },
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    pathRewrite: {
      '^/collect-api/ontology': '/referential/v1/ontology',
      '^/collect-api/search/filingplan': '/units/filingplan',
      '^/collect-api/schemas': '/schemas',
      '^/collect-api/rules': '/referential/v1/rules',
    },
  },
  {
    context: ['/collect-api/projects', '/collect-api/transactions', '/collect-api/archive-units'],
    target: {
      protocol: 'https:',
      host: 'localhost',
      port: 8090,
      pfx: fs.readFileSync('../../dev-deployment/environments/certs/server/hosts/localhost/ui-collect.p12'),
      passphrase: 'changeme',
    },
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    pathRewrite: {
      '^/collect-api/projects/archive-units/searchcriteriahistory': '/archive-units/searchcriteriahistory',
      '^/collect-api/projects': '/collect-api/v1/projects',
      '^/collect-api/projects/object-groups': '/collect-api/v1/projects/object-groups',
      '^/collect-api/transactions/archive-units/([^/]+)/search': '/collect-api/v1/transactions/$1/archive-units',
      '^/collect-api/transactions/archive-units/([^/]+)/export-csv-search':
        '/collect-api/v1/transactions/$1/archive-units/export-csv-search',
      '^/collect-api/transactions/archive-units/([^/]+)/update-units-metadata':
        '/collect-api/v1/transactions/$1/archive-units/update-units-metadata',
      '^/collect-api/transactions/archive-units': '/collect-api/v1/transactions',
      '^/collect-api/transactions': '/collect-api/v1/transactions',
    },
  },
  {
    // ingest to Referential External Backend
    context: ['/ingest-api/ontology'],
    target: {
      protocol: 'https:',
      host: 'localhost',
      port: 8087,
      pfx: fs.readFileSync('../../dev-deployment/environments/certs/server/hosts/localhost/ui-ingest.p12'),
      passphrase: 'changeme',
    },
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    pathRewrite: {
      '^/ingest-api': '/ingest/v1',
    },
  },
  {
    // ingest to IAM External Backend
    context: [
      '/ingest-api/ui',
      '/ingest-api/externalparameters',
      '/ingest-api/security',
      '/ingest-api/tenants',
      '/ingest-api/userinfos/me',
      '/ingest-api/users/analytics',
      '/ingest-api/logbooks/operations',
    ],
    target: {
      protocol: 'https:',
      host: 'localhost',
      port: 8083,
      pfx: fs.readFileSync('../../dev-deployment/environments/certs/server/hosts/localhost/ui-ingest.p12'),
      passphrase: 'changeme',
    },
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    pathRewrite: {
      '^/ingest-api/security': '/iam/v1/security',
      '^/ingest-api/userinfos': '/iam/v1/userinfos',
      '^/ingest-api/ui/applications': '/iam/v1/applications',
      '^/ingest-api/users': '/iam/v1/users',
      '^/ingest-api/externalparameters': '/iam/v1/externalparameters/me',
      '^/ingest-api': '/v1',
    },
  },
  {
    context: ['/ingest-api/ingest'],
    target: {
      protocol: 'https:',
      host: 'localhost',
      port: 8088,
      pfx: fs.readFileSync('../../dev-deployment/environments/certs/server/hosts/localhost/ui-ingest.p12'),
      passphrase: 'changeme',
    },
    changeOrigin: true,
    secure: false,
    logLevel: 'debug',
    pathRewrite: {
      '^/ingest-api/': '/v1/',
    },
  },
];

module.exports = PROXY_CONFIG;
