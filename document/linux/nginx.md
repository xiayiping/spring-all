# multiple servers

To serve two servers with different domain names using Nginx on port 443 (HTTPS), you need to configure **server blocks** (virtual hosts) in your Nginx configuration. Each server block will handle a specific domain name. Here's how to do it:

---

### **Steps to Configure Nginx for Two Domains**

#### **1. Prerequisites**
- Ensure that both domain names (e.g., `example1.com` and `example2.com`) point to your server's IP address in DNS.
- Obtain SSL certificates for both domain names. You can use **Let's Encrypt** or any other SSL certificate provider.

---

#### **2. Generate SSL Certificates**
If you use Let's Encrypt, you can generate free SSL certificates for both domains using **Certbot**. Run the following commands for each domain:

```bash
sudo certbot certonly --standalone -d example1.com -d www.example1.com
sudo certbot certonly --standalone -d example2.com -d www.example2.com
```

This will generate certificates in `/etc/letsencrypt/live/<domain>`.

---

#### **3. Nginx Configuration**
Edit your Nginx configuration file to create two server blocks, one for each domain.

1. Open the Nginx configuration file:
   ```bash
   sudo nano /etc/nginx/sites-available/example1
   ```

2. Add the configuration for the first domain (`example1.com`):

   ```nginx
   server {
       listen 443 ssl;
       server_name example1.com www.example1.com;

       ssl_certificate /etc/letsencrypt/live/example1.com/fullchain.pem;
       ssl_certificate_key /etc/letsencrypt/live/example1.com/privkey.pem;

       root /var/www/example1;
       index index.html;

       location / {
           try_files $uri $uri/ =404;
       }
   }
   ```

3. Save and exit the file, then create another configuration file for the second domain:

   ```bash
   sudo nano /etc/nginx/sites-available/example2
   ```

4. Add the configuration for the second domain (`example2.com`):

   ```nginx
   server {
       listen 443 ssl;
       server_name example2.com www.example2.com;

       ssl_certificate /etc/letsencrypt/live/example2.com/fullchain.pem;
       ssl_certificate_key /etc/letsencrypt/live/example2.com/privkey.pem;

       root /var/www/example2;
       index index.html;

       location / {
           try_files $uri $uri/ =404;
       }
   }
   ```

5. For both domains, if you want to proxy requests to other backend servers (instead of serving static files), replace the `location` block with this:

   ```nginx
   location / {
       proxy_pass http://backend_server_ip:port;
       proxy_set_header Host $host;
       proxy_set_header X-Real-IP $remote_addr;
       proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
   }
   ```

---

#### **4. Enable the Configuration**
1. Create symbolic links in the `sites-enabled` directory:

   ```bash
   sudo ln -s /etc/nginx/sites-available/example1 /etc/nginx/sites-enabled/
   sudo ln -s /etc/nginx/sites-available/example2 /etc/nginx/sites-enabled/
   ```

2. Test the Nginx configuration for syntax errors:

   ```bash
   sudo nginx -t
   ```

3. Reload Nginx to apply the changes:

   ```bash
   sudo systemctl reload nginx
   ```

---

#### **5. Redirect HTTP to HTTPS (Optional)**
If you want to redirect HTTP traffic to HTTPS for both domains, create additional server blocks for port 80 in the respective configuration files:

```nginx
server {
    listen 80;
    server_name example1.com www.example1.com;

    return 301 https://$host$request_uri;
}
```

Add similar rules for `example2.com`.

---

### **Final Directory Structure**
Make sure your directory structure looks like this:

- `/var/www/example1` -> Content for `example1.com`
- `/var/www/example2` -> Content for `example2.com`

Place your website files (e.g., `index.html`) in the respective directories.

---

### **6. Verify the Setup**
- Use a browser to visit `https://example1.com` and `https://example2.com` to confirm that each domain serves the correct content.
- Test the SSL certificates using tools like [SSL Labs](https://www.ssllabs.com/ssltest/).

---

### **Troubleshooting Tips**
- Check Nginx logs for errors:
  ```bash
  sudo tail -f /var/log/nginx/error.log
  ```
- Ensure that port 443 is open in your firewall:
  ```bash
  sudo ufw allow 443
  ```

This configuration will make Nginx serve two different servers on port 443 with different domain names.


# One File

Yes, you can absolutely put multiple `server` blocks in a single Nginx configuration file. This is a common practice for simplicity when managing configurations for multiple domains or servers.

Here’s how you can configure two `server` blocks for different domains (`example1.com` and `example2.com`) in **one file**:

---

### **Single File with Two Server Blocks**

1. Open a single Nginx configuration file (e.g., `/etc/nginx/sites-available/multiple-domains`):

   ```bash
   sudo nano /etc/nginx/sites-available/multiple-domains
   ```

2. Add the following content, which includes two `server` blocks:

   ```nginx
   # Server block for example1.com
   server {
       listen 443 ssl;
       server_name example1.com www.example1.com;

       ssl_certificate /etc/letsencrypt/live/example1.com/fullchain.pem;
       ssl_certificate_key /etc/letsencrypt/live/example1.com/privkey.pem;

       root /var/www/example1;
       index index.html;

       location / {
           try_files $uri $uri/ =404;
       }
   }

   # Server block for example2.com
   server {
       listen 443 ssl;
       server_name example2.com www.example2.com;

       ssl_certificate /etc/letsencrypt/live/example2.com/fullchain.pem;
       ssl_certificate_key /etc/letsencrypt/live/example2.com/privkey.pem;

       root /var/www/example2;
       index index.html;

       location / {
           try_files $uri $uri/ =404;
       }
   }

   # Redirect HTTP to HTTPS for both domains
   server {
       listen 80;
       server_name example1.com www.example1.com;
       return 301 https://$host$request_uri;
   }

   server {
       listen 80;
       server_name example2.com www.example2.com;
       return 301 https://$host$request_uri;
   }
   ```

---

### **Steps After Configuring**

1. **Enable the Configuration:**
   Create a symbolic link to the `sites-enabled` directory:

   ```bash
   sudo ln -s /etc/nginx/sites-available/multiple-domains /etc/nginx/sites-enabled/
   ```

2. **Test Configuration:**
   Check the syntax of your Nginx configuration file:

   ```bash
   sudo nginx -t
   ```

3. **Reload Nginx:**
   Reload Nginx to apply the changes:

   ```bash
   sudo systemctl reload nginx
   ```

---

### **Advantages of Using One File**
- **Simplified Management:** Fewer configuration files to manage.
- **Easier Debugging:** All related server blocks are in one place.
- **Quick Edits:** Changes to multiple domains can be made in one file.

---

### **Drawbacks of Using One File**
- **Reduced Modularity:** If you have many domains or server blocks, a single file can become large and harder to maintain.
- **Potential for Errors:** If there’s an error in the file, it could affect all server blocks.

---

### **Best Practice**
For a small number of domains (e.g., 2–3), putting multiple `server` blocks in one file is fine. However, for larger setups or to follow a modular approach, use separate files for each domain in the `/etc/nginx/sites-available/` directory.

Both approaches work, so feel free to choose the one that best fits your workflow!