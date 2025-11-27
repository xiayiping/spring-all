# boot mvc gateway customer header

In **Spring Cloud Gateway (MVC)**, you can create a custom filter like `SetRequestHeader` to modify or add headers to the request, and then configure it in `application.yml`. While Spring Cloud Gateway (WebFlux) has built-in filters like `SetRequestHeader`, in Spring MVC Gateway, you need to create your own filter and register it programmatically. Here's how you can achieve this:

---

## **1. Create a Custom `SetRequestHeader` Filter**

You can create a custom filter by implementing `HandlerInterceptor`. This filter will allow you to set headers on the incoming request.

### Example Code: Custom `SetRequestHeaderFilter`

```java
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class SetRequestHeaderFilter implements HandlerInterceptor {

    private final String headerName;
    private final String headerValue;

    // Constructor to initialize header name and value
    public SetRequestHeaderFilter(String headerName, String headerValue) {
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Add or modify the header on the request
        request.setAttribute(headerName, headerValue);
        return true; // Continue the request lifecycle
    }
}
```

---

## **2. Configure the Filter in `WebMvcConfig`**

Next, register your custom filter in Spring MVC Gateway's `InterceptorRegistry` programmatically. You can also load the header name and value from `application.yml`.

### Example Code: Register Filter in `WebMvcConfig`
```java
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${custom.filter.header-name}")
    private String headerName;

    @Value("${custom.filter.header-value}")
    private String headerValue;

    @Bean
    public SetRequestHeaderFilter setRequestHeaderFilter() {
        return new SetRequestHeaderFilter(headerName, headerValue);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Add the custom filter to the registry
        registry.addInterceptor(setRequestHeaderFilter());
    }
}
```

---

## **3. Configure the Header in `application.yml`**

Define the header name and value in `application.yml` so that it can be configured without modifying the code.

### Example `application.yml`
```yaml
custom:
  filter:
    header-name: X-Custom-Header
    header-value: MyCustomValue
```

In this example:
- `X-Custom-Header` is the name of the header to be set.
- `MyCustomValue` is the value to assign to the header.

---

## **4. Adding the Header to Downstream Requests**

To ensure the header is forwarded to downstream services, modify the `HttpServletRequest` object in the filter. Since `HttpServletRequest` is immutable, you need to use a wrapper.

### Example: Modify the Filter to Add Headers
Use `HttpServletRequestWrapper` to add or modify headers.

```java
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;

public class SetRequestHeaderFilter implements HandlerInterceptor {

    private final String headerName;
    private final String headerValue;

    public SetRequestHeaderFilter(String headerName, String headerValue) {
        this.headerName = headerName;
        this.headerValue = headerValue;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request) {
            @Override
            public String getHeader(String name) {
                if (name.equalsIgnoreCase(headerName)) {
                    return headerValue;
                }
                return super.getHeader(name);
            }
        };

        // Pass modified request along the filter chain
        request.setAttribute("wrappedRequest", requestWrapper);
        return true;
    }
}
```

---

## **5. Dynamically Configure Routes in `application.yml`**

To apply your custom header logic for specific routes, you can define route-specific filters and conditions in `application.yml`. Use a service-level configuration to enable or disable the filter for specific routes.

### Example `application.yml` for Routes
```yaml
spring:
  mvc:
    gateway:
      routes:
        - id: route1
          uri: http://localhost:8081
          predicates:
            - Path=/api/service1/**
          filters:
            - SetRequestHeader=X-Custom-Header,MyCustomValue
        - id: route2
          uri: http://localhost:8082
          predicates:
            - Path=/api/service2/**
          filters:
            - SetRequestHeader=X-Another-Header,AnotherValue
```

---

## **6. Dynamically Apply the Filter Based on Routes**
To dynamically support route-based filtering, create a filter registry that reads the configuration dynamically.

### Example: Filter Registry
```java
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FilterRegistry {

    private final Map<String, SetRequestHeaderFilter> filters = new HashMap<>();

    public void addFilter(String routeId, SetRequestHeaderFilter filter) {
        filters.put(routeId, filter);
    }

    public SetRequestHeaderFilter getFilter(String routeId) {
        return filters.get(routeId);
    }
}
```

---

### Example: Dynamic Filter Configuration
Register filters dynamically based on route configuration.

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class DynamicWebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private FilterRegistry filterRegistry;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> routeIds = List.of("route1", "route2"); // Load from application.yml or DB
        for (String routeId : routeIds) {
            SetRequestHeaderFilter filter = filterRegistry.getFilter(routeId);
            if (filter != null) {
                registry.addInterceptor(filter);
            }
        }
    }
}
```

---

## **7. Test the Filter**

1. Start your Spring MVC Gateway application.
2. Make a request to the gateway for a specific route (e.g., `/api/service1/endpoint`).
3. Verify the `X-Custom-Header` is added to the request sent to the downstream service.

### Example cURL Command
```bash
curl -H "Authorization: Bearer <token>" http://localhost:8080/api/service1/endpoint
```

---

## **8. Summary**
- Implement a custom `SetRequestHeader` filter using `HandlerInterceptor` or `HttpServletRequestWrapper`.
- Dynamically configure the filter via `application.yml` for different routes.
- Optionally, use a `FilterRegistry` to manage filters dynamically for specific routes.
- Test to ensure the headers are set properly based on route configurations.

Let me know if you need further clarification or assistance!