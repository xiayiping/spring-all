# Basic

## Nginx Proxy Configuration to Pass Host to Spring Boot

### **Basic Nginx Configuration**

```nginx
server {
    listen 80;
    server_name example.com www.example.com;
    
    location / {
        proxy_pass http://localhost:8080;
        
        # Pass original host
        proxy_set_header Host $host;
        
        # Pass original protocol (http/https)
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Pass original client IP
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
        # Pass original request URI
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
        
        # Pass real IP
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### **HTTPS Configuration**

```nginx
server {
    listen 443 ssl;
    server_name example.com www.example.com;
    
    ssl_certificate /etc/nginx/ssl/certificate.crt;
    ssl_certificate_key /etc/nginx/ssl/private.key;
    
    location / {
        proxy_pass http://localhost:8080;
        
        # Essential headers
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;  # Will be "https"
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
        
        # WebSocket support (if needed)
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}

# Redirect HTTP to HTTPS
server {
    listen 80;
    server_name example.com www.example.com;
    return 301 https://$host$request_uri;
}
```

### **Complete Production Configuration**

```nginx
upstream springboot_backend {
    # Load balancing multiple Spring Boot instances
    server localhost:8080;
    server localhost:8081;
    server localhost:8082;
    
    # Optional: session persistence
    ip_hash;
}

server {
    listen 80;
    server_name example.com www.example.com;
    
    # Redirect to HTTPS
    return 301 https://$host$request_uri;
}

server {
    listen 443 ssl http2;
    server_name example.com www.example.com;
    
    # SSL Configuration
    ssl_certificate /etc/nginx/ssl/certificate.crt;
    ssl_certificate_key /etc/nginx/ssl/private.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    
    # Logging
    access_log /var/log/nginx/example.com.access.log;
    error_log /var/log/nginx/example.com.error.log;
    
    # Max upload size
    client_max_body_size 50M;
    
    location / {
        proxy_pass http://springboot_backend;
        
        # Host headers
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Port $server_port;
        
        # Preserve original request info
        proxy_set_header X-Original-URI $request_uri;
        proxy_set_header X-Original-Method $request_method;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
        
        # Buffer settings
        proxy_buffering on;
        proxy_buffer_size 4k;
        proxy_buffers 8 4k;
        proxy_busy_buffers_size 8k;
        
        # WebSocket support
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection $connection_upgrade;
    }
    
    # Static files (if served by nginx)
    location ~* \.(jpg|jpeg|png|gif|ico|css|js|svg|woff|woff2|ttf)$ {
        proxy_pass http://springboot_backend;
        expires 30d;
        add_header Cache-Control "public, immutable";
    }
    
    # Health check endpoint (bypass proxy overhead)
    location /actuator/health {
        proxy_pass http://springboot_backend;
        access_log off;
    }
}

# WebSocket upgrade mapping
map $http_upgrade $connection_upgrade {
    default upgrade;
    '' close;
}
```

### **Spring Boot Configuration to Read Headers**

#### **application.yml (WebFlux):**

```yaml
server:
  port: 8080
  forward-headers-strategy: framework  # Important!

spring:
  webflux:
    # Trust all proxies (use with caution in production)
    base-path: /
```

#### **application.yml (Servlet/MVC):**

```yaml
server:
  port: 8080
  forward-headers-strategy: framework  # or "native"
  
  # For Tomcat
  tomcat:
    remote-ip-header: X-Forwarded-For
    protocol-header: X-Forwarded-Proto
    port-header: X-Forwarded-Port
    
  # Trust proxy
  use-forward-headers: true
```

#### **Java Configuration (Servlet):**

```java
@Configuration
public class WebConfig {
    
    @Bean
    public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
        FilterRegistrationBean<ForwardedHeaderFilter> bean = new FilterRegistrationBean<>();
        bean.setFilter(new ForwardedHeaderFilter());
        return bean;
    }
}
```

#### **Java Configuration (WebFlux):**

```java
@Configuration
public class WebFluxConfig {
    
    @Bean
    public ForwardedHeaderTransformer forwardedHeaderTransformer() {
        return new ForwardedHeaderTransformer();
    }
}
```

### **Spring Boot Controller to Access Host**

#### **WebFlux:**

```java
@RestController
public class HostController {
    
    @GetMapping("/host-info")
    public Mono<Map<String, String>> getHostInfo(ServerHttpRequest request) {
        Map<String, String> info = new HashMap<>();
        
        // These will now contain the original values from nginx
        info.put("host", request.getHeaders().getFirst("Host"));
        info.put("x-forwarded-host", request.getHeaders().getFirst("X-Forwarded-Host"));
        info.put("x-forwarded-proto", request.getHeaders().getFirst("X-Forwarded-Proto"));
        info.put("x-forwarded-port", request.getHeaders().getFirst("X-Forwarded-Port"));
        info.put("x-real-ip", request.getHeaders().getFirst("X-Real-IP"));
        
        // URI will be correct with forward-headers-strategy: framework
        info.put("uri-host", request.getURI().getHost());
        info.put("uri-scheme", request.getURI().getScheme());
        
        return Mono.just(info);
    }
}
```

#### **Servlet:**

```java
@RestController
public class HostController {
    
    @GetMapping("/host-info")
    public Map<String, String> getHostInfo(HttpServletRequest request) {
        Map<String, String> info = new HashMap<>();
        
        // With ForwardedHeaderFilter, these return original values
        info.put("serverName", request.getServerName());
        info.put("scheme", request.getScheme());
        info.put("serverPort", String.valueOf(request.getServerPort()));
        info.put("requestURL", request.getRequestURL().toString());
        
        // Original headers from nginx
        info.put("host-header", request.getHeader("Host"));
        info.put("x-forwarded-host", request.getHeader("X-Forwarded-Host"));
        info.put("x-forwarded-proto", request.getHeader("X-Forwarded-Proto"));
        info.put("x-real-ip", request.getHeader("X-Real-IP"));
        
        return info;
    }
}
```

### **Testing the Configuration**

```bash
# Test from outside
curl -v https://example.com/host-info

