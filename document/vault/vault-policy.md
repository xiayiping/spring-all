## Policy

### Policy 

```hcl
# Permit reading the "teamb" path under any top-level path under secret/
path "secret/+/teamb" {
  capabilities = ["read"]
}

# Permit reading secret/foo/bar/teamb, secret/bar/foo/teamb, etc.
path "secret/+/+/teamb" {
  capabilities = ["read"]
}

```

#### Capabilities

1. create (POST/PUT) - Allows creating data at the given path. Very few parts of Vault distinguish between create and update, so most operations require both create and update capabilities. Parts of Vault that provide such a distinction are noted in documentation.
2. read (GET) - Allows reading the data at the given path.
3. update (POST/PUT) - Allows changing the data at the given path. In most parts of Vault, this implicitly includes the ability to create the initial value at the path.
4. patch (PATCH) - Allows partial updates to the data at a given path.
5. delete (DELETE) - Allows deleting the data at the given path.
6. list (LIST) - Allows listing values at the given path. Note that the keys returned by a list operation are not filtered by policies. Do not encode sensitive information in key names. Not all backends support listing.


### Samples

```shell
## List policies

curl -k \
    --header "X-Vault-Token: $VAULT_TOKEN" \
    $VAULT_ADDR/v1/sys/policy \
    | jq

## read policy 
curl -k \
    --header "X-Vault-Token: $VAULT_TOKEN" \
    $VAULT_ADDR/v1/sys/policy/default \
    | jq

## create or update
tee kv.a.ro.hcl <<EOF
path "kv/data/a/*" {
  capabilities = ["read", "list"]
}
EOF
## printf "%s\\n", $0: Prints each line followed by the literal \n instead of a real newline.
tee kv.a.ro.json <<EOF
{
  "policy": "$(awk '{gsub(/"/, "\\\""); printf "%s\\n", $0}' kv.a.ro.hcl)"
}
EOF
curl -k \
  --request POST \
  --header "X-Vault-Token: $VAULT_TOKEN" \
  --data @kv.a.ro.json \
  $VAULT_ADDR/v1/sys/policy/kv-a-ro

```

```shell
## another policy for creating orphan token
tee orphan.token.rw.hcl <<EOF
path "auth/token/create-orphan" {
  capabilities = ["read", "list", "create", "update", "delete"]
}
EOF
tee orphan.token.rw.json <<EOF
{
  "policy": "$(awk '{gsub(/"/, "\\\""); printf "%s\\n", $0}' orphan.token.rw.hcl)"
}
EOF
curl -k \
  --request POST \
  --header "X-Vault-Token: $VAULT_TOKEN" \
  --data @orphan.token.rw.json \
  $VAULT_ADDR/v1/sys/policy/orphan-token-rw

```