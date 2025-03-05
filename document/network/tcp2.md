# TCP Transfer

In **TCP (Transmission Control Protocol)**, multiplexing and determining when a message is complete are handled in different ways depending on the layer of the network stack.

### **How Multiplexing Works in TCP**
Multiplexing allows multiple applications to use the same transport layer protocol (like TCP) simultaneously. TCP achieves this using **port numbers**:

1. **Source Port**: A unique port number on the client side identifies the application sending the data.
2. **Destination Port**: A port number on the server side identifies the application or service that should handle the data (e.g., 80 for HTTP, 443 for HTTPS).
3. **IP Address**: Combined with port numbers, the IP address ensures that data is delivered to the correct machine and application.

When a client sends data, the combination of the **source IP, source port, destination IP, and destination port** uniquely identifies the connection (this is often called a **socket pair**). The TCP stack on the server uses this information to demultiplex (route) the incoming data to the correct application.

---

### **How the Server Knows a Chunk of Data Is Finished**
TCP is a **stream-oriented protocol**, meaning it doesn't inherently have message boundaries. Unlike UDP, which sends discrete packets, TCP provides a continuous stream of bytes. Determining when a "message" is complete depends on the **application layer**, not TCP itself.

Here’s how this works in detail:

1. **TCP and Segments**:
    - TCP breaks data into segments for transmission. These segments are reassembled at the receiver in the correct order based on their sequence numbers.
    - However, TCP only ensures that the bytes are delivered reliably and in order. It does not group bytes into "messages" or "chunks" with boundaries.

2. **Application Layer Determines Message Completion**:
    - The application layer must define how to interpret the stream of bytes received over TCP.
    - Common techniques include:
        - **Length Prefixing**: The sender includes the size of the message at the beginning of the data. The receiver reads the length prefix to know how many bytes make up the message.
        - **Delimiters**: The sender inserts special characters or sequences (e.g., `\n` in text protocols like HTTP or FTP) to mark the end of a message.
        - **Fixed-Length Messages**: If the application knows the message size in advance, it can read a fixed number of bytes.
    - For example:
        - HTTP uses delimiters (e.g., headers followed by a blank line) or Content-Length headers to indicate when a message is complete.
        - Protocols like FTP use fixed-length fields for metadata and delimiters for data.

3. **TCP Connection Termination**:
    - When a TCP connection is closed (via a FIN or RST flag), the receiver knows that no more data will be sent. This is another way to determine the end of communication, but it’s not specific to individual messages.

---

### **Summary**
- In TCP, **multiplexing** is achieved using port numbers and IP addresses to differentiate connections.
- TCP itself does not define "message boundaries"; it delivers a reliable, ordered stream of bytes.
- Determining when a message is complete is the **responsibility of the application layer**. Applications typically use techniques like length prefixing, delimiters, or fixed-length messages to achieve this.

# Send Multiple Message

No, you are not required to finish sending the first message before starting the second message in **TCP**, but if you do send multiple messages (like XML or JSON), **your application must define a way to separate or parse them**. TCP itself doesn't enforce boundaries between messages because it treats everything as a continuous stream of bytes.

### **Key Points**:

1. **TCP is Stream-Oriented**:
    - TCP does not know or care about "messages." It only ensures that all bytes you send are delivered in order and without errors.
    - If you send two JSON or XML messages back-to-back, they might arrive as a single continuous stream of bytes at the receiving end.

      For example:
      ```json
      {"message": "first"}{"message": "second"}
      ```
      The receiver would see this as a single stream of bytes. Without a way to differentiate the two messages, it would not know where one ends and the other begins.

2. **Messages Can Be Split or Merged**:
    - TCP segments data for transmission. This means a single "message" might be split across multiple TCP packets, or multiple "messages" might be packed into a single packet.
    - The application layer must be able to handle incomplete or combined messages (e.g., partial JSON, XML chunks) and reconstruct them.

