## KV

```shell

curl -k \
  --header "X-Vault-Token: $VAULT_TOKEN" \
  $VAULT_ADDR/v1/kv/data/a/b/c \
  | jq 

```