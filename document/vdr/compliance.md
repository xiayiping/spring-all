# Compliance

# GDPR, HIPAA, and PCI-DSS Compliance - Detailed Guide

## 1. GDPR (General Data Protection Regulation) 🇪🇺

### Overview
- **Jurisdiction**: European Union (applies to ANY company processing EU citizen data)
- **Effective**: May 25, 2018
- **Penalties**: Up to €20 million or 4% of global annual revenue (whichever is higher)
- **Scope**: Personal data of EU residents, regardless of where processing occurs

### What is Personal Data?
Any information relating to an identified or identifiable natural person:
- **Direct identifiers**: Name, email, phone, address, SSN
- **Indirect identifiers**: IP address, cookie IDs, device IDs, location data
- **Special categories** (sensitive): Race, health data, biometric data, political opinions, sexual orientation, religious beliefs

### Key Principles

#### 1. Lawfulness, Fairness, Transparency
- Must have **legal basis** for processing:
    - **Consent**: Freely given, specific, informed, unambiguous
    - **Contract**: Necessary for contract performance
    - **Legal obligation**: Required by law
    - **Vital interests**: Protect life
    - **Public interest**: Official authority or public task
    - **Legitimate interests**: Balancing test (your interests vs. individual rights)

#### 2. Purpose Limitation
- Collect data for **specified, explicit, legitimate purposes**
- Cannot use for incompatible purposes later
- Example: ❌ Collect email for "account notifications", then use for marketing

#### 3. Data Minimization
- Only collect data that is **adequate, relevant, and necessary**
- Example: ❌ Require date of birth when only age verification needed

#### 4. Accuracy
- Keep data **accurate and up-to-date**
- Provide mechanisms for users to update their data
- Erase inaccurate data without delay

#### 5. Storage Limitation
- Keep data only as long as **necessary**
- Define retention periods
- Automatic deletion after retention expires

#### 6. Integrity and Confidentiality (Security)
- **Encryption** at rest and in transit
- **Access controls** - principle of least privilege
- **Pseudonymization/anonymization** where possible
- **Regular security assessments**

#### 7. Accountability
- **Demonstrate** compliance
- Maintain **records of processing activities** (ROPA)
- Conduct **Data Protection Impact Assessments** (DPIA) for high-risk processing

### Individual Rights (Data Subject Rights)

#### Right to Access (Article 15)
- Users can request copy of their personal data
- Must respond within **1 month** (extendable to 3 months)
- Provide data in **structured, commonly used, machine-readable format**

```java
// Example implementation
@GetMapping("/api/gdpr/data-export")
public ResponseEntity<GdprDataExport> exportUserData(
    @AuthenticationPrincipal UserDetails user
) {
    GdprDataExport export = new GdprDataExport();
    
    // Export all personal data
    export.userProfile = userRepository.findByUsername(user.getUsername());
    export.files = fileRepository.findByUserId(user.getUsername());
    export.auditLogs = auditRepository.findByUserId(user.getUsername());
    export.exportedAt = LocalDateTime.now();
    
    return ResponseEntity.ok(export);
}
```

#### Right to Rectification (Article 16)
- Users can correct inaccurate data
- Must update within 1 month

#### Right to Erasure / "Right to be Forgotten" (Article 17)
- Delete data when:
    - No longer necessary for original purpose
    - User withdraws consent
    - User objects to processing
    - Data processed unlawfully
- **Exceptions**: Legal obligations, public interest, legal claims

```java
@DeleteMapping("/api/gdpr/delete-account")
@Transactional
public ResponseEntity<Void> deleteUserAccount(
    @AuthenticationPrincipal UserDetails user
) {
    String userId = user.getUsername();
    
    // Check if we can delete (exceptions)
    if (hasActiveLegalHold(userId)) {
        throw new GdprException("Cannot delete - active legal hold");
    }
    
    // Anonymize instead of delete (preserve audit trail)
    anonymizeUserData(userId);
    
    // Or hard delete everything
    deleteAllUserData(userId);
    
    return ResponseEntity.noContent().build();
}
```

#### Right to Restriction (Article 18)
- Temporarily suspend processing while disputes are resolved

#### Right to Data Portability (Article 20)
- Receive data in machine-readable format (CSV, JSON, XML)
- Transmit data to another controller

```java
@GetMapping("/api/gdpr/data-portability")
public ResponseEntity<Resource> exportDataPortable(
    @AuthenticationPrincipal UserDetails user
) {
    // Export in standard format (JSON)
    String jsonData = exportToJson(user.getUsername());
    
    return ResponseEntity.ok()
        .contentType(MediaType.APPLICATION_JSON)
        .header(HttpHeaders.CONTENT_DISPOSITION, 
            "attachment; filename=\"my-data.json\"")
        .body(new ByteArrayResource(jsonData.getBytes()));
}
```

#### Right to Object (Article 21)
- Object to processing for direct marketing (absolute right)
- Object to automated decision-making/profiling

#### Right Not to Be Subject to Automated Decision-Making (Article 22)
- Cannot make decisions based solely on automated processing (e.g., AI/algorithms)
- Exceptions: Necessary for contract, authorized by law, explicit consent

### Technical Requirements

#### Encryption
```yaml
gdpr:
  encryption:
    algorithm: AES-256-GCM
    key-rotation-days: 90
    encrypt-at-rest: true
    encrypt-in-transit: true # TLS 1.2+
```

#### Pseudonymization
Replace identifiable data with pseudonyms:
```java
// Instead of storing: email = "john@example.com"
// Store: email_hash = "a3f5b8c9d2e1..." (one-way hash)
// Store separately: hash_map = {"a3f5b8c9d2e1" -> "john@example.com"}
```

#### Data Breach Notification
- Notify supervisory authority within **72 hours** of breach discovery
- Notify affected individuals if high risk to their rights
- Document all breaches (even if not required to notify)

```java
@Service
public class BreachNotificationService {
    
    public void handleDataBreach(DataBreach breach) {
        // Assess severity
        BreachSeverity severity = assessBreachSeverity(breach);
        
        if (severity.requiresNotification()) {
            // Notify within 72 hours
            notifySupervisoryAuthority(breach);
        }
        
        if (severity.isHighRisk()) {
            // Notify affected individuals
            notifyDataSubjects(breach);
        }
        
        // Always document
        documentBreach(breach);
    }
}
```

