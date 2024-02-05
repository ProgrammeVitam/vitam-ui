const JWS_ALGORITHMS = [
  '',
  'HS256',
  'HS384',
  'HS512',
  'RS256',
  'RS384',
  'RS512',
  'ES256',
  'ES256K',
  'ES384',
  'ES512',
  'PS256',
  'PS384',
  'PS512',
];
export enum ProtocoleType {
  SAML = 'SAML',
  OIDC = 'OIDC',
  CERTIFICAT = 'CERTIFICAT',
}

export default JWS_ALGORITHMS;
