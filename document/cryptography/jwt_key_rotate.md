# Key rotate

Great question! JWT signing key rotation is critical for security in OAuth2/OIDC servers. Here's how to implement it:

## 1. Key Rotation Concepts

JWT tokens are signed with a private key and verified with a public key (for asymmetric algorithms like RS256). When rotating keys:

- **Old keys must remain available** for verification until all tokens signed with them expire
- **New keys are used** for signing new tokens
- **Multiple keys are published** in the JWKS (JSON Web Key Set) endpoint
- Each key has a unique **Key ID (kid)** in the JWT header

## 2. Spring Authorization Server - Key Rotation Implementation

```java
@Configuration
public class JwtKeyRotationConfig {
    
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        return new RotatingJWKSource();
    }
    
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }
    
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }
}

// Custom rotating JWK source
public class RotatingJWKSource implements JWKSource<SecurityContext> {
    
    private final Map<String, RSAKey> keys = new ConcurrentHashMap<>();
    private volatile String currentKeyId;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public RotatingJWKSource() {
        // Initialize with first key
        rotateKey();
        
        // Schedule automatic rotation every 30 days
        scheduler.scheduleAtFixedRate(
            this::rotateKey,
            30, // Initial delay
            30, // Period
            TimeUnit.DAYS
        );
        
        // Schedule cleanup of old keys after 60 days (2x token lifetime)
        scheduler.scheduleAtFixedRate(
            this::cleanupOldKeys,
            60,
            60,
            TimeUnit.DAYS
        );
    }
    
    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) {
        // Return all active keys for verification
        List<JWK> selectedKeys = new ArrayList<>();
        
        for (RSAKey key : keys.values()) {
            if (jwkSelector.getMatcher().matches(key)) {
                selectedKeys.add(key);
            }
        }
        
        return selectedKeys;
    }
    
    // Get current key for signing
    public RSAKey getCurrentKey() {
        return keys.get(currentKeyId);
    }
    
    private synchronized void rotateKey() {
        try {
            String newKeyId = generateKeyId();
            RSAKey newKey = generateRSAKey(newKeyId);
            
            keys.put(newKeyId, newKey);
            currentKeyId = newKeyId;
            
            log.info("Rotated JWT signing key. New key ID: {}", newKeyId);
        } catch (Exception e) {
            log.error("Failed to rotate JWT key", e);
        }
    }
    
    private void cleanupOldKeys() {
        // Keep only current key and previous key
        if (keys.size() > 2) {
            List<String> sortedKeyIds = keys.keySet().stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toList());
            
            // Remove all but the 2 most recent keys
            for (int i = 2; i < sortedKeyIds.size(); i++) {
                String oldKeyId = sortedKeyIds.get(i);
                keys.remove(oldKeyId);
                log.info("Removed old JWT key: {}", oldKeyId);
            }
        }
    }
    
    private RSAKey generateRSAKey(String keyId) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        
        return new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(keyId)
            .algorithm(JWSAlgorithm.RS256)
            .keyUse(KeyUse.SIGNATURE)
            .build();
    }
    
    private String generateKeyId() {
        // Use timestamp-based key ID
        return "key-" + Instant.now().toEpochMilli();
    }
}
```

## 3. Enhanced Version with Database Persistence

