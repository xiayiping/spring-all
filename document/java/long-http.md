# Long Live http


To establish and manage a long-lived HTTP connection in Java (client-side) and on the server (e.g., with Spring Boot), you can use **Server-Sent Events (SSE)** or **WebSockets**, depending on the use case. Here's how you can achieve this:

---

## 1. **Client-Side: Java HTTP Long-Lived Connection**
### Using HTTP Client for SSE
You can use Java's `HttpClient` to establish a long-lived connection (e.g., to consume Server-Sent Events).

#### Example: Java Client for SSE
```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class SSEClient {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/sse")) // Replace with your server's endpoint
                .build();

        // Open a long-lived connection
        client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> {
                    // Read the server events in real-time
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(response.body()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println("Received: " + line);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

        // Keep the client running
        Thread.currentThread().join();
    }
}
```

In this example:
- The client connects to a long-lived endpoint (`/sse`) on the server.
- It keeps reading data from the server as it arrives.
- The connection remains open until the server closes it or the client is interrupted.

---

## 2. **Server-Side: Managing a Long-Lived Connection in Spring Boot**
### Use Case 1: Server-Sent Events (SSE)
With **SSE**, the server sends a stream of data to the client. The HTTP connection remains open, and the server pushes updates to the client whenever necessary.

#### Example: Spring Boot SSE Endpoint
```java
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.stream.Stream;

@RestController
public class SSEController {

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamEvents() {
        // Stream data every second
        return Flux.interval(Duration.ofSeconds(1))
                   .map(sequence -> "Event " + sequence);
    }
}
```

In this example:
- The `/sse` endpoint produces a stream of events using `MediaType.TEXT_EVENT_STREAM_VALUE`.
- The `Flux` from Project Reactor is used to send updates periodically (every second in this case).

### How It Works:
- The client connects to `/sse` and keeps the connection open.
- The server sends events (`Event 0`, `Event 1`, ...) over time without closing the connection.

---

### Use Case 2: WebSockets (for Bi-Directional Communication)
If you need **two-way communication** (client-to-server and server-to-client), WebSockets are a better choice.

#### Example: Spring Boot WebSocket Server
Add the WebSocket dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

Create a WebSocket configuration and endpoint:
```java
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandler(), "/ws");
    }

    private static class WebSocketHandler extends TextWebSocketHandler {
        @Override
        public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            System.out.println("Received: " + message.getPayload());
            session.sendMessage(new TextMessage("Hello from server!"));
        }
    }
}
```

### How It Works:
1. The client connects to the `/ws` WebSocket endpoint.
2. The server handles incoming messages and responds in real-time.

#### Example: Java Client for WebSocket
```java
import java.net.URI;
import java.net.http.WebSocket;
import java.util.concurrent.CompletionStage;

public class WebSocketClient {
    public static void main(String[] args) throws Exception {
        WebSocket webSocket = WebSocket.newBuilder()
                .uri(URI.create("ws://localhost:8080/ws"))
                .build();

        webSocket.sendText("Hello from client!", true);
        webSocket.request(1);
    }
}
```

---

## Choosing Between SSE and WebSockets
- **SSE** is simpler and ideal for one-way communication (server to client).
- **WebSockets** are better for bi-directional, real-time communication.

Both approaches allow you to maintain long-lived connections. Use SSE if you only need server-to-client updates and WebSockets if you need full duplex (two-way) communication.


# Sender keep long live 

If you want the client to send requests to the server **without closing the connection for performance reasons**, you can achieve this by using **HTTP/2**, **Connection Keep-Alive**, or **WebSockets**, depending on your requirements.

---

### 1. **Use HTTP/2 with Persistent Connections**
HTTP/2 allows **multiplexing** multiple HTTP requests over a single persistent connection. This means the client can send multiple requests to the server without closing and reopening the connection after each request.

#### Server-Side: Spring Boot HTTP/2 Configuration
To enable HTTP/2 in Spring Boot:
1. Add an HTTPS certificate (HTTP/2 requires HTTPS).
2. Configure HTTP/2 in `application.properties` or `application.yml`:
   ```properties
   server.http2.enabled=true
   server.ssl.key-store=classpath:keystore.p12
   server.ssl.key-store-password=yourpassword
   server.ssl.key-store-type=PKCS12
   server.ssl.key-alias=tomcat
   ```

3. Generate a self-signed certificate or provide one from a certificate authority.