3. **You Need a Message Boundary Mechanism**:
    - To handle multiple messages sent over a TCP connection, your application needs a way to identify the boundaries between messages. Common approaches include:
        - **Length Prefixing**: Include the length of each message before the message itself.
          Example:
          ```
          19{"message": "first"}
          19{"message": "second"}
          ```
          The application reads the length (`19`), then reads 19 bytes for the first message, and so on.
        - **Delimiters**: Use a special character or sequence to separate messages.
          Example:
          ```json
          {"message": "first"}\n{"message": "second"}\n
          ```
          The receiver splits the stream using `\n` as the delimiter.
        - **Protocol-Specific Measures**: Some protocols (like HTTP) define their own methods, such as using headers (e.g., `Content-Length`) or chunked transfer encoding to indicate message boundaries.

4. **Parallel Messages**:
    - If you want to send two messages simultaneously, you can still send them over the same TCP connection. However, they must be clearly identifiable as separate messages by the application layer.
    - Alternatively, you could establish **separate TCP connections** for each message, but this is usually inefficient.

---

### **Example with JSON Over TCP**

#### Without Boundaries:
If you send two JSON messages without defining boundaries:
```json
{"message": "first"}{"message": "second"}
```
The receiver won't know where the first message ends and the second begins. If the first message is incomplete, the JSON parser might throw an error.

#### With Length Prefix:
Add a length prefix to the messages:
```
25{"message": "first"}
25{"message": "second"}
```
The receiver reads 25 bytes for each message, ensuring proper separation.

#### With Delimiters:
Use a delimiter like `\n`:
```json
{"message": "first"}\n{"message": "second"}\n
```
The receiver splits the data on `\n`, parsing each part as a separate JSON message.

---

### **Conclusion**:
You do not need to "finish" the first message before starting the second, but you **must implement a mechanism at the application layer to separate or parse the messages**. This is essential because TCP itself does not provide message boundaries; it only ensures reliable delivery of a continuous byte stream.


# Peer Reset

In **TCP communication**, a **peer reset** refers to a situation where one side of a TCP connection (the "peer") sends a **TCP reset (RST)** packet to the other side. This effectively terminates the connection immediately and ungracefully. The reset indicates that the peer is no longer willing or able to continue the communication.

---

### **What is a TCP Reset (RST)?**
A **TCP reset** is a control flag in the TCP header used to abruptly end a connection. It is sent when:

1. A peer is in an invalid state to continue the connection.
2. There is an issue with the connection, such as an application crash or incorrect state synchronization.
3. The connection is no longer valid or recognized by the peer.

---

### **Common Causes of a Peer Reset**
Here are the most common reasons why a peer might send a TCP reset:

1. **Application-Level Issues:**
    - The application running on the peer crashed or is not responding.
    - The application explicitly closed the socket in an unusual way, resulting in an RST.

2. **Connection Issues:**
    - One peer tries to send data to a port or socket that is closed or does not exist anymore.
    - The connection state between the peers becomes desynchronized (e.g., due to network issues or packet loss).

3. **Timeouts:**
    - The peer detects that the connection has been idle for too long and decides to terminate it forcefully.

4. **Firewall or Network Device Behavior:**
    - A firewall, NAT (Network Address Translation), or other network middleboxes might inject a TCP reset to terminate the connection for security or policy reasons.

5. **Protocol Violations:**
    - A peer sends unexpected or invalid data, prompting the other peer to reset the connection.

6. **Resource Constraints:**
    - The peer runs out of resources (e.g., memory, file descriptors) and cannot maintain the connection.

---

### **How Does a TCP Reset Work?**
1. The peer sends a TCP packet with the **RST flag** set.
2. The recipient of the RST packet immediately terminates the connection.
3. Any further communication on that connection is invalid.

---

### **How to Handle a Peer Reset**
If you're encountering peer resets in your application or system, consider the following:

1. **Check the Application:**
    - Ensure that the application is properly handling socket connections and closing them gracefully when needed.

2. **Monitor Network Behavior:**
    - Use tools like Wireshark or tcpdump to analyze the TCP packets and identify the exact cause of the reset.

3. **Inspect Firewalls or Middleboxes:**
    - Ensure that firewalls, NAT devices, or proxies are not terminating the connection unexpectedly.

4. **Address Resource Constraints:**
    - Check for resource exhaustion on the peer (e.g., ensure there is enough memory and file descriptors).