# Check nginx is passing headers
curl -H "Host: example.com" http://localhost:8080/host-info

# Test with custom headers
curl -H "X-Forwarded-Host: custom.com" \
     -H "X-Forwarded-Proto: https" \
     http://localhost:8080/host-info
```

### **Common Issues and Solutions**

#### **Issue 1: Spring Boot still shows localhost**

```yaml
# Add this to application.yml
server:
  forward-headers-strategy: framework  # Must be set!
```

#### **Issue 2: HTTPS redirects to HTTP**

```nginx
# Ensure this is set in nginx
proxy_set_header X-Forwarded-Proto $scheme;
```

```yaml
# And in Spring Boot
server:
  tomcat:
    protocol-header: X-Forwarded-Proto
    protocol-header-https-value: https
```

#### **Issue 3: Port number incorrect**

```nginx
# Add this to nginx
proxy_set_header X-Forwarded-Port $server_port;
```

```yaml
# And in Spring Boot
server:
  tomcat:
    port-header: X-Forwarded-Port
```

### **Nginx Variables Reference**

| Variable | Example Value | Description |
|----------|---------------|-------------|
| `$host` | `example.com` | Host header from client |
| `$scheme` | `https` | Protocol (http/https) |
| `$server_port` | `443` | Nginx listening port |
| `$remote_addr` | `192.168.1.100` | Client IP address |
| `$proxy_add_x_forwarded_for` | `192.168.1.100, 10.0.0.1` | Chain of IPs |
| `$request_uri` | `/api/users?id=1` | Full request URI |
| `$http_upgrade` | `websocket` | Upgrade header value |

### **Security Considerations**

```nginx
# Only trust specific IPs (production)
set_real_ip_from 10.0.0.0/8;     # Internal network
set_real_ip_from 172.16.0.0/12;  # Docker network
real_ip_header X-Forwarded-For;
real_ip_recursive on;
```

```yaml
# Spring Boot - restrict trusted proxies
server:
  tomcat:
    remoteip:
      internal-proxies: 10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}
```

With `forward-headers-strategy: framework` in Spring Boot and proper `proxy_set_header` directives in Nginx, your Spring Boot application will correctly receive and use the original host, protocol, and port information.

# Origin IP and Real IP

These comments are referring to **different scenarios** for passing client information through proxies. Let me explain the difference:

## **Original Client IP vs Real IP**

### **Original Client IP (X-Forwarded-For)**

This is the **first/original client** that made the request, tracked through multiple proxies.

```
Client -> Proxy1 -> Proxy2 -> Your Server

X-Forwarded-For: 203.0.113.1, 198.51.100.2, 192.0.2.3
                  ^            ^              ^
                  |            |              |
            Original Client  Proxy1         Proxy2
```

**nginx configuration:**
```nginx
location / {
    # Pass original client IP (preserves the chain)
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    
    proxy_pass http://backend;
}
```

`$proxy_add_x_forwarded_for` appends the current remote address to any existing `X-Forwarded-For` header, preserving the **full chain** of IPs.

### **Real IP (X-Real-IP)**

This is the **immediate/direct client** that connected to this specific proxy (could be another proxy, not the original client).

```
Client -> Proxy1 -> Your nginx -> Your Server

X-Real-IP: 198.51.100.2  (IP of Proxy1, not the original client)
```

**nginx configuration:**
```nginx
location / {
    # Pass real IP (immediate client only)
    proxy_set_header X-Real-IP $remote_addr;
    
    proxy_pass http://backend;
}
```

`$remote_addr` is the IP address of the **direct connection** to nginx.

## **Complete Example**

### **Scenario: Client -> Cloudflare -> Your nginx -> Spring Boot**

```nginx
server {
    listen 80;
    server_name example.com;
    
    # Trust Cloudflare IPs
    set_real_ip_from 103.21.244.0/22;
    set_real_ip_from 103.22.200.0/22;
    # ... other Cloudflare IP ranges
    
    # Use CF-Connecting-IP as the real client IP
    real_ip_header CF-Connecting-IP;
    
    location / {
        # Pass original client IP (full chain)
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
        # Pass real IP (after processing trusted proxies)
        proxy_set_header X-Real-IP $remote_addr;
        
        # Pass protocol
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Pass host
        proxy_set_header Host $host;
        
        proxy_pass http://localhost:8080;
    }
}
```

**What each header contains:**

```
Actual request flow:
Client (203.0.113.1) -> Cloudflare (104.16.0.1) -> nginx -> Spring Boot

Headers received by Spring Boot:
X-Forwarded-For: 203.0.113.1, 104.16.0.1
                 ^              ^
                 |              |
            Original Client   Cloudflare

