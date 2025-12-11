Ah! You want to **aggregate all microservices' Swagger documentation in the API Gateway**. Here's how:

## Solution 1: SpringDoc Gateway Integration (Best & Easiest)

### API Gateway Configuration

**pom.xml**
```xml
<dependencies>
    <!-- Spring Cloud Gateway -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    
    <!-- SpringDoc for Gateway -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
        <version>2.3.0</version>
    </dependency>
</dependencies>
```

**application.yml**
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        # User Service
        - id: user-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/users/**
          filters:
            - RewritePath=/api/users/(?<segment>.*), /${segment}

        # Product Service
        - id: product-service
          uri: http://localhost:8082
          predicates:
            - Path=/api/products/**
          filters:
            - RewritePath=/api/products/(?<segment>.*), /${segment}

        # Order Service
        - id: order-service
          uri: http://localhost:8083
          predicates:
            - Path=/api/orders/**
          filters:
            - RewritePath=/api/orders/(?<segment>.*), /${segment}

# SpringDoc configuration - Aggregate all services
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    # List all microservices here
    urls:
      - name: User Service
        url: /api/users/v3/api-docs
      - name: Product Service
        url: /api/products/v3/api-docs
      - name: Order Service
        url: /api/orders/v3/api-docs
    # Show all services on load
    urls-primary-name: User Service
```

### User Service (8081)

**pom.xml**
```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.3.0</version>
</dependency>
```

**application.yml**
```yaml
server:
  port: 8081

spring:
  application:
    name: user-service

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

**OpenApiConfig.java**
```java
package com.example.userservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceAPI() {
        final String securitySchemeName = "Bearer Authentication";
        
        return new OpenAPI()
            .info(new Info()
                .title("User Service API")
                .version("1.0")
                .description("User management and authentication"))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080/api/users")
                    .description("API Gateway (Use this)"),
                new Server()
                    .url("http://localhost:8081")
                    .description("Direct Access")
            ))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

### Product Service (8082)

**application.yml**
```yaml
server:
  port: 8082

spring:
  application:
    name: product-service

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

**OpenApiConfig.java**
```java
package com.example.productservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI productServiceAPI() {
        final String securitySchemeName = "Bearer Authentication";
        
        return new OpenAPI()
            .info(new Info()
                .title("Product Service API")
                .version("1.0")
                .description("Product catalog and inventory management"))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080/api/products")
                    .description("API Gateway (Use this)"),
                new Server()
                    .url("http://localhost:8082")
                    .description("Direct Access")
            ))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

### Order Service (8083)

**application.yml**
```yaml
server:
  port: 8083

spring:
  application:
    name: order-service

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
```

**OpenApiConfig.java**
```java
package com.example.orderservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderServiceAPI() {
        final String securitySchemeName = "Bearer Authentication";
        
        return new OpenAPI()
            .info(new Info()
                .title("Order Service API")
                .version("1.0")
                .description("Order processing and management"))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080/api/orders")
                    .description("API Gateway (Use this)"),
                new Server()
                    .url("http://localhost:8083")
                    .description("Direct Access")
            ))
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
```

---

## Solution 2: Auto-Discovery (Dynamic - Best for Many Services)

### API Gateway with Auto-Discovery

**SwaggerConfig.java**
```java
package com.example.gateway.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class SwaggerConfig {

    @Autowired
    private RouteDefinitionLocator locator;

    @Bean
    public List<GroupedOpenApi> apis() {
        List<GroupedOpenApi> groups = new ArrayList<>();
        List<RouteDefinition> definitions = locator.getRouteDefinitions().collectList().block();
        
        definitions.stream()
            .filter(routeDefinition -> routeDefinition.getId().matches(".*-service"))
            .forEach(routeDefinition -> {
                String name = routeDefinition.getId().replaceAll("-service", "");
                groups.add(GroupedOpenApi.builder()
                    .pathsToMatch("/" + name + "/**")
                    .group(name)
                    .build());
            });
        
        return groups;
    }
}
```

**application.yml**
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/users/**
          filters:
            - RewritePath=/api/users/(?<segment>.*), /${segment}
        - id: product-service
          uri: http://localhost:8082
          predicates:
            - Path=/api/products/**
          filters:
            - RewritePath=/api/products/(?<segment>.*), /${segment}
        - id: order-service
          uri: http://localhost:8083
          predicates:
            - Path=/api/orders/**
          filters:
            - RewritePath=/api/orders/(?<segment>.*), /${segment}

springdoc:
  swagger-ui:
    path: /swagger-ui.html
    urls:
      - name: User Service
        url: /api/users/v3/api-docs
      - name: Product Service
        url: /api/products/v3/api-docs
      - name: Order Service
        url: /api/orders/v3/api-docs
```

---

## Solution 3: With Service Discovery (Eureka/Consul)

### API Gateway with Eureka

**pom.xml**
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
        <version>2.3.0</version>
    </dependency>
</dependencies>
```

**application.yml**
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      discovery:
        locator:
          enabled: true
          lower-case-service-id: true
      routes:
        - id: user-service
          uri: lb://user-service
          predicates:
            - Path=/api/users/**
          filters:
            - RewritePath=/api/users/(?<segment>.*), /${segment}
        - id: product-service
          uri: lb://product-service
          predicates:
            - Path=/api/products/**
          filters:
            - RewritePath=/api/products/(?<segment>.*), /${segment}
        - id: order-service
          uri: lb://order-service
          predicates:
            - Path=/api/orders/**
          filters:
            - RewritePath=/api/orders/(?<segment>.*), /${segment}

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/

springdoc:
  swagger-ui:
    path: /swagger-ui.html
    urls:
      - name: User Service
        url: /api/users/v3/api-docs
      - name: Product Service
        url: /api/products/v3/api-docs
      - name: Order Service
        url: /api/orders/v3/api-docs
```

**SwaggerConfig.java** (Auto-discovery from Eureka)
```java
package com.example.gateway.config;

import org.springdoc.core.properties.SwaggerUiConfigParameters;
import org.springframework.cloud.gateway.route.RouteDefinitionLocator;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class SwaggerConfig {

    private final RouteDefinitionLocator locator;
    private final SwaggerUiConfigParameters swaggerUiParameters;

    public SwaggerConfig(RouteDefinitionLocator locator, SwaggerUiConfigParameters swaggerUiParameters) {
        this.locator = locator;
        this.swaggerUiParameters = swaggerUiParameters;
    }

    @PostConstruct
    public void init() {
        locator.getRouteDefinitions()
            .filter(routeDefinition -> routeDefinition.getId().matches(".*-service"))
            .subscribe(routeDefinition -> {
                String name = routeDefinition.getId();
                String url = routeDefinition.getUri().toString()
                    .replace("lb://", "/api/")
                    .replace("-service", "s") + "/v3/api-docs";
                swaggerUiParameters.addGroup(name, url);
            });
    }
}
```

---

## Solution 4: Custom Aggregator Endpoint

**GatewaySwaggerController.java**
```java
package com.example.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
public class GatewaySwaggerController {

    @Autowired
    private WebClient.Builder webClientBuilder;

    @GetMapping("/v3/api-docs")
    public Mono<ResponseEntity<Map<String, Object>>> getAggregatedDocs() {
        Map<String, Object> aggregatedDocs = new HashMap<>();
        
        // Fetch from all services
        Mono<String> userDocs = webClientBuilder.build()
            .get()
            .uri("http://localhost:8081/v3/api-docs")
            .retrieve()
            .bodyToMono(String.class);
            
        Mono<String> productDocs = webClientBuilder.build()
            .get()
            .uri("http://localhost:8082/v3/api-docs")
            .retrieve()
            .bodyToMono(String.class);
            
        Mono<String> orderDocs = webClientBuilder.build()
            .get()
            .uri("http://localhost:8083/v3/api-docs")
            .retrieve()
            .bodyToMono(String.class);
        
        return Mono.zip(userDocs, productDocs, orderDocs)
            .map(tuple -> {
                aggregatedDocs.put("user-service", tuple.getT1());
                aggregatedDocs.put("product-service", tuple.getT2());
                aggregatedDocs.put("order-service", tuple.getT3());
                return ResponseEntity.ok(aggregatedDocs);
            });
    }
}
```

---

## Solution 5: Complete Working Example

### API Gateway - Complete Configuration

**application.yml**
```yaml
server:
  port: 8080

spring:
  application:
    name: api-gateway
  cloud:
    gateway:
      routes:
        # User Service Routes
        - id: user-service-docs
          uri: http://localhost:8081
          predicates:
            - Path=/api/users/v3/api-docs
          filters:
            - RewritePath=/api/users/v3/api-docs, /v3/api-docs
            
        - id: user-service
          uri: http://localhost:8081
          predicates:
            - Path=/api/users/**
          filters:
            - RewritePath=/api/users/(?<segment>.*), /${segment}

        # Product Service Routes
        - id: product-service-docs
          uri: http://localhost:8082
          predicates:
            - Path=/api/products/v3/api-docs
          filters:
            - RewritePath=/api/products/v3/api-docs, /v3/api-docs
            
        - id: product-service
          uri: http://localhost:8082
          predicates:
            - Path=/api/products/**
          filters:
            - RewritePath=/api/products/(?<segment>.*), /${segment}

        # Order Service Routes
        - id: order-service-docs
          uri: http://localhost:8083
          predicates:
            - Path=/api/orders/v3/api-docs
          filters:
            - RewritePath=/api/orders/v3/api-docs, /v3/api-docs
            
        - id: order-service
          uri: http://localhost:8083
          predicates:
            - Path=/api/orders/**
          filters:
            - RewritePath=/api/orders/(?<segment>.*), /${segment}

# Aggregate all services' Swagger docs
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    # Dropdown will show these options
    urls:
      - name: User Service
        url: /api/users/v3/api-docs
        display-name: User Service (Authentication & Users)
      - name: Product Service
        url: /api/products/v3/api-docs
        display-name: Product Service (Catalog & Inventory)
      - name: Order Service
        url: /api/orders/v3/api-docs
        display-name: Order Service (Orders & Payments)
    # Default service to show
    urls-primary-name: User Service
    # Enable filtering
    filter: true
    # Try it out enabled by default
    try-it-out-enabled: true
```

### How It Works

1. **Start all services:**
    - User Service: `http://localhost:8081`
    - Product Service: `http://localhost:8082`
    - Order Service: `http://localhost:8083`
    - API Gateway: `http://localhost:8080`

2. **Access Gateway Swagger UI:**
   ```
   http://localhost:8080/swagger-ui.html
   ```

3. **You'll see a dropdown** at the top right with:
    - User Service (Authentication & Users)
    - Product Service (Catalog & Inventory)
    - Order Service (Orders & Payments)

4. **Select any service** from the dropdown to see its APIs

5. **All APIs are accessible** through the Gateway URL:
    - `http://localhost:8080/api/users/...`
    - `http://localhost:8080/api/products/...`
    - `http://localhost:8080/api/orders/...`

### Result

You now have:
- ✅ **Single Swagger UI** at API Gateway
- ✅ **All microservices' APIs** in one place
- ✅ **Dropdown to switch** between services
- ✅ **JWT authentication** works across all services
- ✅ **Try it out** functionality for testing
- ✅ **All requests go through** API Gateway

Access: `http://localhost:8080/swagger-ui.html` and you'll see all your microservices in one unified Swagger documentation! 🎉