## Keep Alive

**TCP itself does not have a built-in auto-disconnection mechanism** based on inactivity or periods of no data transfer.
TCP is designed to provide a reliable, connection-oriented communication channel between two endpoints, and it will
keep the connection open until one of the endpoints explicitly closes it or a network failure occurs.

However, there are mechanisms and configurations at different layers or systems that can lead to disconnection due to
inactivity:

### 1. **Application-Level Timeouts**

At the application layer, developers can implement their own timeouts. If no data is sent or received within a specified
period, the application can decide to close the TCP connection. For example:

- HTTP/1.1 uses a "keep-alive" mechanism to maintain a connection for a short time, but it may close the connection
  after inactivity.
- SSH has a configurable timeout for idle connections.

### 2. **TCP Keep-Alive**

- TCP itself provides an optional **keep-alive mechanism**, which can be enabled by the application using the socket.
  If enabled, the system will periodically send small keep-alive packets to ensure the connection is still active.
- If the remote side doesn't respond to keep-alive probes, the TCP connection may be considered dead, and the system
  can close it. The keep-alive feature is typically disabled by default and must be explicitly configured.

**Key parameters for TCP keep-alive** (configurable in most operating systems):

- **Keep-Alive Time**: How long the connection should remain idle before sending keep-alive probes.
- **Keep-Alive Interval**: The time between keep-alive probes.
- **Keep-Alive Retries**: The number of failed probes before the connection is closed.

### 3. **Operating System-Level Idle Connection Timeouts**

- Many operating systems have network stack settings that can close idle TCP connections after a certain period of
  inactivity.
- For example:
    - Linux: `tcp_keepalive_time`, `tcp_keepalive_intvl`, and `tcp_keepalive_probes` parameters.
    - Windows: Registry settings like `KeepAliveTime` and `KeepAliveInterval`.

### 4. **Middleboxes (Firewalls, NATs, Load Balancers)**

- Firewalls, NAT devices, and load balancers often impose their own idle timeout policies to free up resources. If a
  TCP connection remains idle for too long, these middleboxes might terminate it.
- For example:
    - AWS Elastic Load Balancer has a default idle timeout of 60 seconds.
    - Some NATs may drop connections after 30 seconds to a few minutes of inactivity.

### 5. **Application Protocol-Specific Mechanisms**

Some higher-level protocols built on top of TCP implement their own timeout mechanisms. For instance:

- HTTP/2 and HTTP/3 have ping or idle timeout mechanisms.
- WebSocket connections may use pings/pongs to detect disconnections.

### Summary

While TCP itself does not automatically disconnect due to inactivity, mechanisms like **TCP keep-alives**,
**application timeouts**, **OS settings**, and **network middleboxes** can enforce disconnection after periods of no
data transfer. To ensure a connection stays alive, applications can send periodic messages (like "heartbeat" packets)
or enable TCP keep-alives.

## TCP protocol

https://en.wikipedia.org/wiki/Transmission_Control_Protocol

### Key Points:

TCP is a reliable byte stream delivery service that guarantees that all bytes received will be identical and in the
same order as those sent. Since packet transfer by many networks is not reliable, TCP achieves this using a technique
known as **positive acknowledgment with re-transmission**. This requires the receiver to respond with an
**acknowledgment** message as it receives the data. The sender keeps a record of each packet it sends and maintains
a timer from when the packet was sent. The sender re-transmits a packet if the timer expires before receiving the
acknowledgment. The timer is needed in case a packet gets lost or corrupted.

While IP handles actual delivery of the data, TCP keeps track of segments – the individual units of data transmission
that a message is divided into for efficient routing through the network. For example, when an HTML file is sent from
a web server, the TCP software layer of that server divides the file into segments and forwards them individually
to the **internet layer** in the **network stack**. The internet layer software encapsulates each TCP segment into an
IP packet by adding a header that includes (among other data) the destination IP address. When the client program on
the destination computer receives them, the TCP software in the transport layer re-assembles the segments and ensures
they are correctly ordered and error-free as it streams the file contents to the receiving application.

### Key Fields in TCP Header:

#### Source Port: 16 bits

Identifies the sending port.

#### Destination Port: 16 bits

Identifies the receiving port.

#### Sequence Number: 32 bits

Has a dual role:

- If the SYN flag is set (1), then this is the initial sequence number. The sequence number of the actual first data
  byte and the acknowledged number in the corresponding ACK are then this sequence number plus 1.
- If the SYN flag is unset (0), then this is the accumulated sequence number of the first data byte of this segment for
  the current session.

#### Acknowledgment Number: 32 bits

