

```shell

export VAULT_TOKEN=hvs.2H7QJraNFezS7Glg2KX2ZWRY
export VAULT_ADDR=https://localhost:7200
export AUTH_PATH=approle
export APP_ROLE=test_role
if [ ! -d tmp ]; then mkdir tmp ; fi

```

```shell


curl -k \
  -H "X-Vault-Token: $VAULT_TOKEN" \
  --request POST \
  --data '{"type" : "approle", "secret_id_ttl" : "0" }' \
  $VAULT_ADDR/v1/sys/auth/$AUTH_PATH

```

```shell

tee tmp/role.json <<EOF
{
  "role_name" : "$APP_ROLE",
  "token_policies" : [ "kv-a-ro" ],
  "secret_id_num_uses" : 0,
  "secret_id_ttl" : 0,
  "token_max_ttl" : "10m"
}
EOF

```

```shell

curl -k \
  --header "X-Vault-Token: $VAULT_TOKEN" \
  --request POST \
  --data @tmp/role.json \
  $VAULT_ADDR/v1/auth/$AUTH_PATH/role/$APP_ROLE
  

```

```shell
curl -k \
  --header "X-Vault-Token: $VAULT_TOKEN" \
  --request LIST \
  $VAULT_ADDR/v1/auth/$AUTH_PATH/role | jq
  
curl -k \
  --header "X-Vault-Token: $VAULT_TOKEN" \
  $VAULT_ADDR/v1/auth/$AUTH_PATH/role/$APP_ROLE | jq
  

```

```shell
curl -k \
  --header "X-Vault-Token: $VAULT_TOKEN" \
  $VAULT_ADDR/v1/auth/$AUTH_PATH/role/$APP_ROLE/role-id | jq


```

```shell
curl -k \
  --header "X-Vault-Token: $VAULT_TOKEN" \
  --request POST \
  --data '{ "ttl": 0, "num_uses": 0 }' \
  $VAULT_ADDR/v1/auth/$AUTH_PATH/role/$APP_ROLE/secret-id


```