X-Real-IP: 203.0.113.1  (because real_ip_header processed Cloudflare's CF-Connecting-IP)
           ^
           |
      Actual client (after nginx processed trusted proxy)
```

## **Reading in Spring Boot**

### **WebFlux:**

```java
@GetMapping("/client-info")
public Mono<Map<String, String>> getClientInfo(ServerHttpRequest request) {
    Map<String, String> info = new HashMap<>();
    
    // Original client IP (first in chain)
    String forwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
    if (forwardedFor != null) {
        String originalClient = forwardedFor.split(",")[0].trim();
        info.put("originalClientIP", originalClient);
        info.put("fullChain", forwardedFor);
    }
    
    // Real IP (immediate client after proxy processing)
    String realIP = request.getHeaders().getFirst("X-Real-IP");
    info.put("realIP", realIP);
    
    // Remote address (direct connection to your app)
    InetSocketAddress remoteAddress = request.getRemoteAddress();
    if (remoteAddress != null) {
        info.put("remoteAddress", remoteAddress.getAddress().getHostAddress());
    }
    
    return Mono.just(info);
}
```

### **Servlet:**

```java
@GetMapping("/client-info")
public Map<String, String> getClientInfo(HttpServletRequest request) {
    Map<String, String> info = new HashMap<>();
    
    // Original client IP (first in chain)
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null) {
        String originalClient = forwardedFor.split(",")[0].trim();
        info.put("originalClientIP", originalClient);
        info.put("fullChain", forwardedFor);
    }
    
    // Real IP (immediate client after proxy processing)
    String realIP = request.getHeader("X-Real-IP");
    info.put("realIP", realIP);
    
    // Remote address (direct connection)
    info.put("remoteAddress", request.getRemoteAddr());
    
    return info;
}
```

## **Common Use Cases**

### **Use Original Client IP when:**
- Rate limiting by actual user
- Geolocation
- Security logging
- Analytics
- IP-based access control

```java
public String getOriginalClientIP(HttpServletRequest request) {
    String forwardedFor = request.getHeader("X-Forwarded-For");
    if (forwardedFor != null && !forwardedFor.isEmpty()) {
        // Get first IP (original client)
        return forwardedFor.split(",")[0].trim();
    }
    return request.getRemoteAddr();
}
```

### **Use Real IP when:**
- Debugging proxy configuration
- Trusting specific proxy IPs
- Internal network routing
- Identifying immediate upstream

```java
public String getRealIP(HttpServletRequest request) {
    String realIP = request.getHeader("X-Real-IP");
    if (realIP != null && !realIP.isEmpty()) {
        return realIP;
    }
    return request.getRemoteAddr();
}
```

## **Best Practice: Get True Client IP**

```java
@Component
public class ClientIPResolver {
    
    private static final List<String> TRUSTED_HEADERS = Arrays.asList(
        "CF-Connecting-IP",      // Cloudflare
        "True-Client-IP",        // Akamai, Cloudflare Enterprise
        "X-Real-IP",             // nginx
        "X-Forwarded-For"        // Standard
    );
    
    public String getClientIP(HttpServletRequest request) {
        // Try trusted headers in order
        for (String header : TRUSTED_HEADERS) {
            String ip = request.getHeader(header);
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // For X-Forwarded-For, get first IP
                if ("X-Forwarded-For".equals(header)) {
                    return ip.split(",")[0].trim();
                }
                return ip;
            }
        }
        
