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



### Enable Jwt Token

```shell

## enable
export JWT_AUTH_PATH=jwt

tee payload.json <<EOF
{
  "path": "${JWT_AUTH_PATH}", 
  "type": "${JWT_AUTH_PATH}"
}
EOF
curl -k \
  --header "X-Vault-Token: $VAULT_TOKEN" \
  --request POST \
  --data @payload.json \
  $VAULT_ADDR/v1/sys/auth/${JWT_AUTH_PATH}


## add jwt key
cat ./my_cert.json  | jq .data.certificate > jwt_pub.txt
tee jwt_pub.json <<EOF
{
  "jwt_validation_pubkeys": $(cat jwt_pub.txt)
}
EOF
curl -k \
  --header "X-Vault-Token: $VAULT_TOKEN" \
  --request POST \
  --data @jwt_pub.json \
  $VAULT_ADDR/v1/auth/${JWT_AUTH_PATH}/config

## add jwt role
export JWT_ROLE=deploy-service-role
tee jwt_role.json <<EOF
{
  "name": "${JWT_ROLE}",
  "role_type" : "jwt",
  "user_claim" : "${JWT_ROLE}",
  "bound_claims": {
    "role": "deployer"
  }
}
EOF
curl -k \
  --header "X-Vault-Token: $VAULT_TOKEN" \
  --request POST \
  --data @jwt_role.json \
  $VAULT_ADDR/v1/auth/${JWT_AUTH_PATH}/role/${JWT_ROLE}


## list roles 
curl -k \
    --header "X-Vault-Token: $VAULT_TOKEN" \
    --request LIST \
    $VAULT_ADDR/v1/auth/${JWT_AUTH_PATH}/role

curl -k \
    --header "X-Vault-Token: $VAULT_TOKEN" \
    $VAULT_ADDR/v1/auth/${JWT_AUTH_PATH}/role/${JWT_ROLE}

```