#### Cross-Border Transfers
- Can only transfer EU data to countries with **adequate protection**:
    - **Adequacy decisions**: UK, Canada, Japan, Switzerland, etc.
    - **Standard Contractual Clauses (SCCs)**
    - **Binding Corporate Rules (BCRs)**
    - **Consent** (weak basis)

```java
@PostMapping("/api/files/share-international")
public ResponseEntity<?> shareFileInternationally(
    @RequestParam String fileId,
    @RequestParam String targetCountry
) {
    // Check if country has adequate protection
    if (!gdprService.hasAdequateProtection(targetCountry)) {
        // Require additional safeguards
        if (!hasStandardContractualClauses(targetCountry)) {
            throw new GdprException(
                "Cannot transfer to " + targetCountry + 
                " without adequate safeguards"
            );
        }
    }
    
    // Proceed with transfer
    shareFile(fileId, targetCountry);
    return ResponseEntity.ok().build();
}
```

### GDPR Compliance Checklist for File Storage

✅ **Consent Management**
- Obtain clear, specific consent for data processing
- Allow users to withdraw consent easily
- Keep records of consent (when, how, what for)

✅ **Privacy Policy**
- Clear explanation of data processing
- Legal basis for each processing activity
- Retention periods
- User rights
- Contact details of Data Protection Officer (DPO)

✅ **Data Mapping**
- Document what personal data you collect
- Where it's stored
- Who has access
- How long you keep it

✅ **Encryption**
- AES-256 encryption at rest
- TLS 1.2+ in transit
- Encrypted backups

✅ **Access Controls**
- Role-based access control (RBAC)
- Multi-factor authentication
- Audit logs of all access

✅ **Data Subject Requests**
- Process for handling access requests
- Data export functionality
- Account deletion functionality

✅ **Data Protection Impact Assessment (DPIA)**
- Required for high-risk processing
- Document risks and mitigation measures

✅ **Vendor Management**
- Data Processing Agreements (DPAs) with all vendors
- Ensure vendors are GDPR compliant

✅ **Breach Response Plan**
- 72-hour notification procedure
- Incident response team
- Breach documentation process

---

## 2. HIPAA (Health Insurance Portability and Accountability Act) 🏥

### Overview
- **Jurisdiction**: United States
- **Effective**: 1996 (Privacy Rule: 2003, Security Rule: 2005)
- **Penalties**: $100 - $50,000 per violation (up to $1.5M per year)
- **Scope**: Protected Health Information (PHI) in healthcare

### Who Must Comply?

#### Covered Entities
- Healthcare providers (doctors, hospitals, clinics)
- Health plans (insurance companies)
- Healthcare clearinghouses

