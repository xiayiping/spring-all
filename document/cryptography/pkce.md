# How PKCE are Sent

Let me clarify how the **authorization code** and **code verifier** are sent in the OAuth 2.0 Authorization Code Flow with **PKCE**, and whether they are sent in the URL or elsewhere. The location of these codes depends on the stage of the flow and whether the **redirect URI** is being used.

---

### **1. The Initial Authorization Request**
When the client (SPA) redirects the user to the authorization server, the following parameters are sent via a **URL** (query string) appended to the authorization server's endpoint. For example:

```
https://authorization-server.com/auth
  ?response_type=code
  &client_id=YOUR_CLIENT_ID
  &redirect_uri=https://your-spa.com/callback
  &code_challenge=CHALLENGE_VALUE
  &code_challenge_method=S256
  &state=STATE_VALUE
```

#### Key Parameters:
- `code_challenge`: A hashed version of the **code verifier** is sent here. This is part of PKCE.
- `code_challenge_method`: Indicates the hashing method (e.g., `S256` for SHA-256).
- `response_type=code`: Requests an **authorization code**.
- `state`: A unique value to prevent CSRF attacks.

#### Where is this sent?
- This is sent **via a URL redirect**, so it is visible in the browser's address bar.

---

### **2. Receiving the Authorization Code**
After the user successfully authenticates, the authorization server redirects them back to the **redirect URI** provided in the initial request. The **authorization code** is sent as a query parameter in the URL:

```
https://your-spa.com/callback?code=AUTHORIZATION_CODE&state=STATE_VALUE
```

#### Where is this sent?
- The **authorization code** is sent **via the redirect URI** in the browser's address bar.

#### Security Implications:
- If an attacker can intercept the redirect (e.g., via network sniffing or an insecure redirect URI over HTTP), they can steal the **authorization code**. This is why PKCE is used.

---

### **3. Exchanging the Authorization Code for Tokens**
Once the SPA receives the **authorization code**, it sends an HTTP POST request to the authorization server's **token endpoint** to exchange it for an access token:

#### Example HTTP Request:
```http
POST https://authorization-server.com/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
&client_id=YOUR_CLIENT_ID
&code=AUTHORIZATION_CODE
&redirect_uri=https://your-spa.com/callback
&code_verifier=ORIGINAL_CODE_VERIFIER
```

#### Key Parameters:
- `code`: The **authorization code** received from the redirect.
- `code_verifier`: The **original code verifier** (not the hashed one).
- `redirect_uri`: Must match the one used in the initial request.

#### Where is this sent?
- This is sent **via an HTTP POST request** (not in the URL), so it is not visible in the browser's address bar.

#### Security Implications:
- If an attacker intercepts this request (e.g., via man-in-the-middle attack), they could steal both the **authorization code** and **code verifier**. This is why HTTPS is critical.

---

### **Summary: Where Are Codes Sent?**

| **Code/Challenge**       | **Sent In**              | **Visibility**                |
|---------------------------|--------------------------|--------------------------------|
| `code_challenge`          | Authorization request URL | Visible in the browser (query string). |
| `authorization_code`      | Redirect URI (URL query) | Visible in the browser (query string). |
| `code_verifier`           | HTTP POST body (to token endpoint) | Not visible in the browser (sent securely in POST body). |

---

### **Why PKCE Protects the Flow**
1. Even if the **authorization code** is intercepted (e.g., from the redirect URI), it cannot be exchanged for tokens without the corresponding **code verifier**.
2. The **code verifier** is sent in a secure POST request, not in the URL, reducing the chance of exposure.
3. An attacker cannot forge a valid **code verifier** because it must match the hashed `code_challenge` sent in the initial request.

---

### **What If All Codes Are Intercepted?**
If an attacker intercepts:
- The **authorization code** (from the redirect URI).
- The **code verifier** (from the POST to the token endpoint).

Then, yes, they could steal the tokens. This is why additional security measures are important:
- Use HTTPS to encrypt all communication.
- Harden the SPA (e.g., against XSS) to prevent attackers from injecting malicious scripts.
- Implement short-lived authorization codes and rotate refresh tokens.

---

### **Conclusion**
- The **authorization code** is sent in the URL during the redirect phase.
- The **code verifier** is not sent in the URL but securely via a POST request during the token exchange.
- PKCE ensures that even if the **authorization code** is intercepted, the attacker cannot use it without the **code verifier**, significantly improving security.

# URL and HTTPS

Great question! Let's address your concerns about whether data in the URL (particularly parameters in a GET request) is encrypted with HTTPS and whether it can be leaked during the DNS resolution process.

---

## **1. Is the URL Encrypted in HTTPS?**

Yes, when you use **HTTPS**, the **entire URL path and query parameters** are encrypted during transmission between the client and the server. Here's how it works:

- HTTPS secures the communication using **TLS (Transport Layer Security)**, which encrypts all data exchanged between the client and the server. This includes the HTTP headers, the body of the request (for POST requests), and the URL itself (excluding the domain name).
- For example, if a URL like this is sent over HTTPS:
  ```
  https://example.com/callback?code=AUTH_CODE&state=STATE_VALUE
  ```
  The query string (`code=AUTH_CODE&state=STATE_VALUE`) is encrypted and cannot be intercepted or read by an attacker monitoring the network.

### **What Remains Unencrypted in HTTPS?**
- The **domain name** (`example.com`) and the port number (default: 443 for HTTPS) are not encrypted because they are required for routing the request to the correct server. These are part of the **DNS resolution** process, which happens before the HTTPS connection is established.

### **Security Implications:**
- As long as HTTPS is used, attackers cannot see or tamper with the query parameters or any part of the URL path after the domain (e.g., `/callback?code=AUTH_CODE`).
- However, the domain name itself (e.g., `example.com`) and the fact that a connection is being made to it are visible during the DNS resolution process.

---

## **2. Are GET Request Parameters Sent During DNS Resolution?**

No, the parameters in the GET request (query string) are **not sent during DNS resolution**. Here's why:

- DNS resolution is the process of converting a human-readable domain name (e.g., `example.com`) into an IP address (e.g., `192.168.1.1`) so the client knows where to send the request.
- Only the **domain name** is sent to the DNS resolver during this process. For example:
    - If your full URL is:
      ```
      https://example.com/callback?code=AUTH_CODE&state=STATE_VALUE
      ```
      The DNS resolver only sees `example.com`. The path `/callback` and the query string `code=AUTH_CODE&state=STATE_VALUE` are not sent as part of the DNS resolution process.

### **How the Full URL Is Used:**
- After the DNS resolution is complete and the IP address of the server is obtained, the client establishes a connection to the server using **TLS (for HTTPS)**. At this point, the full URL (including the path and query parameters) is sent, but it is encrypted as part of the HTTPS protocol.

---

## **3. Can Query Parameters Still Be Leaked?**

While HTTPS encrypts the URL path and query parameters, there are specific scenarios where query parameters may still be exposed:

### **a. Browser History**
- Query parameters in GET requests are stored in the browser's history. For example, if a user visits:
  ```
  https://example.com/callback?code=AUTH_CODE&state=STATE_VALUE
  ```
  The full URL, including the query parameters, is saved in the browser's history unless explicitly cleared.

### **b. Referer Header**
- If the user clicks on a link from a page with query parameters, the browser may send the full URL (including query parameters) in the `Referer` header to the next site. For example:
    - If the user navigates from:
      ```
      https://example.com/callback?code=AUTH_CODE&state=STATE_VALUE
      ```
      to another website, the `Referer` header might include the full URL:
      ```
      Referer: https://example.com/callback?code=AUTH_CODE&state=STATE_VALUE
      ```
    - This can leak sensitive query parameters to third-party websites, depending on how the navigation occurs.

  **Mitigation**: Use the `Referrer-Policy` HTTP header to control what information is sent in the `Referer` header.

### **c. Logging (on Servers, Proxies, or Network Devices)**
- If the URL with sensitive query parameters is logged by a server, proxy, or network appliance (e.g., a firewall), the query parameters might be exposed in logs.

  **Mitigation**: Avoid including sensitive information (e.g., authorization codes) in query parameters. Instead, use POST requests or secure cookies for transmitting sensitive data.

---

## **4. Best Practices for Avoiding Leaks**

To minimize the risk of leaking sensitive data in the URL:
1. **Use HTTPS Everywhere**:
    - Ensure all communications happen over HTTPS to encrypt query parameters during transmission.
2. **Avoid Sensitive Data in Query Strings**:
    - If possible, avoid including sensitive information (e.g., authorization codes or tokens) in the URL. Instead, use more secure methods like:
        - **POST requests** (the body is encrypted over HTTPS).
        - **Cookies with appropriate flags** (e.g., `HttpOnly` and `Secure`).
3. **Control Referrer Information**:
    - Use the `Referrer-Policy` header to control what parts of the URL are included in the `Referer` header. For example:
      ```
      Referrer-Policy: no-referrer
      ```
      This prevents the browser from sending any referer information to other sites.
4. **Secure the Redirect URI**:
    - Ensure your redirect URI is secure, uses HTTPS, and is tightly controlled to prevent redirection to malicious sites.

---

## **Conclusion**

- **Query parameters are encrypted in HTTPS**: The full URL, including the query string, is encrypted during transmission. Attackers cannot see or tamper with it unless HTTPS is not used.
- **DNS resolution only exposes the domain name**: The query parameters and path are not sent during DNS resolution.
- **Be mindful of leaks through browser history, referers, and logs**: While HTTPS protects query parameters during transmission, they can still leak in browser history, `Referer` headers, or server logs. Follow best practices to minimize these risks.