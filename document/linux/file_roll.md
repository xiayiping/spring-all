# File Roll

If you want to **roll logs based on both file size and date**, you can configure `logrotate` to use **both triggers**. This means the log file will rotate either:
1. When it exceeds a specified size (e.g., `10M`), or
2. At a specific time interval (e.g., `daily`, `weekly`).

Here’s how to set it up:

---

### 1. **Create or Edit a Logrotate Configuration**
Open or create a new logrotate configuration file in `/etc/logrotate.d/`. For example:

```bash
sudo nano /etc/logrotate.d/custom
```

Add the following configuration:

```plaintext
/path/to/custom/folder/custom.log {
    daily                 # Rotate based on the date (daily in this case)
    size 10M              # Rotate if the file exceeds 10MB
    rotate 7              # Keep the last 7 rotated logs
    compress              # Compress old logs (e.g., to .gz files)
    delaycompress         # Delay compression by one cycle
    missingok             # Ignore errors if the log file is missing
    notifempty            # Do not rotate if the log file is empty
    create 0644 root root # Create a new log file with specified permissions
    dateext               # Append the date to rotated logs
    dateformat -%Y-%m-%d  # Use a custom date format (e.g., YYYY-MM-DD)
}
```

---

### 2. **Key Configuration Options Explained**
- **`daily`**: Rotates the log files every day, regardless of size.
- **`size 10M`**: Rotates the log when it exceeds 10MB, even if it's not the scheduled rotation time.
- **`rotate 7`**: Keeps the last 7 rotated logs and deletes older ones.
- **`compress`**: Compresses old log files (e.g., `custom.log-2025-11-25.gz`).
- **`delaycompress`**: Delays compression of the most recently rotated log until the next rotation cycle.
- **`missingok`**: Prevents errors if the log file is missing.
- **`notifempty`**: Skips rotation if the log file is empty.
- **`create 0644 root root`**: Creates a new log file with the specified permissions and ownership after rotation.
- **`dateext`**: Appends the current date to the rotated log file (e.g., `custom.log-2025-11-26`).

---

### 3. **How It Works**
With this configuration:
- If the log file exceeds `10M`, it will rotate immediately, even if it's not the daily rotation time.
- If the log file does **not** exceed `10M`, it will still rotate at the next scheduled daily rotation.
- This ensures that the log file is managed by both **size** and **time**.

---

### 4. **Test the Configuration**
To test the configuration, run:

```bash
sudo logrotate -d /etc/logrotate.d/custom
```

The `-d` option performs a dry-run, simulating the rotation without making any changes. If no errors are reported, your configuration is correct.

---

### 5. **Force Rotation for Testing**
To force actual rotation and verify the results:

```bash
sudo logrotate -f /etc/logrotate.d/custom
```

---

### 6. **Automate with Cron**
Logrotate is typically triggered daily by a system cron job. To ensure this is happening, check the system's cron configuration for `logrotate`:

```bash
cat /etc/cron.daily/logrotate
```

If needed, you can manually add a cron job to run logrotate at a specific interval. For example:

```bash
sudo crontab -e
```

Add the following line to run logrotate daily at midnight:

```bash
0 0 * * * /usr/sbin/logrotate /etc/logrotate.conf
```

---

### Example Output
After rotation, your log directory (`/path/to/custom/folder/`) will contain files like:

```plaintext
custom.log
custom.log-2025-11-24.gz
custom.log-2025-11-25.gz
custom.log-2025-11-26.gz
```

If the log exceeds the size before the next scheduled rotation, you might see:

```plaintext
custom.log
custom.log-2025-11-26.gz
custom.log-2025-11-26-1.gz   # Rotated mid-day due to size
```

---

### 7. **Advanced: Customize Date and Size Rotation Further**
You can fine-tune further depending on your needs:
- **Hourly rotation**: Replace `daily` with `hourly` if you want hourly rotation.
- **Larger size thresholds**: Adjust `size` to values like `50M` or `1G`.
- **Custom retention**: Change `rotate` to keep more or fewer old logs.

---

### Summary
To rotate logs based on both size and date:
1. Use the `size` directive to specify the size limit.
2. Use the `daily`, `weekly`, or `monthly` directive for time-based rotation.
3. Combine these in your logrotate configuration for dual triggers.
4. Test and automate the process with cron.

This ensures logs are rotated efficiently and managed for both growth and time.


# Restart Nginx

