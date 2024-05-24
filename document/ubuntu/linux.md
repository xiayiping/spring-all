## /etc/passwd
```shell
cat /etc/passwd
username:x:UID:GID:full_name:home_directory:shell
```

Here's what each field represents:

username: The user's login name.
x: A placeholder for the password (actual passwords are stored in /etc/shadow).
UID: The user's unique ID number.
GID: The user's primary group ID number.
full_name: The user's full name or other information.
home_directory: The path to the user's home directory.
shell: The user's default shell (e.g., /bin/bash).


## check user group
```shell
groups username
## or 
id user
```


```shell
# Update package list and install prerequisites
sudo apt update
sudo apt install -y curl gnupg2

# Install Node.js (using NodeSource)
curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -
sudo apt install -y nodejs

# Verify installation
node -v
npm -v

# Install Nginx
sudo apt install -y nginx

# Verify installation
nginx -v
```

## Create a Systemd Service File
Create a systemd service file for your Node.js application. This file tells systemd how to manage your application.

bash
Copy
sudo nano /etc/systemd/system/my-node-app.service
In the file, add the following configuration:

```ini
[Unit]
Description=My Node.js Application
Documentation=https://example.com
After=network.target

[Service]
Environment=NODE_PORT=3000
Type=simple
User=nobody
Group=nogroup
WorkingDirectory=/path/to/your/app
ExecStart=/usr/bin/node /path/to/your/app/app.js
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

### systemctl Common Targets
Here are a few other common targets in systemd:

- graphical.target: Extends multi-user.target by starting a graphical user interface (GUI). It corresponds to runlevel 5 in SysV init systems.
- basic.target: A minimal target that starts essential services required for running basic system functions.
- default.target: The default target that the system boots into, which is typically a symlink to either multi-user.target or graphical.target.
- rescue.target: A target for single-user mode, used for system rescue and maintenance. It corresponds to runlevel 1.
- shutdown.target: A target that shuts down the system.
- reboot.target: A target that reboots the system.

# TMUX

### sync panels
```shell
:setw synchronize-panes on
:setw synchronize-panes off
```


# IPV6
## Key Differences Between IPv4 and IPv6 Headers
### Address Length:
- IPv4: 32-bit addresses.
- IPv6: 128-bit addresses.
### Header Length:
- IPv4: Variable length (20-60 bytes).
- IPv6: Fixed length (40 bytes).
### Header Complexity:
- IPv4: More complex with options field.
- IPv6: Simplified header with fixed length.
### Fragmentation:
- IPv4: Routers can perform fragmentation.
- IPv6: Fragmentation is handled by the sending host, not routers.
### Checksum:
- IPv4: Includes a header checksum.
- IPv6: No header checksum (relies on higher-layer protocols like TCP/UDP for error checking).
### Options/Extensions:
- IPv4: Options field within the header.
- IPv6: Uses extension headers which are separate from the main header.
### Field Replacements:
- The IHL field in IPv4 is not needed in IPv6 due to the fixed header size.
- The Type of Service field in IPv4 is replaced by the Traffic Class field in IPv6.
- The Identification, Flags, and Fragment Offset fields in IPv4 are replaced by the Next Header field and extension headers in IPv6.