If the ACK flag is set then the value of this field is the next sequence number that the sender of the ACK is expecting.
This acknowledges receipt of all prior bytes (if any).[19] The first ACK sent by each end acknowledges the other end's
initial sequence number itself, but no data.[20]

#### Data Offset (DOffset): 4 bits

Specifies the size of the TCP header in 32-bit words. The minimum size header is 5 words and the maximum is 15 words
thus giving the minimum size of 20 bytes and maximum of 60 bytes, allowing for up to 40 bytes of options in the header.
This field gets its name from the fact that it is also the offset from the start of the TCP segment to the actual
data.[citation needed]

#### Flags: 8 bits

Contains 8 1-bit flags (control bits) as follows:

##### CWR: 1 bit

Congestion window reduced (CWR) flag is set by the sending host to indicate that it received a TCP segment with the ECE
flag set and had responded in congestion control mechanism.[22][a]

##### ECE: 1 bit

ECN-Echo has a dual role, depending on the value of the SYN flag. It indicates:
If the SYN flag is set (1), the TCP peer is ECN capable.[23]
If the SYN flag is unset (0), a packet with the Congestion Experienced flag set (ECN=11) in its IP header was received
during normal transmission.[a] This serves as an indication of network congestion (or impending congestion) to the TCP
sender.[24]

##### URG: 1 bit

Indicates that the Urgent pointer field is significant.

##### ACK: 1 bit

Indicates that the Acknowledgment field is significant. All packets after the initial SYN packet sent by the client
should have this flag set.[25]

##### PSH: 1 bit

Push function. Asks to push the buffered data to the receiving application.

##### RST: 1 bit

Reset the connection

##### SYN: 1 bit

Synchronize sequence numbers. Only the first packet sent from each end should have this flag set. Some other flags and
fields change meaning based on this flag, and some are only valid when it is set, and others when it is clear.

##### FIN: 1 bit

Last packet from sender

### Connection Establishment

Before a client attempts to connect with a server, the server must first bind to and listen at a port to open it up for
connections: this is called a passive open. Once the passive open is established, a client may establish a connection by
initiating an active open using the three-way (or 3-step) handshake:

- SYN: The active open is performed by the client sending a SYN to the server. The client sets the segment's sequence
  number to a random value A.
- SYN-ACK: In response, the server replies with a SYN-ACK. The acknowledgment number is set to one more than the
  received sequence number i.e. A+1, and the sequence number that the server chooses for the packet is another random
  number, B.
- ACK: Finally, the client sends an ACK back to the server. The sequence number is set to the received acknowledgment
  value i.e. A+1, and the acknowledgment number is set to one more than the received sequence number i.e. B+1.

Steps 1 and 2 establish and acknowledge the sequence number for one direction (client to server). Steps 2 and 3
establish and acknowledge the sequence number for the other direction (server to client). Following the completion of
these steps, both the client and server have received acknowledgments and a full-duplex communication is established.

