# Get Http Request

To access an HTTP request anywhere in a **Spring Boot** application, you can use **`RequestContextHolder`** with `HttpServletRequest`. Here's how you can achieve this:

### 1. Access the `HttpServletRequest` using `RequestContextHolder`
Spring Boot provides `RequestContextHolder`, which allows you to get details of the current HTTP request.

Here’s how you can do it:

```java
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;

@Component
public class HttpRequestHelper {

    public HttpServletRequest getCurrentHttpRequest() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null; // No request context is available
        }
        return (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
    }
}
```

### 2. Use the `HttpRequestHelper` wherever needed
You can now inject the `HttpRequestHelper` and use `getCurrentHttpRequest` to access the currently active HTTP request.

For example:

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class SomeService {

    @Autowired
    private HttpRequestHelper httpRequestHelper;

    public void doSomethingWithRequest() {
        HttpServletRequest request = httpRequestHelper.getCurrentHttpRequest();
        if (request != null) {
            String currentUri = request.getRequestURI();
            System.out.println("Current request URI: " + currentUri);
        } else {
            System.out.println("No HTTP request is available.");
        }
    }
}
```

### 3. Additional Notes
- **Thread Safety:** Ensure you’re accessing the request within a thread that handles the HTTP request. Otherwise, you may encounter `null` values (e.g., in background threads).
- **Alternative for Controllers:** For controller-specific logic, you can use the `@RequestScope` or directly inject `HttpServletRequest` (e.g., as a method parameter).

### Example Controller
Here's an example that fetches the HTTP request in a controller using `HttpRequestHelper`:

```java
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class ExampleController {

    @Autowired
    private HttpRequestHelper httpRequestHelper;

    @GetMapping("/test-request")
    public String getHttpRequestDetails() {
        HttpServletRequest request = httpRequestHelper.getCurrentHttpRequest();
        return request != null
                ? "Request URI: " + request.getRequestURI()
                : "No Active Request";
    }
}
```

### 4. Testing in Background Threads
If background threads need access to the request, you can pass the current HTTP request manually as a context parameter, since `RequestContextHolder` only works for the main thread handling the request. You can achieve this by wrapping the request or using Spring's **`@Async`** or similar mechanisms carefully.

---

By following this structure, you can conveniently access the HTTP request details from anywhere in your Spring Boot application!

