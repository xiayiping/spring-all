feign different loadbalancer map to different config:

https://github.com/KurdTt/feign_client_ssl_example/blob/master/client-feign/src/main/java/com/example/feign/client/common/DefaultLoadBalancerConfiguration.java


I got trustAnchor empty problem, probably because I didn't set

```shell

java -Djavax.net.ssl.trustStore=/some/loc/on/server/our_truststore.jks /
  -Djavax.net.ssl.trustStorePassword=our_password /
  -jar application.jar

## D:/develop/spring-all/echo/src/main/resources/keystore/certstore.p12
```

https://www.baeldung.com/java-trustanchors-parameter-must-be-non-empty

The client for LoadBalancer is a ```FeignBlockingLoadBalancerClient```,
which wraps a normal HttpClient, here I'll use ApacheHttp5Client

according to ```HttpClient5FeignConfiguration```,
we should override```ClosableHttpClient```,
so that the underlying client of LB client will be overridden.


# Error handler

To handle responses in Feign clients when the HTTP status is **non-2xx** (e.g., `4xx`, `5xx`), especially when those responses follow a format like the **RFC 7807 Problem Details** (formerly RFC 9457), you need to do the following:

1. **Configure Feign to Handle Non-2xx Responses Elegantly**: By default, Feign throws exceptions for non-2xx responses. You can customize this behavior to parse and handle error responses.

2. **Map the Error Response to a Java Object**: Since RFC 7807 uses a structured format, you can use a POJO to deserialize the problem detail JSON.

3. **Customize Feign Error Decoding**: Use a custom `ErrorDecoder` to map non-2xx responses to your custom exception or directly return the problem details as a Java object.

Below is a step-by-step approach to implementing this:

---

### **1. RFC 7807 (Problem Details) Example**
An RFC 7807-compliant error response might look like this:

```json
{
  "type": "https://example.com/problem",
  "title": "Unauthorized Access",
  "status": 401,
  "detail": "You must authenticate to access this resource",
  "instance": "/api/resource/123"
}
```

This structured JSON provides details about the error (`status`, `type`, `title`, etc.).

---

### **2. Define a Problem Details POJO**

Create a POJO to represent the structure of the problem detail JSON:

```java
public class ProblemDetail {

    private String type;     // URL identifying the problem type
    private String title;    // Short summary/title of the problem
    private int status;      // HTTP status code
    private String detail;   // Human-readable explanation
    private String instance; // URL/path of the specific instance

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getInstance() {
        return instance;
    }

    public void setInstance(String instance) {
        this.instance = instance;
    }

    @Override
    public String toString() {
        return "ProblemDetail{" +
                "type='" + type + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", detail='" + detail + '\'' +
                ", instance='" + instance + '\'' +
                '}';
    }
}
```

---

### **3. Create a Custom Feign ErrorDecoder**

Feign allows you to define a custom `ErrorDecoder` to intercept and process non-2xx responses.

Here’s how to implement one for `ProblemDetail`:

```java
import feign.Response;
import feign.codec.ErrorDecoder;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

// Custom Error Decoder Implementation
public class CustomErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Exception decode(String methodKey, Response response) {
        try {
            // Parse the ProblemDetail object from the body
            ProblemDetail problemDetail = objectMapper
                    .readValue(response.body().asInputStream(), ProblemDetail.class);

            // Return a custom exception including the problem detail
            return new FeignClientException(problemDetail);
        } catch (IOException e) {
            // Handle errors gracefully
            return new Exception("Failed to decode error response: " + e.getMessage(), e);
        }
    }
}
```

Here, the `decode` method reads the error response body and maps it to a `ProblemDetail` object using Jackson. You can then encapsulate this detail into a custom exception (e.g., `FeignClientException`).

---

### **4. Define a Custom Exception**

Create a custom exception to contain the `ProblemDetail`:

```java
public class FeignClientException extends RuntimeException {

    private final ProblemDetail problemDetail;

    public FeignClientException(ProblemDetail problemDetail) {
        super(problemDetail.getTitle()); // You can include the title in the exception
        this.problemDetail = problemDetail;
    }

    public ProblemDetail getProblemDetail() {
        return problemDetail;
    }
}
```

---

### **5. Set the Custom ErrorDecoder in Feign Client**

When defining your Feign client, configure it to use the custom `ErrorDecoder`.

#### Using Spring Boot:
With Feign configured in Spring Boot, you can provide a `Bean` for the `ErrorDecoder`:

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public ErrorDecoder errorDecoder() {
        return new CustomErrorDecoder();
    }
}
```

---

### **6. Define Your Feign Client**

Define your Feign client interface as usual:

```java
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "exampleClient", url = "https://example.com", configuration = FeignConfig.class)
public interface ExampleClient {

    @GetMapping("/api/resource/{id}")
    String getResourceById(@PathVariable("id") String id);
}
```

If the server responds with a non-2xx status code, the `CustomErrorDecoder` processes the error and throws a `FeignClientException` instead of the default Feign exception.

---

### **7. Handling the Custom Exception**

In your application code where you call the Feign client, you can catch and handle the `FeignClientException`:

```java
@Service
public class ExampleService {

    private final ExampleClient exampleClient;

    public ExampleService(ExampleClient exampleClient) {
        this.exampleClient = exampleClient;
    }

    public String getResource(String id) {
        try {
            return exampleClient.getResourceById(id);
        } catch (FeignClientException e) {
            // Handle the custom exception
            ProblemDetail problemDetail = e.getProblemDetail();
            System.err.println("API Error: " + problemDetail);
            throw e; // rethrow or handle gracefully
        }
    }
}
```

---

### **8. Key Benefits**

- **Seamless Error Handling**: Your application gets structured error details for non-2xx responses.
- **Single Exception Type**: Centralized via `FeignClientException`, so you don't handle raw `FeignException` anymore.
- **Flexibility with RFC 7807**: Any API adhering to the Problem Details format will integrate cleanly.

---

### **9. Test the Implementation**

Write a test case where the server responds with a `4xx` or `5xx` error and verify that the `ProblemDetail` object is correctly deserialized, and the appropriate exception is thrown.

For example, mock an error response using tools like **WireMock** or **MockServer**.

---

### Complete Flow Recap:

1. **Feign Client requests a resource**.
2. The server responds with a `4xx` or `5xx` HTTP status and a **Problem Details JSON** body.
3. Feign passes the response to a **custom `ErrorDecoder`**.
4. The `ErrorDecoder` deserializes the JSON into a `ProblemDetail` POJO.
5. It throws a `FeignClientException` containing the problem details, which the application can catch and handle.

With this setup, your Feign client is now fully capable of handling structured non-2xx responses gracefully!