# linux

## user

```shell
sudo useradd -m -d /home/username -s /usr/sbin/nologin username
```

## rpm

```shell
sudo rpm -ivh package.rpm
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
audit2allow -a -M mycertwatch
```
disown

systemctl restart consul
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

## Oauth

https://spring.io/guides/tutorials/spring-boot-oauth2

## IODC 
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