#### Business Associates (BA)
- ANY entity that processes PHI on behalf of covered entities:
    - Cloud storage providers (that's you!)
    - Medical billing companies
    - IT support companies
    - Lawyers, accountants handling PHI
    - Email/communication providers

**Key**: If you store files for healthcare providers, you're a Business Associate!

### What is PHI (Protected Health Information)?

#### Individually Identifiable Health Information
Any health information that can identify an individual:

**18 HIPAA Identifiers**:
1. Names
2. Geographic subdivisions smaller than state
3. Dates (except year) - birth, admission, discharge, death, age over 89
4. Phone numbers
5. Fax numbers
6. Email addresses
7. Social Security numbers
8. Medical record numbers
9. Health plan beneficiary numbers
10. Account numbers
11. Certificate/license numbers
12. Vehicle identifiers and serial numbers
13. Device identifiers and serial numbers
14. URLs
15. IP addresses
16. Biometric identifiers (fingerprints, retinal scans)
17. Full-face photos
18. Any other unique identifying number or code

**Combined with health data**:
- Diagnoses
- Medications
- Lab results
- Treatment plans
- Medical history
- Insurance claims

```java
// DLP Pattern for PHI detection
public class PhiDetector {
    
    // Medical Record Number patterns
    private static final Pattern MRN_PATTERN = 
        Pattern.compile("\\b[A-Z]{2,3}\\d{6,8}\\b");
    
    // ICD-10 diagnosis codes
    private static final Pattern ICD10_PATTERN = 
        Pattern.compile("\\b[A-Z]\\d{2}(\\.\\d{1,2})?\\b");
    
    // Prescription patterns
    private static final Pattern RX_PATTERN = 
        Pattern.compile("\\b(Rx:|PRESCRIPTION:|Take:)\\s+.+", 
            Pattern.CASE_INSENSITIVE);
    
    // Medical terms
    private static final Set<String> MEDICAL_TERMS = Set.of(
        "diagnosis", "treatment", "medication", "prescription",
        "patient", "doctor", "hospital", "clinic", "surgery",
        "cancer", "diabetes", "hypertension", "HIV", "AIDS"
    );
    
    public boolean containsPhi(String content) {
        // Check for identifiers + health info
        boolean hasIdentifier = containsIdentifier(content);
        boolean hasHealthInfo = containsHealthInfo(content);
        
        return hasIdentifier && hasHealthInfo;
    }
}
```

### HIPAA Rules

#### Privacy Rule
Governs **use and disclosure** of PHI

**Permitted Uses** (without authorization):
- Treatment
- Payment operations
- Healthcare operations
- Required by law
- Public health activities
- Law enforcement (limited)

**Minimum Necessary Standard**:
- Only access/disclose minimum PHI necessary
- Example: Billing department doesn't need full medical records

```java
@Service
public class PhiAccessControl {
    
    public boolean canAccessPhi(User user, File file, AccessPurpose purpose) {
        // Verify Business Associate Agreement exists
        if (!hasBusinessAssociateAgreement(user.getTenantId())) {
            throw new HipaaException("No BAA on file");
        }
        
        // Check minimum necessary
        if (purpose == AccessPurpose.BILLING) {
            // Billing should only see billing-related fields
            return file.getFileType().equals("BILLING_RECORD");
        }
        
        if (purpose == AccessPurpose.TREATMENT) {
            // Doctors can access full medical records
            return user.hasRole("DOCTOR") || user.hasRole("NURSE");
        }
        
        return false;
    }
}
```

**Patient Rights**:
- Right to access PHI (within 30 days)
- Right to request corrections
- Right to accounting of disclosures
- Right to request restrictions
- Right to confidential communications

#### Security Rule
Technical **safeguards** to protect ePHI (electronic PHI)

**Administrative Safeguards**:
- Security management process
- Assigned security responsibility
- Workforce training
- Evaluation procedures

**Physical Safeguards**:
- Facility access controls
- Workstation security
- Device and media controls

**Technical Safeguards**:

1. **Access Control** (Required)
    - Unique user IDs
    - Emergency access procedures
    - Automatic logoff
    - Encryption and decryption

```java
@Configuration
public class HipaaSecurityConfig {
    
    @Bean
    public SecurityFilterChain hipaaFilterChain(HttpSecurity http) {
        http
            // Unique user identification
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            
            // Automatic logoff (session timeout)
            .sessionManagement()
                .maximumSessions(1)
                .maxSessionsPreventsLogin(true)
            .and()
            
            // Emergency access procedure
            .authorizeRequests()
                .antMatchers("/api/phi/emergency/**")
                    .hasRole("EMERGENCY_ACCESS")
            .and()
            
            // Encryption required
            .requiresChannel()
                .anyRequest().requiresSecure(); // HTTPS only
        
        return http.build();
    }
}
```

2. **Audit Controls** (Required)
    - Log all PHI access
    - Record who, what, when, where
    - Retain logs for 6 years

```java
@Service
public class HipaaAuditService {
    
    public void logPhiAccess(
        String userId,
        String patientId,
        String fileId,
        AccessType accessType,
        String purpose
    ) {
        HipaaAuditLog log = new HipaaAuditLog();
        log.setUserId(userId);
        log.setPatientId(patientId);
        log.setResourceId(fileId);
        log.setAccessType(accessType); // READ, WRITE, DELETE
        log.setPurpose(purpose); // TREATMENT, PAYMENT, OPERATIONS
        log.setTimestamp(LocalDateTime.now());
        log.setIpAddress(getCurrentIpAddress());
        log.setWorkstation(getCurrentWorkstation());
        
        // Must retain for 6 years
        log.setRetentionEndDate(LocalDateTime.now().plusYears(6));
        
        auditRepository.save(log);
    }
}
```

3. **Integrity** (Addressable)
    - Ensure ePHI is not improperly altered or destroyed
    - Use checksums, digital signatures

```java
@Service
public class PhiIntegrityService {
    
    public void verifyIntegrity(File file) {
        // Calculate current hash
        String currentHash = calculateFileHash(file);
        
        // Compare with stored hash
        if (!currentHash.equals(file.getStoredHash())) {
            // PHI has been tampered with!
            throw new HipaaException(
                "PHI integrity violation - file has been altered"
            );
        }
    }
    
    private String calculateFileHash(File file) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(readFileBytes(file));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Hash calculation failed", e);
        }
    }
}
```

4. **Person/Entity Authentication** (Required)
    - Verify identity before granting access
    - Multi-factor authentication recommended

```java
@Configuration
public class HipaaMfaConfig {
    
    @Bean
    public AuthenticationProvider mfaProvider() {
        return new AuthenticationProvider() {
            @Override
            public Authentication authenticate(Authentication auth) {
                // Step 1: Username + Password
                validateCredentials(auth);
                
                // Step 2: MFA (SMS, TOTP, Biometric)
                validateMfaToken(auth);
                
                return new UsernamePasswordAuthenticationToken(
                    auth.getPrincipal(), 
                    auth.getCredentials(),
                    getAuthorities()
                );
            }
        };
    }
}
```

5. **Transmission Security** (Addressable)
    - Protect ePHI during transmission
    - End-to-end encryption

```java
@Configuration
public class HipaaTransmissionSecurity {
    
    @Bean
    public TomcatServletWebServerFactory servletContainer() {
        TomcatServletWebServerFactory tomcat = 
            new TomcatServletWebServerFactory();
        
        // Force HTTPS (TLS 1.2+)
        tomcat.addAdditionalTomcatConnectors(createHttpsConnector());
        
        return tomcat;
    }
    
    private Connector createHttpsConnector() {
        Connector connector = new Connector(
            "org.apache.coyote.http11.Http11NioProtocol"
        );
        connector.setScheme("https");
        connector.setSecure(true);
        connector.setPort(8443);
        
        // TLS 1.2+ only
        connector.setAttribute("sslProtocol", "TLSv1.2");
        connector.setAttribute("SSLEnabled", true);
        
        return connector;
    }
}
```

#### Breach Notification Rule
- Notify HHS within **60 days** of breach discovery
- Notify affected individuals without unreasonable delay (within 60 days)
- If breach affects 500+ individuals → notify media
- Annual report for breaches < 500 individuals

```java
@Service
public class HipaaBreachNotification {
    
    public void handlePhiBreach(PhiBreach breach) {
        int affectedIndividuals = breach.getAffectedPatientCount();
        
        // Assess if it's a breach (4-factor risk assessment)
        if (isBreachUnderHipaa(breach)) {
            
            // Notify HHS within 60 days
            notifyHhs(breach);
            
            // Notify affected individuals
            notifyPatients(breach);
            
            // If 500+ affected, notify media
            if (affectedIndividuals >= 500) {
                notifyMedia(breach);
            }
        }
        
        // Document everything
        documentBreach(breach);
    }
    
    private boolean isBreachUnderHipaa(PhiBreach breach) {
        // 4-factor risk assessment:
        // 1. Nature and extent of PHI
        // 2. Unauthorized person who accessed PHI
        // 3. Whether PHI was actually acquired/viewed
        // 4. Extent of risk mitigation
        
        return breach.getRiskScore() > BREACH_THRESHOLD;
    }
}
```

### Business Associate Agreement (BAA)

**Required** before any PHI is disclosed to a business associate:

```markdown
## Business Associate Agreement (BAA)

### Definitions
- Covered Entity: [Healthcare Provider Name]
- Business Associate: [Your Company Name]
- PHI: Protected Health Information as defined by HIPAA

### Permitted Uses
Business Associate may use PHI only for:
- File storage services as specified in Service Agreement
- Data processing as directed by Covered Entity

### Obligations of Business Associate
1. Not use or disclose PHI except as permitted
2. Use appropriate safeguards (HIPAA Security Rule)
3. Report security incidents within 24 hours
4. Ensure subcontractors agree to same restrictions
5. Make PHI available to individuals upon request
6. Make PHI available to HHS for compliance review
7. Return or destroy PHI upon termination

### Termination
Either party may terminate if the other breaches material term
and fails to cure within 30 days.
```

### HIPAA Compliance Checklist for File Storage

✅ **Business Associate Agreement**
- Obtain signed BAA from every healthcare client
- Ensure subcontractors (cloud providers) have BAAs

✅ **Encryption**
- AES-256 encryption at rest
- TLS 1.2+ in transit
- Encrypted backups
- Encrypted laptops/devices

✅ **Access Controls**
- Unique user IDs
- Strong passwords (12+ characters, complexity)
- MFA for administrative access
- Automatic logoff after 15 minutes inactivity
- Emergency access procedures

✅ **Audit Logging**
- Log all PHI access (who, what, when, where, why)
- Retain logs for 6 years
- Regular log review
- Tamper-proof logs (write-once)

✅ **Integrity Controls**
- File checksums/hashes
- Digital signatures
- Version control
- Change tracking

✅ **Transmission Security**
- HTTPS/TLS for all communications
- VPN for remote access
- Encrypted email for PHI

✅ **Physical Security**
- Locked server rooms
- Badge access systems
- Security cameras
- Visitor logs

✅ **Workforce Training**
- Annual HIPAA training for all employees
- Security awareness training
- Incident response training

✅ **Policies & Procedures**
- Written security policies
- Incident response plan
- Disaster recovery plan
- Sanctions policy for violations

✅ **Risk Assessment**
- Annual security risk assessment
- Document risks and mitigation
- Update safeguards based on findings

✅ **Breach Response Plan**
- 60-day notification procedure
- Forensic investigation capability
- Patient notification templates

✅ **Contingency Planning**
- Data backup plan
- Disaster recovery plan
- Emergency mode operation plan

---

## 3. PCI-DSS (Payment Card Industry Data Security Standard) 💳

### Overview
- **Jurisdiction**: Global (required by card brands: Visa, Mastercard, Amex, Discover)
- **Managed by**: PCI Security Standards Council
- **Penalties**: $5,000 - $100,000 per month, card brand fines, loss of card processing
- **Scope**: Cardholder Data (CHD) and Sensitive Authentication Data (SAD)

### Who Must Comply?

**Merchant Levels** (based on transaction volume):
- **Level 1**: 6M+ Visa/Mastercard transactions/year
- **Level 2**: 1M - 6M transactions/year
- **Level 3**: 20K - 1M e-commerce transactions/year
- **Level 4**: < 20K e-commerce transactions/year

**Service Providers**:
- Payment gateways
- Hosting providers
- **File storage services that handle CHD** ← That's you!

### What is Cardholder Data?

#### Cardholder Data (CHD) - Can be stored if encrypted
1. **Primary Account Number (PAN)** - 13-19 digit card number
2. **Cardholder Name**
3. **Expiration Date**
4. **Service Code**

#### Sensitive Authentication Data (SAD) - NEVER store!
1. **Full magnetic stripe data** (track 1 & 2)
2. **CAV2/CVC2/CVV2/CID** - 3-4 digit security code
3. **PIN/PIN Block**

```java
public class PciDataClassifier {
    
    // PAN patterns (Visa, MC, Amex, Discover)
    private static final Pattern PAN_PATTERN = Pattern.compile(
        "\\b(?:4\\d{12}(?:\\d{3})?|" +          // Visa
        "5[1-5]\\d{14}|" +                       // Mastercard
        "3[47]\\d{13}|" +                        // Amex
        "6(?:011|5\\d{2})\\d{12})\\b"           // Discover
    );
    
    // CVV pattern
    private static final Pattern CVV_PATTERN = 
        Pattern.compile("\\b\\d{3,4}\\b");
    
    // Expiration date
    private static final Pattern EXPIRY_PATTERN = 
        Pattern.compile("\\b(0[1-9]|1[0-2])/\\d{2}\\b");
    
    public PciClassification classifyContent(String content) {
        boolean hasPan = PAN_PATTERN.matcher(content).find();
        boolean hasCvv = CVV_PATTERN.matcher(content).find();
        boolean hasExpiry = EXPIRY_PATTERN.matcher(content).find();
        
        if (hasCvv) {
            // CRITICAL: CVV must NEVER be stored!
            return PciClassification.PROHIBITED_SAD;
        }
        
        if (hasPan) {
            return PciClassification.CARDHOLDER_DATA;
        }
        
        return PciClassification.NO_CHD;
    }
}
```

### The 12 PCI-DSS Requirements

#### Build and Maintain Secure Network

**Requirement 1: Install and maintain firewall configuration**
- Firewall rules to protect cardholder data environment (CDE)
- Deny all inbound/outbound traffic except explicitly allowed
- Restrict connections to trusted entities

```yaml
# Firewall rules for CDE
firewall:
  inbound:
    - allow: 443 from payment_gateway
    - allow: 22 from admin_subnet
    - deny: all
  
  outbound:
    - allow: 443 to card_processor
    - allow: 443 to logging_server
    - deny: all
```

**Requirement 2: Don't use vendor-supplied defaults**
- Change default passwords
- Remove unnecessary accounts
- Disable unnecessary services

```java
@Configuration
public class PciSecureDefaults {
    
    @PostConstruct
    public void validateNoDefaults() {
        // Check no default passwords
        if (adminPassword.equals("admin")) {
            throw new PciException("Default password detected!");
        }
        
        // Remove sample accounts
        userRepository.deleteByUsername("admin");
        userRepository.deleteByUsername("test");
        
        // Disable unnecessary services
        disableService("telnet");
        disableService("ftp");
    }
}
```

#### Protect Cardholder Data

**Requirement 3: Protect stored cardholder data**

**Key Rules**:
- Store only what you need (data minimization)
- Render PAN unreadable using:
    - Strong encryption (AES-256)
    - Truncation (show only last 4 digits)
    - Tokenization
    - Hashing (one-way, with salt)
- **NEVER store CVV2, PIN, or full mag stripe**

```java
@Service
public class PciDataProtection {
    
    @Autowired
    private EncryptionService encryptionService;
    
    // Encrypt PAN before storage
    public String encryptPan(String pan) {
        // AES-256 encryption
        return encryptionService.encrypt(pan);
    }
    
    // Truncate PAN for display
    public String truncatePan(String pan) {
        if (pan.length() < 6) return "****";
        
        // Show first 6 and last 4: 411111******1111
        return pan.substring(0, 6) + "******" + 
               pan.substring(pan.length() - 4);
    }
    
    // Tokenize PAN
    public String tokenizePan(String pan) {
        // Replace real PAN with token
        String token = UUID.randomUUID().toString();
        tokenVault.store(token, encryptPan(pan));
        return token;
    }
    
    // NEVER do this!
    public void storeProhibitedData(String cvv, String pin) {
        throw new PciException(
            "Storing CVV/PIN violates PCI-DSS Requirement 3.2"
        );
    }
}
```

**Requirement 4: Encrypt transmission of CHD across public networks**
- TLS 1.2+ for all transmissions
- Strong cryptography
- Never send PAN via email, chat, messaging

```java
@Configuration
public class PciTransmissionSecurity {
    
    @Bean
    public RestTemplate pciCompliantRestTemplate() {
        TrustStrategy acceptingTrustStrategy = (cert, authType) -> false;
        
        SSLContext sslContext = SSLContexts.custom()
            .loadTrustMaterial(null, acceptingTrustStrategy)
            // TLS 1.2+ only
            .setProtocol("TLSv1.2")
            .build();
        
        SSLConnectionSocketFactory csf = 
            new SSLConnectionSocketFactory(sslContext);
        
        CloseableHttpClient httpClient = HttpClients.custom()
            .setSSLSocketFactory(csf)
            .build();
        
        HttpComponentsClientHttpRequestFactory requestFactory =
            new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        
        return new RestTemplate(requestFactory);
    }
    
    // Prevent CHD in emails
    @Before("execution(* sendEmail(..)) && args(email,..)")
    public void validateEmailContent(Email email) {
        if (containsChd(email.getBody())) {
            throw new PciException(
                "Cannot send CHD via email (PCI-DSS 4.2)"
            );
        }
    }
}
```

#### Maintain Vulnerability Management Program

**Requirement 5: Protect against malware**
- Anti-virus on all systems
- Keep anti-virus up to date
- Log anti-virus events

**Requirement 6: Develop secure systems**
- Patch critical vulnerabilities within 30 days
- Secure coding practices
- Application security testing
- Change control procedures

```java
// Secure coding example
@Service
public class SecurePaymentProcessing {
    
    // SQL Injection prevention
    public Payment findPayment(String paymentId) {
        // Use parameterized queries
        String sql = "SELECT * FROM payments WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, 
            new Object[]{paymentId}, 
            paymentRowMapper);
    }
    
    // Input validation
    public void processPayment(PaymentRequest request) {
        // Validate PAN format
        if (!isValidPan(request.getPan())) {
            throw new ValidationException("Invalid PAN format");
        }
        
        // Sanitize inputs
        String sanitizedName = sanitize(request.getCardholderName());
        
        // Log without CHD
        log.info("Processing payment for: {}", 
            truncatePan(request.getPan()));
    }
    
    // XSS prevention
    public String sanitize(String input) {
        return StringEscapeUtils.escapeHtml4(input);
    }
}
```

#### Implement Strong Access Control Measures

**Requirement 7: Restrict access by business need-to-know**
- Role-based access control
- Default deny all
- Grant least privilege

```java
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class PciAccessControl {
    
    @PreAuthorize("hasRole('PAYMENT_PROCESSOR')")
    public Payment processPayment(PaymentRequest request) {
        // Only payment processors can access
        return paymentService.process(request);
    }
    
    @PreAuthorize("hasRole('CARDHOLDER_DATA_VIEWER')")
    public String viewPan(String paymentId) {
        // Separate role for viewing full PAN
        Payment payment = paymentRepository.findById(paymentId);
        
        // Audit CHD access
        auditService.logChdAccess(getCurrentUser(), paymentId);
        
        return decryptPan(payment.getEncryptedPan());
    }
}
```

**Requirement 8: Identify and authenticate access**
- Unique ID for each user
- Multi-factor authentication for remote access
- Strong passwords (7+ characters, complexity)
- Lock account after 6 failed attempts
- Session timeout after 15 minutes

```java
@Configuration
public class PciAuthentication {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        // Strong password hashing
        return new BCryptPasswordEncoder(12);
    }
    
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            User user = userRepository.findByUsername(username);
            
            // Check account locked (6 failed attempts)
            if (user.getFailedLoginAttempts() >= 6) {
                throw new LockedException("Account locked");
            }
            
            return user;
        };
    }
    
    // Session timeout
    @Bean
    public SessionConfiguration sessionConfig() {
        return SessionConfiguration.builder()
            .maxInactiveInterval(Duration.ofMinutes(15))
            .build();
    }
}
```

**Requirement 9: Restrict physical access to CHD**
- Badge access to data centers
- Visitor logs
- Secure destruction of media
- Inventory of devices

#### Regularly Monitor and Test Networks

**Requirement 10: Track and monitor all access to network resources and CHD**
- Audit all access to CHD
- Log security events
- Retain logs for 1 year (3 months online)
- Time synchronization (NTP)

```java
@Service
public class PciAuditLogging {
    
    public void logChdAccess(String userId, String resource, String action) {
        PciAuditLog log = new PciAuditLog();
        log.setUserId(userId);
        log.setResourceId(resource);
        log.setAction(action); // READ, WRITE, DELETE
        log.setTimestamp(LocalDateTime.now()); // Time-synced
        log.setSuccess(true);
        log.setIpAddress(getCurrentIpAddress());
        
        // Log must include (Req 10.3):
        // 10.3.1 User identification
        // 10.3.2 Type of event
        // 10.3.3 Date and time
        // 10.3.4 Success or failure
        // 10.3.5 Origination of event
        // 10.3.6 Identity/name of affected data
        
        // Retain for 1 year
        log.setRetentionEndDate(LocalDateTime.now().plusYears(1));
        
        auditRepository.save(log);
    }
}
```

**Requirement 11: Regularly test security systems**
- Quarterly vulnerability scans (by ASV)
- Annual penetration testing
- File integrity monitoring
- Intrusion detection/prevention

```java
@Service
public class PciFileIntegrityMonitoring {
    
    @Scheduled(fixedRate = 3600000) // Every hour
    public void monitorCriticalFiles() {
        List<String> criticalFiles = List.of(
            "/config/payment.properties",
            "/config/encryption-keys.jks",
            "/app/payment-processor.jar"
        );
        
        for (String filePath : criticalFiles) {
            String currentHash = calculateFileHash(filePath);
            String expectedHash = getExpectedHash(filePath);
            
            if (!currentHash.equals(expectedHash)) {
                // File modified!
                alertSecurityTeam(
                    "File integrity violation: " + filePath
                );
            }
        }
    }
}
```

#### Maintain Information Security Policy

**Requirement 12: Maintain policy that addresses information security**
- Written security policy
- Annual risk assessment
- Quarterly reviews
- Security awareness training
- Incident response plan

### PCI-DSS Compliance Levels

#### Level 1 (Highest Scrutiny)
- Annual **on-site audit** by QSA (Qualified Security Assessor)
- Quarterly network scans by ASV (Approved Scanning Vendor)
- Attestation of Compliance (AOC)

#### Level 2-4
- Annual **Self-Assessment Questionnaire (SAQ)**
- Quarterly network scans by ASV
- Attestation of Compliance

### PCI Compliance Checklist for File Storage

✅ **Scope Reduction**
- Minimize systems that store CHD
- Segment CDE from other networks
- Use tokenization to remove CHD from storage

✅ **Data Storage**
- Encrypt all stored PANs (AES-256)
- NEVER store CVV, PIN, full mag stripe
- Truncate PAN for display (show last 4 only)
- Implement data retention policy

✅ **Encryption**
- TLS 1.2+ for all transmissions
- Strong cryptographic keys
- Key rotation procedures
- Secure key storage (HSM recommended)

✅ **Access Controls**
- Unique user IDs
- MFA for administrative access
- Role-based access (least privilege)
- Account lockout after 6 failed attempts
- 15-minute session timeout

✅ **Audit Logging**
- Log all CHD access
- Include: user, action, date/time, success/failure, source
- Retain logs for 1 year
- Daily log review
- Time synchronization (NTP)

✅ **Vulnerability Management**
- Monthly vulnerability scans
- Patch critical vulnerabilities within 30 days
- Anti-virus on all systems
- Secure coding practices

✅ **Physical Security**
- Locked server rooms
- Badge access
- Visitor logs
- Security cameras
- Secure media destruction

✅ **Network Security**
- Firewall protecting CDE
- Network segmentation
- Intrusion detection/prevention
- Wireless encryption (WPA2+)

✅ **Testing**
- Quarterly ASV scans
- Annual penetration testing
- File integrity monitoring
- Code reviews

✅ **Policies**
- Written security policy
- Acceptable use policy
- Incident response plan
- Annual risk assessment
- Quarterly policy reviews

✅ **Training**
- Annual security awareness training for all staff
- Specialized training for those handling CHD

---

## Comparison Matrix

| Aspect | GDPR | HIPAA | PCI-DSS |
|--------|------|-------|---------|
| **Scope** | EU personal data | US healthcare PHI | Payment card data globally |
| **Data Protected** | Any personal data | Health information | Card numbers, CVV, PIN |
| **Max Penalty** | €20M or 4% revenue | $1.5M per year | $100K/month + card ban |
| **Encryption** | Recommended | Required | Required (AES-256) |
| **Breach Notification** | 72 hours | 60 days | Immediately to card brands |
| **Audit Logs** | Recommended | 6 years | 1 year (3 months online) |
| **User Rights** | Extensive (8 rights) | Limited (access, correction) | None |
| **Consent Required** | Yes (for most processing) | No (for treatment) | No |
| **Cross-Border Transfer** | Restricted | Allowed within US | Allowed globally |
| **Compliance Validation** | Self-assessment or auditor | Self-assessment | SAQ or QSA audit |
| **Storage Restrictions** | Minimize, time-limited | 6 years minimum | Never store CVV/PIN |

---

## Implementation Priority

For a **file storage system**, prioritize in this order:

1. **Start with GDPR** (affects all users, broadest scope)
    - Consent management
    - Data export/deletion
    - Encryption
    - Audit logging

2. **Add HIPAA** (if serving healthcare)
    - Business Associate Agreements
    - PHI detection and classification
    - Enhanced access controls
    - 6-year audit retention

3. **Implement PCI-DSS** (if storing payment data)
    - **Better yet: Don't store CHD!** Use tokenization
    - If you must: AES-256 encryption, never store CVV
    - Quarterly scans, annual audits

**Best Practice**: Design your system to **avoid storing CHD** entirely. Use payment processors (Stripe, PayPal) that handle PCI compliance for you!


# Question:

Great question! This highlights a critical tension in GDPR compliance between **pseudonymization** (for security) and **practical business needs** (like sending notifications).

## The Problem with One-Way Hashing

You're absolutely right - if you use a one-way hash, you **cannot** recover the original data:

```java
// ONE-WAY HASH - Cannot recover email!
String emailHash = SHA256(email); // "a3f5b8c9d2e1..."
// There's NO way to get back "john@example.com" from the hash
```

This creates a dilemma:
- ❌ Can't send notifications (no email address)
- ❌ Can't display user's name
- ❌ Can't fulfill "Right to Access" requests (can't identify the user's data)

## Solutions for Reversible Pseudonymization

### Solution 1: **Two-Way Encryption** (Recommended for Most Cases)

Use **symmetric encryption** instead of hashing. This is still considered pseudonymization under GDPR!

```java
@Service
public class PseudonymizationService {
    
    @Autowired
    private EncryptionService encryptionService;
    
    // PSEUDONYMIZE: Encrypt the data
    public String pseudonymize(String personalData) {
        // AES-256-GCM encryption
        return encryptionService.encrypt(personalData);
    }
    
    // DE-PSEUDONYMIZE: Decrypt when needed
    public String depseudonymize(String encryptedData) {
        return encryptionService.decrypt(encryptedData);
    }
}

// Usage
@Entity
public class User {
    @Id
    private String id;
    
    // Store encrypted (pseudonymized)
    @Column(name = "email_encrypted")
    private String emailEncrypted;
    
    // Don't store plain text email in DB
    @Transient
    private String email;
    
    public void setEmail(String email) {
        this.email = email;
        this.emailEncrypted = pseudonymizationService.pseudonymize(email);
    }
    
    public String getEmail() {
        if (email == null && emailEncrypted != null) {
            email = pseudonymizationService.depseudonymize(emailEncrypted);
        }
        return email;
    }
}

// Send notification
public void sendNotification(String userId) {
    User user = userRepository.findById(userId);
    
    // Decrypt email when needed
    String email = user.getEmail(); // "john@example.com"
    
    emailService.send(email, "Your file is ready!");
}
```

**Key Point**: Under GDPR Article 4(5), this is **still pseudonymization** because:
- The encrypted data cannot identify the person **without additional information** (the encryption key)
- The encryption key is stored separately with strict access controls
- An attacker with only the database cannot identify individuals

### Solution 2: **Token Vault / Lookup Table**

Store a mapping separately from your main database:

```java
// Main Database (public-facing)
@Entity
public class File {
    @Id
    private String id;
    
    @Column(name = "user_token")
    private String userToken; // "token_abc123"
    
    private String filename;
    private byte[] content;
}

// Separate Secure Vault (restricted access)
@Entity
@Table(name = "identity_vault")
public class IdentityVault {
    @Id
    private String token; // "token_abc123"
    
    @Column(name = "email_encrypted")
    private String emailEncrypted;
    
    @Column(name = "name_encrypted")
    private String nameEncrypted;
}

@Service
public class TokenVaultService {
    
    // Create pseudonym token
    public String createUserToken(String email, String name) {
        String token = "token_" + UUID.randomUUID().toString();
        
        IdentityVault vault = new IdentityVault();
        vault.setToken(token);
        vault.setEmailEncrypted(encrypt(email));
        vault.setNameEncrypted(encrypt(name));
        
        vaultRepository.save(vault);
        
        return token;
    }
    
    // Retrieve real identity
    public UserIdentity resolveToken(String token) {
        IdentityVault vault = vaultRepository.findById(token)
            .orElseThrow(() -> new NotFoundException("Token not found"));
        
        return new UserIdentity(
            decrypt(vault.getEmailEncrypted()),
            decrypt(vault.getNameEncrypted())
        );
    }
}

// Usage
public void sendNotification(String fileId) {
    File file = fileRepository.findById(fileId);
    
    // Resolve token to get real email
    UserIdentity identity = tokenVaultService.resolveToken(file.getUserToken());
    
    emailService.send(identity.getEmail(), "Notification message");
}
```

**Security Benefits**:
- Main database has **zero** personal data (just tokens)
- Vault is in **separate database** with strict access controls
- Even if main DB is breached, attacker gets nothing

### Solution 3: **Format-Preserving Encryption (FPE)**

Encrypts data while maintaining the original format (e.g., email looks like email):

```java
@Service
public class FormatPreservingEncryptionService {
    
    private final FPE fpe;
    
    public FormatPreservingEncryptionService() {
        // Initialize FPE with key
        this.fpe = new FPE(loadKey());
    }
    
    // Encrypt email while preserving format
    public String encryptEmail(String email) {
        // john@example.com -> kqpm@fybznqmf.dpn
        String[] parts = email.split("@");
        
        String encryptedLocal = fpe.encrypt(parts[0]);
        String encryptedDomain = fpe.encrypt(parts[1]);
        
        return encryptedLocal + "@" + encryptedDomain;
    }
    
    public String decryptEmail(String encryptedEmail) {
        // kqpm@fybznqmf.dpn -> john@example.com
        String[] parts = encryptedEmail.split("@");
        
        String decryptedLocal = fpe.decrypt(parts[0]);
        String decryptedDomain = fpe.decrypt(parts[1]);
        
        return decryptedLocal + "@" + decryptedDomain;
    }
}
```

**Benefits**:
- Preserves data type and validation rules
- Can still perform pattern matching
- Fully reversible

---

## When to Use One-Way Hashing vs Encryption

### Use **One-Way Hashing** When:

✅ **Authentication** (passwords)
```java
// NEVER store plain text passwords
String passwordHash = bcrypt.hash(password);
```

✅ **Anonymization** (when you truly don't need the original data)
```java
// Analytics: just need to count unique users
String userHash = SHA256(userId);
analyticsService.track(userHash, "file_uploaded");
```

✅ **Data Matching** (without revealing data)
```java
// Check if email exists without storing it
String emailHash = SHA256(email);
if (emailHashSet.contains(emailHash)) {
    return "Email already registered";
}
```

### Use **Encryption** (Reversible Pseudonymization) When:

✅ **Notifications** (need actual email/phone)
```java
String email = decrypt(user.getEmailEncrypted());
emailService.send(email, notification);
```

✅ **Display Names** (need to show user their name)
```java
String name = decrypt(user.getNameEncrypted());
return "Welcome, " + name;
```

✅ **GDPR Data Export** (Right to Access)
```java
public GdprExport exportUserData(String userId) {
    User user = userRepository.findById(userId);
    
    // Must decrypt to provide readable export
    export.setEmail(decrypt(user.getEmailEncrypted()));
    export.setName(decrypt(user.getNameEncrypted()));
    
    return export;
}
```

✅ **Business Operations** (customer service, billing)
```java
// Customer service needs to see user's real email
String email = decrypt(customer.getEmailEncrypted());
supportTicket.setCustomerEmail(email);
```

---

## Hybrid Approach: Hash for Lookup, Encrypt for Storage

Combine both techniques:

```java
@Entity
public class User {
    @Id
    private String id;
    
    // One-way hash for fast lookup (indexed)
    @Column(name = "email_hash", unique = true)
    private String emailHash;
    
    // Encrypted for retrieval
    @Column(name = "email_encrypted")
    private String emailEncrypted;
    
    public void setEmail(String email) {
        this.emailHash = SHA256(email.toLowerCase());
        this.emailEncrypted = encrypt(email);
    }
    
    public String getEmail() {
        return decrypt(this.emailEncrypted);
    }
}

// Login: Use hash for lookup
public User findByEmail(String email) {
    String emailHash = SHA256(email.toLowerCase());
    return userRepository.findByEmailHash(emailHash);
}

// Notification: Decrypt when needed
public void sendNotification(User user) {
    String email = user.getEmail(); // Decrypts
    emailService.send(email, "Your file is ready");
}
```

**Benefits**:
- Fast indexed lookups (hash)
- Can retrieve original value (encryption)
- Protects against SQL injection revealing emails
- Database admin can't see real emails

---

## Key Management (Critical!)

Your pseudonymization is only as secure as your encryption keys:

```java
@Configuration
public class KeyManagementConfig {
    
    @Bean
    public SecretKey encryptionKey() {
        // DON'T hardcode keys!
        // DON'T store in application.properties!
        
        // Option 1: Environment variable
        String keyBase64 = System.getenv("ENCRYPTION_KEY");
        
        // Option 2: AWS KMS, Azure Key Vault, HashiCorp Vault
        String keyBase64 = keyVaultService.getSecret("encryption-key");
        
        // Option 3: Hardware Security Module (HSM)
        return hsmService.getKey("pseudonymization-key");
    }
    
    // Rotate keys periodically
    @Scheduled(cron = "0 0 0 1 * ?") // Monthly
    public void rotateEncryptionKey() {
        SecretKey newKey = generateNewKey();
        
        // Re-encrypt all data with new key
        reEncryptAllData(oldKey, newKey);
        
        // Archive old key (needed for backups)
        archiveKey(oldKey);
    }
}
```

**Best Practices**:
- ✅ Store keys in **separate system** (AWS KMS, Azure Key Vault)
- ✅ Restrict key access to **minimal personnel**
- ✅ **Rotate keys** regularly (every 90 days)
- ✅ **Audit** all key access
- ✅ **Backup keys** securely (you can't decrypt without them!)

---

## GDPR Compliance with Encryption

According to **GDPR Recital 26**:

> "Personal data which have undergone pseudonymisation, which could be attributed to a natural person by the use of additional information should be considered to be information on an identifiable natural person."

**This means**:
- ✅ Encrypted data is **still personal data** under GDPR
- ✅ All GDPR rights still apply (access, deletion, portability)
- ✅ But pseudonymization **reduces risk**, allowing relaxed controls in some cases

**Benefits of pseudonymization** (GDPR Article 32):
- Considered a **security safeguard**
- May allow data processing that would otherwise be risky
- Reduces impact of data breaches
- Can enable analytics while protecting privacy

---

## Practical Implementation Example

```java
@Service
public class GdprCompliantUserService {
    
    @Autowired
    private EncryptionService encryptionService;
    
    // Create user with pseudonymized data
    public User createUser(String email, String name) {
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        
        // Pseudonymize PII
        user.setEmailHash(hash(email)); // For lookups
        user.setEmailEncrypted(encrypt(email)); // For retrieval
        user.setNameEncrypted(encrypt(name));
        
        return userRepository.save(user);
    }
    
    // Send notification (de-pseudonymize)
    public void sendNotification(String userId, String message) {
        User user = userRepository.findById(userId)
            .orElseThrow();
        
        // Decrypt email for sending
        String email = decrypt(user.getEmailEncrypted());
        
        // Check consent
        if (user.hasConsentFor(ConsentType.EMAIL_NOTIFICATIONS)) {
            emailService.send(email, message);
            
            // Audit log (don't log plain email!)
            auditLog.log("Notification sent to user " + userId);
        }
    }
    
    // GDPR Right to Access
    public GdprExport exportData(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow();
        
        GdprExport export = new GdprExport();
        
        // De-pseudonymize for export
        export.setEmail(decrypt(user.getEmailEncrypted()));
        export.setName(decrypt(user.getNameEncrypted()));
        export.setFiles(user.getFiles());
        
        return export;
    }
    
    // GDPR Right to Erasure
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow();
        
        // Option 1: Hard delete
        userRepository.delete(user);
        
        // Option 2: Anonymize (if need to retain data)
        user.setEmailEncrypted(null);
        user.setEmailHash(null);
        user.setNameEncrypted(null);
        user.setAnonymized(true);
        userRepository.save(user);
    }
}
```

---

## Summary: How to Handle Your Use Case

For **consent-based email notifications**, use this approach:

1. **Store email encrypted** (not hashed)
   ```java
   user.setEmailEncrypted(encrypt(email));
   ```

2. **Also store hash for lookup** (optional, for performance)
   ```java
   user.setEmailHash(SHA256(email));
   ```

3. **Decrypt when sending notifications**
   ```java
   String email = decrypt(user.getEmailEncrypted());
   emailService.send(email, notification);
   ```

4. **Store encryption keys securely**
    - Use AWS KMS, Azure Key Vault, or HSM
    - Never hardcode or commit to Git

5. **This still counts as pseudonymization** under GDPR because:
    - The encrypted data alone doesn't identify the person
    - Requires the encryption key (additional information)
    - Key is stored separately with access controls

**You get the best of both worlds**: security through pseudonymization + ability to use the data for legitimate purposes!