#### Client-Side: Java HTTP Client
Use the `HttpClient` in Java (introduced in Java 11), which supports HTTP/2 out of the box.

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class Http2Client {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2) // Enable HTTP/2
                .build();

        // Create a single persistent connection
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:8443/your-endpoint"))
                .POST(HttpRequest.BodyPublishers.ofString("Your data"))
                .header("Content-Type", "application/json")
                .build();

        // Send multiple requests over the same connection
        for (int i = 0; i < 10; i++) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response: " + response.body());
        }
    }
}
```

**How it works:**
- HTTP/2 keeps the connection alive and allows multiple requests to be sent efficiently.
- The connection remains open until explicitly closed or idle for a long time.

---

### 2. **Use HTTP Connection Keep-Alive**
HTTP/1.1 supports the **Connection: Keep-Alive** header to maintain a persistent connection between the client and server. This avoids the overhead of repeatedly opening and closing connections.

#### Server-Side: Spring Boot Keep-Alive Configuration
Spring Boot uses **Keep-Alive** by default with embedded servers like Tomcat. You can fine-tune it with these settings in `application.properties`:

```properties
server.tomcat.keep-alive-timeout=60000 # Keep connection alive for 60 seconds
server.tomcat.max-connections=200     # Maximum number of connections
server.tomcat.accept-count=100        # Queue size for incoming connections
```

#### Client-Side: Java `HttpClient` with Keep-Alive
The `HttpClient` in Java 11 or later automatically uses persistent connections (Keep-Alive) for HTTP/1.1. You don’t need to do anything special to enable it.

Here’s an example:
```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KeepAliveClient {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1) // HTTP/1.1 with Keep-Alive
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/your-endpoint"))
                .POST(HttpRequest.BodyPublishers.ofString("Your data"))
                .header("Content-Type", "application/json")
                .header("Connection", "keep-alive") // Optional, Keep-Alive is default in HTTP/1.1
                .build();

        // Send multiple requests on the same connection
        for (int i = 0; i < 10; i++) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Response: " + response.body());
        }
    }
}
```

**How it works:**
- The connection stays open between requests, reducing the overhead of establishing a new connection for every request.
- The server will close the connection after the `keep-alive-timeout` is reached or if the client explicitly closes it.

---

### 3. **Use WebSockets for Full-Duplex, Long-Lived Connections**
If you need continuous communication and the client frequently sends requests to the server (e.g., in real-time applications), WebSockets are the most efficient option. WebSockets keep a single connection open for bidirectional communication.

#### Server-Side: Spring Boot WebSocket Setup
Add the WebSocket dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

Configure the WebSocket server:
```java
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(new WebSocketHandler(), "/ws");
    }

    private static class WebSocketHandler extends TextWebSocketHandler {
        @Override
        public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            System.out.println("Received from client: " + message.getPayload());
            session.sendMessage(new TextMessage("Response from server: " + message.getPayload()));
        }
    }
}
```

#### Client-Side: Java WebSocket Client
Use the `WebSocket` API in Java:
```java
import java.net.URI;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;