```java
@Entity
@Table(name = "jwt_keys")
public class JwtKeyEntity {
    
    @Id
    private String keyId;
    
    @Column(columnDefinition = "TEXT")
    private String publicKey;
    
    @Column(columnDefinition = "TEXT")
    private String privateKey;
    
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean active;
    private boolean current;
    
    // Getters and setters
}

@Repository
public interface JwtKeyRepository extends JpaRepository<JwtKeyEntity, String> {
    List<JwtKeyEntity> findByActiveTrue();
    Optional<JwtKeyEntity> findByCurrentTrue();
    List<JwtKeyEntity> findByExpiresAtBefore(LocalDateTime dateTime);
}

@Service
public class JwtKeyRotationService {
    
    @Autowired
    private JwtKeyRepository keyRepository;
    
    private final Map<String, RSAKey> cachedKeys = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        // Load existing keys from database
        loadKeysFromDatabase();
        
        // Ensure we have at least one key
        if (getCurrentKey() == null) {
            rotateKey();
        }
    }
    
    @Scheduled(cron = "0 0 0 1 * ?") // First day of every month at midnight
    public void scheduledKeyRotation() {
        rotateKey();
    }
    
    @Scheduled(cron = "0 0 1 * * ?") // Daily at 1 AM
    public void cleanupExpiredKeys() {
        LocalDateTime now = LocalDateTime.now();
        List<JwtKeyEntity> expiredKeys = keyRepository.findByExpiresAtBefore(now);
        
        expiredKeys.forEach(key -> {
            keyRepository.delete(key);
            cachedKeys.remove(key.getKeyId());
            log.info("Deleted expired key: {}", key.getKeyId());
        });
    }
    
    @Transactional
    public synchronized void rotateKey() {
        try {
            // Mark current key as non-current
            keyRepository.findByCurrentTrue().ifPresent(current -> {
                current.setCurrent(false);
                keyRepository.save(current);
            });
            
            // Generate new key
            String keyId = "key-" + UUID.randomUUID().toString();
            RSAKey rsaKey = generateRSAKey(keyId);
            
            // Save to database
            JwtKeyEntity keyEntity = new JwtKeyEntity();
            keyEntity.setKeyId(keyId);
            keyEntity.setPublicKey(rsaKey.toPublicJWK().toJSONString());
            keyEntity.setPrivateKey(rsaKey.toJSONString());
            keyEntity.setCreatedAt(LocalDateTime.now());
            keyEntity.setExpiresAt(LocalDateTime.now().plusDays(60)); // Keep for 60 days
            keyEntity.setActive(true);
            keyEntity.setCurrent(true);
            
            keyRepository.save(keyEntity);
            
            // Update cache
            cachedKeys.put(keyId, rsaKey);
            
            log.info("Successfully rotated JWT key. New key ID: {}", keyId);
            
        } catch (Exception e) {
            log.error("Failed to rotate JWT key", e);
            throw new RuntimeException("Key rotation failed", e);
        }
    }
    
    public RSAKey getCurrentKey() {
        return keyRepository.findByCurrentTrue()
            .map(this::toRSAKey)
            .orElse(null);
    }
    
    public List<RSAKey> getAllActiveKeys() {
        return keyRepository.findByActiveTrue().stream()
            .map(this::toRSAKey)
            .collect(Collectors.toList());
    }
    
    private void loadKeysFromDatabase() {
        List<JwtKeyEntity> activeKeys = keyRepository.findByActiveTrue();
        activeKeys.forEach(key -> {
            try {
                RSAKey rsaKey = toRSAKey(key);
                cachedKeys.put(key.getKeyId(), rsaKey);
            } catch (Exception e) {
                log.error("Failed to load key: {}", key.getKeyId(), e);
            }
        });
    }
    
    private RSAKey toRSAKey(JwtKeyEntity entity) {
        try {
            return RSAKey.parse(entity.getPrivateKey());
        } catch (Exception e) {
            log.error("Failed to parse RSA key", e);
            throw new RuntimeException(e);
        }
    }
    
    private RSAKey generateRSAKey(String keyId) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        
        return new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(keyId)
            .algorithm(JWSAlgorithm.RS256)
            .keyUse(KeyUse.SIGNATURE)
            .build();
    }
}
```

## 4. JWK Source with Database Integration

