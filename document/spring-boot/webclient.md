# Reuse

In Spring Boot, you can configure `WebClient` to use HTTP connection pooling to reuse connections. Here are several approaches:

## 1. **Using Reactor Netty Connection Pool (Recommended)**

```java
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        // Configure connection pool
        ConnectionProvider connectionProvider = ConnectionProvider.builder("custom-pool")
                .maxConnections(500)           // Maximum connections in pool
                .maxIdleTime(Duration.ofSeconds(20))  // Max idle time
                .maxLifeTime(Duration.ofSeconds(60))  // Max connection lifetime
                .pendingAcquireTimeout(Duration.ofSeconds(60))  // Timeout waiting for connection
                .evictInBackground(Duration.ofSeconds(120))  // Background eviction interval
                .build();

        // Configure HttpClient with connection pool
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(5))
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS))
                );

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
```

## 2. **Simple Connection Pool Configuration**

```java
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("http-pool")
                .maxConnections(100)
                .pendingAcquireMaxCount(500)
                .pendingAcquireTimeout(Duration.ofSeconds(45))
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
```

## 3. **With Keep-Alive Configuration**

```java
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        ConnectionProvider provider = ConnectionProvider.builder("keep-alive-pool")
                .maxConnections(50)
                .maxIdleTime(Duration.ofSeconds(30))
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.SO_KEEPALIVE, true)  // Enable TCP keep-alive
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
```

## 4. **Per-Host Connection Pool**

```java
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient apiClient() {
        // Separate pool for specific API
        ConnectionProvider provider = ConnectionProvider.builder("api-pool")
                .maxConnections(200)  // Per host
                .pendingAcquireMaxCount(1000)
                .build();

        HttpClient httpClient = HttpClient.create(provider);

        return WebClient.builder()
                .baseUrl("https://api.example.com")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
```

## 5. **Using Default Connection Provider (Simplest)**

```java
@Configuration
public class WebClientConfig {

    @Bean
    public WebClient webClient() {
        // Uses default connection pool with sensible defaults
        HttpClient httpClient = HttpClient.create();

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
```

## 6. **Multiple WebClients with Different Pools**

```java
@Configuration
public class WebClientConfig {

    @Bean
    @Qualifier("internalApiClient")
    public WebClient internalApiClient() {
        ConnectionProvider provider = ConnectionProvider.builder("internal-pool")
                .maxConnections(100)
                .build();

        HttpClient httpClient = HttpClient.create(provider);

        return WebClient.builder()
                .baseUrl("http://internal-service")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    @Bean
    @Qualifier("externalApiClient")
    public WebClient externalApiClient() {
        ConnectionProvider provider = ConnectionProvider.builder("external-pool")
                .maxConnections(50)
                .maxIdleTime(Duration.ofSeconds(20))
                .build();

        HttpClient httpClient = HttpClient.create(provider);

        return WebClient.builder()
                .baseUrl("https://external-api.com")
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
```

## 7. **Production-Ready Configuration**

```java
@Configuration
public class WebClientConfig {

    @Value("${webclient.pool.max-connections:500}")
    private int maxConnections;

    @Value("${webclient.pool.max-idle-time:20}")
    private int maxIdleTime;

    @Value("${webclient.timeout.connect:5000}")
    private int connectTimeout;

    @Value("${webclient.timeout.response:10}")
    private int responseTimeout;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        ConnectionProvider connectionProvider = ConnectionProvider.builder("production-pool")
                .maxConnections(maxConnections)
                .maxIdleTime(Duration.ofSeconds(maxIdleTime))
                .maxLifeTime(Duration.ofMinutes(5))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .metrics(true)  // Enable metrics
                .build();

        HttpClient httpClient = HttpClient.create(connectionProvider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .responseTimeout(Duration.ofSeconds(responseTimeout))
                .doOnConnected(conn -> 
                    conn.addHandlerLast(new ReadTimeoutHandler(responseTimeout, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(responseTimeout, TimeUnit.SECONDS))
                )
                .compress(true);  // Enable compression

        return builder
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
```

## Configuration Properties (application.yml)

```yaml
webclient:
  pool:
    max-connections: 500
    max-idle-time: 20
    max-lifetime: 300
  timeout:
    connect: 5000
    response: 10
    read: 10
    write: 10
```

## Usage Example

```java
@Service
public class ApiService {

    private final WebClient webClient;

    public ApiService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<User> getUser(Long id) {
        return webClient.get()
                .uri("/users/{id}", id)
                .retrieve()
                .bodyToMono(User.class);
    }

    // Connection is reused from pool automatically
    public Flux<User> getAllUsers() {
        return webClient.get()
                .uri("/users")
                .retrieve()
                .bodyToFlux(User.class);
    }
}
```

## Key Connection Pool Parameters:

| Parameter | Description | Default | Recommendation |
|-----------|-------------|---------|----------------|
| `maxConnections` | Max connections per pool | 500 | 100-500 based on load |
| `maxIdleTime` | How long idle connection stays in pool | 20s | 20-60s |
| `maxLifeTime` | Max time connection can exist | ∞ | 5-10 minutes |
| `pendingAcquireTimeout` | Max wait time for connection | 45s | 30-60s |
| `pendingAcquireMaxCount` | Max pending requests | 1000 | 500-2000 |

## Dependencies (pom.xml)

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
</dependency>

<!-- For connection pooling metrics -->
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-core</artifactId>
</dependency>
```

## Benefits:

✅ **Reuses TCP connections** - No handshake overhead  
✅ **Reduces latency** - Connections ready immediately  
✅ **Improves throughput** - More concurrent requests  
✅ **Resource efficient** - Limits total connections  
✅ **Auto cleanup** - Evicts stale connections

The **recommended approach** is **#7 (Production-Ready Configuration)** as it provides configurable pooling with proper timeouts and monitoring.