# What is SCRAM-SHA256

The **SCRAM-SHA-256** (Salted Challenge Response Authentication Mechanism with SHA-256) authentication mechanism is designed to provide secure password authentication without ever transmitting the plaintext password over the network. This is achieved through the use of **challenge-response authentication** and **cryptographic hashing**. Below is a detailed explanation of how SCRAM-SHA-256 works:

---

### **Key Algorithms and Techniques Used**
1. **Hashing with SHA-256**:
    - The password is processed using the SHA-256 hash algorithm to produce a hashed version of the password.
    - The hashing ensures that the server does not store or receive the plaintext password—only a derived, hashed value.

2. **Salting**:
    - A **salt** (a random value unique to each user) is combined with the password before hashing. This prevents attackers from using precomputed hash tables (e.g., rainbow tables) to guess passwords.

3. **Iterative Hashing**:
    - The password and salt are hashed repeatedly (using thousands of iterations). This increases the computational cost of brute force attacks, making it expensive for an attacker to guess passwords.

4. **Challenge-Response Authentication**:
    - The client and server exchange nonces (random values) and proofs (derived from the hashed password, salt, and nonces) to verify each other's identities without exposing the original password.

---

### **How SCRAM-SHA-256 Works**
Here’s a step-by-step breakdown of the SCRAM-SHA-256 protocol:

#### **1. Server Initialization**
- The server stores a hashed version of the password, called the **StoredKey**, which is computed as follows:
    - `SaltedPassword = HMAC(Salt, Password)`
    - `StoredKey = H(ClientKey)`
        - `ClientKey = HMAC(SaltedPassword, "Client Key")`
- The server also stores the **salt** and the number of iterations used for hashing.
- "Client Key": A literal string (in the SCRAM protocol, this is typically "Client Key").

> The server does not store the plaintext password, only the derived hashed values.

#### **2. Client Sends Initial Message**
- The client initiates the authentication process by sending its username and a randomly generated **client nonce** (`nonce_c`) to the server.

#### **3. Server Responds with Challenge**
- The server responds with:
    - The **salt** associated with the user.
    - The number of iterations for hashing.
    - A randomly generated **server nonce** (`nonce_s`).
    
#### **4. Client Computes Proof**
- The client computes the **SaltedPassword** using the received salt and number of iterations:
    - `SaltedPassword = PBKDF2(SHA-256, Password, Salt, Iterations)`
- Using the `SaltedPassword`, the client computes:
    - `ClientKey = HMAC(SaltedPassword, "Client Key")`
    - `StoredKey = H(ClientKey)`
- The client also calculates the **AuthMessage**, which is a concatenation of the exchanged messages (client nonce, server nonce, etc.).
- The client creates a **ClientProof**:
    - `ClientSignature = HMAC(StoredKey, AuthMessage)`
    - `ClientProof = ClientKey XOR ClientSignature`
- The client sends the **ClientProof** and the combined nonce (`nonce_c + nonce_s`) to the server.

#### **5. Server Verifies Client**
- The server verifies the **ClientProof**:
    - It computes the `StoredKey` and `ClientSignature` using the same salt, iterations, and AuthMessage.
    - It checks that the received `ClientProof` matches the calculated one:
        - `ClientProof XOR ClientSignature = ClientKey`

#### **6. Server Sends Its Proof**
- If the client is verified, the server computes its own **ServerProof**:
    - `ServerKey = HMAC(SaltedPassword, "Server Key")`
    - `ServerSignature = HMAC(ServerKey, AuthMessage)`
- The server sends the **ServerProof** to the client.

#### **7. Client Verifies Server**
- The client verifies the **ServerProof** to confirm the server is legitimate. If the verification succeeds, authentication is complete.

---

### **Security Benefits**
1. **No Plaintext Password Exchange**:
    - The password is never sent over the network—only derived cryptographic values are exchanged.

2. **Resistance to Replay Attacks**:
    - The use of nonces ensures that each authentication session is unique.

3. **Protection Against Precomputed Attacks**:
    - Salting ensures that attackers cannot use precomputed hash tables to guess passwords.

4. **Protection Against Brute Force Attacks**:
    - Iterative hashing increases computational cost, making brute force attacks slower.

