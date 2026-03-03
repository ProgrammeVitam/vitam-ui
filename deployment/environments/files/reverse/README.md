# External Reverse Proxy Certificates

Place your certificates provided by an external Certificate Authority (CA) in this directory. By providing these files manually, you prevent the VitamUI PKI scripts from overwriting them with generated self-signed certificates during deployment.

## Expected Files

The following files must be present in this directory:

* **`reverse.crt`**: The certificate bundle.
* **`reverse.key`**: The private key associated with the certificate.

## Certificate Bundle Requirements

The `reverse.crt` file must be a concatenation of your certificates in **PEM format**. For the reverse proxy to function correctly and provide a full trust chain to browsers, use the following order:

1. The Entity (Domain) Certificate
2. The Intermediate CA Certificate(s)
3. The Root CA Certificate (optional, but recommended)

> **Note:** You can create this bundle via the command line:
> `cat domain.crt intermediate.crt root.crt > reverse.crt`

## Configuration & Security

If your private key (`reverse.key`) is encrypted with a passphrase, you **must** define it in the VitamUI vault to allow Nginx to start automatically.

1. Open the vault file: `environments/group_vars/all/vault-vitamui.yml`
2. Set the following variable:

```yml
nginx_cert_key_password: "your_password_here"
```
