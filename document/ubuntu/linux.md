## /etc/passwd
```shell
cat /etc/passwd
username:x:UID:GID:full_name:home_directory:shell
```

Here's what each field represents:

username: The user's login name.
x: A placeholder for the password (actual passwords are stored in /etc/shadow).
UID: The user's unique ID number.
GID: The user's primary group ID number.
full_name: The user's full name or other information.
home_directory: The path to the user's home directory.
shell: The user's default shell (e.g., /bin/bash).


## check user group
```shell
groups username
## or 
id user
```

## add user 
```shell
useradd -g group_name -m -s /bin/bash -d /home/username username
```

## Create a Systemd Service File
Create a systemd service file for your Node.js application. This file tells systemd how to manage your application.

bash
Copy
sudo nano /etc/systemd/system/my-node-app.service
In the file, add the following configuration:

```ini
[Unit]
Description=My Node.js Application
Documentation=https://example.com
After=network.target

[Service]
Environment=NODE_PORT=3000
Type=simple
User=nobody
Group=nogroup
WorkingDirectory=/path/to/your/app
ExecStart=/usr/bin/node /path/to/your/app/app.js
Restart=on-failure

[Install]
WantedBy=multi-user.target
```

### systemctl Common Targets
Here are a few other common targets in systemd:

- graphical.target: Extends multi-user.target by starting a graphical user interface (GUI). It corresponds to runlevel 5 in SysV init systems.
- basic.target: A minimal target that starts essential services required for running basic system functions.
- default.target: The default target that the system boots into, which is typically a symlink to either multi-user.target or graphical.target.
- rescue.target: A target for single-user mode, used for system rescue and maintenance. It corresponds to runlevel 1.
- shutdown.target: A target that shuts down the system.
- reboot.target: A target that reboots the system.


```ini
[Unit]
Description=Consul Agent
Requires=network-online.target
After=network-online.target

[Service]
Restart=on-failure
WorkingDirectory=/home/user
ExecStart=/usr/local/bin/consul agent -config-dir=/etc/consul.d/
StandardOutput=file:start.log
StandardError=append:error.log
ExecReload=/bin/kill -HUP $MAINPID
KillMode=process
KillSignal=SIGINT
TimeoutStopSec=5
LimitNOFILE=65536

[Install]
WantedBy=multi-user.target
```

# nginx

```shell
# Update package list and install prerequisites
sudo apt update
sudo apt install -y curl gnupg2

# Install Node.js (using NodeSource)
curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -
sudo apt install -y nodejs

# Verify installation
node -v
npm -v

# Install Nginx
sudo apt install -y nginx

# Verify installation
nginx -v
```

# TMUX

### sync panels
```shell
:setw synchronize-panes on
:setw synchronize-panes off
```


# IPV6
## Key Differences Between IPv4 and IPv6 Headers
### Address Length:
- IPv4: 32-bit addresses.
- IPv6: 128-bit addresses.
### Header Length:
- IPv4: Variable length (20-60 bytes).
- IPv6: Fixed length (40 bytes).
### Header Complexity:
- IPv4: More complex with options field.
- IPv6: Simplified header with fixed length.
### Fragmentation:
- IPv4: Routers can perform fragmentation.
- IPv6: Fragmentation is handled by the sending host, not routers.
### Checksum:
- IPv4: Includes a header checksum.
- IPv6: No header checksum (relies on higher-layer protocols like TCP/UDP for error checking).
### Options/Extensions:
- IPv4: Options field within the header.
- IPv6: Uses extension headers which are separate from the main header.
### Field Replacements:
- The IHL field in IPv4 is not needed in IPv6 due to the fixed header size.
- The Type of Service field in IPv4 is replaced by the Traffic Class field in IPv6.
- The Identification, Flags, and Fragment Offset fields in IPv4 are replaced by the Next Header field and extension headers in IPv6.


# encryption communication

## Salted Challenge Response Authentication Mechanism

How SCRAM-SHA-256 Works
The SCRAM-SHA-256 authentication process generally involves the following steps:

1. Client Sends Initial Message: The client sends an initial message to the server containing a nonce and the username.
2. Server Responds with Salt and Nonce: The server responds with its own nonce, a salt value, and iteration count.
3. Client Computes Proof: The client uses the salt, nonce, and its password to compute a client proof and sends it to the server.
4. Server Verifies Client Proof: The server verifies the client proof using the stored password hash and other received information.
5. Server Computes and Sends Proof: If the client proof is correct, the server computes its own proof and sends it to the client.
6. Client Verifies Server Proof: The client verifies the server proof to ensure that the server is legitimate.