---

### **Summary**
SCRAM-SHA-256 uses a combination of **PBKDF2 (Password-Based Key Derivation Function) with SHA-256**, **HMAC**, and **nonces** to mutually authenticate the client and server without exchanging the plaintext password. Instead, it relies on hashed and salted representations of the password, ensuring security against various types of attacks.


# Why Server Can Verify

The reason the **server can verify the `ClientProof`** lies in the use of **cryptographic properties of HMAC and hash functions**, particularly their deterministic and secure nature. The math principle behind this process is based on the **one-way nature of hash functions** and the **shared knowledge of derived values** (like `StoredKey` and the `AuthMessage`) between the client and server.

Here’s a detailed explanation of how and why the server can verify the `ClientProof`:

---

### **Core Components in SCRAM-SHA-256**
1. **`StoredKey`**:
    - The server stores `StoredKey`, which is derived as:
      ```plaintext
      StoredKey = H(ClientKey)
      ```
    - The server doesn’t store the `ClientKey` or the password, but it can verify the client’s proof indirectly using `StoredKey`.

2. **`ClientProof`**:
    - The client generates the `ClientProof` as:
      ```plaintext
      ClientProof = ClientKey XOR ClientSignature
      ```
        - The `ClientSignature` is derived as:
          ```plaintext
          ClientSignature = HMAC(StoredKey, AuthMessage)
          ```
        - `AuthMessage` is a concatenation of exchanged messages during the authentication process.

3. **`ClientKey` Reconstruction**:
    - The server can reconstruct the `ClientKey` indirectly by using the formula:
      ```plaintext
      ClientKey = ClientProof XOR ClientSignature
      ```
    - From the reconstructed `ClientKey`, the server computes:
      ```plaintext
      H(ClientKey) == StoredKey
      ```
    - If this equality holds, the server knows the client possesses the correct password (or its derived keys).

---

### **Why Does This Work?**
The verification process works because of the **deterministic and one-way properties** of cryptographic hashes and HMAC:

1. **HMAC is Deterministic**:
    - Given the same `StoredKey` and `AuthMessage`, the output of `HMAC(StoredKey, AuthMessage)` (i.e., `ClientSignature`) will always be the same for both the client and server.
    - Since the `StoredKey` on the server matches the `StoredKey` derived by the client (from the shared password), both parties can compute the same `ClientSignature`.

2. **XOR is Reversible**:
    - The client computes the `ClientProof` as:
      ```plaintext
      ClientProof = ClientKey XOR ClientSignature
      ```
    - The server, after computing the `ClientSignature`, can reverse this operation to recover the `ClientKey`:
      ```plaintext
      ClientKey = ClientProof XOR ClientSignature
      ```

3. **Hashing is One-Way**:
    - The server verifies the `ClientKey` by checking:
      ```plaintext
      H(ClientKey) == StoredKey
      ```
    - Because the hash function `H` (SHA-256) is **one-way and collision-resistant**, the only way this equality will hold is if the client used the correct password to derive the `ClientKey`.

4. **Shared Knowledge of `AuthMessage`**:
    - Both the client and server compute the same `AuthMessage`, which includes:
        - The client and server nonces.
        - The username.
        - Exchanged messages.
    - This ensures that the `ClientSignature` (and therefore the `ClientProof`) is tied to the specific authentication session.

---

### **Mathematical Principles Behind It**

1. **Hash Functions**:
    - Hash functions (like SHA-256) are one-way functions, meaning it’s computationally infeasible to reverse the hash to recover the input.
    - This ensures that even if an attacker obtains the `StoredKey`, they cannot retrieve the `ClientKey` or the password.

2. **HMAC**:
    - HMAC is a keyed hash function, meaning it combines a secret key (here, `StoredKey`) and a message (`AuthMessage`) to produce a secure hash.
    - HMAC is deterministic but cryptographically secure, ensuring that it’s infeasible to guess the input values (key or message) from the output.

3. **XOR**:
    - XOR is a reversible operation, meaning:
      ```plaintext
      x XOR y XOR y = x
      ```
    - This property allows the server to recover `ClientKey` from the `ClientProof` and `ClientSignature`.

