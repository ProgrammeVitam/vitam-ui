const fs = require('fs');

const applications = [
  { name: 'portal', pfx: 'portal' },
  { name: 'identity', pfx: 'identity-admin' },
  { name: 'referential', pfx: 'referential' },
  { name: 'archive-search', pfx: 'archive-search' },
  { name: 'pastis', pfx: 'pastis' },
  { name: 'collect', pfx: 'collect' },
  { name: 'ingest', pfx: 'ingest' },
];

module.exports = applications.map((application) => ({
  context: [`/${application.name}-api`],
  target: {
    protocol: 'https:',
    host: 'localhost',
    port: 8070,
    pfx: fs.readFileSync(`../../dev-deployment/environments/certs/server/hosts/localhost/ui-${application.pfx}.p12`),
    passphrase: 'changeme',
  },
  changeOrigin: true,
  secure: false,
  logLevel: 'debug',
}));
