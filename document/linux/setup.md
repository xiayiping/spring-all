
# linux

## user

```shell
sudo useradd -m -d /home/username -s /usr/sbin/nologin username
```

## rpm

```shell
sudo rpm -ivh package.rpm
```

## filewall

```shell
sudo firewall-cmd --list-all
sudo firewall-cmd --zone=public --add-port=8100/tcp --permanent
sudo firewall-cmd --zone=public --add-port=8080/tcp --permanent
sudo firewall-cmd --zone=public --add-port=8301/tcp --permanent
sudo firewall-cmd --zone=public --add-port=1300/tcp --permanent
sudo firewall-cmd --zone=public --add-port=8500/tcp --permanent
sudo firewall-cmd --zone=public --add-port=8600/tcp --permanent
sudo firewall-cmd --reload
sudo firewall-cmd --reload
sudo firewall-cmd --zone=public --remove-port=80/tcp --permanent
sudo firewall-cmd --reload
```

## systemctl

```shell

sudo systemctl daemon-reload

systemctl daemon-reload
systemctl stop trex.service
systemctl restart trex.service
journalctl  -u trex.service
sudo journalctl -xeu apigw.service

## start with env
sudo systemctl set-environment MY_ENV_VAR="yyy" && sudo systemctl restart go-app

```

## sudo-er add
```shell

sudo visudo

```