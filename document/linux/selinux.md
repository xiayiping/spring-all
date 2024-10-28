
# SELinux

## selinux

### systemctl allow
https://access.redhat.com/documentation/en-us/red_hat_enterprise_linux/6/html/security-enhanced_linux/sect-security-enhanced_linux-fixing_problems-allowing_access_audit2allow

```shell
audit2allow -w -a
audit2allow -a -M mySample
ausearch -m avc -ts recent | audit2allow -M mySample  # this one is better than audit2allow
```

### A Sample
The log entry you provided is from an SELinux (Security-Enhanced Linux) audit log. It indicates that a particular action was denied due to SELinux policies. Let's break down the components of this log entry to understand what it means:

```
denied  { write } for  pid=189124 comm="consul" name="local.snapshot.compact" dev="dm-0" ino=202009666 scontext=system_u:system_r:init_t:s0 tcontext=system_u:object_r:user_home_t:s0 tclass=file permissive=0
```

#### Breakdown of the Log Entry

- **`denied  { write }`**: This indicates that a write operation was denied.
- **`pid=189124`**: The process ID (PID) of the process attempting the operation.
- **`comm="consul"`**: The name of the command or executable that attempted the operation (in this case, `consul`).
- **`name="local.snapshot.compact"`**: The name of the file that the process was attempting to write to.
- **`dev="dm-0"`**: The device on which the file resides. `dm-0` typically refers to a device-mapper device, which is often used in LVM (Logical Volume Manager) setups.
- **`ino=202009666`**: The inode number of the file.
- **`scontext=system_u:system_r:init_t:s0`**: The SELinux security context of the source (the process, in this case). It consists of user (system_u), role (system_r), type (init_t), and sensitivity level (s0).
- **`tcontext=system_u:object_r:user_home_t:s0`**: The SELinux security context of the target (the file, in this case). It consists of user (system_u), role (object_r), type (user_home_t), and sensitivity level (s0).
- **`tclass=file`**: The class of the target object, indicating that it is a file.
- **`permissive=0`**: Indicates that SELinux is in enforcing mode (0 means enforcing, 1 means permissive).

#### Explanation

The log entry indicates that the `consul` process, with the security context `system_u:system_r:init_t:s0`, attempted to write to a file named `local.snapshot.compact`, which has the security context `system_u:object_r:user_home_t:s0`. However, this action was denied by SELinux because the `init_t` type does not have permission to write to files of type `user_home_t`.

#### Resolving the Issue

To resolve this issue, you have several options:

1. **Modify the SELinux Policy**: You can modify the SELinux policy to allow the `init_t` type to write to files of type `user_home_t`. This can be done by creating a custom SELinux policy module.

2. **Change the File Context**: You can change the SELinux context of the file to a type that is permissible for the process. For example, you might change the file's context to a type that `consul` is allowed to write to.

3. **Run in Permissive Mode**: As a temporary measure, you can switch SELinux to permissive mode, which will log denials but not enforce them. This is not recommended for production environments.

##### Example: Creating a Custom SELinux Policy Module

Here’s an example of how you might create a custom SELinux policy module to allow the `init_t` type to write to `user_home_t` files:

1. **Create a policy file** (e.g., `consul_write_user_home.te`):
    ```sh
    module consul_write_user_home 1.0;

    require {
        type init_t;
        type user_home_t;
        class file { read write };
    }

    # Allow init_t (e.g., consul process) to write to user_home_t files
    allow init_t user_home_t:file { read write };
    ```

2. **Compile and load the policy module**:
    ```sh
    checkmodule -M -m -o consul_write_user_home.mod consul_write_user_home.te
    semodule_package -o consul_write_user_home.pp -m consul_write_user_home.mod
    semodule -i consul_write_user_home.pp
    ```

This will create and install a custom SELinux policy module that allows the `init_t` type to write to `user_home_t` files.

#### Summary

The log entry indicates that the `consul` process was denied write access to a file due to SELinux policies. To resolve this, you can modify SELinux policies, change file contexts, or temporarily switch to permissive mode. However, modifying policies or contexts is generally the preferred solution for maintaining security.



