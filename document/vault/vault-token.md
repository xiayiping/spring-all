## Token

```shell
## issue a token related to policies
tee token.json <<EOF
{
  "policies": ["kv-a-ro"],
  "meta": {
    "user": "yipingx"
  },
  "ttl": "5m",
  "renewable": false
}
EOF
curl -k \
    --header "X-Vault-Token: $VAULT_TOKEN" \
    --request POST \
    --data @token.json \
    $VAULT_ADDR/v1/auth/token/create-orphan

```