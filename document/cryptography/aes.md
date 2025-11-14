# AES-GCM 

(Advanced Encryption Standard in Galois Counter Mode) is an encryption mode that provides both confidentiality and data integrity. Unlike AES in ECB or CBC mode, AES-GCM requires an **IV (Initialization Vector)** in addition to the **secret key** for encryption and decryption.

Below, I'll explain **why the IV is needed** and provide a **Java example** for encrypting and decrypting data with AES-GCM-256.

---

### **Why does AES-GCM need an IV in addition to the secret key?**

1. **Uniqueness Requirement**:
   - AES-GCM is a mode of operation designed to work securely only when the IV is **unique** for each encryption operation using the same key.
   - The IV ensures that even if the same plaintext is encrypted multiple times with the same key, the ciphertext will still be different.

2. **Counter-based Mode**:
   - GCM uses a counter mode internally to encrypt plaintext. The IV serves as the starting point for the counter, ensuring that the encryption operation doesn't repeat patterns.

3. **Authentication**:
   - AES-GCM not only encrypts data but also generates an **authentication tag** to verify the integrity of both the ciphertext and the associated data. A unique IV ensures the authentication tag is valid and prevents replay attacks.

4. **Security Risks Without IV**:
   - Reusing the same IV with the same key can result in catastrophic security breaches, such as leaking parts of the plaintext or allowing attackers to forge ciphertexts.

---

### **Java Example: AES-GCM-256 Encryption and Decryption**

To implement AES-GCM-256 in Java, you'll need:

1. A **256-bit secret key** (requires Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy for keys >128 bits).
2. A **12-byte IV** (recommended length for AES-GCM).
3. A library like `javax.crypto` for cryptographic operations.

Here's an example:

```java
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

public class AESGCMExample {

    private static final int AES_KEY_SIZE = 256; // Key size in bits
    private static final int GCM_IV_LENGTH = 12; // IV size in bytes
    private static final int GCM_TAG_LENGTH = 16; // Authentication tag length in bytes

    public static void main(String[] args) throws Exception {
        // Generate a random secret key
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(AES_KEY_SIZE);
        SecretKey secretKey = keyGen.generateKey();

        // Randomly generate a 12-byte IV
        byte[] iv = new byte[GCM_IV_LENGTH];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(iv);

        // Plaintext to encrypt
        String plaintext = "Hello, AES-GCM-256!";
        System.out.println("Original Text: " + plaintext);

        // Encrypt the plaintext
        byte[] ciphertext = encrypt(plaintext, secretKey, iv);

        // Decrypt the ciphertext
        String decryptedText = decrypt(ciphertext, secretKey, iv);

        System.out.println("Decrypted Text: " + decryptedText);
    }

    // AES-GCM Encryption
    public static byte[] encrypt(String plaintext, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

        // Encrypt the plaintext
        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        
        // Combine IV and ciphertext for easy transport (IV is needed for decryption)
        byte[] combined = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(ciphertext, 0, combined, iv.length, ciphertext.length);
        
        return combined;
    }

    // AES-GCM Decryption
    public static String decrypt(byte[] encryptedData, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH * Byte.SIZE, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

        // Extract the IV and ciphertext
        byte[] ciphertext = new byte[encryptedData.length - iv.length];
        System.arraycopy(encryptedData, iv.length, ciphertext, 0, ciphertext.length);

        // Decrypt the ciphertext
        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext, StandardCharsets.UTF_8);
    }
}
```

---

### **How It Works**
1. **Key Generation**:
   - A random 256-bit secret key is generated using `KeyGenerator`.

2. **IV Generation**:
   - A 12-byte IV is randomly generated using `SecureRandom`.

3. **Encryption**:
   - Initialize the `Cipher` in `AES/GCM/NoPadding` mode.
   - Use the IV and secret key to encrypt the plaintext.
   - Combine the IV and ciphertext for transport.

4. **Decryption**:
   - Extract the IV and ciphertext from the combined data.
   - Initialize the `Cipher` with the same IV and secret key.
   - Decrypt the ciphertext back to the plaintext.

---

### **Key Points**
- **IV Size**: Always use a 12-byte IV for AES-GCM, as it's the most efficient and secure size.
- **Key Rotation**: Regularly rotate your secret key to reduce the risk of compromise.
- **Authentication Tag**: AES-GCM provides an authentication tag (part of the ciphertext), ensuring data integrity and authenticity.

This implementation ensures secure and efficient encryption/decryption using AES-GCM-256.