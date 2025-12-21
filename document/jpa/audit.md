# Enable JPA Audit

`@EnableJpaAuditing` in Spring enables automatic population of audit fields in your JPA entities. It tracks **who** created/modified an entity and **when** it happened.

**What it does:**

Automatically fills these fields:
- `@CreatedBy` - Who created the entity
- `@CreatedDate` - When the entity was created
- `@LastModifiedBy` - Who last modified the entity
- `@LastModifiedDate` - When the entity was last modified

**Basic Setup:**

**1. Enable JPA Auditing:**
```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
```

**2. Create a Base Entity with Audit Fields:**
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class Auditable {
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdDate;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastModifiedDate;
    
    @CreatedBy
    @Column(nullable = false, updatable = false)
    private String createdBy;
    
    @LastModifiedBy
    @Column(nullable = false)
    private String lastModifiedBy;
    
    // Getters and setters
}
```

**3. Use in Your Entities:**
```java
@Entity
public class User extends Auditable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String username;
    private String email;
    
    // Spring automatically fills createdDate, lastModifiedDate, 
    // createdBy, lastModifiedBy from parent class
}
```

**How to Provide the "Who" (CreatedBy/LastModifiedBy):**

**Option 1: Using AuditorAware (Most Common):**
```java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {
    
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }
}

public class AuditorAwareImpl implements AuditorAware<String> {
    
    @Override
    public Optional<String> getCurrentAuditor() {
        // Get current user from Spring Security
        Authentication authentication = SecurityContextHolder
            .getContext()
            .getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        
        return Optional.of(authentication.getName());
    }
}
```

**Option 2: Lambda Style (Java 8+):**
```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.ofNullable(SecurityContextHolder.getContext())
            .map(SecurityContext::getAuthentication)
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getName);
    }
}
```

**Option 3: Using User ID instead of Username:**
```java
@Bean
public AuditorAware<Long> auditorProvider() {
    return () -> Optional.ofNullable(SecurityContextHolder.getContext())
        .map(SecurityContext::getAuthentication)
        .filter(Authentication::isAuthenticated)
        .map(auth -> ((CustomUserDetails) auth.getPrincipal()).getUserId());
}

// Then in your entity:
@CreatedBy
private Long createdBy;

@LastModifiedBy
private Long lastModifiedBy;
```

**What Happens Automatically:**

```java
// When you save a new entity:
User user = new User();
user.setUsername("john");
userRepository.save(user);

// Spring automatically sets:
// user.createdDate = LocalDateTime.now()
// user.createdBy = "currentUser" (from AuditorAware)
// user.lastModifiedDate = LocalDateTime.now()
// user.lastModifiedBy = "currentUser"

// When you update an existing entity:
user.setEmail("new@email.com");
userRepository.save(user);

// Spring automatically updates:
// user.lastModifiedDate = LocalDateTime.now()
// user.lastModifiedBy = "currentUser"
// (createdDate and createdBy remain unchanged)
```

**Advanced Features:**

**1. Custom Date Provider:**
```java
@Configuration
@EnableJpaAuditing(dateTimeProviderRef = "dateTimeProvider")
public class JpaConfig {
    
    @Bean
    public DateTimeProvider dateTimeProvider() {
        return () -> Optional.of(LocalDateTime.now(ZoneId.of("UTC")));
    }
}
```

**2. Conditional Auditing:**
```java
@Configuration
@EnableJpaAuditing(modifyOnCreate = false) // Don't set lastModified on create
public class JpaConfig {
}
```

**3. Using with MongoDB (instead of JPA):**
```java
@Configuration
@EnableMongoAuditing
public class MongoConfig {
    
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> Optional.of(getCurrentUser());
    }
}
```

**Common Use Cases:**

- Track who created/modified records for compliance
- Audit trails for security
- Debugging data changes
- Soft deletes with audit info
- Legal/regulatory requirements

**Complete Example:**

```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {
    
    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            Authentication auth = SecurityContextHolder
                .getContext()
                .getAuthentication();
            
            if (auth == null || !auth.isAuthenticated() 
                || "anonymousUser".equals(auth.getPrincipal())) {
                return Optional.of("system");
            }
            
            return Optional.of(auth.getName());
        };
    }
}

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(updatable = false)
    private String createdBy;
    
    @LastModifiedBy
    private String updatedBy;
}

@Entity
public class Product extends BaseEntity {
    @Id
    @GeneratedValue
    private Long id;
    
    private String name;
    private BigDecimal price;
}
```

In summary, `@EnableJpaAuditing` saves you from manually tracking creation/modification metadata in every entity - Spring does it automatically.