```java
@Component
public class DatabaseJWKSource implements JWKSource<SecurityContext> {
    
    @Autowired
    private JwtKeyRotationService keyRotationService;
    
    @Override
    public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) {
        // Return all active keys for verification
        List<RSAKey> activeKeys = keyRotationService.getAllActiveKeys();
        
        return activeKeys.stream()
            .filter(key -> jwkSelector.getMatcher().matches(key))
            .collect(Collectors.toList());
    }
}
```

## 5. Custom Token Customizer with Key ID

```java
@Bean
public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer(
        JwtKeyRotationService keyRotationService) {
    
    return context -> {
        if (context.getTokenType().equals(OAuth2TokenType.ACCESS_TOKEN)) {
            // Get current signing key
            RSAKey currentKey = keyRotationService.getCurrentKey();
            
            // Add key ID to JWT header
            context.getJwsHeader()
                .keyId(currentKey.getKeyID())
                .algorithm(SignatureAlgorithm.RS256);
            
            // Add custom claims
            context.getClaims()
                .claim("issued_at", Instant.now())
                .claim("key_version", currentKey.getKeyID());
        }
    };
}
```

## 6. JWKS Endpoint Configuration

```java
@Configuration
public class JwksEndpointConfig {
    
    @Bean
    public SecurityFilterChain jwksSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/.well-known/jwks.json")
            .authorizeHttpRequests(authorize -> 
                authorize.anyRequest().permitAll()
            );
        
        return http.build();
    }
}

@RestController
public class JwksController {
    
    @Autowired
    private JwtKeyRotationService keyRotationService;
    
    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwks() {
        List<RSAKey> activeKeys = keyRotationService.getAllActiveKeys();
        
        JWKSet jwkSet = new JWKSet(activeKeys.stream()
            .map(RSAKey::toPublicJWK) // Only expose public keys
            .collect(Collectors.toList()));
        
        return jwkSet.toJSONObject();
    }
}
```

## 7. Complete Authorization Server Configuration

```java
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig {
    
    @Autowired
    private JwtKeyRotationService keyRotationService;
    
    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
            .issuer("https://your-auth-server.com")
            .jwkSetEndpoint("/.well-known/jwks.json")
            .build();
    }
    
    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        return new JWKSource<SecurityContext>() {
            @Override
            public List<JWK> get(JWKSelector jwkSelector, SecurityContext context) {
                List<RSAKey> activeKeys = keyRotationService.getAllActiveKeys();
                
                return activeKeys.stream()
                    .filter(key -> jwkSelector.getMatcher().matches(key))
                    .collect(Collectors.toList());
            }
        };
    }
    
    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }
    
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }
    
    @Bean
    public OAuth2TokenGenerator<?> tokenGenerator(
            JwtEncoder jwtEncoder,
            OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer) {
        
        JwtGenerator jwtGenerator = new JwtGenerator(jwtEncoder);
        jwtGenerator.setJwtCustomizer(tokenCustomizer);
        
        OAuth2AccessTokenGenerator accessTokenGenerator = new OAuth2AccessTokenGenerator();
        OAuth2RefreshTokenGenerator refreshTokenGenerator = new OAuth2RefreshTokenGenerator();
        
        return new DelegatingOAuth2TokenGenerator(
            jwtGenerator, accessTokenGenerator, refreshTokenGenerator);
    }
}
```

## 8. Manual Key Rotation REST Endpoint

```java
@RestController
@RequestMapping("/admin/keys")
public class KeyManagementController {
    
    @Autowired
    private JwtKeyRotationService keyRotationService;
    
    // Trigger manual key rotation
    @PostMapping("/rotate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> rotateKey() {
        try {
            keyRotationService.rotateKey();
            return ResponseEntity.ok(Map.of(
                "message", "Key rotated successfully",
                "newKeyId", keyRotationService.getCurrentKey().getKeyID()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    // Get all active keys (public info only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllKeys() {
        List<RSAKey> keys = keyRotationService.getAllActiveKeys();
        
        List<Map<String, Object>> keyInfo = keys.stream()
            .map(key -> Map.of(
                "keyId", key.getKeyID(),
                "algorithm", key.getAlgorithm().getName(),
                "use", key.getKeyUse().getValue()
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(keyInfo);
    }
    
    // Get current signing key info
    @GetMapping("/current")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getCurrentKey() {
        RSAKey currentKey = keyRotationService.getCurrentKey();
        
        return ResponseEntity.ok(Map.of(
            "keyId", currentKey.getKeyID(),
            "algorithm", currentKey.getAlgorithm().getName()
        ));
    }
}
```