5. **Handle Resets Gracefully:**
    - Design your application to handle resets by detecting them and re-establishing the connection if necessary.

---

### **Conclusion**
A TCP **peer reset** occurs when one side of a connection sends a **TCP RST** packet, typically indicating a problem or a deliberate decision to terminate the connection. Understanding the cause of the reset—whether it's application behavior, network issues, or external devices—can help diagnose and resolve this issue effectively.

# Hand Shake

When a **TCP handshake** fails, the type of error that is reported depends on the system, programming language, and the specific library or API being used for the TCP connection. However, the failure typically results in one of the following errors:

---

### **1. Connection Timeout**
If the client sends a **SYN** (synchronize) packet and does not receive a **SYN-ACK** (synchronize-acknowledge) response from the server within the timeout period, a **connection timeout error** is thrown. This often occurs when:

- The server is unreachable (e.g., down or offline).
- A firewall is blocking the connection.
- The server is not listening on the specified port.

**Example Error Messages:**
- In Linux/Unix: `Connection timed out`
- In Python (using `socket`): `socket.timeout`
- In Java: `java.net.SocketTimeoutException: connect timed out`

---

### **2. Connection Refused**
If the client sends a **SYN** packet but receives a **RST** (reset) packet instead of a **SYN-ACK**, it means the server is actively refusing the connection. This often occurs when:

- The server is running but not listening on the specified port.
- A firewall or security rule explicitly denies the connection.

**Example Error Messages:**
- In Linux/Unix: `Connection refused`
- In Python (using `socket`): `ConnectionRefusedError`
- In Java: `java.net.ConnectException: Connection refused`

---

### **3. Network Unreachable**
If the client cannot reach the server's network (e.g., due to a routing issue, DNS error, or incorrect IP address), a **network unreachable** error occurs. This typically happens before the handshake even starts.

**Example Error Messages:**
- In Linux/Unix: `Network is unreachable`
- In Python: `OSError: [Errno 101] Network is unreachable`
- In Java: `java.net.SocketException: Network is unreachable`

---

### **4. Host Unreachable**
If the client can reach the network but not the specific host (e.g., due to an unreachable IP or a misconfigured network), a **host unreachable** error is raised.

**Example Error Messages:**
- In Linux/Unix: `No route to host`
- In Python: `OSError: [Errno 113] No route to host`
- In Java: `java.net.NoRouteToHostException`

---

### **5. TLS/SSL Errors (If Using HTTPS)**
If the handshake involves a secure connection and fails during the TLS/SSL negotiation (after the TCP handshake), you might see an SSL/TLS-specific error. This can occur due to:

- Invalid certificates.
- Protocol mismatches.
- Incompatible encryption algorithms.

**Example Error Messages:**
- In Python (using `ssl`): `SSLError: [SSL: CERTIFICATE_VERIFY_FAILED]`
- In Java: `javax.net.ssl.SSLHandshakeException`

---

### **6. General Socket Errors**
In some cases, the exact error might not map directly to the above reasons, and you may receive a more generic socket error.

**Example Error Messages:**
- In Python: `OSError: [Errno 104] Connection reset by peer`
- In Java: `java.net.SocketException: Connection reset`

---

### **How to Troubleshoot TCP Handshake Failures**
1. **Check Server Availability:**
    - Verify that the server is running and listening on the correct IP and port using tools like `telnet` or `nc`.

2. **Check Network Connectivity:**
    - Use tools like `ping` or `traceroute` to ensure the client can reach the server's network.

3. **Inspect Firewall Rules:**
    - Ensure that firewalls or security groups are not blocking the connection.

4. **Verify DNS Configuration:**
    - Ensure the hostname resolves to the correct IP address.

5. **Enable Debugging:**
    - Use packet-sniffing tools like Wireshark or tcpdump to analyze the handshake attempts and detect where it fails.

---

### **Summary**
When a TCP handshake fails, the error typically falls into one of these categories: **connection timeout**, **connection refused**, **network unreachable**, **host unreachable**, or **TLS/SSL errors**. Understanding the context and error messages can help you diagnose and resolve the issue effectively.