## SELINUX SAMPLE
```shell
## esop_type.te 

module esop_type 1.0;

require {
    type systemd_systemctl_exec_t;
    type init_t;
    type http_port_t;
    type unreserved_port_t;
    type sshd_t;
    attribute file_type;
    class dir { write read create getattr add_name };
    class file { open write read getattr execute execute_no_trans map create append };
    class lnk_file { read };
    class tcp_socket name_connect;
}

type esop_t;
typeattribute esop_t file_type;
allow init_t esop_t:file { map getattr open read execute execute_no_trans create write append };
allow init_t esop_t:lnk_file { read };
allow init_t esop_t:dir { read write create getattr add_name };
allow init_t http_port_t:tcp_socket name_connect;
allow init_t unreserved_port_t:tcp_socket name_connect;
allow sshd_t esop_t:file { getattr read write open };

######

#!/usr/bin/env bash

sudo checkmodule -M -m -o ./esop_type.mod ./esop_type.te
sudo semodule_package -o esop_type.pp -m esop_type.mod
sudo semodule -i esop_type.pp  ## this can update existing, so no need to delete existing one if update

sudo semanage fcontext -a -t esop_t "/home/esop(/.*)?"
sudo semanage fcontext -l | grep esop
sudo restorecon -Rv /home/esop

```

### common

```shell
getenforce
setenforce 0
setenforce 1
```

## config file
one can only modify config and reboot to enable the change for selinux

```shell
vim /etc/selinux
sestatus
setenforce
getenforce
```

```shell
SELINUX=permissive
# targeted - targeted processes are protected
# minium - Modificatio of targeted, only selected processes are protected.
# mls - Multi level security protection (too high level, military maybe need this)
SELINUXTYPE=targeted
```

```shell

ls -lZ   ## Z will show you the linux label of the file
# like:
# unconfined_u:object_r:httpd_sys_content_t:s0
# user:role:target:security(for mls)

# -t for change type
chcon -t httpd_sys_content_t move.html
# will move file move.html to httpd_sys_content_t type

restorecon move.html
restorecon -R * # recursive
# auto restore file to correct context type

# -a means add
semanage fcontext -a -t httpd_sys_content_t "/web(/.*)?"
# will set default type under the /web folder to a spec type

semanage boolean --list
semanage boolean --modify --on httpd_enable_homedirs
# will change current and default boolean to on
# if you want to only change for current session (not default)
setsebool httpd_enable_homedirs 1
# use -P also set the default value
setsebool -P httpd_enable_homedirs 1
```
```shell
/etc/selinux/targeted/contexts/file_content.*
# is where selinux file labels definitions are stored

# file labels are usually stored in the inode Xattrs
matchpathcon /path # tells you what the label should be

```

```shell

## create and install 
## vim :

module msa_apigw_type 1.0;

require {
    type systemd_systemctl_exec_t;
    type init_t;
    attribute file_type;
    class dir { write read getattr };
    class file { open write read getattr execute execute_no_trans };
    class lnk_file { read };
}

type msa_apigw_t;
typeattribute msa_apigw_t file_type;
allow init_t msa_apigw_t:file { getattr open read execute execute_no_trans };
allow init_t msa_apigw_t:lnk_file { read };



#### if you don't know how to create te file, use 
sudo audit2allow -w -a
sudo audit2allow -a -M mysample
## to generate a sample file

###############
###############
sudo rm ./*.pp ./*.mod
sudo checkmodule -M -m -o ./msa_apigw_type.mod ./msa_apigw_type.te
sudo semodule_package -o msa_apigw_type.pp -m msa_apigw_type.mod
sudo semodule -i msa_apigw_type.pp  ## this can update existing, so no need to delete existing one if update

#### if want to remove 
sudo semodule -l | grep msa_apigw_type
sudo semodule -r msa_apigw_type


###### setup context type
sudo semanage fcontext -a -t msa_apigw_t "/home/esop/apigw(/.*)?"
sudo semanage fcontext -l | grep apigw
sudo restorecon -Rv /home/esop/apigw

####### want to remove fcontext
sudo semanage fcontext -d "/home/esop/apigw(/.*)?" 
sudo restorecon -Rv /home/esop/apigw

```

