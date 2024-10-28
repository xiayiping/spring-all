

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