<table class="wikitable" style="text-align: center; border: none;">
<caption>TCP header format<sup id="cite_ref-FOOTNOTERFC_92933.1._Header_Format_17-1" class="reference"><a href="#cite_note-FOOTNOTERFC_92933.1._Header_Format-17"><span class="cite-bracket">[</span>17<span class="cite-bracket">]</span></a></sup>
</caption>
<tbody><tr>
<th style="min-width:42px; border-bottom:none; border-right:none;"><i>Offset</i>
</th>
<th style="border-left:none;"><a href="/wiki/Octet_(computing)" title="Octet (computing)">Octet</a>
</th>
<th colspan="8">0
</th>
<th colspan="8">1
</th>
<th colspan="8">2
</th>
<th colspan="8">3
</th></tr>
<tr>
<th style="min-width: 42px;border-top: none;">Octet
</th>
<th style="min-width: 42px;"><a href="/wiki/Bit" title="Bit">Bit</a>
</th>
<th style="min-width:11px;">0
</th>
<th style="min-width:11px;">1
</th>
<th style="min-width:11px;">2
</th>
<th style="min-width:11px;">3
</th>
<th style="min-width:11px;">4
</th>
<th style="min-width:11px;">5
</th>
<th style="min-width:11px;">6
</th>
<th style="min-width:11px;">7
</th>
<th style="min-width:11px;">8
</th>
<th style="min-width:11px;">9
</th>
<th style="min-width:16px;">10
</th>
<th style="min-width:16px;">11
</th>
<th style="min-width:16px;">12
</th>
<th style="min-width:16px;">13
</th>
<th style="min-width:16px;">14
</th>
<th style="min-width:16px;">15
</th>
<th style="min-width:16px;">16
</th>
<th style="min-width:16px;">17
</th>
<th style="min-width:16px;">18
</th>
<th style="min-width:16px;">19
</th>
<th style="min-width:16px;">20
</th>
<th style="min-width:16px;">21
</th>
<th style="min-width:16px;">22
</th>
<th style="min-width:16px;">23
</th>
<th style="min-width:16px;">24
</th>
<th style="min-width:16px;">25
</th>
<th style="min-width:16px;">26
</th>
<th style="min-width:16px;">27
</th>
<th style="min-width:16px;">28
</th>
<th style="min-width:16px;">29
</th>
<th style="min-width:16px;">30
</th>
<th style="min-width:16px;">31
</th></tr>
<tr>
<th style="width:35px;">0
</th>
<th style="width:30px;">0
</th>
<td colspan="16"><i>Source Port</i>
</td>
<td colspan="16"><i>Destination Port</i>
</td></tr>
<tr>
<th style="width:35px;">4
</th>
<th style="width:30px;">32
</th>
<td colspan="32"><i>Sequence Number</i>
</td></tr>
<tr>
<th style="width:35px;">8
</th>
<th style="width:30px;">64
</th>
<td colspan="32"><i>Acknowledgement Number (meaningful when ACK bit set)</i>
</td></tr>
<tr>
<th style="width:35px;">12
</th>
<th style="width:30px;">96
</th>
<td colspan="4"><i>Data Offset</i>
</td>
<td colspan="4"><i>Reserved</i>
</td>
<td><i><style data-mw-deduplicate="TemplateStyles:r1231500821">@supports(writing-mode:vertical-lr){.mw-parser-output .ts-vertical-text{letter-spacing:-0.12em;line-height:1em;text-orientation:upright;writing-mode:vertical-lr;width:1em}}</style><span class="ts-vertical-text" style="">CWR</span></i>
</td>
<td><i><link rel="mw-deduplicated-inline-style" href="mw-data:TemplateStyles:r1231500821"><span class="ts-vertical-text" style="">ECE</span></i>
</td>
<td><i><link rel="mw-deduplicated-inline-style" href="mw-data:TemplateStyles:r1231500821"><span class="ts-vertical-text" style="">URG</span></i>
</td>
<td><i><link rel="mw-deduplicated-inline-style" href="mw-data:TemplateStyles:r1231500821"><span class="ts-vertical-text" style="">ACK</span></i>
</td>
<td><i><link rel="mw-deduplicated-inline-style" href="mw-data:TemplateStyles:r1231500821"><span class="ts-vertical-text" style="">PSH</span></i>
</td>
<td><i><link rel="mw-deduplicated-inline-style" href="mw-data:TemplateStyles:r1231500821"><span class="ts-vertical-text" style="">RST</span></i>
</td>
<td><i><link rel="mw-deduplicated-inline-style" href="mw-data:TemplateStyles:r1231500821"><span class="ts-vertical-text" style="">SYN</span></i>
</td>
<td><i><link rel="mw-deduplicated-inline-style" href="mw-data:TemplateStyles:r1231500821"><span class="ts-vertical-text" style="">FIN</span></i>
</td>
<td colspan="16"><i>Window</i>
</td></tr>
<tr>
<th style="width:35px;">16
</th>
<th style="width:30px;">128
</th>
<td colspan="16"><i><a href="/wiki/Internet_checksum" title="Internet checksum">Checksum</a></i>
</td>
<td colspan="16"><i>Urgent Pointer (meaningful when URG bit set)<sup id="cite_ref-FOOTNOTERFC_92933.8.5_The_Communication_of_Urgent_Information_18-0" class="reference"><a href="#cite_note-FOOTNOTERFC_92933.8.5_The_Communication_of_Urgent_Information-18"><span class="cite-bracket">[</span>18<span class="cite-bracket">]</span></a></sup></i>
</td></tr>
<tr>
<th style="width:35px;">20
</th>
<th style="width:30px;">160
</th>
<td colspan="32" rowspan="3" style="background: linen;"><i>(Options) If present, Data Offset will be greater than 5.<br>Padded with zeroes to a multiple of 32 bits, since Data Offset counts words of 4 octets.</i>
</td></tr>
<tr>
<th>⋮
</th>
<th>⋮
</th></tr>
<tr>
<th>56
</th>
<th>448
</th></tr>
<tr>
<th style="width:35px;">60
</th>
<th style="width:30px;">480
</th>
<td colspan="32" rowspan="3" style="background: mistyrose;"><i>Data</i>
</td></tr>
<tr>
<th>64
</th>
<th>512
</th></tr>
<tr>
<th>⋮
</th>
<th>⋮
</th></tr></tbody></table>

