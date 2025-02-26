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


