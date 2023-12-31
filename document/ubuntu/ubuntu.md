ubuntu server 222

- the network interface is maintained by network-manager in yaml form with name of 'netplan'
```shell
vim /etc/netplan/00-xxxx.yml

#sample of plan:
#network:
#  ethernets:
#    enp0s3:
#      dhcp4: true
#    enp0s8:
#      dhcp4: true
#  version: 2

sudo netplan apply

```