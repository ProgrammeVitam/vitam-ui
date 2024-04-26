#!/bin/bash
set -e

pushd $(dirname $0)

echo -e "\n\n###################################\nCleaning up..."
rm *.crt || true
rm *.key || true
rm *.p12 || true

echo -e "\n\n###################################\nGenerating root CA key and certificate..."
openssl genpkey -algorithm RSA -out root-ca.key
openssl req -new -x509 -key root-ca.key -out root-ca.crt -subj "/CN=root-ca" -days 36500 -addext "nsComment=CA Root" -addext "subjectKeyIdentifier=hash" -addext "authorityKeyIdentifier=keyid,issuer" -addext "basicConstraints=critical,CA:true,pathlen:1" -addext "keyUsage=keyCertSign,cRLSign" -addext "nsCertType=sslCA"


echo -e "\n\n###################################\nGenerating intermediate CA key and certificate..."
cat <<EOT > intermediate-ca.ext
nsComment                       = "CA Intermediate"
subjectKeyIdentifier            = hash
authorityKeyIdentifier          = keyid,issuer:always
basicConstraints                = critical,CA:true,pathlen:0
issuerAltName                   = issuer:copy
keyUsage                        = keyCertSign, cRLSign
nsCertType                      = sslCA
EOT

openssl genpkey -algorithm RSA -out intermediate-ca.key
openssl req -new -key intermediate-ca.key -out intermediate-ca.csr -subj "/CN=intermediate-ca"
openssl x509 -req -in intermediate-ca.csr -CA root-ca.crt -CAkey root-ca.key -CAcreateserial -out intermediate-ca.crt -days 36500 -extfile intermediate-ca.ext
rm intermediate-ca.csr intermediate-ca.ext


echo -e "\n\n###################################\nGenerating reverse key and certificate..."
cat <<EOT > reverse.ext
subjectKeyIdentifier            = hash
authorityKeyIdentifier          = keyid,issuer:always
issuerAltName                   = issuer:copy
basicConstraints                = critical,CA:FALSE
keyUsage                        = digitalSignature, keyEncipherment
nsCertType                      = server
extendedKeyUsage                = serverAuth
EOT

openssl genpkey -algorithm RSA -out reverse.key
openssl req -new -key reverse.key -out reverse.csr -subj "/C=US/ST=State/L=City/O=Organization/OU=Organizational Unit/CN=YourDomainName" -addext "subjectAltName=email:your_email@example.com"
openssl x509 -req -in reverse.csr -CA intermediate-ca.crt -CAkey intermediate-ca.key -CAcreateserial -out reverse.crt -days 7300 -extfile reverse.ext
rm reverse.csr reverse.ext

echo -e "\n\n###################################\nGenerating client key and certificate..."
cat <<EOT > client.ext
subjectKeyIdentifier            = hash
authorityKeyIdentifier          = keyid,issuer:always
issuerAltName                   = issuer:copy
basicConstraints                = critical,CA:FALSE
keyUsage                        = digitalSignature
nsCertType                      = client
extendedKeyUsage                = clientAuth
subjectAltName=email:\${ENV::EMAIL}
EOT

cat client.ext

export EMAIL=user@domain.com
openssl genpkey -algorithm RSA -out client.key

openssl req -new -key client.key -out client.csr -subj "/CN=UserCN/C=FR"
openssl x509 -req -in client.csr -CA intermediate-ca.crt -CAkey intermediate-ca.key -CAcreateserial -out client.crt -days 7300 -extfile client.ext
rm client.csr client.ext

echo -e "\n\n###################################\nVerifying the certificates..."
openssl verify -verbose -verify_depth 3 -CAfile root-ca.crt -untrusted intermediate-ca.crt reverse.crt
openssl verify -verbose -verify_depth 3 -CAfile root-ca.crt -untrusted intermediate-ca.crt client.crt


echo -e "\n\n###################################\nGenerating PKCS12 key stores..."
openssl pkcs12 -export -in reverse.crt -inkey reverse.key -out reverse.p12 -name "reverse" -password pass:azerty
openssl pkcs12 -export -in client.crt -inkey client.key -out client.p12 -name "client" -password pass:azerty

echo -e "\n\n###################################\nGenerating trust stores..."
cat root-ca.crt intermediate-ca.crt > truststore.crt

echo -e "\n\n###################################\nCopying files..."
cp reverse.* ../reverse-nginx/
cp -f truststore.* ../reverse-nginx/

cp client.* ../client/
cp -f truststore.* ../client/

echo -e "\n\n###################################\nCleaning up..."
rm *.crt || true
rm *.key || true
rm *.p12 || true

echo -e "\n\n###################################\nDONE !"
popd
