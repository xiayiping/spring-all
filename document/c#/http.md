# consume SSE

To consume **Server-Sent Events (SSE)** in C# from a Spring Boot server, you can use the **`HttpClient`** class in combination with a streaming mechanism to process the incoming data.

---

### **Step-by-Step Guide to Consuming SSE in C#**

1. **Understand the SSE Protocol**:
    - SSE uses a single HTTP connection where the server streams updates to the client.
    - Each message is sent in the following format:
      ```
      data: <your-message-data>
      ```
    - Messages are separated by a double newline (`\n\n`).

2. **Set Up the Spring Boot SSE Endpoint**:
   Ensure the Spring Boot server is exposing an SSE endpoint. For example:
   ```java
   @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
   public Flux<String> streamEvents() {
       return Flux.interval(Duration.ofSeconds(1))
                  .map(sequence -> "Event " + sequence);
   }
   ```
    - This endpoint streams a message every second.

3. **Consume the SSE Stream in C#**:
   Use `HttpClient` to make a request to the SSE endpoint and process the response as a stream.

---

### **Sample C# Code to Consume SSE**

Here’s how you can consume SSE in C#:

```csharp
using System;
using System.Net.Http;
using System.Threading.Tasks;

class Program
{
    static async Task Main(string[] args)
    {
        // The URL of the Spring Boot SSE endpoint
        string sseUrl = "http://localhost:8080/sse"; // Replace with your Spring Boot server URL

        // Create an HttpClient
        using (HttpClient client = new HttpClient())
        {
            try
            {
                // Send a GET request to the SSE endpoint
                using (HttpResponseMessage response = await client.GetAsync(sseUrl, HttpCompletionOption.ResponseHeadersRead))
                {
                    response.EnsureSuccessStatusCode(); // Ensure the response is 2xx

                    // Read the response as a stream
                    using (var stream = await response.Content.ReadAsStreamAsync())
                    using (var reader = new System.IO.StreamReader(stream))
                    {
                        Console.WriteLine("Listening for SSE events...");

                        while (!reader.EndOfStream)
                        {
                            string line = await reader.ReadLineAsync();

                            // SSE messages are separated by a double newline
                            if (!string.IsNullOrWhiteSpace(line))
                            {
                                // Print the message if it starts with "data:"
                                if (line.StartsWith("data:"))
                                {
                                    string data = line.Substring(5).Trim(); // Extract the data payload
                                    Console.WriteLine($"Received: {data}");
                                }
                            }
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                Console.WriteLine($"Error: {ex.Message}");
            }
        }
    }
}
```

---

### **How It Works**
1. **HttpClient**:
    - Sends a `GET` request to the Spring Boot SSE endpoint.
    - The `HttpCompletionOption.ResponseHeadersRead` option ensures the response is processed as a stream rather than waiting for the entire response.

2. **StreamReader**:
    - Reads the response stream line by line.
    - Processes any line that starts with `data:`, which contains the actual SSE payload.

3. **Reconnect Logic**:
    - If the connection is lost, you can wrap the code in a retry mechanism to reconnect to the server.

---

### **Handling Reconnection**
The SSE protocol is inherently designed for reconnection. If the connection drops, you can implement a simple reconnection mechanism in C#:

```csharp
while (true)
{
    try
    {
        await ListenToSse(); // Your SSE listening code
    }
    catch (Exception ex)
    {
        Console.WriteLine($"Connection lost. Reconnecting in 3 seconds... Error: {ex.Message}");
        await Task.Delay(3000); // Wait and reconnect
    }
}
```

---

### **Sample Output**
If the Spring Boot server is sending events like `Event 0`, `Event 1`, etc., the output will look like this:
```
Listening for SSE events...
Received: Event 0
Received: Event 1
Received: Event 2
```

---

### **Additional Notes**
- **Server Timeout**: If the Spring Boot server has an idle timeout, you should periodically reconnect from the client.
- **Heartbeat**: If the server sends comments (e.g., `: heartbeat`), you can skip them in your processing logic.
- **Custom Headers**: If the Spring Boot server requires authentication (e.g., JWT), add headers to the `HttpClient` request:
   ```csharp
   client.DefaultRequestHeaders.Authorization = new System.Net.Http.Headers.AuthenticationHeaderValue("Bearer", "your-token");
   ```

With this approach, you can consume SSE streams produced by a Spring Boot application in a robust and straightforward way in C#.