## 9. Key Rotation with Vault Integration (Advanced)

```java
@Service
public class VaultJwtKeyRotationService {
    
    @Autowired
    private VaultTemplate vaultTemplate;
    
    private static final String VAULT_PATH = "secret/jwt-keys";
    
    public void rotateKeyInVault() {
        try {
            // Generate new key
            String keyId = "key-" + UUID.randomUUID().toString();
            RSAKey rsaKey = generateRSAKey(keyId);
            
            // Store in Vault
            Map<String, Object> keyData = new HashMap<>();
            keyData.put("keyId", keyId);
            keyData.put("privateKey", rsaKey.toJSONString());
            keyData.put("publicKey", rsaKey.toPublicJWK().toJSONString());
            keyData.put("createdAt", Instant.now().toString());
            
            vaultTemplate.write(VAULT_PATH + "/" + keyId, keyData);
            
            // Update current key reference
            Map<String, Object> currentRef = new HashMap<>();
            currentRef.put("currentKeyId", keyId);
            vaultTemplate.write(VAULT_PATH + "/current", currentRef);
            
            log.info("Key rotated in Vault: {}", keyId);
            
        } catch (Exception e) {
            log.error("Failed to rotate key in Vault", e);
            throw new RuntimeException(e);
        }
    }
    
    public RSAKey getCurrentKeyFromVault() {
        VaultResponseSupport<Map> response = 
            vaultTemplate.read(VAULT_PATH + "/current", Map.class);
        
        if (response != null && response.getData() != null) {
            String currentKeyId = (String) response.getData().get("currentKeyId");
            return getKeyFromVault(currentKeyId);
        }
        
        return null;
    }
    
    public List<RSAKey> getAllActiveKeysFromVault() {
        // Implementation to list and retrieve all active keys from Vault
        // This depends on your Vault setup
        return new ArrayList<>();
    }
    
    private RSAKey getKeyFromVault(String keyId) {
        VaultResponseSupport<Map> response = 
            vaultTemplate.read(VAULT_PATH + "/" + keyId, Map.class);
        
        if (response != null && response.getData() != null) {
            try {
                String privateKeyJson = (String) response.getData().get("privateKey");
                return RSAKey.parse(privateKeyJson);
            } catch (Exception e) {
                log.error("Failed to parse key from Vault", e);
                return null;
            }
        }
        
        return null;
    }
    
    private RSAKey generateRSAKey(String keyId) throws Exception {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();
        
        return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
            .privateKey((RSAPrivateKey) keyPair.getPrivate())
            .keyID(keyId)
            .algorithm(JWSAlgorithm.RS256)
            .keyUse(KeyUse.SIGNATURE)
            .build();
    }
}
```

## Key Rotation Best Practices

1. **Rotation Schedule**: Rotate keys every 30-90 days
2. **Grace Period**: Keep old keys active for at least 2x max token lifetime
3. **Key ID**: Always include `kid` in JWT header
4. **JWKS Endpoint**: Publish all active public keys
5. **Monitoring**: Log all rotation events
6. **Backup**: Store keys securely (database, Vault, KMS)
7. **Testing**: Test rotation in staging before production
8. **Emergency Rotation**: Have a process for immediate rotation if compromised

The database-backed approach (#3) is recommended for production as it provides persistence, audit trail, and supports multi-instance deployments.