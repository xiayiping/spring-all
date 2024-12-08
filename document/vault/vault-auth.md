## AUTH with Cert

### Create CA certificate role

```shell
export AUTH_NAME=deploy-service

## generate a cert file recognized by vault , (new line replace with literal \n)
cat ./my_cert.json | jq '.data.ca_chain | join("\n")' > auth_cert_nl.pem

tee create-auth.json <<EOF
{
  "certificate": $(cat auth_cert_nl.pem),
  "token_policies": "kv-a-ro,orphan-token-rw",
  "token_ttl" : "300",
  "token_max_ttl" : "300"
}
EOF
curl -k --header "X-Vault-Token: $VAULT_TOKEN" \
    --request POST \
    --data @create-auth.json \
    $VAULT_ADDR/v1/auth/cert/certs/${AUTH_NAME}

## Delete !!!
curl -k \
    --header "X-Vault-Token: $VAULT_TOKEN" \
    --request DELETE \
    $VAULT_ADDR/v1/auth/cert/certs/${AUTH_NAME}
    
## update
curl -k --header "X-Vault-Token: $VAULT_TOKEN" \
    --request PUT \
    --data @create-auth.json \
    $VAULT_ADDR/v1/auth/cert/certs/${AUTH_NAME}



```

### Login with CA cert
```shell
curl -k \
    --request POST \
    --cert cert.pem \
    --key auth_key.pem \
    --data '{"name" : "deploy-service"}' \
    $VAULT_ADDR/v1/auth/cert/login

```