---

## TCP Head-Of-Line-Blocking

TCP-level **head-of-line (HOL) blocking** occurs because TCP guarantees **in-order, reliable delivery** of packets. If a
packet is lost or arrives out of order, subsequent packets cannot be processed by the receiving application until the
missing packet is retransmitted and received. Let me explain this in detail with respect to the underlying workings of
the TCP protocol.

---

### **Reasons for TCP Head-of-Line Blocking**

1. **Ordered Delivery Guarantee**:

- TCP ensures that data is delivered to the receiving application in the same order in which it was sent.
- Packets (TCP segments) are numbered using **sequence numbers**, and the receiver reassembles packets into the correct
  order before passing the data to the application.
- If a packet is missing or delayed, the receiver cannot process subsequent packets because they depend on the correct
  sequence.

**Example**:

- Suppose a sender transmits packets with sequence numbers 1, 2, 3, 4, and 5.
- If packet 2 is lost or delayed, the receiver cannot process packets 3, 4, and 5 until packet 2 is retransmitted and
  received, even though packets 3, 4, and 5 have already arrived.

---

2. **Retransmission Mechanism**:

- TCP uses **ACKs (acknowledgments)** and **timeouts** to ensure reliable delivery.
- If a packet is lost, the sender retransmits it after a timeout or upon receiving duplicate ACKs from the receiver.
- Until the missing packet is retransmitted and received, data beyond the missing packet cannot be delivered to the
  application.

**Example**:

- Packets 1, 3, 4, and 5 arrive, but packet 2 is lost.
- The receiver sends duplicate ACKs for packet 1, indicating that it is still waiting for packet 2.
- The sender retransmits packet 2 after detecting the loss.
- Only after packet 2 is received can the receiver process packets 3, 4, and 5.

---

3. **TCP Buffering**:

- TCP uses **buffers** at both the sender and receiver to manage data flow.
- At the receiver, out-of-order packets are stored in a buffer until the missing packet arrives.
- However, the receiver cannot pass any data to the application until all packets are received in order, leading to
  head-of-line blocking.

**Example**:

- If the receiver has received packets 3, 4, and 5 but is waiting for packet 2, it buffers packets 3, 4, and 5 but does
  not deliver them to the application until packet 2 is received.

---

4. **Congestion Control and Flow Control**:

- TCP employs **congestion control** (e.g., slow start, congestion avoidance) and **flow control** (using the receiver's
  advertised window size) to manage the rate of data transmission.
- If packet loss occurs due to network congestion, TCP reduces its sending rate, which can delay retransmissions and
  worsen HOL blocking.
- Similarly, if the receiver's buffer is full (flow control), it may slow down the sender, further impacting delivery.

---

5. **Network-Level Packet Loss or Reordering**:

- Packet loss, delay, or reordering can occur at the network layer (e.g., IP layer) due to issues like:
    - Congestion in routers or switches.
    - Packets taking different paths in the network (causing out-of-order delivery).
    - Dropped packets due to insufficient buffer space in networking devices.
- TCP must handle these network-layer issues by retransmitting and reordering packets, which can cause delays.

---

### **TCP Protocol Details That Cause HOL Blocking**

#### **1. Sequence Numbers and Acknowledgments**

- Each TCP segment is assigned a **sequence number** that represents the byte offset of the segment's data in the
  overall stream.
- The receiver acknowledges the highest sequence number of contiguous data it has received (via an **ACK**).
- If a segment is missing, the receiver will continue sending duplicate ACKs for the last successfully received sequence
  number, indicating it is still waiting for the missing segment.

#### **2. Sliding Window Mechanism**

- TCP uses a **sliding window** for flow control, which defines the range of sequence numbers the sender can transmit
  without waiting for an acknowledgment.
- If a packet is lost, the sliding window cannot move forward until the missing packet is retransmitted and
  acknowledged, delaying subsequent packets.

#### **3. Out-of-Order Delivery**

- While TCP can accept and buffer out-of-order packets, it does not deliver them to the application until the missing
  packet is received.
- This behavior ensures reliability but contributes to HOL blocking.

#### **4. Retransmission Timeout (RTO)**

- TCP relies on a **retransmission timeout (RTO)** to detect packet loss and retransmit missing packets.
- If the RTO is large or poorly tuned, retransmissions may take a long time, further delaying subsequent packets.

#### **5. Congestion Window**

- TCP adjusts the size of its **congestion window (CWND)** based on perceived network congestion.
- If packet loss occurs, TCP reduces the congestion window size, which slows down the overall transmission rate and
  exacerbates HOL blocking.

---

