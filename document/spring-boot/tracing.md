# URL Templating for Zipkin (Avoid High Cardinality)

You want to convert `/get/12345` → `/get/{userId}` to prevent **cardinality explosion** and **sensitive data exposure**.

---

## Solutions by Framework

### 1. **Spring Boot + Sleuth/Micrometer**

```java
@Configuration
public class TracingConfig {
    
    @Bean
    public ObservationConvention<ServerRequestObservationContext> customSpanNameProvider() {
        return new DefaultServerRequestObservationConvention() {
            @Override
            public String getName() {
                return "http.server.requests";
            }
            
            @Override
            public String getContextualName(ServerRequestObservationContext context) {
                // Use URI template pattern instead of actual path
                return context.getCarrier().getMethod() + " " + 
                       getPathPattern(context);
            }
            
            private String getPathPattern(ServerRequestObservationContext context) {
                // Returns /get/{userId} instead of /get/12345
                Object pattern = context.getCarrier()
                    .getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
                return pattern != null ? pattern.toString() : context.getCarrier().getRequestURI();
            }
        };
    }
}
```

### 2. **Spring Cloud Sleuth (Older versions)**

```yaml
spring:
  sleuth:
    web:
      client:
        # Use URI template
        skip-pattern: /health|/info
    http:
      legacy:
        enabled: false
```

```java
@Bean
public HttpClientRequestParser httpClientRequestParser() {
    return (request, context, span) -> {
        // Use route pattern
        String path = request.path();
        String templatedPath = path.replaceAll("/get/\\d+", "/get/{userId}")
                                   .replaceAll("/user/\\d+", "/user/{id}");
        span.name(request.method() + " " + templatedPath);
        span.tag("http.path", templatedPath);
    };
}
```

### 3. **OpenTelemetry Java Agent**

```java
@Bean
public SpanProcessor customSpanProcessor() {
    return new SpanProcessor() {
        @Override
        public void onEnd(ReadableSpan span) {
            // Already ended, can't modify
        }
        
        @Override
        public void onStart(Context parentContext, ReadWriteSpan span) {
            String name = span.getName();
            String sanitized = sanitizeUrl(name);
            span.updateName(sanitized);
        }
        
        private String sanitizeUrl(String url) {
            return url
                .replaceAll("/get/[0-9]+", "/get/{userId}")
                .replaceAll("/user/[a-zA-Z0-9-]+", "/user/{id}")
                .replaceAll("/order/[0-9]+", "/order/{orderId}");
        }
        
        @Override
        public boolean isStartRequired() { return true; }
        
        @Override
        public boolean isEndRequired() { return false; }
    };
}
```

### 4. **Using SpanHandler (Brave/Zipkin)**

```java
@Bean
public SpanHandler spanHandler() {
    return new SpanHandler() {
        @Override
        public boolean end(TraceContext context, MutableSpan span, Cause cause) {
            String name = span.name();
            if (name != null) {
                span.name(sanitizeUrl(name));
            }
            
            String path = span.tag("http.url");
            if (path != null) {
                span.tag("http.url", sanitizeUrl(path));
            }
            
            return true; // keep the span
        }
        
        private String sanitizeUrl(String url) {
            return url
                .replaceAll("/get/\\d+", "/get/{userId}")
                .replaceAll("/users/[^/]+", "/users/{id}");
        }
    };
}
```

---

## Quick Regex Patterns

| Pattern | Regex | Replacement |
|---------|-------|-------------|
| Numeric ID | `/\\d+` | `/{id}` |
| UUID | `/[a-f0-9-]{36}` | `/{uuid}` |
| Alphanumeric | `/[a-zA-Z0-9]+` | `/{id}` |
| Email in path | `/[^/]+@[^/]+` | `/{email}` |

---

## Recommended Approach

```java
@Component
public class UrlSanitizer {
    
    private static final List<PatternReplacement> PATTERNS = List.of(
        new PatternReplacement("/get/(\\d+)", "/get/{userId}"),
        new PatternReplacement("/user/([a-zA-Z0-9-]+)", "/user/{id}"),
        new PatternReplacement("/order/(\\d+)", "/order/{orderId}")
    );
    
    public String sanitize(String url) {
        String result = url;
        for (PatternReplacement pr : PATTERNS) {
            result = result.replaceAll(pr.pattern, pr.replacement);
        }
        return result;
    }
    
    record PatternReplacement(String pattern, String replacement) {}
}
```

---

## What's your setup?

1. **Spring Boot version?**
2. **Using Sleuth, Micrometer, or OpenTelemetry?**
3. **Zipkin reporter library?**

I can provide more specific configuration based on your stack.
