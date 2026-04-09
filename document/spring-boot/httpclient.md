# setup http client 

To configure connection pools, idle sizes, and other connection management settings for `org.springframework.web.client.RestClient` in **Spring Boot 4**, you need to customize the `ClientHttpRequestFactory` that underpins the `RestClient` itself. Here's how you can properly set up a **connection pool** while using `RestClient`. The approach depends on the type of HTTP client implementation you choose: commonly either **Apache HttpComponents (HttpClient)** or **Reactor Netty**.

Let’s tailor it to your `RestClient` setup explicitly.

---

## **Step-by-Step Solution for Pooling and Configuration**

---

### **1. Using Apache HttpComponents (Blocking Client with Connection Pool)**

Apache's `HttpClient` is a popular choice for synchronous/HTTP blocking requests. Below, we configure the connection pool size, idle configurations, and connection management settings with **Apache HttpClient** in a `RestClient` setup.

#### Add Apache HttpClient dependency:

**Maven**:
```xml
<dependency>
    <groupId>org.apache.httpcomponents.client5</groupId>
    <artifactId>httpclient5</artifactId>
</dependency>
```

#### Code for Configuring Pooled `RestClient`:

```java
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

public class PooledRestClientConfig {

    public static RestClient createRestClient(String baseUrl) {
        // Configure connection pooling
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(50); // Max total connections
        connectionManager.setDefaultMaxPerRoute(20); // Max connections per specific route
        connectionManager.setValidateAfterInactivity(TimeValue.ofSeconds(5)); // Validate idle connections
        connectionManager.closeIdle(TimeValue.ofSeconds(15)); // Remove idle connections after 15 seconds
        connectionManager.closeExpired(); // Remove expired connections

        // Build HttpClient with the connection pool
        CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(connectionManager)
            .setConnectionTimeToLive(TimeValue.ofMinutes(5)) // Max lifetime for a connection
            .build();

        // Create a Spring HttpRequestFactory using HttpComponents
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        requestFactory.setConnectTimeout(Duration.ofSeconds(5)); // Connection timeout
        requestFactory.setReadTimeout(Duration.ofSeconds(10)); // Read timeout

        // Build the RestClient
        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory) // Register the HttpRequestFactory
                .build();
    }
}
```

---

#### **Explanation**:
1. **Connection Pooling**:
    - `setMaxTotal`: Pool-wide maximum number of connections.
    - `setDefaultMaxPerRoute`: Maximum connections allowed per route (a single host or endpoint).

2. **Idle Connection Handling**:
    - `closeIdle`: Idle connections removed after 15 seconds.
    - `validateAfterInactivity`: Ensure idle connections are verified before usage.

3. **Time Management**:
    - `setConnectionTimeToLive`: Ensures connections live for a maximum duration, even if active.

4. **Timeouts**:
    - `setConnectTimeout`: For establishing a connection.
    - `setReadTimeout`: For reading the data.

---

### **2. Using Reactor Netty (Non-blocking Connection Pool)**

The **`RestClient`** can also use a **Reactor Netty** HTTP client under the hood. Reactor Netty is event-loop-based and highly configurable for reactive/non-blocking applications.

#### Add Reactor Netty dependency:

**Maven**:
```xml
<dependency>
    <groupId>io.projectreactor.netty</groupId>
    <artifactId>reactor-netty-http</artifactId>
</dependency>
```

#### Code for Configuring a Non-blocking, Pooled `RestClient`:

```java
import reactor.netty.resources.ConnectionProvider;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;

import java.time.Duration;

public class PooledReactorRestClientConfig {

    public static RestClient createRestClient(String baseUrl) {
        // Configure a connection provider for the pool
        ConnectionProvider connectionProvider = ConnectionProvider.builder("custom-pool")
            .maxConnections(50) // Max total connections
            .maxIdleTime(Duration.ofSeconds(10)) // Max idle time for a connection in the pool
            .maxLifeTime(Duration.ofMinutes(5)) // Maximum lifetime for a connection
            .pendingAcquireTimeout(Duration.ofSeconds(5)) // Wait time to acquire a connection
            .build();

        // Build the Reactor Netty HttpClient with the connection pool
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(Duration.ofSeconds(10)); // Configure response timeout

        // Build the RestClient
        return RestClient.builder()
            .baseUrl(baseUrl)
            .httpConnector(new ReactorClientHttpConnector(httpClient)) // Specify the custom HttpClient
            .build();
    }
}
```

---

#### **Explanation**:
1. **Connection Management**:
    - `maxConnections`: Total number of concurrent connections across the pool.
    - `maxIdleTime`: Maximum time idle connections are kept before being evicted.
    - `maxLifeTime`: Maximum lifetime for any active connection.
    - `pendingAcquireTimeout`: Maximum wait time to acquire a pooled connection if none are available.

2. **Reactive Customizations**:
    - `responseTimeout`: Ensures client-side timeouts for response retrievals.

---

### **Choosing Between Apache HttpComponents and Reactor Netty**

| **Feature**                  | **Apache HttpComponents**                     | **Reactor Netty**                  |
|------------------------------|-----------------------------------------------|------------------------------------|
| **Type**                     | Blocking (Synchronous)                       | Non-blocking (Asynchronous)       |
| **Preferred for**            | Classic synchronous REST clients             | Reactive Spring apps              |
| **Advanced Pooling Support** | Yes                                          | Yes                               |
| **Dependencies**             | `httpclient5`                                | `reactor-netty-http`              |
| **Ease of Use with RestClient** | Easy to set up with `RestClient`            | Fully supported                   |

---

### **3. Testing and Debugging Connection Pool**

To ensure that your connection pool configurations are working correctly:

1. **Enable Logging**:
    - Add logging levels to capture HTTP client activity:
      ```properties
      # For Apache HttpClient
      logging.level.org.apache.hc.client5.http=DEBUG
      logging.level.org.apache.hc.client5.http.wire=DEBUG
 
      # For Reactor Netty
      logging.level.reactor.netty.http.client=DEBUG
      logging.level.reactor.netty=DEBUG
      ```

2. **Run Load Tests**: Use tools like **Postman** or **Apache JMeter** to verify that the connection pool behaves as expected under load.

---

### **Summary**

1. **Use `HttpComponentsClientHttpRequestFactory` for synchronous/blocking HttpClient**:
    - Add Apache HttpClient dependency.
    - Configure `PoolingHttpClientConnectionManager`.

2. **Use `ReactorClientHttpConnector` for reactive/non-blocking HttpClient**:
    - Add `reactor-netty-http` dependency.
    - Configure the `ConnectionProvider` for advanced connection pooling.

3. **Integrate with `RestClient.Builder`**:
    - Use `.requestFactory()` for `HttpComponent`.
    - Use `.httpConnector()` for `Reactor Netty`.

Select either **HttpComponents** (for synchronous calls) or **Reactor Netty** (for reactive apps), depending on your needs.

Let me know if you need help configuring additional features! 😊