### **Real-World Example of TCP HOL Blocking**

1. Suppose a client sends a video stream to a server, and the TCP segments are numbered as follows:
   ```
   [Segment 1][Segment 2][Segment 3][Segment 4][Segment 5]
   ```
2. At the receiver:

- Segment 2 is lost.
- Segments 1, 3, 4, and 5 are received, but since Segment 2 is missing, the receiver buffers Segments 3, 4, and 5 and
  waits for Segment 2.
- The sender retransmits Segment 2 after a timeout or upon receiving duplicate ACKs.
- Once Segment 2 is received, the receiver can process all the buffered segments and deliver them to the application.

---

### **Impact of TCP HOL Blocking**

- HOL blocking at the TCP layer can significantly impact performance, especially in high-latency or high-loss networks.
- Applications using protocols like **HTTP/2** over TCP are still affected by TCP-level HOL blocking because a lost
  packet blocks all streams (even if only one stream was responsible for the missing packet).

---

### **How HTTP/3 (QUIC) Avoids TCP HOL Blocking**

HTTP/3, built on QUIC (which uses **UDP**), avoids TCP-level HOL blocking by implementing its own reliable, ordered
delivery mechanism at the application layer:

- Each stream in QUIC is independent. If a packet for one stream is lost, it does not block the delivery of packets for
  other streams.
- QUIC uses per-stream flow control and retransmission, allowing streams to progress independently.

---

### **Summary**

TCP HOL blocking happens because of:

1. TCP's guarantee of in-order delivery.
2. The need to retransmit lost packets before subsequent packets can be processed.
3. Buffering of out-of-order packets.
4. Network-level issues like packet loss, reordering, or congestion.

While HTTP/2 reduces HOL blocking at the HTTP layer, it cannot solve TCP-level HOL blocking, which is a fundamental
limitation of the TCP protocol. This is why protocols like HTTP/3 and QUIC, which operate over UDP, were developed to
address this issue.

## Sequence Number

When the **server and client send data simultaneously** in a TCP connection, the `SequenceNumber` and
`AcknowledgeNumber` fields in the TCP headers are used to track the data flow in both directions independently. Since
TCP is a full-duplex protocol, each side of the connection maintains its own **sequence number space** for the data it
sends and uses the **acknowledgment number** to confirm receipt of data from the other side.

Let me break down how `SequenceNumber` and `AcknowledgeNumber` behave in this scenario.

---

### **Key Concepts**

1. **Sequence Number** (`SequenceNumber`):

- Indicates the position of the first byte of data in this segment within the sender's data stream.
- Each side of the connection starts with an **initial sequence number (ISN)**, which is chosen randomly during the TCP
  handshake.

2. **Acknowledgment Number** (`AcknowledgeNumber`):

- Indicates the next byte the sender of the acknowledgment expects to receive from the other side.
- It effectively acknowledges receipt of all bytes up to (but not including) this number.

3. **Independent Sequence Number Spaces**:

- Each side of a TCP connection has its own sequence number space.
- For example:
    - The client tracks the sequence numbers for the data it sends to the server.
    - The server tracks the sequence numbers for the data it sends to the client.

---

### **Scenario: Simultaneous Data Transfer**

Let’s assume the client and server have already completed the TCP handshake, and their **initial sequence numbers (ISNs)
** are as follows:

- Client ISN: `1000`
- Server ISN: `2000`

Now both client and server send data simultaneously.

#### **Step 1: Initial Data from Client**

- The client sends 500 bytes of data to the server.
- TCP header from the client:
    - **SequenceNumber**: `1000` (the ISN, since this is the first data segment sent by the client).
    - **AcknowledgeNumber**: `2000` (acknowledges the server's ISN, meaning the client has received everything up to
      byte `2000` from the server).
    - **Payload Length**: 500 bytes.

#### **Step 2: Initial Data from Server**

- At the same time, the server sends 400 bytes of data to the client.
- TCP header from the server:
    - **SequenceNumber**: `2000` (the ISN, since this is the first data segment sent by the server).
    - **AcknowledgeNumber**: `1000` (acknowledges the client's ISN, meaning the server has received everything up to
      byte `1000` from the client).
    - **Payload Length**: 400 bytes.

---

### **After the First Exchange**

Now both sides receive data and send acknowledgments for what they’ve received.

#### **Step 3: Acknowledgment from Client**

- The client receives 400 bytes from the server (bytes `2000` to `2399`) and sends an acknowledgment.
- TCP header from the client:
    - **SequenceNumber**: `1500` (this is the sequence number of the next byte the client will send in its own data
      stream, assuming it had already sent 500 bytes previously).
    - **AcknowledgeNumber**: `2400` (acknowledges receipt of 400 bytes from the server, i.e., expects the next byte to
      be `2400`).
    - **Payload Length**: 0 bytes (this is just an acknowledgment).