4. **Collision Resistance**:
    - The hash function used in `H(ClientKey)` is collision-resistant, meaning it’s infeasible to find two different inputs that produce the same output.
    - This ensures that the server's verification (`H(ClientKey) == StoredKey`) is secure and cannot be bypassed.

---

### **Summary of Verification Process**
1. **Shared Knowledge**:
    - Both the client and server share the `StoredKey` (on the server side) and the algorithmic process to compute the `ClientKey`, `ClientSignature`, and `ClientProof`.

2. **Client Proves Knowledge Without Revealing Password**:
    - The client uses the password-derived `StoredKey` and session-specific `AuthMessage` to compute the `ClientProof`. The proof reveals no information about the password itself.

3. **Server Verifies Without the Password**:
    - The server uses the stored `StoredKey` to derive the `ClientSignature` and checks that the reconstructed `ClientKey` produces the stored `StoredKey`.

4. **Mathematical Security**:
    - The process relies on the properties of:
        - **Hash functions** (one-way, collision-resistant).
        - **HMAC** (secure message authentication).
        - **XOR** (reversible operation).

Thus, the server can verify the `ClientProof` because the combination of HMAC, XOR, and hash functions ensures that the client could only generate a valid proof if they possess the correct password.

# Signup and Server side store

You're correct that in **SCRAM-SHA-256**, the **client does not send the plain text password** to the server, even during the very first exchange. Instead, the client sends a derived proof of the password, which allows the server to verify it without ever receiving or storing the plain text password. Here's how the hashed password (`StoredKey`) is securely established during the **first interaction**:

---

### **Key Points About SCRAM-SHA-256**
1. **Plain Text Password is Never Sent**:
    - The client derives hashed values (e.g., `SaltedPassword`, `StoredKey`, etc.) using the password and a server-provided salt.
    - These derived values are used in the challenge-response process, but the password itself is never exposed.

2. **Server Stores Only Derived Values**:
    - The server does not store the password. Instead, it stores:
        - **Salt**: A random value unique to each user.
        - **Iteration Count**: Number of iterations used in PBKDF2.
        - **StoredKey**: A hashed key derived from the password.
        - **ServerKey**: Another derived key used to authenticate the server to the client.

3. **Secure First-Time Exchange**:
    - During the first-time registration (or "signup"), the client securely sends the **derived hashed password** (`StoredKey` and `ServerKey`) to the server, not the plain text password.

---

### **How is the Hashed Password First Sent to the Server?**

#### **1. First-Time Registration Process**
When a user registers for the first time, the following happens:

1. **Client Derives the Password-Related Values**:
    - The client uses the plaintext password to compute the necessary values:
        - Generate a random salt (`Salt`).
        - Derive the `SaltedPassword` using PBKDF2:
          ```plaintext
          SaltedPassword = PBKDF2(SHA-256, Password, Salt, Iterations)
          ```
        - Compute the `ClientKey` and `StoredKey`:
          ```plaintext
          ClientKey = HMAC(SaltedPassword, "Client Key")
          StoredKey = H(ClientKey)
          ```
        - Compute the `ServerKey`:
          ```plaintext
          ServerKey = HMAC(SaltedPassword, "Server Key")
          ```

2. **The Client Sends the Hashed Values to the Server**:
    - The client sends the following data to the server:
        - **Username**: The user's identity.
        - **Salt**: The random salt used for password hashing.
        - **Iteration Count**: Number of iterations for PBKDF2.
        - **StoredKey**: The hashed key derived from the password.
        - **ServerKey**: The server's key derived from the password.

   Example payload sent:
   ```plaintext
   {
       "username": "user1",
       "salt": "random_salt_value",
       "iterations": 10000,
       "storedKey": "hashed_stored_key",
       "serverKey": "hashed_server_key"
   }
   ```

3. **Server Stores the Derived Values**:
    - The server stores this data in its database:
        - `Username`: To identify the user.
        - `Salt`: To ensure unique password hashing.
        - `Iterations`: To verify the password consistently.
        - `StoredKey`: To verify the client's proof during login.
        - `ServerKey`: To prove its own identity to the client.

---