public class WebSocketClient {
    public static void main(String[] args) {
        WebSocket webSocket = WebSocket.newBuilder()
                .uri(URI.create("ws://localhost:8080/ws"))
                .build();

        webSocket.sendText("Hello, Server!", true);

        webSocket.request(1);
        webSocket.sendText("Another message", true);

        // Handle responses asynchronously
        webSocket.receive().thenAccept(message -> {
            System.out.println("Received: " + message.toString());
        });
    }
}
```

**How it works:**
- The WebSocket connection stays alive as long as needed.
- The client and server can send messages to each other over the same connection.

---

### Summary of Options
| Solution              | Use Case                                                                                    | Pros                                  | Cons                             |
|-----------------------|---------------------------------------------------------------------------------------------|---------------------------------------|----------------------------------|
| **HTTP/2**            | When you need efficient, multiplexed HTTP communication.                                    | Fast, multiplexing, no extra setup.   | Requires HTTPS.                  |
| **Keep-Alive**        | When you need to reuse connections for standard HTTP requests.                              | Simple, no significant changes.       | Not real-time or bi-directional. |
| **WebSockets**        | When you need full-duplex, real-time communication for frequent client-server interactions. | Real-time, bi-directional, efficient. | Requires WebSocket setup.        |

#### Recommendation:
- Use **HTTP/2** for sending multiple requests efficiently while keeping the connection alive.
- Use **WebSockets** if you need real-time, two-way communication.

# Idel time Http2 Sender 

### **Idle Timeout in HTTP/2**
The idle timeout for HTTP/2 connections defines how long a connection can remain open without any activity (no requests or responses being sent) before it is closed by either the client or the server.

By default, both the **client** and the **server** have their own timeout configurations, and either side can close the connection if the timeout is exceeded.

- **Client-Side**: The idle timeout for HTTP/2 connections depends on the HTTP client implementation (e.g., Java's `HttpClient`).
- **Server-Side**: The idle timeout is typically controlled by the server framework or the HTTP/2 server's configuration.

---

### **How to Configure Idle Timeout in HTTP/2**

#### **1. Client-Side Configuration in Java's `HttpClient`**
Java's `HttpClient` does not directly expose an "idle timeout" setting for HTTP/2 connections, but you can configure the **connect timeout** and **duration for a request**. If no activity occurs for a request within these timeouts, the connection is closed.

Here’s how you can configure timeouts in `HttpClient`:

```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class Http2Client {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2) // Enable HTTP/2
                .connectTimeout(Duration.ofSeconds(30)) // Connection timeout
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://localhost:8443/your-endpoint"))
                .timeout(Duration.ofSeconds(60)) // Request timeout
                .POST(HttpRequest.BodyPublishers.ofString("Your data"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println("Response: " + response.body());
    }
}
```

**Explanation**:
- `connectTimeout`: Sets the maximum time to establish a connection.
- `timeout`: Sets the maximum time to wait for the request to complete. If the connection is idle for longer than this duration, it will be closed by the client.

> **Note**: There is no direct idle timeout setting in Java's `HttpClient`. If you need a stricter idle timeout, you would need to handle it manually by closing idle connections or using a custom solution.

---

#### **2. Server-Side Configuration in Spring Boot**
Spring Boot uses embedded servers like Tomcat, Jetty, or Undertow, which provide configuration options for idle timeouts. The idle timeout for HTTP/2 connections can be configured on the server.

##### **Example for Tomcat (Default in Spring Boot)**:
Add the following properties in `application.properties` or `application.yml`:

```properties
server.http2.enabled=true
server.tomcat.keep-alive-timeout=20000  # Idle timeout in milliseconds (20 seconds)
server.tomcat.max-connections=200       # Optional: Maximum number of connections
server.tomcat.connection-timeout=30000  # Connection timeout in milliseconds (30 seconds)
```

- `keep-alive-timeout`: Specifies the maximum time (in milliseconds) the connection is kept alive when idle.
- `connection-timeout`: Specifies the maximum time to wait for a connection to be established.

For **application.yml**:
```yaml
server:
  http2:
    enabled: true
  tomcat:
    keep-alive-timeout: 20000  # 20 seconds idle timeout
    max-connections: 200       # Maximum number of connections
    connection-timeout: 30000  # 30 seconds connection timeout
```

##### **Example for Jetty**:
If you're using Jetty as the embedded server, configure idle timeouts in `application.properties`:

```properties
server.http2.enabled=true
server.jetty.idle-timeout=20000 # Idle timeout in milliseconds (20 seconds)
```

---

#### **3. Default Idle Timeouts**
- **Tomcat**: By default, the `keep-alive-timeout` is **60 seconds** (60000 milliseconds).
- **Jetty**: By default, the idle timeout is **30 seconds** (30000 milliseconds).
- **Undertow**: The default idle timeout is **no limit**, but it can be configured explicitly.

---

### **How to Test and Ensure Idle Timeout Behavior**
To ensure the idle timeout works as expected:
1. **Set a low idle timeout** (e.g., 5 seconds) on the server.
2. Open an HTTP/2 connection from the client but do not send any requests after the initial connection.
3. Observe whether the connection closes after the configured timeout period.

---

### **Key Takeaways**
- On the **client side**, use `HttpRequest.timeout()` to control how long the client waits for a response, and `HttpClient.connectTimeout()` to configure the connection timeout.
- On the **server side**, configure the idle timeout using the `keep-alive-timeout` property for Tomcat or `idle-timeout` for Jetty.
- For HTTP/2, the server will close the connection if the idle timeout is exceeded, and the client will need to reconnect.


# Long Live SSE server

### **SSE and Long Idle Times**

In **Server-Sent Events (SSE)**, the connection remains open as long as:
1. The server does not explicitly close the connection.
2. No network or timeout issues cause the connection to drop.

However, if the server does not send data for a long time, the connection might be closed by:
- The **client** (due to idle timeout settings).
- The **server** (due to idle timeout settings).
- **Intermediate network components** like proxies or load balancers (which might terminate idle connections).

If the connection is closed and the server later needs to send new data, the client must **reconnect** to the server to receive further events.

---

### **What Happens If the Connection Closes?**

1. **Timeout or Disconnection**:
    - If the connection is idle for too long, it will eventually be closed (either by client, server, or network). This is normal behavior.

2. **Reconnection**:
    - SSE is designed to handle reconnections gracefully. The client automatically tries to reconnect to the server when the connection is lost, using the `Last-Event-ID` header to resume receiving messages from where it left off (if supported by the server).

    - This reconnection happens by default in most browsers' built-in SSE implementations or can be implemented manually for custom clients.

---

### **How to Handle Reconnection in SSE**

#### **1. Server-Side: Keep Connection Alive**
To minimize disconnections due to idle time, you can periodically send **heartbeat messages** or comments (`:`) from the server to keep the connection alive.

##### Example: Spring Boot SSE with Heartbeats
```java
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;