#### **Step 4: Acknowledgment from Server**

- The server receives 500 bytes from the client (bytes `1000` to `1499`) and sends an acknowledgment.
- TCP header from the server:
    - **SequenceNumber**: `2400` (this is the sequence number of the next byte the server will send in its own data
      stream, assuming it had already sent 400 bytes previously).
    - **AcknowledgeNumber**: `1500` (acknowledges receipt of 500 bytes from the client, i.e., expects the next byte to
      be `1500`).
    - **Payload Length**: 0 bytes (this is just an acknowledgment).

---

### **Visualization of the Sequence and Acknowledgment Numbers**

| **Direction**            | **Sequence Number** | **Acknowledgment Number** | **Data Payload** |  
|--------------------------|---------------------|---------------------------|------------------|  
| Client → Server (Step 1) | `1000`              | `2000`                    | 500 bytes        |  
| Server → Client (Step 2) | `2000`              | `1000`                    | 400 bytes        |  
| Client → Server (Step 3) | `1500`              | `2400`                    | 0 bytes (ACK)    |  
| Server → Client (Step 4) | `2400`              | `1500`                    | 0 bytes (ACK)    |  

---

### **Key Points**

1. **Sequence Numbers Are Independent**:

- The sequence numbers for data sent by the client and the server are independent of each other.
- The client tracks its own sequence numbers, and the server does the same for its data.

2. **Acknowledgments Are Specific to the Opposite Direction**:

- The acknowledgment number in each segment acknowledges the data received from the other side.
- For example, the client acknowledges the server’s data, and the server acknowledges the client’s data.

3. **Simultaneous Data Transfer**:

- TCP allows both sides to send data independently and simultaneously.
- Each side keeps track of its own sequence numbers for the data it sends and uses acknowledgment numbers to confirm
  data received from the other party.

4. **Full-Duplex Communication**:

- The independent sequence number spaces and acknowledgment mechanisms allow TCP to support full-duplex communication,
  meaning data can flow in both directions at the same time.

---

### **What Happens in Case of Packet Loss?**

If a packet is lost in either direction:

1. The receiver will not acknowledge the missing data.
2. The sender will detect the loss (via duplicate ACKs or a retransmission timeout) and retransmit the missing data.
3. Once the missing data is received, the acknowledgment numbers will continue to advance.

---

### **Conclusion**

When the client and server send data simultaneously:

- Each side uses its own **sequence number** to indicate the position of the data it sends.
- Each side uses the **acknowledgment number** to confirm the data it has received from the other side.
- The process allows full-duplex communication, and the sequence and acknowledgment numbers ensure reliable, ordered
  delivery in both directions.

## **State Management in TCP**

In a TCP implementation, each side of a connection must maintain **state information** for the session, which includes
tracking the **sequence numbers** and other details for reliable communication. This state is part of the **Transmission
Control Block (TCB)**, which is maintained in memory for each active TCP connection.

Here’s a breakdown of how the sequence numbers (and other related state information) are managed for each session:

---

### **1. Sequence Numbers**

Each side of a TCP connection maintains:

- **Send Sequence Number (`SND.UNA` and `SND.NXT`)**:
    - `SND.UNA` (Unacknowledged): The sequence number of the first byte that has been sent but not yet acknowledged.
    - `SND.NXT` (Next): The sequence number of the next byte to be sent.
    - Together, these track the progress of data being sent by the local side.

- **Receive Sequence Number (`RCV.NXT`)**:
    - Tracks the next expected sequence number from the remote side.
    - Ensures that data is delivered to the application in the correct order.

### **2. Acknowledgment Numbers**

Each side also maintains:

- The last acknowledgment number it received from the remote side.
- This acknowledgment number is sent back to the sender in every acknowledgment packet to confirm receipt of data.

---

## **Memory State for Each TCP Session**

To manage sequence numbers and other connection details, the TCP implementation on each side keeps a **Transmission
Control Block (TCB)** in memory. The TCB stores all the state information for a single TCP connection. Here are the key
details:

### **Local Variables in the TCB**

1. **Send State**:

- `SND.UNA`: Sequence number of the first unacknowledged byte.
- `SND.NXT`: Sequence number of the next byte to send.
- `SND.WND`: The size of the send window (advertised by the receiver).
- `SND.ISN`: Initial Sequence Number (randomly selected during the handshake).

2. **Receive State**:

- `RCV.NXT`: Sequence number of the next expected byte.
- `RCV.WND`: The size of the receive window (advertised by the local side to the sender).
- `RCV.ISN`: Initial Sequence Number received from the remote side during the handshake.

3. **Timers**:

- Retransmission timer for packets that are not acknowledged.
- Delayed acknowledgment timer (if the implementation delays ACKs to optimize performance).

4. **Connection State**:

- State of the connection (e.g., `ESTABLISHED`, `CLOSED`, etc.).
- Flags for features like selective acknowledgment (SACK) or window scaling.

5. **Buffers**:

- **Send Buffer**: Stores data that has been sent but not yet acknowledged.
- **Receive Buffer**: Stores out-of-order data received from the remote side until the missing packets arrive.

---

## **Why Is This Necessary?**

TCP is a **stateful protocol** that guarantees reliable, in-order delivery of data. To implement this, it must:

1. Track which data has been sent, acknowledged, and retransmitted.
2. Keep a record of the data received and which parts are still missing.
3. Manage flow control and congestion control mechanisms.

Without maintaining this state in memory, TCP would not be able to:

- Detect lost packets.
- Retransmit missing data.
- Reassemble out-of-order packets for the application.

---

## **How Sequence Numbers Are Used in Memory**

Let’s take an example to show how the sequence numbers are used in memory for sending and receiving data.

### **Sending Data**

- Suppose the application writes 1000 bytes to the TCP send buffer.
- The initial sequence number (`SND.ISN`) is `1000`.
- TCP starts sending:
    - Bytes 1000–1099 (100 bytes) with `SND.NXT = 1100`.
    - Bytes 1100–1199 (100 bytes) with `SND.NXT = 1200`.
- If the receiver acknowledges up to byte `1150`, the sender updates:
    - `SND.UNA = 1150` (first unacknowledged byte).
- The sender retransmits any unacknowledged bytes if a timeout occurs.

### **Receiving Data**

- Suppose the server sends bytes 2000–2099 to the client with an initial sequence number (`RCV.ISN`) of `2000`.
- The client expects `RCV.NXT = 2000`.
- If bytes 2000–2099 arrive, the client:
    - Updates `RCV.NXT = 2100` (next expected byte).
    - Sends an acknowledgment (`ACK = 2100`) to the server.
- If bytes arrive out of order (e.g., 2100–2199 arrive before 2000–2099), TCP buffers the out-of-order data and waits
  for the missing packets.

---

## **How Memory Is Allocated for TCP**

- Each active TCP connection requires memory for its **TCB** and associated buffers.
- The size of the buffers depends on the **TCP window size**:
    - Send Buffer: Stores unacknowledged data, usually sized to match the **congestion window**.
    - Receive Buffer: Stores out-of-order data, usually sized to match the **advertised window**.

For example:

- A typical TCP connection might allocate **tens of kilobytes** of memory for the TCB and buffers.
- In high-performance systems with thousands of simultaneous connections, this memory usage can add up, requiring
  efficient resource management.

---

## **Summary**

Yes, in TCP implementations, each side maintains the **sequence numbers and acknowledgment numbers** for each connection
in memory. This is typically done through a **Transmission Control Block (TCB)**, which stores all necessary state
information, including:

- Sequence numbers for sending (`SND.UNA`, `SND.NXT`).
- Sequence numbers for receiving (`RCV.NXT`).
- Buffers for send and receive data.
- Flow and congestion control state.

This state management is essential for ensuring TCP's reliability, ordered delivery, and full-duplex communication.

## Sequence Number Wraps Around

In TCP, the **sequence number** is a 32-bit field, meaning it can represent sequence numbers in the range of `0` to
`2^32 - 1` (i.e., `0 to 4,294,967,295`). If the sequence number reaches its maximum value, it will **wrap around to 0**.
This behavior is perfectly fine and accounted for in the TCP protocol. Here's how it works and why it doesn't cause any
issues:

---

### **What Happens When the Sequence Number Wraps Around?**

1. **Sequence Number Wrap-Around:**

- When the sequence number reaches its maximum value (`4,294,967,295`), the next byte of data will have a sequence
  number of `0`. This is called **sequence number wrap-around**.
- For example:
    - If the last byte sent has a sequence number of `4,294,967,295`, the next byte will have a sequence number of `0`.

2. **Relative Sequence Numbers:**

- TCP uses **relative sequence numbers** internally.
- The sequence number in a TCP header is offset relative to the **initial sequence number (ISN)** chosen during the
  connection establishment (three-way handshake).
- This means that even after the sequence number wraps around, TCP's mechanisms for tracking sequence numbers (e.g.,
  acknowledgment numbers, sliding windows) continue to work seamlessly.

