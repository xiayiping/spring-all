
## Vault PKI

1. Certificate Revocation List (CRL) configuration is common to all issuers/
2. All authority access URLs are common to all issuers.
3. Issued certificates' serial numbers are unique across all issuers.


### Role

A role is a logical name that maps to a policy used to generate those credentials.
It allows configuration parameters to control certificate common names, alternate names,
the key uses that they are valid for, and more.

| Param              | Description                                                                                                                                                        | 
|--------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| allowed_domains    | Specifies the domains of the role (used with allow_bare_domains and allow-subdomains options)                                                                      |
| allow_bare_domains | Specifies if clients can request certificates matching the value of the actual domains themselves                                                                  |
| allow_subdomains   | Specifies if clients can request certificates with CNs that are subdomains of the CNs allowed by the other role options (NOTE: This includes wildcard subdomains.) |
| allow_glob_domains | Allows names specified in allowed_domains to contain glob patterns (e.g. ftp*.example.com)                                                                         |

### Full curl case

```shell

export VAULT_ADDR=https://127.0.0.1:7200
export VAULT_SKIP_VERIFY=true
export VAULT_TOKEN=hvs.2H7QJraNFezS7Glg2KX2ZWRY

# vault secrets enable pki
curl -k --header "X-Vault-Token: $VAULT_TOKEN" \
   --request POST \
   --data '{"type":"pki"}' \
   $VAULT_ADDR/v1/sys/mounts/pki

# vault secrets tune -max-lease-ttl=87600h pki   = 3650 days = 10 years
curl -k --header "X-Vault-Token: $VAULT_TOKEN" \
   --request POST \
   --data '{"max_lease_ttl":"87600h"}' \
   $VAULT_ADDR/v1/sys/mounts/pki/tune


#################################################

# Generate the example.com root CA, give it an issuer name, and save its certificate in the file
# vault write -field=certificate pki/root/generate/internal \
#      common_name="example.com" \
#      issuer_name="root-2023" \
#      ttl=87600h > root_2023_ca.crt
tee payload.json <<EOF
{
  "common_name": "example.com",
  "issuer_name": "root-2023",
  "ttl": "87600h"
}
EOF

curl -k --header "X-Vault-Token: $VAULT_TOKEN" \
   --request POST \
   --data @payload.json \
   $VAULT_ADDR/v1/pki/root/generate/internal \
   | jq -r ".data.certificate" > root_2023_ca.crt

# vault list pki/issuers/
curl -k \
    --silent \
    --header "X-Vault-Request: true" \
    --header "X-Vault-Token: $VAULT_TOKEN" \
    "$VAULT_ADDR"/v1/pki/issuers\?list=true \
    | jq


# vault read pki/issuer/$(vault list -format=json pki/issuers/ | jq -r '.[]') \
#  | tail -n 6
curl -k \
    --silent \
    --header "X-Vault-Token: $VAULT_TOKEN" \
    --header "X-Vault-Request: true" \
    $VAULT_ADDR/v1/pki/issuer/09c2c9a0-a874-36d2-de85-d79a7a51e373 \
    | jq

#################################################

# Create a role for the root CA
# vault write pki/roles/2023-servers allow_any_name=true
curl -k \
    --silent \
    --request PUT \
    --header "X-Vault-Token: $VAULT_TOKEN" \
    --header "X-Vault-Request: true" \
    --data '{"allow_any_name":"true", "issuer_ref": "root-2023"}' \
    "$VAULT_ADDR"/v1/pki/roles/2023-servers | jq


# Configure the CA and CRL URLs. 
# access these urls will download a cr file.  
# clr: a list of revoked certificates 
# issuing_certificates: ca root certificate
# vault write pki/config/urls \
#      issuing_certificates="$VAULT_ADDR/v1/pki/ca" \
#      crl_distribution_points="$VAULT_ADDR/v1/pki/crl"
tee payload-url.json <<EOF
{
  "issuing_certificates": "$VAULT_ADDR/v1/pki/ca",
  "crl_distribution_points": "$VAULT_ADDR/v1/pki/crl"
}
EOF
curl -k --header "X-Vault-Token: $VAULT_TOKEN" \
   --request POST \
   --data @payload-url.json \
   $VAULT_ADDR/v1/pki/config/urls | jq


#################################################
export PKI_INT_NAME=pki_int_cert

# vault secrets enable -path=pki_int pki
curl -k --header "X-Vault-Token: $VAULT_TOKEN" \
   --request POST \
   --data '{"type":"pki"}' \
   $VAULT_ADDR/v1/sys/mounts/${PKI_INT_NAME} 

# vault secrets tune -max-lease-ttl=43800h pki_int
curl -k --header "X-Vault-Token: $VAULT_TOKEN" \
   --request POST \
   --data '{"max_lease_ttl":"43800h"}' \
   $VAULT_ADDR/v1/sys/mounts/${PKI_INT_NAME}/tune

#################################################


# generate scr 
# vault write -format=json ${PKI_INT_NAME}/intermediate/generate/internal \
#      common_name="example.com Intermediate Authority" \
#      issuer_name="example-dot-com-intermediate" \
#      | jq -r '.data.csr' > pki_intermediate.csr
tee payload-int.json <<EOF
{
  "common_name": "example.com Intermediate Authority",
  "issuer_name": "example-dot-com-intermediate"
}
EOF
curl -k \
    --silent \
    --header "X-Vault-Token: $VAULT_TOKEN" \
    --request POST \
    --data @payload-int.json \
    $VAULT_ADDR/v1/${PKI_INT_NAME}/intermediate/generate/internal \
    | jq -c '.data | .csr' > pki_intermediate.csr


# sign using root ca
# vault write -format=json pki/root/sign-intermediate \
#      issuer_ref="root-2023" \
#      csr=@pki_intermediate.csr \
#      format=pem_bundle ttl="43800h" \
#      | jq -r '.data.certificate' > intermediate.cert.pem
tee payload-int-cert.json <<EOF
{
  "csr": $(cat pki_intermediate.csr),
  "format": "pem_bundle",
  "ttl": "43800h"
}
EOF
curl -k \
    --silent \
    --header "X-Vault-Token: $VAULT_TOKEN" \
    --request POST \
    --data @payload-int-cert.json \
    $VAULT_ADDR/v1/pki/root/sign-intermediate \
    | jq '.data | .certificate' > intermediate.cert.pem


# write back signed cert to int
# actually this import the issuer in ${PKI_INT_NAME} (both root and intermediate)
# vault write ${PKI_INT_NAME}/intermediate/set-signed certificate=@intermediate.cert.pem
tee payload-signed.json <<EOF
{
  "certificate": $(cat intermediate.cert.pem)
}
EOF
curl -k \
    --silent \
    --header "X-Vault-Token: $VAULT_TOKEN" \
    --request POST \
    --data @payload-signed.json \
    $VAULT_ADDR/v1/${PKI_INT_NAME}/intermediate/set-signed \
    | jq

#################################################


# create role
# vault write ${PKI_INT_NAME}/roles/example-dot-com \
#      issuer_ref="$(vault read -field=default ${PKI_INT_NAME}/config/issuers)" \
#      allowed_domains="example.com" \
#      allow_subdomains=true \
#      max_ttl="720h"
tee payload-role.json <<EOF
{
  "allowed_domains": "example.com",
  "allow_subdomains": true,
  "issuer_ref": "$(vault read -field=default ${PKI_INT_NAME}/config/issuers)",
  "max_ttl": "720h"
}
EOF
curl -k --header "X-Vault-Token: $VAULT_TOKEN" \
   --request POST \
   --data @payload-role.json \
   $VAULT_ADDR/v1/${PKI_INT_NAME}/roles/example-dot-com | jq


# Invoke the /${PKI_INT_NAME}/issue/<role_name> endpoint to request a new certificate.
# this will give you a json response as below after the command 
tee payload-req-cert.json <<EOF
{
  "common_name": "test3.example.com",
  "alt_names": "alt.example.com",
  "ip_sans":[
    "1.2.3.4"
  ],
  "ttl": "1d"
}
EOF
curl -k --header "X-Vault-Token: $VAULT_TOKEN" \
    --request POST \
    --data @payload-req-cert.json \
    $VAULT_ADDR/v1/${PKI_INT_NAME}/issue/example-dot-com | jq
# sample response:
# {
#   "request_id": "fc952ca3-fa40-1bca-23d3-f19f47edc598",
#   "lease_id": "",
#   "renewable": false,
#   "lease_duration": 0,
#   "data": {
#     "ca_chain": [
#       "-----BEGIN CERTIFICATE-----\nMIIDqDCCApCgAwIBAgIUGQB+RFA8tT8Z1Wkj4BK9CUM3ghwwDQYJKoZIhvcNAQEL\nBQAwFjEUMBIGA1UEAxMLZXhhbXBsZS5jb20wHhcNMjQxMjA3MTM0NjIxWhcNMjkx\nMjA2MTM0NjUxWjAtMSswKQYDVQQDEyJleGFtcGxlLmNvbSBJbnRlcm1lZGlhdGUg\nQXV0aG9yaXR5MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0XLK4lEw\n1Z11xMYiWMTMqCNuupbBuQGEc+ndk/uRcJTRanXLmPFDx+3wPQ117261eEdMwbuq\nMOktvjjkro9H8SLeMTnwbCrek8fmcxyYrZH4kdaH/4we0U6lNJWO18MTBKVbdimA\nCjHnBGXvONnyTWmwKdDNqGkucLFF92NBUaUONbStsbnNUcJpemr+eyph6w0wCIhI\nmSxsNUyoNofJ6kEKnL82i0snS+SlpWMdJrNU44HQjVYOd6wyXj3Iv7DSTC1BzTx5\n03M9V4skNBlqxHKiiamNyy7uIo/Z1mAWlksyHPfXyUhHJ61tS1CottZ1ShpgBPFG\nLcffosutI9OhsQIDAQABo4HWMIHTMA4GA1UdDwEB/wQEAwIBBjAPBgNVHRMBAf8E\nBTADAQH/MB0GA1UdDgQWBBTbfY9+iToeZf3Xgeu15BvLHC5TSzAfBgNVHSMEGDAW\ngBTDJ/ZZJ/rT7rZTPYQtBh8afhCj3TA8BggrBgEFBQcBAQQwMC4wLAYIKwYBBQUH\nMAKGIGh0dHBzOi8vMTI3LjAuMC4xOjcyMDAvdjEvcGtpL2NhMDIGA1UdHwQrMCkw\nJ6AloCOGIWh0dHBzOi8vMTI3LjAuMC4xOjcyMDAvdjEvcGtpL2NybDANBgkqhkiG\n9w0BAQsFAAOCAQEAvJZOyLkcrvsG7FdWVL83UFOlYQ+6TxMm9eqFdwxJxBTtXDwg\n1TaE0KHXdFM3xKcVHbhsDhvMRj1bgQjxiO87CIQen1LKAp9sRyBpkz7i6103zeUv\n9ltU2MK9Wao1NI16qLMB6RfLDdhdA2ltbjpEDgVWJSqW5a+Kd26q+FioGmKiTIBx\naIRf6ZKmKQNSU7k05iwINtm58BKwK0qS8+kVUFLELZfvSaS4QCoyKZDMWnOO/BNg\n5C2wZy+6TU0CHPp5OgBPqpJKFrM78Yn1JMLLYBSeoV0AoVAWDgP3kJm6Iu86lRbe\njA+jDwOB+8nAYmct8K13cY1vOT1fNcGZ6cJGqw==\n-----END CERTIFICATE-----",
#       "-----BEGIN CERTIFICATE-----\nMIIDNTCCAh2gAwIBAgIUcdccJBF48HJpjI2DOFwWyXM0QWswDQYJKoZIhvcNAQEL\nBQAwFjEUMBIGA1UEAxMLZXhhbXBsZS5jb20wHhcNMjQxMjA3MTMyMTM0WhcNMzQx\nMjA1MTMyMjA0WjAWMRQwEgYDVQQDEwtleGFtcGxlLmNvbTCCASIwDQYJKoZIhvcN\nAQEBBQADggEPADCCAQoCggEBAL74ROy9o1ghUjVGDbhIBLJtxI8InsA67n9QCtOq\nGObIdMCFdNjyWAPU7X5ZCI+vik9DCpYitJrT8ovwAR2YWUpEiQKMmnro/7go19XN\n1quArrXK+cd07lxkWRmgVqKHYsKucCYGovMOzDPEl9yqc1rck0I2EfQcHAPoE8++\nxj/kMEJLkwHptzy+pGogvY2cK15qaI7lYZr/5XDZqGC89i3Dv0TX3tCxVF55TCpB\nqfF1pbdmPJTRVa4FGb92j6VCYQV1/Xfv6kf2vEt8yw/QApL3Gdv8KB+FMAwpU68B\ndsL2cZf18MxvCXg+YJieBRxsefie0F5uhiGG6U/bcpQY8uECAwEAAaN7MHkwDgYD\nVR0PAQH/BAQDAgEGMA8GA1UdEwEB/wQFMAMBAf8wHQYDVR0OBBYEFMMn9lkn+tPu\ntlM9hC0GHxp+EKPdMB8GA1UdIwQYMBaAFMMn9lkn+tPutlM9hC0GHxp+EKPdMBYG\nA1UdEQQPMA2CC2V4YW1wbGUuY29tMA0GCSqGSIb3DQEBCwUAA4IBAQCysLgXZ84s\np2MZYqOMl2+4RqZoRZWkv1KiiNEw3WhSbYllniE1Pfodxy7gUhYCam56ypgF5iCv\naNlvbnYnpUTa6cKV5TJkQUzjG6MYkvstpZECVannOadcVmO3ex0cQN88mh0ULMLi\nuherrHBw+z5Gj1lm5kvVjT5xEPA5irrU+NL2HYThn482arjTvEJYGWgGYcNlAVdV\nL3xyEsgLMmE5ZWIDAbifZCUgK5buwwPpsxcxSPs3S93o8lfi7ZR26BpAlBW/10/t\nrwOz+qViruuxDX0eI5ajRNXglFPTWXIMetV7aBTsq2RUSu3fi6Hujv1yB/rafexF\nXKoA5J7TTT7/\n-----END CERTIFICATE-----"
#     ],
#     "certificate": "-----BEGIN CERTIFICATE-----\nMIIDZjCCAk6gAwIBAgIUf9lodAGKEjcDeicL6wqwYRP6H9cwDQYJKoZIhvcNAQEL\nBQAwLTErMCkGA1UEAxMiZXhhbXBsZS5jb20gSW50ZXJtZWRpYXRlIEF1dGhvcml0\neTAeFw0yNDEyMDcxNDAwMzVaFw0yNDEyMDgxNDAxMDVaMBsxGTAXBgNVBAMTEHRl\nc3QuZXhhbXBsZS5jb20wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDC\ncL8kPoJSKllDEBLRWDKd/0JDF7oKnMjL4Z13S8lroGl+ysGIGPKK5RZ2z4phHUOf\nycG8fZgzX3w/HFzWnMa1zj9NeRaAMZ77p9aswfVhntm2P35hI3jbL64CENI8lch5\nlCcW2L6HyQvTTKPFonJToO0Emfl4T4LXnAgzWf1SohGTDnm0MKRBpG7fejbSi6cL\nhoX1KYZCkr4SIqVt1riLducki73J8QWnPkcmqZeRu+7uAr12mQUeYF/qzQrxFqvL\ndBha9qYj71SAnel1mGLHZr+nBLraZ7lo+uEtVux7ivaLshiBW6BT3oZUdx4nA9hs\n+8/XvU/0h05vHmPlPjZtAgMBAAGjgY8wgYwwDgYDVR0PAQH/BAQDAgOoMB0GA1Ud\nJQQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjAdBgNVHQ4EFgQU5z07BcxGZ/V0LPCy\nUaG2eR+pcA4wHwYDVR0jBBgwFoAU232Pfok6HmX914HrteQbyxwuU0swGwYDVR0R\nBBQwEoIQdGVzdC5leGFtcGxlLmNvbTANBgkqhkiG9w0BAQsFAAOCAQEAstwXP6pT\n9KNUBI2tp9FLcYDJOSr6gz1SQnyqfTSn/LuYdni4H2D55eEHrvCiCIrxKDJd9g68\njQTywknQULCrvONrvH9zJKs0MwiEdsR4hVe9EiZ0ZRZnjU5xD25JbjF6s9aP+0uB\naUGBX00VdsDTqVmRupWV6US6OxrPKxYrQyHOfyRJEKatUzQaF9kL/uVjUTgE+oh4\nGvbbzXNzNFo0U5z2cVRevbyYfk0ZGcud+yq3CzKd5YI4tnhtgpOx6mPZjhKE8wzq\npyFBK91BNUU3dQqb2x1BrSsno4d1UAT6k1I3eZNQaxZSAets3U2KcCDlGE2yaYFr\nyfNe3FFFgDyO7Q==\n-----END CERTIFICATE-----",
#     "expiration": 1733666465,
#     "issuing_ca": "-----BEGIN CERTIFICATE-----\nMIIDqDCCApCgAwIBAgIUGQB+RFA8tT8Z1Wkj4BK9CUM3ghwwDQYJKoZIhvcNAQEL\nBQAwFjEUMBIGA1UEAxMLZXhhbXBsZS5jb20wHhcNMjQxMjA3MTM0NjIxWhcNMjkx\nMjA2MTM0NjUxWjAtMSswKQYDVQQDEyJleGFtcGxlLmNvbSBJbnRlcm1lZGlhdGUg\nQXV0aG9yaXR5MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0XLK4lEw\n1Z11xMYiWMTMqCNuupbBuQGEc+ndk/uRcJTRanXLmPFDx+3wPQ117261eEdMwbuq\nMOktvjjkro9H8SLeMTnwbCrek8fmcxyYrZH4kdaH/4we0U6lNJWO18MTBKVbdimA\nCjHnBGXvONnyTWmwKdDNqGkucLFF92NBUaUONbStsbnNUcJpemr+eyph6w0wCIhI\nmSxsNUyoNofJ6kEKnL82i0snS+SlpWMdJrNU44HQjVYOd6wyXj3Iv7DSTC1BzTx5\n03M9V4skNBlqxHKiiamNyy7uIo/Z1mAWlksyHPfXyUhHJ61tS1CottZ1ShpgBPFG\nLcffosutI9OhsQIDAQABo4HWMIHTMA4GA1UdDwEB/wQEAwIBBjAPBgNVHRMBAf8E\nBTADAQH/MB0GA1UdDgQWBBTbfY9+iToeZf3Xgeu15BvLHC5TSzAfBgNVHSMEGDAW\ngBTDJ/ZZJ/rT7rZTPYQtBh8afhCj3TA8BggrBgEFBQcBAQQwMC4wLAYIKwYBBQUH\nMAKGIGh0dHBzOi8vMTI3LjAuMC4xOjcyMDAvdjEvcGtpL2NhMDIGA1UdHwQrMCkw\nJ6AloCOGIWh0dHBzOi8vMTI3LjAuMC4xOjcyMDAvdjEvcGtpL2NybDANBgkqhkiG\n9w0BAQsFAAOCAQEAvJZOyLkcrvsG7FdWVL83UFOlYQ+6TxMm9eqFdwxJxBTtXDwg\n1TaE0KHXdFM3xKcVHbhsDhvMRj1bgQjxiO87CIQen1LKAp9sRyBpkz7i6103zeUv\n9ltU2MK9Wao1NI16qLMB6RfLDdhdA2ltbjpEDgVWJSqW5a+Kd26q+FioGmKiTIBx\naIRf6ZKmKQNSU7k05iwINtm58BKwK0qS8+kVUFLELZfvSaS4QCoyKZDMWnOO/BNg\n5C2wZy+6TU0CHPp5OgBPqpJKFrM78Yn1JMLLYBSeoV0AoVAWDgP3kJm6Iu86lRbe\njA+jDwOB+8nAYmct8K13cY1vOT1fNcGZ6cJGqw==\n-----END CERTIFICATE-----",
#     "private_key": "-----BEGIN RSA PRIVATE KEY-----\nMIIEpAIBAAKCAQEAwnC/JD6CUipZQxAS0Vgynf9CQxe6CpzIy+Gdd0vJa6BpfsrB\niBjyiuUWds+KYR1Dn8nBvH2YM198Pxxc1pzGtc4/TXkWgDGe+6fWrMH1YZ7Ztj9+\nYSN42y+uAhDSPJXIeZQnFti+h8kL00yjxaJyU6DtBJn5eE+C15wIM1n9UqIRkw55\ntDCkQaRu33o20ounC4aF9SmGQpK+EiKlbda4i3bnJIu9yfEFpz5HJqmXkbvu7gK9\ndpkFHmBf6s0K8Rary3QYWvamI+9UgJ3pdZhix2a/pwS62me5aPrhLVbse4r2i7IY\ngVugU96GVHceJwPYbPvP171P9IdObx5j5T42bQIDAQABAoIBAAoRDkRRHCwm1yI6\ntc6y00QM0CBrHQ2pmle74Ywol1AEKRIQaz6hlAUO4T/bdN2cR0EBkA8zLWsTeXpG\n/KN70GnfOLXJHBoc948t3FZrYEnikID3/pIG5R3r9QVUW54ZXxy0R+XN6OdcyEjz\nQdNcmrCrtzmywxCwhSSmhmIqOYxKko0HFerRbuP5GSB1Wg9XP06LyvoUWgqMJopX\nJjYYcbkr6NJtx+Aa9KzIxtKS/Xl0l7dxWTTelp3Hll8h2lxNXTDV8jP0E1auRKqs\n3Z/5Ie37w+rVNzLS37EnmYVg/VKBiREF9nf/A2B8gedmh229TLSSbUao4k8FjK5u\npB3AGf0CgYEAy2u+GZ5w3O4aFD+rsKN+Eo05rR2UlbecPlhdghfJkst6o2/yfwVr\nXk9kwDcG2cUKmwVqePItcUXNhuLLNS3pFR0f1oK0gUk0gWetNEM7Xeq81ey3RklY\n5VYedc1sKzAkyuakTelZnq5To8vSnWZLctr2noKJ9brzbowwKEED3MsCgYEA9LLF\noMCdnztlwORHWuYlTDp2rpmc7ZufbHLySmaGe93kHwLx82b6PVs1+3qgs1qbqSnt\n2pHdntV2/tpEkhKSWhR6NJK/mCjZgZ3EPtQlMZhBC7XXTJNQ4hrpfyTghDfxoiI4\n6U4H8FS+sHE4eOLEvKYUcek4aPHV/Z1q1YdjyqcCgYEAlqEsvmm9p0tE0nOnXgBi\nrbgX2mIKEadJsjza2SdmIyY7pOSTi41myQo/T7ovf3hR9yLWicLC9Zq7q0MrODDL\n5M+RMNXOdeHQWHZ+IjIattvIgxxrRkev5PqVSaQYkg5vYDPTvMGrlD4AP00Bo34J\nJn6PmiaNpAGQmu6CBnzGMGsCgYBIf4LwGsFYgQenu7qsYs2gJR4uotKMz3PPdMr2\nYiPsJRTGX2ACBnuTnCeFR5K/MNQ3rXWMMXVg7svfvBle0ePMjo6abvQXB9tRfMW8\n+6u9Yue6qXiNdoQbSW/uvKVMZyrPJ6aG8RiHCwSoUBM0NbOmVOpvyXJuHWCSUIZr\nlz5otwKBgQC0yZjtWUZBi25aJl5YfS3dqay8BsfUF9uH+dgycr97g3y5N9mMTslT\ncx5dMZecFPTWh40CYILSeKIEx0kIB6N8dZYOVavWfHlrUOO+6KbGa50NbkttmqZO\nBP3a91t3h2r3zuwfV7VnIs7gYH3xtXa19GQXjCpQw6ZyGM+jj7v/sw==\n-----END RSA PRIVATE KEY-----",
#     "private_key_type": "rsa",
#     "serial_number": "7f:d9:68:74:01:8a:12:37:03:7a:27:0b:eb:0a:b0:61:13:fa:1f:d7"
#   },
#   "wrap_info": null,
#   "warnings": null,
#   "auth": null,
#   "mount_type": "pki"
# }
# 

# remove expired certificates 
curl -k --header "X-Vault-Token: $VAULT_TOKEN" \
    --request POST \
    --data '{"tidy_cert_store": true, "tidy_revoked_certs": true}' \
    $VAULT_ADDR/v1/${PKI_INT_NAME}/tidy | jq

# list all certs
curl -k --header "X-Vault-Token: $VAULT_TOKEN" \
     --request LIST \
     $VAULT_ADDR/v1/${PKI_INT_NAME}/certs | jq -r .data
     
```