@RestController
public class SSEController {

    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamEvents() {
        return Flux.interval(Duration.ofSeconds(15)) // Send a heartbeat every 15 seconds
                   .map(sequence -> sequence % 5 == 0 ? "Data event " + sequence : ": heartbeat"); 
                   // Send actual data every 5th event, heartbeat otherwise
    }
}
```

**Explanation**:
- The server sends heartbeat messages (`: heartbeat`) every 15 seconds to keep the connection alive.
- Actual data is sent only when needed (e.g., every 5th event in this example).

---

#### **2. Client-Side: Automatic Reconnection**
Most **browsers** and **SSE libraries** automatically attempt to reconnect when the connection is dropped. You don't need to manually handle reconnections unless you're using a custom client.

##### Example: Browser-Based SSE Client
```javascript
const eventSource = new EventSource('http://localhost:8080/sse');

// Listen for messages
eventSource.onmessage = (event) => {
    console.log('Received:', event.data);
};

// Handle errors (e.g., connection loss)
eventSource.onerror = (error) => {
    console.error('Error:', error);
    // The browser will attempt reconnection automatically
};
```

- The browser automatically reconnects after a disconnection.
- The server can use the `Last-Event-ID` header to resume sending missed events.

##### Custom Reconnection Interval (Optional)
If you want to control the reconnection interval, you can set the `retry` field in the SSE stream sent by the server:

```java
@GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
public Flux<String> streamEvents() {
    return Flux.interval(Duration.ofSeconds(15))
        .map(sequence -> "retry: 5000\n" + "data: Event " + sequence + "\n\n"); // Retry every 5 seconds
}
```

- The browser will wait 5 seconds before attempting to reconnect.

---

#### **3. Custom Java Client for SSE with Manual Reconnection**
If you're building a custom SSE client in Java (e.g., using `HttpClient`), you need to handle reconnections manually since Java doesn’t have a built-in SSE API.

##### Example: Java Client with Reconnection
```java
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class SSEClient {
    private static final String SERVER_URL = "http://localhost:8080/sse";

    public static void main(String[] args) {
        HttpClient client = HttpClient.newHttpClient();
        connectToServer(client);
    }

    private static void connectToServer(HttpClient client) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SERVER_URL))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> {
                    response.body().lines().forEach(line -> {
                        System.out.println("Received: " + line);

                        // Handle reconnection logic if needed
                        if (line.contains("error")) {
                            System.out.println("Reconnecting...");
                            connectToServer(client);
                        }
                    });
                })
                .exceptionally(e -> {
                    System.out.println("Connection lost. Reconnecting...");
                    connectToServer(client); // Reconnect on failure
                    return null;
                });
    }
}
```

**Explanation**:
- The client attempts to reconnect if an exception occurs or an error message is received.
- The `connectToServer` method is recursively called to reopen the connection.

---

### **Best Practices for Handling Long Idle Times**

To ensure the connection remains usable even after long idle times:
1. **Send Heartbeats**:
    - Periodically send a comment (`:`) or dummy event from the server to prevent idle timeouts.
    - Example: `: heartbeat\n\n`.

2. **Configure Idle Timeouts**:
    - Adjust server timeouts to allow longer idle periods.
    - For Spring Boot with Tomcat, configure the `keep-alive-timeout`:
      ```properties
      server.tomcat.keep-alive-timeout=60000 # 60 seconds idle timeout
      ```

3. **Handle Reconnections Gracefully**:
    - Ensure the client automatically reconnects after disconnection.
    - Use the `Last-Event-ID` header to resume from where the client left off.

4. **Use Retry Intervals**:
    - Set a reconnection interval using the `retry` field in the server response.

---

### **Summary**
- If the server doesn't send data for a long time, the connection might close due to idle timeouts (client, server, or network).
- To prevent disconnection:
    - Send heartbeat messages periodically from the server.
    - Configure longer idle timeouts on the server.
- If disconnection happens:
    - The client should automatically reconnect (built-in for most browsers).
    - Use `Last-Event-ID` to resume from the last event after reconnecting.
- For custom clients, implement reconnection logic manually.