Yes, **NGINX will automatically write to the new log file** after log rotation, but only if the new log file is properly created and opened. However, if you're using tools like `logrotate` to rotate the logs, NGINX may still hold a file descriptor to the old log file, and it won't start writing to the new file until you **signal NGINX to reopen its log files**.

To ensure smooth log rotation with NGINX, follow these steps:

---

### 1. **Configure Logrotate for NGINX Logs**
Here’s a sample `logrotate` configuration for NGINX logs (e.g., `/var/log/nginx/access.log` and `/var/log/nginx/error.log`):

```plaintext
/var/log/nginx/*.log {
    daily                     # Rotate logs daily
    size 50M                  # Rotate if the file exceeds 50MB
    rotate 7                  # Keep the last 7 rotated logs
    compress                  # Compress old log files
    delaycompress             # Delay compression until the next rotation
    missingok                 # Ignore errors if the log file is missing
    notifempty                # Do not rotate if the log file is empty
    create 0644 nginx nginx   # Create a new log file with specific permissions
    sharedscripts             # Run post-rotation scripts only once

    postrotate
        [ -s /run/nginx.pid ] && kill -USR1 `cat /run/nginx.pid`
    endscript
}
```

---

### 2. **Key Points in the Configuration**
- **`postrotate` and `endscript`**:
    - The `postrotate` block is critical. It ensures NGINX reopens the log files after rotation.
    - The command `kill -USR1 $(cat /run/nginx.pid)` sends the `SIGUSR1` signal to the NGINX master process, instructing it to close and reopen its log files.
    - The PID file (`/run/nginx.pid`) is where NGINX stores the process ID of its master process. Adjust the path if your PID file is located elsewhere (e.g., `/var/run/nginx.pid`).

- **`create 0644 nginx nginx`**:
    - Ensures a new log file is created with the correct permissions (`0644`) and ownership (`nginx` user and group, or the user running NGINX).

- **`sharedscripts`**:
    - Ensures the `postrotate` script runs only once, even if multiple logs are being rotated.

---

### 3. **How This Works**
1. **Logrotate triggers rotation**:
    - It renames the current log file (e.g., `access.log` → `access.log-YYYY-MM-DD`).
    - It creates a new log file (`access.log`) with the specified permissions.

2. **Post-rotation signal (`SIGUSR1`)**:
    - The `kill -USR1` command tells NGINX to reopen its log files.
    - NGINX closes its file descriptors for the old logs and begins writing to the newly created files.

3. **Compressed logs**:
    - Older logs are compressed (e.g., `access.log-YYYY-MM-DD.gz`) if `compress` is enabled.

---

### 4. **Testing Log Rotation with NGINX**
To test the configuration:

1. **Trigger a manual log rotation**:
   ```bash
   sudo logrotate -f /etc/logrotate.d/nginx
   ```

2. **Verify NGINX is writing to the new log file**:
   Check the new log file (`access.log`) for recent entries:

   ```bash
   tail -f /var/log/nginx/access.log
   ```

3. **Check for errors**:
   If NGINX doesn’t reopen the logs, check the NGINX error logs (`/var/log/nginx/error.log`) and ensure the `SIGUSR1` signal is being sent to the correct process.

---

### 5. **Common Issues**
1. **NGINX still writing to the old log file**:
    - This happens if the `SIGUSR1` signal is not sent after log rotation.
    - Check the `postrotate` script in the logrotate configuration and ensure the correct PID file is used.

2. **Log file permissions issue**:
    - NGINX requires write permissions for the new log file. Ensure the `create` directive in the logrotate config specifies the correct user and group.

3. **Wrong PID file path**:
    - If the `postrotate` script references the wrong PID file, NGINX won’t receive the `SIGUSR1` signal. Verify the location of the PID file:
      ```bash
      sudo nginx -T | grep pid
      ```

---

### 6. **Alternative: Using NGINX's Built-in Log Rotation**
If you prefer, you can avoid relying on `logrotate` entirely and use NGINX's built-in log rotation mechanism. However, this is manual:
1. Rename the current log file:
   ```bash
   mv /var/log/nginx/access.log /var/log/nginx/access.log.old
   ```

2. Signal NGINX to reopen logs:
   ```bash
   sudo kill -USR1 $(cat /run/nginx.pid)
   ```

3. NGINX will create a new `access.log` and start writing to it.

---

### Summary
- **Yes**, NGINX can write to a new log file after rotation, but you must signal it to reopen its logs using `kill -USR1`.
- Configure `logrotate` with a `postrotate` script to handle this automatically.
- Test the setup to ensure there are no issues with permissions or the PID file location.