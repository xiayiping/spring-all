# CA

```shell
# creat ca
../1.18.0/consul tls ca create -domain=consul --days=7300

# check expire day
openssl x509 -in ./consul-agent-ca.pem -text -noout

# rename the file so that you know when it expires
mv ./consul-agent-ca.pem ./consul-agent-ca-20300403.pem
mv ./consul-agent-ca-key.pem ./consul-agent-ca-key-20300403.pem 

```

# Consul Server

```shell

../1.18.0/consul tls cert create -server -dc=prod-hkt -domain=consul --days=7300 \
  -ca ./consul-agent-ca-20300403.pem \
  -key ./consul-agent-ca-key-20300403.pem \
  -additional-ipaddress=10.6.100.50 \
  -additional-ipaddress=10.6.100.51 \
  -additional-ipaddress=10.6.100.52 \

# check expire day
openssl x509 -in ./prod-hkt-server-consul-0.pem -text -noout

# rename the pem files so that you know when it expires
mv ./prod-hkt-server-consul-0.pem ./prod-hkt-server-consul-0-20260404.pem 
mv ./prod-hkt-server-consul-0-key.pem ./prod-hkt-server-consul-0-20260404-key.pem 

```

# ACL

first time deployed need to init ACL manually

```shell
./consul acl bootstrap
# need to record the output 
```
