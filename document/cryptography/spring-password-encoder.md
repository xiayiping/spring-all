# Encoder

In Spring Security, **password hashing and salting** are handled by the **PasswordEncoder** interface. Spring Security provides various implementations of `PasswordEncoder` to securely hash and validate passwords, including support for salting and modern hashing algorithms.

Here’s how Spring Security handles salting, hashing, and password encoding and how you can use it effectively in your Spring Boot application.

---

### **How Spring Security Handles Salt and Digest Methods**

1. **Built-in `PasswordEncoder` Implementations**:
   Spring Security provides several `PasswordEncoder` implementations that automatically handle salting and hashing for you. Some commonly used ones are:

    - `BCryptPasswordEncoder`:
        - Uses the **bcrypt hashing algorithm**, which includes an internal random salt.
        - The salt is generated automatically and embedded in the hashed password.
    - `Pbkdf2PasswordEncoder`:
        - Uses the **PBKDF2 hashing algorithm** with a configurable salt.
        - You can provide your own salt or let it generate one automatically.
    - `Argon2PasswordEncoder`:
        - Uses the **Argon2 hashing algorithm** (a modern, secure password hashing algorithm with salting).
    - `DelegatingPasswordEncoder`:
        - Supports multiple encoders and allows you to specify the hashing algorithm to use with a prefix.

2. **How Salting Works**:
    - Password encoders like `BCryptPasswordEncoder` or `Pbkdf2PasswordEncoder` automatically generate a unique salt for each password.
    - The salt is stored as part of the hashed password (e.g., in the `BCrypt` algorithm, the salt is embedded in the hashed string).
    - When validating a password, the encoder extracts the salt from the stored hash and uses it during the verification process.

3. **Digest Method**:
    - The hashing algorithm (e.g., bcrypt, PBKDF2, SHA-256) is defined by the encoder implementation.
    - For example, `BCryptPasswordEncoder` uses the bcrypt algorithm, which is specifically designed for secure password storage.

---

### **How to Use a Password Encoder in Spring Boot**

#### **1. Configure a PasswordEncoder Bean**
You need to define a `PasswordEncoder` bean in your Spring Boot application:

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Use bcrypt for hashing
    }
}
```

- **`BCryptPasswordEncoder`**: Automatically salts and hashes the password. The resulting hash includes the salt.

You can also use other encoders like `Pbkdf2PasswordEncoder` or `Argon2PasswordEncoder` depending on your requirements.

---

#### **2. Hash a Password**
You can use the `PasswordEncoder` to hash a password when saving a new user:

```java
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(String username, String rawPassword) {
        // Hash the raw password
        String hashedPassword = passwordEncoder.encode(rawPassword);

        // Save the username and hashed password to the database
        System.out.println("Username: " + username);
        System.out.println("Hashed Password: " + hashedPassword);
    }
}
```

Usage:

```java
userService.registerUser("john_doe", "my_secure_password");
```

---

#### **3. Validate a Password**
When validating a password (e.g., during login), use the `matches()` method of `PasswordEncoder`:

```java
public boolean authenticate(String rawPassword, String storedHashedPassword) {
    // Check if the raw password matches the stored hashed password
    return passwordEncoder.matches(rawPassword, storedHashedPassword);
}
```

- The `matches` method performs these steps internally:
    1. Extracts the salt from the stored hash (if applicable).
    2. Hashes the raw password using the same salt and algorithm.
    3. Compares the result with the stored hash.

---

#### **4. Use `DelegatingPasswordEncoder` for Multiple Hashing Algorithms**
`DelegatingPasswordEncoder` allows you to support multiple hashing algorithms. It prefixes the hashed password with the algorithm identifier (e.g., `{bcrypt}`). Example:

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Define available encoders
        Map<String, PasswordEncoder> encoders = new HashMap<>();
        encoders.put("bcrypt", new BCryptPasswordEncoder());
        encoders.put("pbkdf2", new Pbkdf2PasswordEncoder());

        // Default to bcrypt
        return new DelegatingPasswordEncoder("bcrypt", encoders);
    }
}
```

- When encoding a password:
  ```java
  String hashedPassword = passwordEncoder.encode("my_secure_password");
  System.out.println("Hashed Password: " + hashedPassword);
  ```
  Example output:
  ```plaintext
  {bcrypt}$2a$10$X9h4gK3J8XjHzz1G5bS7Oe6XU5c7ePjZPqA3Q9FJ.kqF9z1jPj.Vu
  ```

- When validating, the encoder extracts the prefix (e.g., `{bcrypt}`) and uses the corresponding algorithm.

---

### **5. Store and Retrieve Hashed Passwords**
When storing passwords in your database:
- Save only the hashed password (e.g., the output of `passwordEncoder.encode()`).
- Do **not** save plaintext passwords.
- Example structure for a `User` entity:
  ```java
  @Entity
  public class User {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;

      private String username;
      private String password; // Store the hashed password

      // Getters and setters
  }
  ```

---

### **6. Full Example Workflow**

#### **Register a User**
1. Get the user's plaintext password.
2. Hash the password using `passwordEncoder.encode()`.
3. Store the hashed password in the database.

#### **Authenticate a User**
1. Retrieve the hashed password from the database by username.
2. Use `passwordEncoder.matches(rawPassword, storedHashedPassword)` to validate the password.

---

### **Conclusion**
- Spring Security’s `PasswordEncoder` provides secure methods for hashing and salting passwords.
- Use encoders like `BCryptPasswordEncoder` (recommended) or `Pbkdf2PasswordEncoder` for modern, secure password hashing.
- Always use the `PasswordEncoder` to hash passwords before storing them, and use `matches()` for validation during login.
- Consider using `DelegatingPasswordEncoder` if you need to support multiple algorithms.