        // Fallback to remote address
        return request.getRemoteAddr();
    }
}
```

## **Summary**

| Header | Contains | Use Case |
|--------|----------|----------|
| **X-Forwarded-For** | Full chain: `original_client, proxy1, proxy2` | Get **original client** (first IP) |
| **X-Real-IP** | Single IP of immediate client | Get **direct connection** after proxy processing |
| **CF-Connecting-IP** | Original client (Cloudflare-specific) | Cloudflare's version of real client |
| **Remote Address** | Direct TCP connection IP | Fallback when no headers |

**In most cases, you want the "original client IP"** (first IP in X-Forwarded-For), not the proxy's IP.


## Getting Real Domain Through Multiple Gateways

This is a **very common scenario** in enterprise architectures. Let me explain the challenge and solutions.

## **The Problem**

```
Internet Client (https://api.example.com)
    ↓
CDN/WAF (Cloudflare, Akamai)
    ↓
External Load Balancer (https://lb.company.internal)
    ↓
API Gateway (http://gateway.internal:8080)
    ↓
Internal nginx (http://nginx.cluster.local)
    ↓
Spring Boot (http://localhost:8080)
```

At each hop, the domain changes, and without proper configuration, Spring Boot will only see `localhost:8080`.

## **Header Preservation Strategy**

### **1. External Load Balancer (First Entry Point)**

```nginx
# This is your first nginx that receives public traffic
server {
    listen 443 ssl;
    server_name api.example.com;
    
    location / {
        # CRITICAL: Preserve the ORIGINAL host
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Port $server_port;
        
        # Also set standard Host header for immediate downstream
        proxy_set_header Host $host;
        
        # Chain the forwarded-for
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
        # Preserve original URI
        proxy_set_header X-Original-URI $request_uri;
        
        proxy_pass http://gateway.internal:8080;
    }
}
```

**Result after this hop:**
```
Host: api.example.com
X-Forwarded-Host: api.example.com
X-Forwarded-Proto: https
X-Forwarded-Port: 443
X-Forwarded-For: 203.0.113.1
```

### **2. API Gateway (Middle Layer)**

```nginx
# Internal API Gateway
server {
    listen 8080;
    server_name gateway.internal;
    
    location / {
        # PRESERVE existing X-Forwarded-* headers (DO NOT OVERWRITE!)
        proxy_set_header X-Forwarded-Host $http_x_forwarded_host;
        proxy_set_header X-Forwarded-Proto $http_x_forwarded_proto;
        proxy_set_header X-Forwarded-Port $http_x_forwarded_port;
        
        # Only update Host if needed for routing
        proxy_set_header Host $http_x_forwarded_host;
        
        # Continue the chain
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
        # Add gateway-specific header (optional)
        proxy_set_header X-Gateway-Host $host;
        
        proxy_pass http://nginx.cluster.local;
    }
}
```

**Result after this hop:**
```
Host: api.example.com  (preserved)
X-Forwarded-Host: api.example.com  (preserved)
X-Forwarded-Proto: https  (preserved)
X-Forwarded-Port: 443  (preserved)
X-Gateway-Host: gateway.internal  (new)
```

### **3. Internal nginx (Final Proxy)**

```nginx
# Internal nginx before Spring Boot
server {
    listen 80;
    server_name nginx.cluster.local;
    
    location / {
        # PRESERVE all forwarded headers
        proxy_set_header X-Forwarded-Host $http_x_forwarded_host;
        proxy_set_header X-Forwarded-Proto $http_x_forwarded_proto;
        proxy_set_header X-Forwarded-Port $http_x_forwarded_port;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
        # Set Host to original
        proxy_set_header Host $http_x_forwarded_host;
        
        # Add internal proxy info (optional)
        proxy_set_header X-Internal-Proxy $host;
        
        proxy_pass http://localhost:8080;
    }
}
```

**Final headers received by Spring Boot:**
```
Host: api.example.com
X-Forwarded-Host: api.example.com
X-Forwarded-Proto: https
X-Forwarded-Port: 443
X-Forwarded-For: 203.0.113.1, 10.0.1.5, 10.0.2.8
X-Gateway-Host: gateway.internal
X-Internal-Proxy: nginx.cluster.local
```

## **Advanced: Using Custom Headers for Multi-Tier**

### **External Load Balancer**

```nginx
server {
    listen 443 ssl;
    server_name api.example.com;
    
    location / {
        # Original request info
        proxy_set_header X-Original-Host $host;
        proxy_set_header X-Original-Proto $scheme;
        proxy_set_header X-Original-Port $server_port;
        proxy_set_header X-Original-URI $request_uri;
        
        # Standard forwarded headers
        proxy_set_header X-Forwarded-Host $host;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Port $server_port;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
        # Current hop info
        proxy_set_header X-Proxy-Tier "external-lb";
        
        proxy_pass http://gateway.internal:8080;
    }
}
```

### **API Gateway**

```nginx
server {
    listen 8080;
    server_name gateway.internal;
    
    location / {
        # Preserve ORIGINAL (most important)
        proxy_set_header X-Original-Host $http_x_original_host;
        proxy_set_header X-Original-Proto $http_x_original_proto;
        proxy_set_header X-Original-Port $http_x_original_port;
        proxy_set_header X-Original-URI $http_x_original_uri;
        
        # Preserve forwarded headers
        proxy_set_header X-Forwarded-Host $http_x_forwarded_host;
        proxy_set_header X-Forwarded-Proto $http_x_forwarded_proto;
        proxy_set_header X-Forwarded-Port $http_x_forwarded_port;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
        # Add this tier's info
        proxy_set_header X-Gateway-Tier "api-gateway";
        proxy_set_header X-Gateway-Host $host;
        
        # Use original host for downstream
        proxy_set_header Host $http_x_original_host;
        
        proxy_pass http://nginx.cluster.local;
    }
}
```

### **Internal nginx**

```nginx
server {
    listen 80;
    server_name nginx.cluster.local;
    
    location / {
        # Preserve ALL original headers
        proxy_set_header X-Original-Host $http_x_original_host;
        proxy_set_header X-Original-Proto $http_x_original_proto;
        proxy_set_header X-Original-Port $http_x_original_port;
        proxy_set_header X-Original-URI $http_x_original_uri;
        
        # Preserve forwarded headers
        proxy_set_header X-Forwarded-Host $http_x_forwarded_host;
        proxy_set_header X-Forwarded-Proto $http_x_forwarded_proto;
        proxy_set_header X-Forwarded-Port $http_x_forwarded_port;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        
        # Preserve gateway info
        proxy_set_header X-Gateway-Tier $http_x_gateway_tier;
        proxy_set_header X-Gateway-Host $http_x_gateway_host;
        
        # Add this tier's info
        proxy_set_header X-Internal-Tier "nginx-proxy";
        
        # Host should be original
        proxy_set_header Host $http_x_original_host;
        
        proxy_pass http://localhost:8080;
    }
}
```

## **Spring Boot Configuration**

### **application.yml**

```yaml
server:
  port: 8080
  forward-headers-strategy: framework
  
  tomcat:
    # Trust internal network
    remoteip:
      internal-proxies: 10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}|172\\.(1[6-9]|2\\d|3[01])\\.\\d{1,3}\\.\\d{1,3}
      remote-ip-header: X-Forwarded-For
      protocol-header: X-Forwarded-Proto
      port-header: X-Forwarded-Port
      host-header: X-Forwarded-Host

logging:
  level:
    org.apache.catalina.valves.RemoteIpValve: DEBUG
```

### **Service to Extract Real Domain**

```java
@Service
public class DomainResolver {
    
    private static final Logger log = LoggerFactory.getLogger(DomainResolver.class);
    
    /**
     * Get the real domain through multiple proxies/gateways
     */
    public String getRealDomain(HttpServletRequest request) {
        // Priority order for finding the real domain
        
        // 1. Check X-Original-Host (our custom header from first proxy)
        String originalHost = request.getHeader("X-Original-Host");
        if (isValidHost(originalHost)) {
            log.debug("Using X-Original-Host: {}", originalHost);
            return originalHost;
        }
        
        // 2. Check X-Forwarded-Host (standard header)
        String forwardedHost = request.getHeader("X-Forwarded-Host");
        if (isValidHost(forwardedHost)) {
            // May contain multiple hosts, get first (original)
            String firstHost = forwardedHost.split(",")[0].trim();
            log.debug("Using X-Forwarded-Host: {}", firstHost);
            return firstHost;
        }
        
        // 3. Check Host header (might be rewritten)
        String host = request.getHeader("Host");
        if (isValidHost(host)) {
            log.debug("Using Host header: {}", host);
            return host;
        }
        
        // 4. Fallback to server name
        String serverName = request.getServerName();
        log.debug("Using server name: {}", serverName);
        return serverName;
    }
    
    /**
     * Get complete origin URL
     */
    public String getRealOrigin(HttpServletRequest request) {
        String protocol = getRealProtocol(request);
        String host = getRealDomain(request);
        Integer port = getRealPort(request);
        
        // Don't include default ports
        if ((port == 80 && "http".equals(protocol)) || 
            (port == 443 && "https".equals(protocol))) {
            return protocol + "://" + host;
        }
        
        return protocol + "://" + host + ":" + port;
    }
    
    /**
     * Get real protocol (http/https)
     */
    public String getRealProtocol(HttpServletRequest request) {
        // 1. Check X-Original-Proto
        String originalProto = request.getHeader("X-Original-Proto");
        if (isValidProtocol(originalProto)) {
            return originalProto;
        }
        
        // 2. Check X-Forwarded-Proto
        String forwardedProto = request.getHeader("X-Forwarded-Proto");
        if (isValidProtocol(forwardedProto)) {
            return forwardedProto.split(",")[0].trim();
        }
        
        // 3. Fallback to request scheme
        return request.getScheme();
    }
    
    /**
     * Get real port
     */
    public Integer getRealPort(HttpServletRequest request) {
        // 1. Check X-Original-Port
        String originalPort = request.getHeader("X-Original-Port");
        if (originalPort != null) {
            try {
                return Integer.parseInt(originalPort);
            } catch (NumberFormatException e) {
                log.warn("Invalid X-Original-Port: {}", originalPort);
            }
        }
        
        // 2. Check X-Forwarded-Port
        String forwardedPort = request.getHeader("X-Forwarded-Port");
        if (forwardedPort != null) {
            try {
                String firstPort = forwardedPort.split(",")[0].trim();
                return Integer.parseInt(firstPort);
            } catch (NumberFormatException e) {
                log.warn("Invalid X-Forwarded-Port: {}", forwardedPort);
            }
        }
        
        // 3. Infer from protocol
        String protocol = getRealProtocol(request);
        return "https".equals(protocol) ? 443 : 80;
    }
    
    /**
     * Get all proxy chain information
     */
    public Map<String, Object> getProxyChainInfo(HttpServletRequest request) {
        Map<String, Object> info = new LinkedHashMap<>();
        
        // Original request info
        info.put("originalHost", request.getHeader("X-Original-Host"));
        info.put("originalProto", request.getHeader("X-Original-Proto"));
        info.put("originalPort", request.getHeader("X-Original-Port"));
        info.put("originalURI", request.getHeader("X-Original-URI"));
        
        // Forwarded headers
        info.put("forwardedHost", request.getHeader("X-Forwarded-Host"));
        info.put("forwardedProto", request.getHeader("X-Forwarded-Proto"));
        info.put("forwardedPort", request.getHeader("X-Forwarded-Port"));
        info.put("forwardedFor", request.getHeader("X-Forwarded-For"));
        
        // Gateway info
        info.put("gatewayTier", request.getHeader("X-Gateway-Tier"));
        info.put("gatewayHost", request.getHeader("X-Gateway-Host"));
        
        // Internal proxy info
        info.put("internalTier", request.getHeader("X-Internal-Tier"));
        
        // Current request info
        info.put("host", request.getHeader("Host"));
        info.put("serverName", request.getServerName());
        info.put("serverPort", request.getServerPort());
        info.put("scheme", request.getScheme());
        info.put("remoteAddr", request.getRemoteAddr());
        
        // Computed real values
        info.put("realDomain", getRealDomain(request));
        info.put("realOrigin", getRealOrigin(request));
        info.put("realProtocol", getRealProtocol(request));
        
        return info;
    }
    
    private boolean isValidHost(String host) {
        return host != null && 
               !host.isEmpty() && 
               !host.equalsIgnoreCase("unknown") &&
               !host.equals("localhost") &&
               !host.startsWith("127.") &&
               !host.startsWith("10.") &&
               !host.startsWith("172.") &&
               !host.startsWith("192.168.");
    }
    
    private boolean isValidProtocol(String protocol) {
        return "http".equalsIgnoreCase(protocol) || 
               "https".equalsIgnoreCase(protocol);
    }
}
```

### **WebFlux Version**

```java
@Service
public class DomainResolverWebFlux {
    
    public String getRealDomain(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        
        // 1. X-Original-Host
        String originalHost = headers.getFirst("X-Original-Host");
        if (isValidHost(originalHost)) {
            return originalHost;
        }
        
        // 2. X-Forwarded-Host
        String forwardedHost = headers.getFirst("X-Forwarded-Host");
        if (isValidHost(forwardedHost)) {
            return forwardedHost.split(",")[0].trim();
        }
        
        // 3. Host header
        String host = headers.getFirst("Host");
        if (isValidHost(host)) {
            return host;
        }
        
        // 4. Fallback to URI
        return request.getURI().getHost();
    }
    
    public String getRealOrigin(ServerHttpRequest request) {
        String protocol = getRealProtocol(request);
        String host = getRealDomain(request);
        
        return protocol + "://" + host;
    }
    
    public String getRealProtocol(ServerHttpRequest request) {
        HttpHeaders headers = request.getHeaders();
        
        String originalProto = headers.getFirst("X-Original-Proto");
        if (isValidProtocol(originalProto)) {
            return originalProto;
        }
        
        String forwardedProto = headers.getFirst("X-Forwarded-Proto");
        if (isValidProtocol(forwardedProto)) {
            return forwardedProto.split(",")[0].trim();
        }
        
        return request.getURI().getScheme();
    }
    
    private boolean isValidHost(String host) {
        return host != null && 
               !host.isEmpty() && 
               !host.equalsIgnoreCase("unknown") &&
               !host.equals("localhost") &&
               !host.startsWith("127.") &&
               !host.startsWith("10.");
    }
    
    private boolean isValidProtocol(String protocol) {
        return "http".equalsIgnoreCase(protocol) || 
               "https".equalsIgnoreCase(protocol);
    }
}
```

## **Controller Example**

```java
@RestController
@RequestMapping("/api")
public class DomainController {
    
    @Autowired
    private DomainResolver domainResolver;
    
    @GetMapping("/domain-info")
    public Map<String, Object> getDomainInfo(HttpServletRequest request) {
        return domainResolver.getProxyChainInfo(request);
    }
    
    @GetMapping("/generate-callback-url")
    public String generateCallbackUrl(HttpServletRequest request) {
        String origin = domainResolver.getRealOrigin(request);
        return origin + "/oauth/callback";
    }
}
```

## **Testing**

```bash
# Simulate multi-tier proxy headers
curl -H "X-Original-Host: api.example.com" \
     -H "X-Original-Proto: https" \
     -H "X-Original-Port: 443" \
     -H "X-Forwarded-Host: api.example.com" \
     -H "X-Forwarded-Proto: https" \
     -H "X-Gateway-Host: gateway.internal" \
     -H "Host: localhost" \
     http://localhost:8080/api/domain-info
```

**Response:**
```json
{
  "originalHost": "api.example.com",
  "originalProto": "https",
  "originalPort": "443",
  "forwardedHost": "api.example.com",
  "gatewayHost": "gateway.internal",
  "host": "localhost",
  "serverName": "localhost",
  "realDomain": "api.example.com",
  "realOrigin": "https://api.example.com",
  "realProtocol": "https"
}
```

## **Key Principles**

1. **Set custom `X-Original-*` headers at the FIRST proxy** (external entry point)
2. **Preserve (don't overwrite) these headers** at intermediate proxies using `$http_x_original_host`
3. **Use standard `X-Forwarded-*` headers as fallback**
4. **Check headers in priority order** in your Spring Boot code
5. **Filter out internal domains** (localhost, 10.x.x.x, 172.x.x.x, 192.168.x.x)
6. **Log the full chain for debugging**

This approach ensures you always get the **real, original, public-facing domain** regardless of how many internal gateways/proxies are in between.


# spring boot direct get host

## Getting Host Domain Directly in Spring Boot (No Proxy)

When Spring Boot directly accepts requests without nginx, you can get the host domain from the HTTP request headers. Here's how:

## **Basic Approach - Servlet (Spring MVC)**

```java
@RestController
@RequestMapping("/api")
public class DomainController {
    
    @GetMapping("/host-info")
    public Map<String, String> getHostInfo(HttpServletRequest request) {
        Map<String, String> info = new LinkedHashMap<>();
        
        // Method 1: Host header (most reliable)
        String host = request.getHeader("Host");
        info.put("hostHeader", host);
        
        // Method 2: Server name (parsed from Host header)
        String serverName = request.getServerName();
        info.put("serverName", serverName);
        
        // Method 3: Server port
        int serverPort = request.getServerPort();
        info.put("serverPort", String.valueOf(serverPort));
        
        // Method 4: Scheme (http/https)
        String scheme = request.getScheme();
        info.put("scheme", scheme);
        
        // Method 5: Request URL (complete)
        StringBuffer requestURL = request.getRequestURL();
        info.put("requestURL", requestURL.toString());
        
        // Method 6: Request URI (path only)
        String requestURI = request.getRequestURI();
        info.put("requestURI", requestURI);
        
        return info;
    }
}
```

## **WebFlux Version**

```java
@RestController
@RequestMapping("/api")
public class DomainControllerWebFlux {
    
    @GetMapping("/host-info")
    public Mono<Map<String, String>> getHostInfo(ServerHttpRequest request) {
        Map<String, String> info = new LinkedHashMap<>();
        
        // Method 1: Host header
        String host = request.getHeaders().getHost().toString();
        info.put("host", host);
        
        // Method 2: From URI
        URI uri = request.getURI();
        info.put("uriHost", uri.getHost());
        info.put("uriPort", String.valueOf(uri.getPort()));
        info.put("uriScheme", uri.getScheme());
        
        // Method 3: Complete URL
        info.put("fullURL", uri.toString());
        
        // Method 4: Path
        info.put("path", uri.getPath());
        
        return Mono.just(info);
    }
}
```

## **Comprehensive Domain Resolver Service**

```java
@Service
public class DirectDomainResolver {
    
    private static final Logger log = LoggerFactory.getLogger(DirectDomainResolver.class);
    
    /**
     * Get the host domain (without port)
     */
    public String getHostDomain(HttpServletRequest request) {
        String host = request.getHeader("Host");
        
        if (host != null) {
            // Remove port if present
            int colonIndex = host.indexOf(':');
            if (colonIndex > 0) {
                return host.substring(0, colonIndex);
            }
            return host;
        }
        
        // Fallback to server name
        return request.getServerName();
    }
    
    /**
     * Get host with port
     */
    public String getHostWithPort(HttpServletRequest request) {
        String host = request.getHeader("Host");
        
        if (host != null) {
            return host; // Already includes port if non-standard
        }
        
        // Construct from server name and port
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        
        // Don't include default ports
        if ((serverPort == 80 && "http".equals(request.getScheme())) ||
            (serverPort == 443 && "https".equals(request.getScheme()))) {
            return serverName;
        }
        
        return serverName + ":" + serverPort;
    }
    
    /**
     * Get base URL (scheme + host + port)
     */
    public String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String host = getHostWithPort(request);
        
        return scheme + "://" + host;
    }
    
    /**
     * Get full URL with path and query
     */
    public String getFullUrl(HttpServletRequest request) {
        StringBuffer url = request.getRequestURL();
        String queryString = request.getQueryString();
        
        if (queryString != null) {
            url.append('?').append(queryString);
        }
        
        return url.toString();
    }
    
    /**
     * Get port (returns default port for scheme if not specified)
     */
    public int getPort(HttpServletRequest request) {
        String host = request.getHeader("Host");
        
        // Check if Host header includes port
        if (host != null && host.contains(":")) {
            String[] parts = host.split(":");
            try {
                return Integer.parseInt(parts[1]);
            } catch (NumberFormatException e) {
                log.warn("Invalid port in Host header: {}", host);
            }
        }
        
        // Check server port
        int serverPort = request.getServerPort();
        if (serverPort > 0) {
            return serverPort;
        }
        
        // Return default port based on scheme
        return "https".equals(request.getScheme()) ? 443 : 80;
    }
    
    /**
     * Check if request is HTTPS
     */
    public boolean isSecure(HttpServletRequest request) {
        return "https".equalsIgnoreCase(request.getScheme()) || 
               request.isSecure();
    }
    
    /**
     * Get all domain-related information
     */
    public DomainInfo getDomainInfo(HttpServletRequest request) {
        return DomainInfo.builder()
            .hostHeader(request.getHeader("Host"))
            .domain(getHostDomain(request))
            .hostWithPort(getHostWithPort(request))
            .scheme(request.getScheme())
            .port(getPort(request))
            .baseUrl(getBaseUrl(request))
            .fullUrl(getFullUrl(request))
            .path(request.getRequestURI())
            .queryString(request.getQueryString())
            .isSecure(isSecure(request))
            .serverName(request.getServerName())
            .serverPort(request.getServerPort())
            .build();
    }
}

@Data
@Builder
class DomainInfo {
    private String hostHeader;
    private String domain;
    private String hostWithPort;
    private String scheme;
    private Integer port;
    private String baseUrl;
    private String fullUrl;
    private String path;
    private String queryString;
    private Boolean isSecure;
    private String serverName;
    private Integer serverPort;
}
```

## **WebFlux Version**

```java
@Service
public class DirectDomainResolverWebFlux {
    
    private static final Logger log = LoggerFactory.getLogger(DirectDomainResolverWebFlux.class);
    
    /**
     * Get host domain (without port)
     */
    public String getHostDomain(ServerHttpRequest request) {
        InetSocketAddress host = request.getHeaders().getHost();
        
        if (host != null) {
            return host.getHostString();
        }
        
        // Fallback to URI
        return request.getURI().getHost();
    }
    
    /**
     * Get host with port
     */
    public String getHostWithPort(ServerHttpRequest request) {
        InetSocketAddress host = request.getHeaders().getHost();
        
        if (host != null) {
            String hostString = host.getHostString();
            int port = host.getPort();
            
            URI uri = request.getURI();
            String scheme = uri.getScheme();
            
            // Don't include default ports
            if ((port == 80 && "http".equals(scheme)) ||
                (port == 443 && "https".equals(scheme)) ||
                port == -1) {
                return hostString;
            }
            
            return hostString + ":" + port;
        }
        
        // Fallback to URI
        URI uri = request.getURI();
        String uriHost = uri.getHost();
        int uriPort = uri.getPort();
        
        if (uriPort == -1) {
            return uriHost;
        }
        
        return uriHost + ":" + uriPort;
    }
    
    /**
     * Get base URL
     */
    public String getBaseUrl(ServerHttpRequest request) {
        URI uri = request.getURI();
        String scheme = uri.getScheme();
        String host = getHostWithPort(request);
        
        return scheme + "://" + host;
    }
    
    /**
     * Get full URL
     */
    public String getFullUrl(ServerHttpRequest request) {
        return request.getURI().toString();
    }
    
    /**
     * Get port
     */
    public int getPort(ServerHttpRequest request) {
        InetSocketAddress host = request.getHeaders().getHost();
        
        if (host != null && host.getPort() != -1) {
            return host.getPort();
        }
        
        // Check URI
        int uriPort = request.getURI().getPort();
        if (uriPort != -1) {
            return uriPort;
        }
        
        // Default based on scheme
        String scheme = request.getURI().getScheme();
        return "https".equals(scheme) ? 443 : 80;
    }
    
    /**
     * Get domain info
     */
    public DomainInfo getDomainInfo(ServerHttpRequest request) {
        URI uri = request.getURI();
        
        return DomainInfo.builder()
            .hostHeader(request.getHeaders().getHost() != null ? 
                       request.getHeaders().getHost().toString() : null)
            .domain(getHostDomain(request))
            .hostWithPort(getHostWithPort(request))
            .scheme(uri.getScheme())
            .port(getPort(request))
            .baseUrl(getBaseUrl(request))
            .fullUrl(getFullUrl(request))
            .path(uri.getPath())
            .queryString(uri.getQuery())
            .isSecure("https".equals(uri.getScheme()))
            .build();
    }
}
```

## **Practical Usage Examples**

### **1. Generate Callback URLs (OAuth, Webhooks)**

```java
@Service
public class CallbackUrlService {
    
    @Autowired
    private DirectDomainResolver domainResolver;
    
    public String generateOAuthCallbackUrl(HttpServletRequest request) {
        String baseUrl = domainResolver.getBaseUrl(request);
        return baseUrl + "/oauth/callback";
    }
    
    public String generateWebhookUrl(HttpServletRequest request, String endpoint) {
        String baseUrl = domainResolver.getBaseUrl(request);
        return baseUrl + "/webhooks/" + endpoint;
    }
}
```

### **2. Dynamic CORS Configuration**

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            .allowedOriginPatterns("*") // Will validate against request host
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowCredentials(true);
    }
}

@Component
public class DynamicCorsFilter implements Filter {
    
    @Autowired
    private DirectDomainResolver domainResolver;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        String origin = req.getHeader("Origin");
        String requestHost = domainResolver.getHostDomain(req);
        
        // Allow same-origin requests
        if (origin != null && origin.contains(requestHost)) {
            res.setHeader("Access-Control-Allow-Origin", origin);
            res.setHeader("Access-Control-Allow-Credentials", "true");
        }
        
        chain.doFilter(request, response);
    }
}
```

### **3. Build Canonical URLs (SEO)**

```java
@Controller
public class PageController {
    
    @Autowired
    private DirectDomainResolver domainResolver;
    
    @GetMapping("/products/{id}")
    public String productPage(@PathVariable Long id, 
                            HttpServletRequest request, 
                            Model model) {
        
        // Generate canonical URL
        String baseUrl = domainResolver.getBaseUrl(request);
        String canonicalUrl = baseUrl + "/products/" + id;
        
        model.addAttribute("canonicalUrl", canonicalUrl);
        return "product";
    }
}
```

**In Thymeleaf template:**
```html
<head>
    <link rel="canonical" th:href="${canonicalUrl}" />
</head>
```

### **4. Multi-Tenant Domain Detection**

```java
@Service
public class TenantResolver {
    
    @Autowired
    private DirectDomainResolver domainResolver;
    
    public String resolveTenant(HttpServletRequest request) {
        String domain = domainResolver.getHostDomain(request);
        
        // Extract subdomain as tenant identifier
        // e.g., tenant1.myapp.com -> tenant1
        String[] parts = domain.split("\\.");
        
        if (parts.length >= 3) {
            return parts[0]; // subdomain
        }
        
        return "default";
    }
    
    public boolean isCustomDomain(HttpServletRequest request) {
        String domain = domainResolver.getHostDomain(request);
        
        // Check if it's a custom domain (not your main domain)
        return !domain.endsWith("myapp.com");
    }
}
```

### **5. Logging and Monitoring**

```java
@Component
public class RequestLoggingFilter implements Filter {
    
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    
    @Autowired
    private DirectDomainResolver domainResolver;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        
        DomainInfo info = domainResolver.getDomainInfo(req);
        
        log.info("Request received - Domain: {}, Path: {}, Secure: {}, Full URL: {}", 
                info.getDomain(), 
                info.getPath(), 
                info.getIsSecure(),
                info.getFullUrl());
        
        chain.doFilter(request, response);
    }
}
```

## **Testing Locally**

### **Test with different hosts in /etc/hosts:**

```bash
# /etc/hosts
127.0.0.1 local.myapp.com
127.0.0.1 tenant1.myapp.com
127.0.0.1 customdomain.com
```

### **Test requests:**

```bash
# Test with localhost
curl http://localhost:8080/api/host-info

# Test with custom domain
curl http://local.myapp.com:8080/api/host-info

# Test with tenant subdomain
curl http://tenant1.myapp.com:8080/api/host-info

# Test HTTPS (if SSL configured)
curl https://local.myapp.com:8443/api/host-info
```

## **Important Notes**

### **The Host Header**

The `Host` header is set by the client (browser/curl) and contains:
```
GET /api/users HTTP/1.1
Host: api.example.com:8080
```

Spring Boot parses this into:
- `request.getHeader("Host")` → `"api.example.com:8080"`
- `request.getServerName()` → `"api.example.com"`
- `request.getServerPort()` → `8080`

### **Security Consideration**

**The Host header can be spoofed!** If you're using it for security decisions:

```java
@Service
public class SecureDomainValidator {
    
    private static final Set<String> ALLOWED_HOSTS = Set.of(
        "myapp.com",
        "www.myapp.com",
        "api.myapp.com"
    );
    
    public boolean isValidHost(HttpServletRequest request) {
        String host = request.getHeader("Host");
        
        if (host == null) {
            return false;
        }
        
        // Remove port
        String domain = host.split(":")[0];
        
        // Check against whitelist
        return ALLOWED_HOSTS.contains(domain) || 
               domain.endsWith(".myapp.com");
    }
}

@Component
public class HostValidationFilter implements Filter {
    
    @Autowired
    private SecureDomainValidator validator;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                        FilterChain chain) throws IOException, ServletException {
        
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        
        if (!validator.isValidHost(req)) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Host header");
            return;
        }
        
        chain.doFilter(request, response);
    }
}
```

## **Summary**

When Spring Boot directly accepts requests:

1. **Use `request.getHeader("Host")`** for the most reliable host information
2. **Use `request.getServerName()`** for the domain without port
3. **Use `request.getServerPort()`** for the port
4. **Use `request.getScheme()`** for http/https
5. **Use `request.getRequestURL()`** for the complete URL
6. **Validate the Host header** if using it for security decisions
7. **Be aware** the Host header is set by the client and can be spoofed

The key difference from the proxy scenario is that **all information comes directly from the HTTP request**, not from forwarded headers.