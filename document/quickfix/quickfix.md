

**QuickFIX**, a popular open-source FIX engine implementation, can use either **TCP** or **UDP** for transport, depending on the configuration of the FIX session. However, in most real-world implementations, **TCP** is used as the transport layer for FIX communications. Here's a detailed explanation:

---

### **1. Does QuickFIX use TCP for the transport layer?**

Yes, **QuickFIX typically uses TCP** as the transport layer for FIX protocol communication. TCP is the preferred choice for FIX because it provides:

- **Reliable delivery**: Messages are delivered in the correct order, and lost packets are retransmitted.
- **Connection-oriented communication**: FIX sessions require persistent connections between counterparties.
- **Error checking and correction**: Built into TCP, ensuring message delivery integrity.

The FIX protocol itself is **session-oriented**, and TCP is a natural fit because it maintains a persistent connection throughout a FIX session. QuickFIX establishes and maintains this TCP connection for the duration of the FIX session.

- **Default Behavior**: QuickFIX establishes a TCP socket for message exchange between the client (Initiator) and the server (Acceptor).
- **Custom Transport Options**: While QuickFIX primarily uses TCP, it can be configured to work over other transport mechanisms (like UDP) if necessary. However, using TCP is the most common option.

---

### **2. Does QuickFIX use a non-blocking I/O model?**

No, **QuickFIX does not use a non-blocking I/O model** by default. QuickFIX uses a **blocking I/O model** for its socket communication, where:

- The application thread waits (blocks) while reading or writing to the socket until the operation completes.
- This design is simpler and is suitable for many FIX implementations where performance requirements are moderate.

However, **QuickFIX is multithreaded**, which helps mitigate the blocking nature of its I/O operations. For example:
- **Separate Threads**: QuickFIX uses separate threads for managing different tasks, such as handling messages, connection management, and processing incoming or outgoing FIX messages.
- **Session Independence**: Each FIX session runs in its own thread, so a blocking operation in one session does not interfere with other sessions.

---

### **What if you need non-blocking I/O?**

If your use case requires **non-blocking I/O** (e.g., to handle a large number of connections or achieve low latency), you can:

1. **Modify QuickFIX**:
    - Since QuickFIX is open-source, you can modify its code to use a non-blocking I/O library (e.g., **Java NIO**, **epoll**, or **libuv**) for handling sockets.

2. **Use QuickFIX/J with Java NIO**:
    - **QuickFIX/J**, the Java version of QuickFIX, can be extended to use **Java NIO**, which supports non-blocking I/O.

3. **Switch to another FIX engine**:
    - Some FIX engines (commercial or open-source) are specifically designed to use non-blocking I/O and event-driven architectures for high-performance use cases. Examples include **FIXimate**, **FIX8**, or other custom implementations.

---

### **Summary**

- **Transport Layer**: QuickFIX **typically uses TCP** as the transport layer, ensuring reliable, session-based communication.
- **I/O Model**: QuickFIX uses a **blocking I/O model** by default, but it is multithreaded to handle multiple FIX sessions efficiently.
- **Custom I/O**: Non-blocking I/O is not natively supported in QuickFIX, but it can be implemented by modifying the library or using external tools like Java NIO with QuickFIX/J.

If you are working on a high-performance FIX application that requires non-blocking I/O, you may need to extend QuickFIX or use a FIX engine designed for non-blocking operation.


# Implementation Detail

Yes, **QuickFIX/J**, the Java implementation of the QuickFIX engine, implements all the details of the **FIX session layer**, including handling **ResendRequest** messages, **ResetSeqNum** messages, and other session-related events. These are core functionalities of the FIX protocol, and QuickFIX/J fully supports them as part of its session management.

Additionally, QuickFIX/J provides **hooks and callbacks** that allow you to listen for and respond to such session events. Below is a detailed explanation:

---

### **1. Does QuickFIX/J Handle FIX Session Layer Details?**

Yes, QuickFIX/J implements the FIX session layer according to the FIX protocol specification, including:

#### **Handling ResendRequest Messages**
- When a counterparty detects a message gap (due to sequence number mismatches), it sends a **ResendRequest** message.
- QuickFIX/J automatically:
   - Retrieves the requested messages from the message store.
   - Re-sends the messages to the counterparty.
   - Handles whether the re-sent messages should be sent as **original (full)** messages or as **administrative messages** (e.g., sequence resets).

#### **Handling ResetSeqNum Messages**
- QuickFIX/J also handles **Sequence Reset (Gap Fill)** (`35=4`) and **Sequence Reset (Reset)** (`35=4` with `GapFillFlag=N`) messages:
   - **Gap Fill**: Adjusts the sequence range to match the counterparty's expectations without resending certain messages.
   - **Reset**: Resets the sequence number to a new number, typically `1`, or a specified value.
   - These behaviors are managed according to the FIX protocol specification.

#### **Other Session-Level Features**
- **Logon/Logout Handshake**:
   - Automatically manages the **Logon** (`35=A`) and **Logout** (`35=5`) messages.
   - Handles sequence number synchronization during the logon process.