#### **2. Why Doesn't the Server Need the Plain Text Password?**
- The server never needs the plain text password because the SCRAM protocol is designed around **mutual proof of possession** of the password.
- By storing only derived values (`StoredKey`, `ServerKey`), the server can verify the client’s authenticity during the login process using challenge-response authentication.

---

### **Subsequent Logins (Authentication)**

When the user logs in later:

1. **Client Computes Proof**:
    - The client retrieves the password from its memory or storage (e.g., user input) and derives:
        - `SaltedPassword` using the stored salt and iteration count.
        - `ClientKey` and `StoredKey`.
        - `ClientProof` using:
          ```plaintext
          ClientProof = ClientKey XOR HMAC(StoredKey, AuthMessage)
          ```

2. **Client Sends Proof**:
    - The client sends:
        - `ClientProof`
        - Combined nonce (`client_nonce + server_nonce`)

3. **Server Verifies Proof**:
    - The server:
        - Computes the expected `ClientSignature` using its stored `StoredKey` and the `AuthMessage`.
        - Reconstructs the `ClientKey`:
          ```plaintext
          ClientKey = ClientProof XOR ClientSignature
          ```
        - Verifies that:
          ```plaintext
          H(ClientKey) == StoredKey
          ```

4. **Server Responds with Proof**:
    - The server sends back the `ServerProof` to prove its identity:
      ```plaintext
      ServerProof = HMAC(ServerKey, AuthMessage)
      ```

---

### **Why is This Secure?**
1. **Password is Never Transmitted**:
    - The client only sends derived hashes (`StoredKey`, `ServerKey`, or proofs), which cannot be reversed to recover the password.

2. **Salting Prevents Precomputed Attacks**:
    - The client uses a unique salt for each user, ensuring even users with the same password have different hashed values.

3. **Mutual Authentication**:
    - Both the client and server prove they know the password without sharing it, ensuring resistance to **man-in-the-middle (MITM)** attacks.

4. **Iterative Hashing Adds Brute Force Resistance**:
    - PBKDF2 with thousands of iterations makes brute-forcing computationally expensive.

---

### **Summary**

- During **first-time registration**, the client:
    - Derives `StoredKey` and `ServerKey` using the password, salt, and iterations.
    - Sends these keys and the salt to the server.
- The **plaintext password is never sent** to the server.
- The server stores only the salt, iteration count, and derived keys, which it uses for verifying proofs in future logins.

This ensures that even if the database is compromised, the attacker's ability to recover the password is significantly limited.

# Leak of StoredKey

Great questions! Let's break this down step by step:

---

### **1. What Does `AuthMessage` Consist Of?**

In **SCRAM-SHA-256**, the `AuthMessage` is a critical component used to compute the `ClientProof` and the `ServerProof`. It ensures the integrity of the authentication process by binding together the exchanged messages. The `AuthMessage` consists of:

```plaintext
AuthMessage = client-first-message-bare + "," +
              server-first-message + "," +
              client-final-message-without-proof
```

#### **Details:**
- **`client-first-message-bare`**:
    - This is part of the initial message sent by the client, excluding the optional `gs2-header`.
    - Example:
      ```plaintext
      n=client_username,r=client_nonce
      ```
        - `n`: The username (used without escaping special characters).
        - `r`: The client-generated random nonce.

- **`server-first-message`**:
    - This is the server’s response to the client’s first message.
    - Example:
      ```plaintext
      r=client_nonce+server_nonce,s=base64_salt,i=iterations
      ```
        - `r`: The combined client and server nonces.
        - `s`: The base64-encoded salt for the user.
        - `i`: The iteration count for PBKDF2.

- **`client-final-message-without-proof`**:
    - This is the final message sent by the client, **excluding the proof**.
    - Example:
      ```plaintext
      c=base64_gs2_header,r=client_nonce+server_nonce
      ```
        - `c`: The base64-encoded GS2 header (usually indicates channel binding).
        - `r`: The combined client and server nonces.

#### **Purpose of `AuthMessage`:**
- It ties **all prior messages** in the SCRAM authentication process together, ensuring that the `ClientProof` and `ServerProof` are bound to this specific session.
- Any tampering or replay attacks would result in a mismatched `AuthMessage`, causing authentication to fail.