---

### **How TCP Handles Wrap-Around**

TCP handles sequence number wrap-around by relying on the following mechanisms:

1. **Sliding Window Protocol:**

- TCP uses a **sliding window** for flow control and ensures that only a small, contiguous range of sequence numbers is
  valid at any given time.
- The **window size** (determined by the receiver's advertised window) ensures that the sender only transmits a limited
  amount of data before waiting for acknowledgment.
- Since the window size is always much smaller than the full 32-bit sequence number range, there is no ambiguity about
  which sequence numbers are valid, even after wrap-around.

**Example:**

- Suppose the receiver's window size is 65,535 bytes (the maximum for a 16-bit window field without window scaling).
- If the current sequence number is `4,294,967,295` and the window size is 65,535, the valid range of sequence numbers
  is:
  ```
  4,294,967,295 → 0 → 65,534
  ```
- Any sequence numbers outside this range are not valid.

2. **Acknowledgment Numbers:**

- The **acknowledgment number** in TCP specifies the next sequence number expected by the receiver.
- This acknowledgment mechanism ensures that the sender knows which sequence numbers have been received and which are
  still pending, regardless of wrap-around.

**Example:**

- If the receiver has acknowledged up to sequence number `4,294,967,295`, the next acknowledgment number will be `0`.

3. **Modulo Arithmetic:**

- Sequence number calculations in TCP are done using **modulo arithmetic** with `2^32` (the size of the sequence number
  space).
- This means that comparisons between sequence numbers wrap around naturally.
- For example:
    - If `SeqNum = 4,294,967,295` and the next number is `0`, TCP will treat `0` as the next logical sequence number.

4. **Avoiding Ambiguity:**

- TCP ensures that sequence numbers are **unique within the lifetime of a segment** by using the following rules:
    - The **Maximum Segment Lifetime (MSL)** ensures that old packets (with potentially conflicting sequence numbers)
      are discarded before their sequence numbers could overlap with new packets.
    - The window size is much smaller than the sequence number space, so even after wrap-around, there is no ambiguity
      about which sequence numbers are valid.

---

### **Why Wrap-Around Doesn’t Cause Problems**

1. **The Sequence Number Space Is Large:**

- The 32-bit sequence number space allows for `2^32` (4.29 billion) sequence numbers.
- Even at very high data rates, it takes significant time for the sequence numbers to wrap around.

**Example:**

- At a data rate of 1 Gbps (1 billion bits per second), it would take approximately:
  ```
  (4,294,967,296 bytes × 8 bits per byte) / (1,000,000,000 bits per second) = ~34 seconds
  ```
- At lower data rates, it takes even longer.

2. **Sliding Window Prevents Overlap:**

- Because the sliding window mechanism limits the range of valid sequence numbers, there is no overlap or ambiguity when
  sequence numbers wrap around.

3. **MSL (Maximum Segment Lifetime):**

- TCP ensures that old segments cannot interfere with new ones by discarding them after the **Maximum Segment Lifetime (
  MSL)**, which is typically 2 minutes.
- This ensures that even if sequence numbers wrap around, old packets will not conflict with new packets.

---

### **Example of Sequence Number Wrap-Around**

Let’s walk through an example where the sequence number wraps around:

1. **Initial State**:

- Assume the sender's initial sequence number (`ISN`) is `4,294,967,000`.
- The receiver advertises a window size of `10,000`.

2. **Data Transmission**:

- The sender transmits 300 bytes of data:
    - Sequence number: `4,294,967,000` → `4,294,967,299`.
- The receiver acknowledges receipt of these bytes:
    - Acknowledgment number: `4,294,967,300`.

3. **Wrap-Around**:

- The sender continues transmitting data, reaching the maximum sequence number:
    - Sequence number: `4,294,967,295`.
- For the next byte, the sequence number wraps around to `0`:
    - Sequence number: `0`.

4. **Acknowledgment After Wrap-Around**:

- The receiver acknowledges the data received after the wrap-around:
    - Acknowledgment number: `300` (indicating the next byte expected after the wrap-around).

---

### **Conclusion**

- TCP handles sequence number wrap-around seamlessly using **modulo arithmetic**, **sliding windows**, and *
  *acknowledgments**.
- The **window size** ensures that there is no ambiguity about valid sequence numbers, even after the 32-bit sequence
  number space wraps around.
- Wrap-around is a fundamental part of TCP's design, and it does not cause issues in practice, thanks to the large
  sequence number space and mechanisms like **MSL** to prevent conflicts.

This is why TCP remains robust even in high-speed networks where sequence numbers wrap around relatively quickly.