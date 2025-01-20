## Start Consul in DEV
```shell
# https://developer.hashicorp.com/consul/tutorials/developer-discovery/service-registration-external-services
consul agent -dev
```

## Install in VM
https://developer.hashicorp.com/consul/downloads
```shell
sudo apt update 
wget -O- https://apt.releases.hashicorp.com/gpg | sudo gpg --dearmor -o /usr/share/keyrings/hashicorp-archive-keyring.gpg
echo "deb [signed-by=/usr/share/keyrings/hashicorp-archive-keyring.gpg] https://apt.releases.hashicorp.com $(lsb_release -cs) main" | sudo tee /etc/apt/sources.list.d/hashicorp.list
sudo apt update && sudo apt install consul

# not sure if this is necessary
sudo apt-get install terraform


```

## Run in linux
```shell
# this can startup a ui but error with no cluster leader
consul agent -server -ui   -bind=172.22.247.107  -client=172.22.247.107 -data-dir=/home/yipingx/consul/data
# the key point of ui is -server -ui -client=x.x.x.x
```

maybe check here: \
https://developer.hashicorp.com/consul/docs/agent#starting-the-consul-agent

below config also enables me to start the server
```shell
consul agent -config-file=/home/yipingx/consul/config/config.json
```

```json
{
    "bootstrap": true, 
    "server": true,
    "ui" : true,
    "log_level" : "DEBUG",
    "datacenter" : "server1",
    "addresses" : {
        "http": "0.0.0.0"
    },
    "bind_addr": "172.22.247.107",
    "client_addr": "172.22.247.107",
    "node_name": "consul-01",
    "data_dir": "/home/yipingx/consul/config/",
    "acl_datacenter" :"server1",
    "acl_default_policy":"allow",
    "encrypt": "C7y5PjP6sYgRMfbYajqyAHSMFRZ/uosw6hplQpynHg8="
}
```
I only know
- bind_addr
- client_addr
- data_dir
- server
- ui

# Useful Commands

```shell

## list all nodes
consul members
curl http://<consul-server>:8500/v1/catalog/nodes
## list all services
consul catalog services
curl http://<consul-server>:8500/v1/catalog/services
## list all nodes for provided service
consul catalog nodes -service=web
curl http://<consul-server>:8500/v1/catalog/service/web


```