- **Heartbeat/Gap Detection**:
   - Manages **Heartbeat** (`35=0`) and **TestRequest** (`35=1`) messages to monitor the connection.
   - Detects gaps in sequence numbers and initiates **ResendRequest** messages if necessary.
- **Message Storage**:
   - Stores outgoing and incoming messages in a message store (e.g., file store, database store, or in-memory store) for potential resends.
- **Session Time Management**:
   - Automatically validates if a session is active based on the configured **start time** and **end time**.

---

### **2. Hooks to Listen to Session Events**

QuickFIX/J provides several ways to **listen to session events** or customize session behavior. Most of these hooks are implemented via interfaces or abstract methods in the **Application** class or other related components.

#### **Key Hooks for Session-Level Events**

Here are some important hooks and methods provided by QuickFIX/J:

1. **`onLogon`**:
   - Triggered when a session successfully logs on (i.e., after the exchange of `Logon` messages).
   - Use this to handle post-logon initialization or notify your application of a successful connection.
   ```java
   @Override
   public void onLogon(SessionID sessionId) {
       System.out.println("Session logged on: " + sessionId);
   }
   ```

2. **`onLogout`**:
   - Triggered when a session logs out (i.e., after the exchange of `Logout` messages or due to disconnection).
   - Use this to handle cleanup or notify your application of session termination.
   ```java
   @Override
   public void onLogout(SessionID sessionId) {
       System.out.println("Session logged out: " + sessionId);
   }
   ```

3. **`toAdmin`**:
   - Called before an **administrative message** (e.g., `Logon`, `Logout`, `ResendRequest`, etc.) is sent to the counterparty.
   - Use this to modify outgoing administrative messages.
   ```java
   @Override
   public void toAdmin(Message message, SessionID sessionId) {
       System.out.println("Sending admin message: " + message);
   }
   ```

4. **`fromAdmin`**:
   - Called when an **administrative message** is received (e.g., `Logon`, `Logout`, `ResendRequest`, etc.).
   - Use this to process or validate incoming administrative messages.
   ```java
   @Override
   public void fromAdmin(Message message, SessionID sessionId) {
       System.out.println("Received admin message: " + message);
   }
   ```

5. **`toApp`**:
   - Called before an **application-level message** (e.g., `Order`, `ExecutionReport`) is sent to the counterparty.
   - Use this to modify or inspect outgoing application messages.
   ```java
   @Override
   public void toApp(Message message, SessionID sessionId) throws DoNotSend {
       System.out.println("Sending application message: " + message);
   }
   ```

6. **`fromApp`**:
   - Called when an **application-level message** is received.
   - Use this to process or validate incoming application messages.
   ```java
   @Override
   public void fromApp(Message message, SessionID sessionId) {
       System.out.println("Received application message: " + message);
   }
   ```

---

#### **Hooks for Customizing Sequence Number Handling**
QuickFIX/J allows you to override default sequence number handling if needed. This is done via:

1. **Custom Message Stores**:
   - QuickFIX/J uses a **message store** (e.g., file-based, database, or in-memory) to store and retrieve messages for resends.
   - You can implement a custom message store by extending the `quickfix.MessageStore` interface if you need more control over sequence number storage or message retrieval.

2. **Custom Session Settings**:
   - You can configure session behavior in the `config` file (e.g., `quickfix.cfg`) using settings such as:
      - `ResetOnLogon=Y`: Resets sequence numbers on logon.
      - `ResetOnLogout=Y`: Resets sequence numbers on logout.
      - `ResetOnDisconnect=Y`: Resets sequence numbers on disconnection.
   - These settings allow you to define how QuickFIX/J handles sequence resets.

---

### **3. How to Listen for Specific Events Like ResendRequest or ResetSeqNum?**

While QuickFIX/J handles **ResendRequest** and **ResetSeqNum** internally, you can monitor these events by implementing the `fromAdmin` or `toAdmin` hooks.

#### **Example: Listening for ResendRequest**
```java
@Override
public void fromAdmin(Message message, SessionID sessionId) {
    try {
        String msgType = message.getHeader().getString(MsgType.FIELD);
        if (MsgType.RESEND_REQUEST.equals(msgType)) {
            System.out.println("Received ResendRequest: " + message);
        }
    } catch (FieldNotFound e) {
        e.printStackTrace();
    }
}
```

#### **Example: Listening for Sequence Reset**
```java
@Override
public void fromAdmin(Message message, SessionID sessionId) {
    try {
        String msgType = message.getHeader().getString(MsgType.FIELD);
        if (MsgType.SEQUENCE_RESET.equals(msgType)) {
            System.out.println("Received Sequence Reset: " + message);
        }
    } catch (FieldNotFound e) {
        e.printStackTrace();
    }
}
```

---

### **4. Customizing Session Behavior**

If you need to customize how QuickFIX/J handles session-level events (e.g., ResendRequest, ResetSeqNum), you can:

- Implement a **custom session factory** by extending `quickfix.SessionFactory`.
- Override specific methods in the session lifecycle.

---

### **Summary**

1. **Session Layer Details**:
   - QuickFIX/J fully implements the FIX session layer, including handling **ResendRequest**, **Sequence Reset**, **Logon/Logout**, and sequence number synchronization.

2. **Hooks for Session Events**:
   - You can use methods in the `Application` interface (`onLogon`, `onLogout`, `fromAdmin`, etc.) to listen to or customize session-level events.

3. **Monitoring Specific Messages**:
   - Use `fromAdmin` or `toAdmin` to monitor or modify administrative messages such as **ResendRequest** and **ResetSeqNum**.

4. **Customization**:
   - You can configure session behaviors via the `quickfix.cfg` file or extend QuickFIX/J with custom implementations if needed.

By leveraging these hooks and settings, you can effectively monitor and customize QuickFIX/J to suit your application's needs!


If you receive a **ResendRequest** (`35=2`) message in **QuickFIX/J**, you **do not need to manually handle or resend messages yourself**. QuickFIX/J will automatically handle the resend process for you. The library is designed to manage all the mechanics of the FIX session layer, including:

- Retrieving the requested messages from the **message store**.
- Resending the messages starting from the **fromSeqNum** to **toSeqNum** specified in the `ResendRequest`.

### **How QuickFIX/J Handles ResendRequest**
When a **ResendRequest** is received:
1. **QuickFIX/J retrieves the requested messages** from its message store (e.g., file-based, database, or in-memory storage).
2. It re-sends the messages to the counterparty.
   - Messages are re-sent either as **original messages** or as **administrative messages (e.g., Sequence Reset)**, depending on the session configuration.
3. If configured to use **GapFill** (via `UseDataDictionary` or session settings), it may send **Sequence Reset (GapFill)** messages instead of re-sending certain application messages.

---

### **What You Need to Do**
You **do not need to manually re-send messages** in response to a `ResendRequest`. However, you can hook into the process for monitoring or logging, or (if absolutely necessary) customize the behavior.

#### **Key Session Settings for Resend Behavior**
The behavior of the resend process can be influenced by session settings in your `quickfix.cfg` file:

1. **`PersistMessages`**
   - **Default: `Y`**
   - If set to `Y`, QuickFIX/J will store all sent and received messages in the message store, ensuring they are available for resending during a `ResendRequest`.

2. **`UseDataDictionary`**
   - **Default: `Y`**
   - If set to `Y`, certain messages (e.g., administrative ones or non-critical messages) can be replaced with **Sequence Reset (GapFill)** messages during a resend.

3. **`ValidateSequenceNumbers`**
   - **Default: `Y`**
   - If set to `Y`, QuickFIX/J will automatically handle sequence number mismatches and send `ResendRequest` messages or respond to ones it receives.

4. **`ResetOnLogon` / `ResetOnLogout`**
   - These settings ensure sequence numbers are reset during specific events, reducing the need for large-scale resending in certain cases.

---

### **Monitoring ResendRequest Handling**
If you want to monitor or log when a `ResendRequest` is received and how QuickFIX/J handles it, you can use the `fromAdmin` hook in your `Application` implementation. For example:

```java
@Override
public void fromAdmin(Message message, SessionID sessionId) {
    try {
        String msgType = message.getHeader().getString(MsgType.FIELD);
        if (MsgType.RESEND_REQUEST.equals(msgType)) {
            System.out.println("Received ResendRequest: " + message);
        }
    } catch (FieldNotFound e) {
        e.printStackTrace();
    }
}
```

This allows you to log or inspect `ResendRequest` messages without interfering with QuickFIX/J's automatic handling.

---

### **Customizing Resend Behavior**
If you need to customize how QuickFIX/J handles `ResendRequest` messages (e.g., to modify which messages are re-sent or how they are formatted), you would need to:

1. **Override the Message Store**:
   - Implement a custom message store by extending the `quickfix.MessageStore` interface.
   - Your custom store can control how messages are retrieved for resending.

2. **Modify the Default Session Behavior**:
   - Extend `quickfix.Session` or implement a custom session factory to control how sessions handle certain administrative messages, such as `ResendRequest`.

---

### **What Happens If Messages Are Missing?**
If QuickFIX/J cannot find the requested messages in its message store (e.g., because they were not persisted or were deleted), it will send a **Sequence Reset (GapFill)** message to the counterparty for the missing range. This is part of the FIX protocol specification and is handled automatically.

---

### **Summary**
- QuickFIX/J will **automatically resend messages** in response to a `ResendRequest`. You **do not need to resend messages manually**.
- To monitor or log the `ResendRequest`, use the `fromAdmin` hook.
- Customize the resend behavior by modifying configuration settings or implementing a custom message store if necessary.
- Ensure your session configuration (`quickfix.cfg`) is set up properly, especially with `PersistMessages=Y` to enable proper message storage and retrieval.

Let QuickFIX/J handle the resend logic for you, and only intervene if you have specific custom requirements.