# Does io keep reading/writing even the coroutine is suspended

When working with coroutines, especially in the context of input/output (I/O) operations, 
it's important to understand how suspensions and I/O operations interact. 

Coroutines are designed to handle asynchronous programming in a more straightforward 
way compared to traditional callback-based approaches.

Understanding Coroutine Suspension and I/O
In many coroutine frameworks, such as Kotlin coroutines, the concept of suspension is used to make asynchronous programming easier. When a coroutine is suspended, it does not block the underlying thread. Instead, it allows other coroutines to run on the same thread, improving efficiency and scalability.

For I/O operations, this means:

- Non-blocking I/O: If the I/O operation is non-blocking (e.g., using asynchronous I/O libraries), the coroutine can be suspended while waiting for the I/O operation to complete. The underlying thread can then be used for other tasks, and the coroutine will be resumed once the I/O operation is done.
- Blocking I/O in a Coroutine Context: If the I/O operation is traditionally blocking, it is usually recommended to perform such operations in a dedicated thread pool (often referred to as an I/O dispatcher). This way, the blocking I/O does not block the main thread or the event loop (in case of single-threaded event loops).

## Example in Kotlin Coroutines

Let's look at an example in Kotlin coroutines, which are part of the Kotlin standard library:

```kotlin
import kotlinx.coroutines.*
import java.net.URL

fun main() = runBlocking {
    val result = withContext(Dispatchers.IO) {
        fetchUrl("https://example.com")
    }
    println(result)
}

suspend fun fetchUrl(url: String): String {
    return withContext(Dispatchers.IO) {
        URL(url).readText()
    }
}
```

### Explanation:
- runBlocking:
  - This function starts a coroutine that blocks the current thread until its execution completes. It's often used in main functions or tests.
- withContext(Dispatchers.IO):
  - This function changes the coroutine context to Dispatchers.IO, which is a dispatcher designed for blocking I/O operations. It uses a shared pool of threads.
  - By using Dispatchers.IO, you ensure that the blocking I/O operation (URL(url).readText()) does not block the main thread or the default dispatcher threads.
- Coroutine Suspension:
  - When fetchUrl("https://example.com") is called, the coroutine is suspended and does not block the main thread.
  - The actual I/O operation (URL(url).readText()) is performed on one of the threads in the I/O dispatcher pool.
  - Once the I/O operation completes, the coroutine is resumed, and the result is printed.

# HMAC
In the formula for HMAC (Hash-based Message Authentication Code), each component plays a crucial role in ensuring the security and integrity of the authentication code. Let's break down the formula and explain each operand:

HMAC ( 𝐾 , 𝑚 ) = hash ( ( 𝐾 ⊕ opad ) ∥ hash ( ( 𝐾 ⊕ ipad ) ∥ 𝑚 ) )
HMAC(K,m)=hash((K⊕opad)∥hash((K⊕ipad)∥m))

Where:

- 𝐾 K is the secret key.
- 𝑚 m is the message or data to be authenticated.
- ⊕ ⊕ denotes the bitwise XOR operation.
- ∥ ∥ denotes concatenation.
- hash hash refers to the hash function (e.g., SHA-256).
- opad opad is the outer padding constant.
- ipad ipad is the inner padding constant.
Detailed Explanation
1. Key Preparation
   Before applying the formula, the key
   - 𝐾 K needs to be prepared. If
     - 𝐾 K is longer than the block size of the hash function (e.g., 64 bytes for SHA-256), it must be hashed first. If
     - 𝐾 K is shorter, it should be padded with zeros to the block size.

2. Padding Constants
   - opad opad (outer padding) is a constant consisting of repeated bytes valued 0x5c.
   - ipad ipad (inner padding) is a constant consisting of repeated bytes valued 0x36.
   - These constants are used to provide additional complexity and ensure different inputs for the inner and outer hash calculations.

3. Inner Hash Calculation
   hash ( ( 𝐾 ⊕ ipad ) ∥ 𝑚 )
   hash((K⊕ipad)∥m)

