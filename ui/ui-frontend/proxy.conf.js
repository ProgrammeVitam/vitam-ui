const fs = require('fs');

const applications = [
  { name: 'portal', crt: 'portal' },
  { name: 'identity', crt: 'identity-admin' },
  { name: 'referential', crt: 'referential' },
  { name: 'archive-search', crt: 'archive-search' },
  { name: 'pastis', crt: 'pastis' },
  { name: 'collect', crt: 'collect' },
  { name: 'ingest', crt: 'ingest' },
];

module.exports = applications.map((application) => ({
  context: [`/${application.name}-api`],
  target: {
    protocol: 'https:',
    host: 'localhost',
    port: 8070,
    cert: fs.readFileSync(`../../dev-deployment/environments/certs/vitamui-services/clients/ui-${application.crt}/ui-${application.crt}.crt`),
    key: fs.readFileSync(`../../dev-deployment/environments/certs/vitamui-services/clients/ui-${application.crt}/ui-${application.crt}.key`),
    passphrase: 'changeme',
  },
  changeOrigin: true,
  secure: false,
  logLevel: 'debug',
}));