---

### **2. What If the `StoredKey` is Leaked? Can a Hacker Create the `ClientProof`?**

If the **`StoredKey`** is leaked, it **does not allow an attacker to directly create a valid `ClientProof`**. Here’s why:

#### **How `ClientProof` is Generated:**
The client generates the `ClientProof` as follows:

1. Compute the `ClientSignature`:
   ```plaintext
   ClientSignature = HMAC(StoredKey, AuthMessage)
   ```
    - `AuthMessage`: Contains all messages exchanged so far, ensuring that the proof is tied to this authentication session.

2. Derive the `ClientProof`:
   ```plaintext
   ClientProof = ClientKey XOR ClientSignature
   ```
    - `ClientKey` is derived from `StoredKey`:
      ```plaintext
      StoredKey = H(ClientKey)
      ```

#### **Why a Leaked `StoredKey` is Insufficient**:
1. **No Access to the `SaltedPassword`**:
    - The `StoredKey` is derived from the `SaltedPassword`, which itself is derived using:
      ```plaintext
      SaltedPassword = PBKDF2(SHA-256, Password, Salt, Iterations)
      ```
    - Without the **password**, **salt**, and **iteration count**, an attacker cannot reconstruct the `SaltedPassword` or `ClientKey`.

2. **HMAC Requires the `AuthMessage`**:
    - To compute the `ClientSignature`, the attacker needs the `AuthMessage`, which includes:
        - The unique `client_nonce` and `server_nonce` for the session.
    - These nonces must be generated during the actual authentication session. Without them, the attacker cannot compute a valid `ClientSignature`.

3. **XOR Cannot Be Reversed Without `ClientKey`**:
    - The attacker would also need the `ClientKey` to construct the `ClientProof`:
      ```plaintext
      ClientProof = ClientKey XOR ClientSignature
      ```
    - Since the `ClientKey` is tied to the `SaltedPassword` and ultimately to the password, it cannot be derived from the `StoredKey`.

#### **What an Attacker Can Do with `StoredKey`:**
While the `StoredKey` alone does not allow the attacker to directly create a valid `ClientProof`, it does pose a potential risk if combined with other information:
- If the **password database** is leaked (including the `StoredKey`, `ServerKey`, `salt`, and iteration count), the attacker could perform a **brute force attack** to guess the password:
    - Use the leaked salt and iteration count to repeatedly hash guessed passwords until the derived `StoredKey` matches the leaked one.

---

### **3. Mitigating Risks of a Leaked `StoredKey`**

To mitigate the impact of a leaked `StoredKey`, the following measures are essential:

#### **Use a Strong Password Policy**
- Enforce long, complex passwords to make brute-forcing impractical.

#### **Use High Iteration Counts for PBKDF2**
- Increase the number of iterations to make password cracking via brute force computationally expensive.
- E.g., 10,000–100,000 iterations or more (based on system performance).

#### **Use a Unique Salt for Each User**
- Ensure that each user has a random, unique salt.
- This prevents attackers from using precomputed hash tables (rainbow tables).

#### **Monitor for Credential Leaks**
- Regularly monitor for data breaches and take action (e.g., force password resets) if any sensitive data is leaked.

#### **Consider Argon2 or BCrypt**
- Use modern, memory-hard password hashing algorithms like **Argon2** or **BCrypt** instead of PBKDF2 to further resist brute-force attacks.

---

### **4. Summary**

1. **`AuthMessage`** consists of:
    - `client-first-message-bare`, `server-first-message`, and `client-final-message-without-proof`.
    - It ensures that the `ClientProof` and `ServerProof` are tied to the specific session.

2. **If `StoredKey` is Leaked**:
    - An attacker **cannot directly create the `ClientProof`** because:
        - They need the session-specific `AuthMessage`.
        - They cannot derive the `ClientKey` without the password.

3. **Mitigation**:
    - Use strong passwords, unique salts, high iteration counts, and modern hashing algorithms to make brute force attacks infeasible.

By design, SCRAM-SHA-256 ensures that even if certain derived values are leaked, the protocol remains secure against unauthorized access unless the attacker has the original password.