𝐾 ⊕ ipad K⊕ipad: The key 𝐾 K is XORed with the inner padding constant ipad ipad. This operation mixes the key with the inner padding.
∥ 𝑚 ∥m: The result of the XOR operation is then concatenated with the message 𝑚 m.
hash ( ⋅ ) hash(⋅): The concatenated result is hashed using the chosen hash function (e.g., SHA-256).

4. Outer Hash Calculation
   hash ( ( 𝐾 ⊕ opad ) ∥ inner_hash ) hash((K⊕opad)∥inner_hash)

𝐾 ⊕ opad K⊕opad: The key 𝐾 K is XORed with the outer padding constant opad opad. 
This operation mixes the key with the outer padding.
∥ inner_hash ∥inner_hash: The result of this XOR operation is then concatenated with the result of the inner hash calculation. 
hash ( ⋅ ) hash(⋅): The concatenated result is hashed again using the chosen hash function.

5. Final HMAC Value
   The final output of the outer hash calculation is the HMAC value for the given message
   𝑚 m and key 𝐾 K.

Summary
The formula essentially describes a two-step hashing process:

Inner Hash: Hash the concatenation of the key (XORed with an inner padding) and the message.
Outer Hash: Hash the concatenation of the key (XORed with an outer padding) and the result of the inner hash.
This two-step process ensures that HMAC is resistant to certain types of attacks and provides a robust mechanism for verifying data integrity and authenticity.

# sha256
The SHA-256 algorithm is part of the SHA-2 (Secure Hash Algorithm 2) family, designed by the National Security Agency (NSA) and published by the National Institute of Standards and Technology (NIST). SHA-256 produces a 256-bit (32-byte) hash value, typically rendered as a 64-character hexadecimal number. It is widely used in various security applications and protocols, including TLS and SSL, PGP, SSH, IPsec, and more.

## Key Properties of SHA-256
- Fixed Output Size: SHA-256 always produces a 256-bit (32-byte) hash value.
- Deterministic: The same input will always produce the same output hash.
- Collision-Resistant: It is computationally infeasible to find two different inputs that produce the same hash output.
- Preimage-Resistant: Given a hash value, it is computationally infeasible to find an input that hashes to that value.
- Avalanche Effect: A small change in the input (even a single bit) will produce a significantly different hash.

## The SHA-256 Algorithm

### Preprocessing

Padding the Message

The input message is padded to ensure its length is a multiple of 512 bits (64 bytes). Padding involves:

- Adding a single '1' bit to the end of the message.
- Adding '0' bits until the length of the message is 64 bits short of a multiple of 512.
- Adding a 64-bit representation of the original message length to the padded message.

The padded message is divided into 512-bit (64-byte) blocks for processing.

### Hash Computation

#### Initial Hash Values

SHA-256 uses 8 initial hash values, each 32 bits:

- h0 = 0x6a09e667
- h1 = 0xbb67ae85
- h2 = 0x3c6ef372
- h3 = 0xa54ff53a
- h4 = 0x510e527f
- h5 = 0x9b05688c
- h6 = 0x1f83d9ab
- h7 = 0x5be0cd19

##### Constants
SHA-256 uses 64 constant values (K), each 32 bits. \
These constants are derived from the fractional parts of the cube roots of the first 64 prime numbers.

#### Processing Each Block
https://www.youtube.com/watch?v=orIgy2MjqrA&t=93s

For each 512-bit block, the following steps are performed: \

##### 1. Message Schedule (W) Preparation:
- A message schedule array of 64 words (32 bits each) is created.
- The first 16 words are directly derived from the block.
- The remaining 48 words are generated using a specific formula involving bitwise operations and previous words in the schedule.
##### 2. Initialize Working Variables:
- Eight working variables (a, b, c, d, e, f, g, h) are initialized to the current hash values (h0, h1, ..., h7).
##### 3. Compression Function:
- For each of the 64 rounds:
  - Calculate the temporary variables T1 and T2 using bitwise operations, the message schedule, and the constants.
  - Update the working variables using 𝑇 1 T1 and T2.
##### 4. Update Hash Values:
- After processing each block, the hash values h0,h1,...,h7 are updated by adding the working variables.

### Final Hash Value
After all blocks have been processed, the concatenation of the hash values \
   h0,h1,...,h7 forms the final 256-bit hash output.




- what is 2 factor auth
- kerberos
- detail ssl