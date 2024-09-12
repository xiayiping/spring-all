<div style="font-size: 18px;">

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
```

## sudo-er add
```shell

sudo visudo

```

## selinux

### common

```shell
getenforce
setenforce 0
setenforce 1
```

### systemctl allow
https://access.redhat.com/documentation/en-us/red_hat_enterprise_linux/6/html/security-enhanced_linux/sect-security-enhanced_linux-fixing_problems-allowing_access_audit2allow

```shell
audit2allow -w -a
audit2allow -a -M mySample
ausearch -m avc -ts recent | audit2allow -M mySample  # this one is better than audit2allow
```
disown

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

######

#!/usr/bin/env bash

sudo checkmodule -M -m -o ./esop_type.mod ./esop_type.te
sudo semodule_package -o esop_type.pp -m esop_type.mod
sudo semodule -i esop_type.pp  ## this can update existing, so no need to delete existing one if update

sudo semanage fcontext -a -t esop_t "/home/esop(/.*)?"
sudo semanage fcontext -l | grep esop
sudo restorecon -Rv /home/esop

```


# db

## SqlServer

### Show active connections

```sql
SELECT
    s.session_id,
    s.login_name,
    s.host_name,
    s.program_name,
    r.status,
    r.command,
    r.database_id,
    r.start_time,
    r.cpu_time,
    r.total_elapsed_time
FROM
    sys.dm_exec_sessions s
LEFT JOIN
    sys.dm_exec_requests r
ON
    s.session_id = r.session_id
WHERE
    s.is_user_process = 1; 


SELECT
    spid,
    kpid,
    blocked,
    dbid,
    loginame,
    hostname,
    program_name,
    status,
    cmd,
    cpu,
    physical_io,
    memusage,
    login_time,
    last_batch,
    dbid,
    open_tran
FROM
    sys.sysprocesses
WHERE
    dbid = DB_ID('your_database_name');


SELECT
    c.session_id,
    c.connect_time,
    c.client_net_address,
    s.login_name,
    s.host_name,
    s.program_name,
    s.status,
    s.database_id
FROM
    sys.dm_exec_connections c
JOIN
    sys.dm_exec_sessions s
ON
    c.session_id = s.session_id
WHERE
    s.database_id = DB_ID('your_database_name');
```

# Spring Boot

## Data

- for data jdbc, for refer to other entity , using AggregateReference<ServletSecurityConfig,Long> ss;

- for jpa, if you need publish event on persist, extend AbstractAggregateRoot rr;


## Oauth

https://spring.io/guides/tutorials/spring-boot-oauth2

## OIDC 
An OIDC client (OpenID Connect client) is a software application or service that interacts with an OpenID Connect (OIDC) 
provider to authenticate users and obtain user identity information. 
OpenID Connect (OIDC) is an identity layer built on top of the OAuth 2.0 protocol, 
providing a standardized way to authenticate users and obtain identity tokens.

### Key Components and Flow of an OIDC Client
1. Authorization Request:
   - The OIDC client initiates the authentication process by redirecting the user to the OpenID provider's authorization endpoint. This request includes parameters such as:
     - client_id: The unique identifier of the client application.
     - redirect_uri: The URI where the authorization server will send the user after authorization.
     - response_type: Typically set to code for the Authorization Code Flow.
     - scope: Specifies the level of access requested (e.g., openid, profile, email).
     - state: A value used to maintain state between the request and callback to prevent CSRF attacks.
2. User Authentication and Consent:
   - The user authenticates with the OpenID provider (if not already authenticated) and may be prompted to consent to the requested scopes.
3. Authorization Response:
   - After successful authentication and consent, the OpenID provider redirects the user back to the OIDC client with an authorization code and the state parameter (if provided).
4. Token Exchange:
   - The OIDC client exchanges the authorization code for tokens (ID token, access token, and optionally a refresh token) by making a request to the OpenID provider's token
5. Token Validation:
   - The OIDC client validates the ID token to ensure its authenticity and integrity. This involves verifying the 
     token’s signature and checking claims such as:
     - iss (issuer): The issuer of the token.
     - aud (audience): The audience for which the token is intended (should match the client_id).
     - exp (expiration): The expiration time of the token.
6. User Information:
   - The OIDC client can use the access token to request additional user information from the OpenID provider's userinfo endpoint.

SpringOpaqueTokenIntrospector is used for obtain opa


# RACI

A RACI matrix is a project management tool used to clarify roles and responsibilities in a project or process. RACI stands for:

- **R**esponsible
- **A**ccountable
- **C**onsulted
- **I**nformed

Each letter represents a level of responsibility and involvement for team members in specific tasks or deliverables. Here's a breakdown of each role:

1. **Responsible**: The person(s) who actually complete the task. They are responsible for doing the work or executing the task.
2. **Accountable**: The person who is ultimately answerable for the correct and thorough completion of the task. This person delegates the work and reviews it. Only one person can be accountable for each task.
3. **Consulted**: The people who need to give input before the work can be done and signed off. These are usually subject matter experts and there is two-way communication.
4. **Informed**: The people who need to be kept up-to-date on progress and decisions. This is usually one-way communication.

### Creating a RACI Matrix

To create a RACI matrix, follow these steps:

1. **List all tasks and deliverables**: Write down all the tasks and deliverables for the project.
2. **Identify roles**: List all the roles involved in the project across the top of the matrix.
3. **Assign RACI codes**: For each task, assign one or more of the RACI roles to the roles involved.

### Example of a RACI Matrix

Here is an example of a RACI matrix for a project:

| Task/Deliverable      | Project Manager | Team Lead | Developer | Quality Assurance | Stakeholder |
|-----------------------|-----------------|-----------|-----------|-------------------|-------------|
| Requirement Gathering | A               | R         | C         | I                 | I           |
| Design                | C               | A         | R         | C                 | I           |
| Implementation        | I               | C         | A, R      | I                 | I           |
| Testing               | I               | C         | R         | A                 | I           |
| Deployment            | A               | R         | C         | I                 | I           |

### Benefits of a RACI Matrix

- **Clarity**: It provides clear definitions of responsibilities and ensures everyone knows their role.
- **Avoids Overlap**: Helps to prevent overlap and ensures that tasks are not neglected.
- **Improves Communication**: Ensures that the right people are consulted and informed, improving communication and reducing misunderstandings.
- **Accountability**: Clearly defines who is accountable for each task, which helps in tracking progress and performance.

### Conclusion

The RACI matrix is a simple yet powerful tool for clarifying roles and responsibilities in a project or process. By explicitly defining who is responsible, accountable, consulted, and informed, it helps to streamline project management, improve communication, and ensure successful project execution.


</div>