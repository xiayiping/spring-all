# prompt

I deon't want to use any 3rd party software like minio, i just want to write my own file storeage system.

1. single file size larger than a threshold split into multi part save to different path.
2. file encrypted on rest.
3. performance must be good.
4. support preview + watermark
5. file with same name has version.
7. even delete, keep the file for configurable times (like 10 years)
8. every operation on file, including open, preview , modification deletion, need to be audited.
9. each file has an only one owner.
10. files can be shared to others.
11. file has tenant.
12. file can be setup by owner to be public to all/tenent/specified-persons
13. as VDR system, need to subject to major country and organization's compliance audit

# solution

Great! Let's build a **production-grade file storage system** from scratch. I'll design a comprehensive solution with all your requirements.

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                     Application Layer                        │
├─────────────────────────────────────────────────────────────┤
│  FileService │ ChunkService │ EncryptionService │ AuditService│
├─────────────────────────────────────────────────────────────┤
│                     Database Layer                           │
│  PostgreSQL: Metadata, Permissions, Audit, Versions         │
├─────────────────────────────────────────────────────────────┤
│                  Physical Storage Layer                      │
│  Local Filesystem: /storage/{tenant}/{year}/{month}/{uuid}/ │
└─────────────────────────────────────────────────────────────┘
```

## Complete Implementation

### 1. Entity Models

```java
// File.java
package com.filestore.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "files", indexes = {
    @Index(name = "idx_tenant_owner", columnList = "tenant_id,owner_id"),
    @Index(name = "idx_parent_name", columnList = "parent_id,file_name"),
    @Index(name = "idx_tenant_path", columnList = "tenant_id,full_path")
})
@Data
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String tenantId;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String fullPath; // /folder1/folder2/file.pdf
    
    private String parentId; // For directory structure
    
    @Column(nullable = false)
    private String ownerId;
    
    @Column(nullable = false)
    private Long fileSize; // Original size in bytes
    
    private String mimeType;
    
    @Column(nullable = false)
    private Integer version = 1;
    
    @Column(nullable = false)
    private String currentVersionId; // Points to latest FileVersion
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileStatus status = FileStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessLevel accessLevel = AccessLevel.PRIVATE;
    
    // Encryption metadata
    @Column(columnDefinition = "BYTEA")
    private byte[] encryptedDek; // Data Encryption Key encrypted by KEK
    
    @Column(columnDefinition = "BYTEA")
    private byte[] dekIv; // IV used to encrypt DEK
    
    private String keyVersion; // For key rotation
    
    // Soft delete
    private LocalDateTime deletedAt;
    
    private LocalDateTime permanentDeleteAt; // deletedAt + retention period
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL)
    private List<FileVersion> versions = new ArrayList<>();
    
    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL)
    private List<FileShare> shares = new ArrayList<>();
    
    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL)
    private List<FileChunk> chunks = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

// FileStatus.java
public enum FileStatus {
    ACTIVE,
    DELETED,
    PERMANENTLY_DELETED,
    LOCKED
}

// AccessLevel.java
public enum AccessLevel {
    PRIVATE,        // Only owner
    TENANT,         // All users in tenant
    PUBLIC,         // Everyone
    SHARED          // Specific users (managed via FileShare)
}
```

```java
// FileVersion.java
package com.filestore.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "file_versions", indexes = {
    @Index(name = "idx_file_version", columnList = "file_id,version")
})
@Data
public class FileVersion {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;
    
    @Column(nullable = false)
    private Integer version;
    
    @Column(nullable = false)
    private Long fileSize;
    
    @Column(nullable = false, length = 64)
    private String sha256Hash; // For integrity check
    
    @Column(columnDefinition = "BYTEA")
    private byte[] encryptedDek; // Each version can have different DEK
    
    @Column(columnDefinition = "BYTEA")
    private byte[] dekIv;
    
    @Column(nullable = false)
    private String uploadedBy;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private String comment; // Version comment
    
    @OneToMany(mappedBy = "version", cascade = CascadeType.ALL)
    private List<FileChunk> chunks = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

```java
// FileChunk.java
package com.filestore.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "file_chunks", indexes = {
    @Index(name = "idx_version_sequence", columnList = "version_id,sequence_number")
})
@Data
public class FileChunk {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "version_id", nullable = false)
    private FileVersion version;
    
    @Column(nullable = false)
    private Integer sequenceNumber; // 0, 1, 2, ...
    
    @Column(nullable = false)
    private String storagePath; // Physical path on disk
    
    @Column(nullable = false)
    private Long encryptedSize; // Size of encrypted chunk
    
    @Column(nullable = false)
    private Long originalSize; // Size before encryption
    
    @Column(columnDefinition = "BYTEA", nullable = false)
    private byte[] iv; // IV for this chunk
    
    @Column(nullable = false, length = 64)
    private String sha256Hash; // Hash of encrypted chunk
}
```

```java
// FileShare.java
package com.filestore.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_shares", indexes = {
    @Index(name = "idx_file_user", columnList = "file_id,shared_with_user_id"),
    @Index(name = "idx_user_active", columnList = "shared_with_user_id,expires_at")
})
@Data
public class FileShare {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id", nullable = false)
    private File file;
    
    @Column(nullable = false)
    private String sharedByUserId;
    
    @Column(nullable = false)
    private String sharedWithUserId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SharePermission permission = SharePermission.READ;
    
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

public enum SharePermission {
    READ,           // Can read and preview
    READ_WRITE,     // Can read and upload new versions
    FULL            // Can read, write, delete, share
}
```

```java
// FileAudit.java
package com.filestore.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_audits", indexes = {
    @Index(name = "idx_file_date", columnList = "file_id,created_at"),
    @Index(name = "idx_user_date", columnList = "user_id,created_at"),
    @Index(name = "idx_tenant_date", columnList = "tenant_id,created_at")
})
@Data
public class FileAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String fileId;
    
    @Column(nullable = false)
    private String tenantId;
    
    @Column(nullable = false)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;
    
    @Column(columnDefinition = "TEXT")
    private String details; // JSON with additional context
    
    private String ipAddress;
    
    private String userAgent;
    
    private Integer versionNumber; // Which version was accessed
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

public enum AuditAction {
    UPLOAD,
    DOWNLOAD,
    PREVIEW,
    UPDATE,
    DELETE,
    RESTORE,
    SHARE,
    UNSHARE,
    PERMISSION_CHANGE,
    PERMANENT_DELETE,
    ACCESS_DENIED
}
```

### 2. Configuration

```java
// FileStorageConfig.java
package com.filestore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "file.storage")
@Data
public class FileStorageConfig {
    private String basePath = "/var/filestore/data";
    private Long chunkSizeBytes = 10 * 1024 * 1024L; // 10MB chunks
    private Long singleFileThreshold = 50 * 1024 * 1024L; // 50MB
    private Integer maxVersionsPerFile = 100;
    private Integer softDeleteRetentionDays = 3650; // 10 years
    private Boolean enableWatermark = true;
    private String watermarkText = "CONFIDENTIAL";
    private Integer previewMaxWidth = 1920;
    private Integer previewMaxHeight = 1080;
    
    // Encryption
    private String masterKeyPath = "/var/filestore/keys/master.key";
    private String keyAlgorithm = "AES";
    private Integer keySize = 256;
}
```

```yaml
# application.yml
file:
  storage:
    base-path: /var/filestore/data
    chunk-size-bytes: 10485760  # 10MB
    single-file-threshold: 52428800  # 50MB
    max-versions-per-file: 100
    soft-delete-retention-days: 3650  # 10 years
    enable-watermark: true
    watermark-text: "CONFIDENTIAL"
    preview-max-width: 1920
    preview-max-height: 1080
    master-key-path: /var/filestore/keys/master.key
    key-algorithm: AES
    key-size: 256

spring:
  servlet:
    multipart:
      max-file-size: 10GB
      max-request-size: 10GB
  datasource:
    url: jdbc:postgresql://localhost:5432/filestore
    username: filestore
    password: your_password
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true
```

### 3. Core Services

```java
// EncryptionService.java
package com.filestore.service;

import com.filestore.config.FileStorageConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class EncryptionService {
    
    private final FileStorageConfig config;
    private SecretKey masterKey;
    private final SecureRandom secureRandom = new SecureRandom();
    
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    
    @PostConstruct
    public void init() throws Exception {
        Path keyPath = Path.of(config.getMasterKeyPath());
        
        if (Files.exists(keyPath)) {
            byte[] keyBytes = Files.readAllBytes(keyPath);
            masterKey = new SecretKeySpec(keyBytes, config.getKeyAlgorithm());
            log.info("Master key loaded from {}", keyPath);
        } else {
            KeyGenerator keyGen = KeyGenerator.getInstance(config.getKeyAlgorithm());
            keyGen.init(config.getKeySize());
            masterKey = keyGen.generateKey();
            
            Files.createDirectories(keyPath.getParent());
            Files.write(keyPath, masterKey.getEncoded());
            log.info("New master key generated and saved to {}", keyPath);
        }
    }
    
    /**
     * Generate a new Data Encryption Key (DEK)
     */
    public SecretKey generateDEK() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(config.getKeyAlgorithm());
        keyGen.init(config.getKeySize());
        return keyGen.generateKey();
    }
    
    /**
     * Encrypt DEK with Master Key (KEK)
     */
    public EncryptedKey encryptDEK(SecretKey dek) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, masterKey, spec);
        
        byte[] encryptedDek = cipher.doFinal(dek.getEncoded());
        
        return new EncryptedKey(encryptedDek, iv);
    }
    
    /**
     * Decrypt DEK with Master Key (KEK)
     */
    public SecretKey decryptDEK(byte[] encryptedDek, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, masterKey, spec);
        
        byte[] dekBytes = cipher.doFinal(encryptedDek);
        return new SecretKeySpec(dekBytes, config.getKeyAlgorithm());
    }
    
    /**
     * Encrypt data chunk with DEK
     */
    public EncryptedChunk encryptChunk(InputStream input, SecretKey dek, long maxBytes) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        secureRandom.nextBytes(iv);
        
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, dek, spec);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        CipherOutputStream cipherOut = new CipherOutputStream(outputStream, cipher);
        
        byte[] buffer = new byte[8192];
        int bytesRead;
        long totalRead = 0;
        
        while ((bytesRead = input.read(buffer)) != -1 && totalRead < maxBytes) {
            int toWrite = (int) Math.min(bytesRead, maxBytes - totalRead);
            cipherOut.write(buffer, 0, toWrite);
            totalRead += toWrite;
        }
        
        cipherOut.close();
        
        byte[] encryptedData = outputStream.toByteArray();
        
        return new EncryptedChunk(encryptedData, iv, totalRead, encryptedData.length);
    }
    
    /**
     * Decrypt data chunk with DEK
     */
    public byte[] decryptChunk(byte[] encryptedData, byte[] iv, SecretKey dek) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, dek, spec);
        
        return cipher.doFinal(encryptedData);
    }
    
    public record EncryptedKey(byte[] encryptedData, byte[] iv) {}
    public record EncryptedChunk(byte[] data, byte[] iv, long originalSize, long encryptedSize) {}
}
```

```java
// StorageService.java
package com.filestore.service;

import com.filestore.config.FileStorageConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {
    
    private final FileStorageConfig config;
    
    /**
     * Generate storage path for chunk
     * Pattern: {basePath}/{tenantId}/{year}/{month}/{uuid}/chunk_{seq}.enc
     */
    public String generateChunkPath(String tenantId, String fileId, int sequenceNumber) {
        LocalDateTime now = LocalDateTime.now();
        String year = now.format(DateTimeFormatter.ofPattern("yyyy"));
        String month = now.format(DateTimeFormatter.ofPattern("MM"));
        
        Path dirPath = Path.of(
            config.getBasePath(),
            tenantId,
            year,
            month,
            fileId
        );
        
        String fileName = String.format("chunk_%04d.enc", sequenceNumber);
        return dirPath.resolve(fileName).toString();
    }
    
    /**
     * Write encrypted chunk to disk
     */
    public void writeChunk(String storagePath, byte[] encryptedData) throws IOException {
        Path path = Path.of(storagePath);
        Files.createDirectories(path.getParent());
        Files.write(path, encryptedData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        log.debug("Wrote chunk to {}, size: {} bytes", storagePath, encryptedData.length);
    }
    
    /**
     * Read encrypted chunk from disk
     */
    public byte[] readChunk(String storagePath) throws IOException {
        Path path = Path.of(storagePath);
        if (!Files.exists(path)) {
            throw new FileNotFoundException("Chunk not found: " + storagePath);
        }
        return Files.readAllBytes(path);
    }
    
    /**
     * Delete chunk from disk
     */
    public void deleteChunk(String storagePath) throws IOException {
        Path path = Path.of(storagePath);
        if (Files.exists(path)) {
            Files.delete(path);
            log.debug("Deleted chunk: {}", storagePath);
        }
    }
    
    /**
     * Calculate directory size
     */
    public long calculateDirectorySize(String directoryPath) throws IOException {
        Path path = Path.of(directoryPath);
        if (!Files.exists(path)) {
            return 0;
        }
        
        return Files.walk(path)
            .filter(Files::isRegularFile)
            .mapToLong(p -> {
                try {
                    return Files.size(p);
                } catch (IOException e) {
                    return 0;
                }
            })
            .sum();
    }
}
```

```java
// FileService.java
package com.filestore.service;

import com.filestore.config.FileStorageConfig;
import com.filestore.dto.*;
import com.filestore.entity.*;
import com.filestore.exception.*;
import com.filestore.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.SecretKey;
import java.io.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    
    private final FileRepository fileRepository;
    private final FileVersionRepository versionRepository;
    private final FileChunkRepository chunkRepository;
    private final FileShareRepository shareRepository;
    private final EncryptionService encryptionService;
    private final StorageService storageService;
    private final AuditService auditService;
    private final PermissionService permissionService;
    private final FileStorageConfig config;
    
    /**
     * Upload new file or new version
     */
    @Transactional
    public FileUploadResponse uploadFile(
        String tenantId,
        String userId,
        String fileName,
        String fullPath,
        MultipartFile multipartFile,
        String comment
    ) throws Exception {
        
        auditService.logAttempt(tenantId, userId, fileName, AuditAction.UPLOAD);
        
        // Check if file exists
        File existingFile = fileRepository.findByTenantIdAndFullPathAndStatusNot(
            tenantId, fullPath, FileStatus.PERMANENTLY_DELETED
        );
        
        if (existingFile != null && existingFile.getStatus() == FileStatus.DELETED) {
            throw new BusinessException("File is in deleted state. Restore it first.");
        }
        
        boolean isNewFile = existingFile == null;
        File file = isNewFile ? createNewFile(tenantId, userId, fileName, fullPath) : existingFile;
        
        // Version control
        if (!isNewFile) {
            if (file.getVersions().size() >= config.getMaxVersionsPerFile()) {
                throw new BusinessException("Maximum versions reached: " + config.getMaxVersionsPerFile());
            }
            file.setVersion(file.getVersion() + 1);
        }
        
        // Generate new DEK for this version
        SecretKey dek = encryptionService.generateDEK();
        EncryptionService.EncryptedKey encryptedDek = encryptionService.encryptDEK(dek);
        
        // Create version
        FileVersion version = new FileVersion();
        version.setFile(file);
        version.setVersion(file.getVersion());
        version.setFileSize(multipartFile.getSize());
        version.setEncryptedDek(encryptedDek.encryptedData());
        version.setDekIv(encryptedDek.iv());
        version.setUploadedBy(userId);
        version.setComment(comment);
        
        // Process and encrypt file
        List<FileChunk> chunks = processFile(file, version, multipartFile.getInputStream(), dek);
        
        // Calculate hash
        String hash = calculateFileHash(multipartFile.getInputStream());
        version.setSha256Hash(hash);
        
        // Update file metadata
        file.setFileSize(multipartFile.getSize());
        file.setMimeType(multipartFile.getContentType());
        file.setEncryptedDek(encryptedDek.encryptedData());
        file.setDekIv(encryptedDek.iv());
        file.setCurrentVersionId(version.getId());
        
        version.getChunks().addAll(chunks);
        file.getVersions().add(version);
        
        fileRepository.save(file);
        
        auditService.logSuccess(file.getId(), tenantId, userId, AuditAction.UPLOAD, 
            Map.of("version", version.getVersion(), "size", file.getFileSize()));
        
        return new FileUploadResponse(file.getId(), version.getId(), version.getVersion());
    }
    
    /**
     * Process file into encrypted chunks
     */
    private List<FileChunk> processFile(
        File file,
        FileVersion version,
        InputStream inputStream,
        SecretKey dek
    ) throws Exception {
        
        List<FileChunk> chunks = new ArrayList<>();
        long fileSize = version.getFileSize();
        boolean needsChunking = fileSize > config.getSingleFileThreshold();
        
        if (!needsChunking) {
            // Single chunk for small files
            FileChunk chunk = createChunk(file, version, inputStream, dek, 0, fileSize);
            chunks.add(chunk);
        } else {
            // Multiple chunks for large files
            int sequenceNumber = 0;
            long remaining = fileSize;
            
            while (remaining > 0) {
                long chunkSize = Math.min(config.getChunkSizeBytes(), remaining);
                FileChunk chunk = createChunk(file, version, inputStream, dek, sequenceNumber, chunkSize);
                chunks.add(chunk);
                
                remaining -= chunkSize;
                sequenceNumber++;
            }
        }
        
        log.info("Created {} chunks for file {} (version {})", chunks.size(), file.getId(), version.getVersion());
        return chunks;
    }
    
    /**
     * Create and save encrypted chunk
     */
    private FileChunk createChunk(
        File file,
        FileVersion version,
        InputStream input,
        SecretKey dek,
        int sequenceNumber,
        long maxBytes
    ) throws Exception {
        
        // Encrypt chunk
        EncryptionService.EncryptedChunk encrypted = encryptionService.encryptChunk(input, dek, maxBytes);
        
        // Generate storage path
        String storagePath = storageService.generateChunkPath(
            file.getTenantId(),
            file.getId(),
            sequenceNumber
        );
        
        // Write to disk
        storageService.writeChunk(storagePath, encrypted.data());
        
        // Create chunk entity
        FileChunk chunk = new FileChunk();
        chunk.setFile(file);
        chunk.setVersion(version);
        chunk.setSequenceNumber(sequenceNumber);
        chunk.setStoragePath(storagePath);
        chunk.setOriginalSize(encrypted.originalSize());
        chunk.setEncryptedSize(encrypted.encryptedSize());
        chunk.setIv(encrypted.iv());
        chunk.setSha256Hash(calculateHash(encrypted.data()));
        
        return chunk;
    }
    
    /**
     * Download file (latest version or specific version)
     */
    @Transactional(readOnly = true)
    public FileDownloadResponse downloadFile(
        String fileId,
        Integer versionNumber,
        String userId,
        String tenantId
    ) throws Exception {
        
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        // Permission check
        permissionService.checkReadPermission(file, userId, tenantId);
        
        if (file.getStatus() == FileStatus.DELETED) {
            throw new BusinessException("File is deleted");
        }
        
        // Get version
        FileVersion version;
        if (versionNumber == null) {
            version = versionRepository.findById(file.getCurrentVersionId())
                .orElseThrow(() -> new ResourceNotFoundException("Current version not found"));
        } else {
            version = versionRepository.findByFileAndVersion(file, versionNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Version not found"));
        }
        
        // Audit
        auditService.logSuccess(fileId, tenantId, userId, AuditAction.DOWNLOAD,
            Map.of("version", version.getVersion()));
        
        // Decrypt DEK
        SecretKey dek = encryptionService.decryptDEK(version.getEncryptedDek(), version.getDekIv());
        
        // Stream decrypted chunks
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        
        List<FileChunk> chunks = chunkRepository.findByVersionOrderBySequenceNumber(version);
        for (FileChunk chunk : chunks) {
            byte[] encryptedData = storageService.readChunk(chunk.getStoragePath());
            byte[] decryptedData = encryptionService.decryptChunk(encryptedData, chunk.getIv(), dek);
            output.write(decryptedData);
        }
        
        return new FileDownloadResponse(
            file.getFileName(),
            file.getMimeType(),
            output.toByteArray()
        );
    }
    
    /**
     * Soft delete file
     */
    @Transactional
    public void deleteFile(String fileId, String userId, String tenantId) throws Exception {
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        permissionService.checkDeletePermission(file, userId, tenantId);
        
        file.setStatus(FileStatus.DELETED);
        file.setDeletedAt(LocalDateTime.now());
        file.setPermanentDeleteAt(
            LocalDateTime.now().plusDays(config.getSoftDeleteRetentionDays())
        );
        
        fileRepository.save(file);
        
        auditService.logSuccess(fileId, tenantId, userId, AuditAction.DELETE, null);
        
        log.info("File {} soft deleted by user {}, will be permanently deleted on {}",
            fileId, userId, file.getPermanentDeleteAt());
    }
    
    /**
     * Restore soft-deleted file
     */
    @Transactional
    public void restoreFile(String fileId, String userId, String tenantId) throws Exception {
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        if (file.getStatus() != FileStatus.DELETED) {
            throw new BusinessException("File is not deleted");
        }
        
        permissionService.checkOwnership(file, userId);
        
        file.setStatus(FileStatus.ACTIVE);
        file.setDeletedAt(null);
        file.setPermanentDeleteAt(null);
        
        fileRepository.save(file);
        
        auditService.logSuccess(fileId, tenantId, userId, AuditAction.RESTORE, null);
    }
    
    /**
     * Share file with another user
     */
    @Transactional
    public void shareFile(
        String fileId,
        String userId,
        String tenantId,
        String sharedWithUserId,
        SharePermission permission,
        LocalDateTime expiresAt
    ) throws Exception {
        
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        permissionService.checkSharePermission(file, userId, tenantId);
        
        // Check if already shared
        Optional<FileShare> existing = shareRepository.findByFileAndSharedWithUserId(file, sharedWithUserId);
        
        FileShare share;
        if (existing.isPresent()) {
            share = existing.get();
            share.setPermission(permission);
            share.setExpiresAt(expiresAt);
        } else {
            share = new FileShare();
            share.setFile(file);
            share.setSharedByUserId(userId);
            share.setSharedWithUserId(sharedWithUserId);
            share.setPermission(permission);
            share.setExpiresAt(expiresAt);
        }
        
        shareRepository.save(share);
        
        auditService.logSuccess(fileId, tenantId, userId, AuditAction.SHARE,
            Map.of("sharedWith", sharedWithUserId, "permission", permission));
    }
    
    // Helper methods
    
    private File createNewFile(String tenantId, String userId, String fileName, String fullPath) {
        File file = new File();
        file.setTenantId(tenantId);
        file.setFileName(fileName);
        file.setFullPath(fullPath);
        file.setOwnerId(userId);
        file.setStatus(FileStatus.ACTIVE);
        file.setAccessLevel(AccessLevel.PRIVATE);
        return file;
    }
    
    private String calculateFileHash(InputStream input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buffer = new byte[8192];
        int bytesRead;
        
        while ((bytesRead = input.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }
        
        byte[] hashBytes = digest.digest();
        return Base64.getEncoder().encodeToString(hashBytes);
    }
    
    private String calculateHash(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(data);
        return Base64.getEncoder().encodeToString(hashBytes);
    }
}
```

I'll continue with the remaining services in the next message due to length. Shall I continue with:
1. PreviewService (with watermark support)
2. PermissionService
3. AuditService
4. Scheduled cleanup jobs
5. REST Controllers
6. DTOs and exceptions

?


# Compliance and Audit

I'll design a **comprehensive VDR (Virtual Data Room) compliant file storage system** that meets major regulatory requirements (SOC 2, ISO 27001, GDPR, HIPAA, SEC 17a-4, etc.).

## Complete VDR-Compliant File Storage System

### 1. Enhanced Entity Models with Compliance Features

```java
// File.java
package com.filestore.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "files", indexes = {
    @Index(name = "idx_tenant_owner", columnList = "tenant_id,owner_id"),
    @Index(name = "idx_classification", columnList = "data_classification"),
    @Index(name = "idx_retention", columnList = "retention_policy_id,retention_end_date"),
    @Index(name = "idx_legal_hold", columnList = "legal_hold_status")
})
@Data
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String tenantId;
    
    @Column(nullable = false)
    private String fileName;
    
    @Column(nullable = false)
    private String fullPath;
    
    private String parentId;
    
    @Column(nullable = false)
    private String ownerId;
    
    @Column(nullable = false)
    private Long fileSize;
    
    private String mimeType;
    
    @Column(nullable = false)
    private Integer version = 1;
    
    @Column(nullable = false)
    private String currentVersionId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FileStatus status = FileStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccessLevel accessLevel = AccessLevel.PRIVATE;
    
    // Compliance: Data Classification
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DataClassification dataClassification = DataClassification.INTERNAL;
    
    // Compliance: Retention Policy
    @Column(nullable = false)
    private String retentionPolicyId;
    
    @Column(nullable = false)
    private LocalDateTime retentionStartDate;
    
    @Column(nullable = false)
    private LocalDateTime retentionEndDate;
    
    // Compliance: Legal Hold (prevents deletion even after retention)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LegalHoldStatus legalHoldStatus = LegalHoldStatus.NONE;
    
    private String legalHoldId;
    private String legalHoldReason;
    private LocalDateTime legalHoldAppliedAt;
    private String legalHoldAppliedBy;
    
    // Compliance: WORM (Write Once Read Many) for SEC 17a-4
    @Column(nullable = false)
    private Boolean wormEnabled = false;
    
    private LocalDateTime wormLockedAt;
    
    // Compliance: Geographic restrictions (GDPR, data residency)
    @Column(nullable = false)
    private String storageRegion = "US-EAST";
    
    @ElementCollection
    @CollectionTable(name = "file_allowed_jurisdictions")
    private Set<String> allowedJurisdictions = new HashSet<>();
    
    // Compliance: PII Detection
    @Column(nullable = false)
    private Boolean containsPii = false;
    
    @Column(nullable = false)
    private Boolean containsPhi = false; // Protected Health Information
    
    @Column(nullable = false)
    private Boolean containsPci = false; // Payment Card Information
    
    // Encryption metadata (envelope encryption)
    @Column(columnDefinition = "BYTEA")
    private byte[] encryptedDek;
    
    @Column(columnDefinition = "BYTEA")
    private byte[] dekIv;
    
    @Column(nullable = false)
    private String keyVersion = "v1";
    
    @Column(nullable = false)
    private String encryptionAlgorithm = "AES-256-GCM";
    
    // Integrity verification
    @Column(nullable = false, length = 64)
    private String sha256Hash;
    
    @Column(nullable = false, length = 128)
    private String sha512Hash;
    
    // Digital signature for non-repudiation
    @Column(columnDefinition = "TEXT")
    private String digitalSignature;
    
    private String signatureAlgorithm;
    
    // Soft delete with compliance
    private LocalDateTime deletedAt;
    private String deletedBy;
    private String deletionReason;
    private LocalDateTime permanentDeleteAt;
    
    // Compliance: Tamper detection
    @Column(nullable = false)
    private String integrityChecksum; // Combined checksum of all metadata
    
    private LocalDateTime lastIntegrityCheck;
    
    // Metadata
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(columnDefinition = "JSONB")
    private String customMetadata; // For extensibility
    
    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL)
    private List<FileVersion> versions = new ArrayList<>();
    
    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL)
    private List<FileShare> shares = new ArrayList<>();
    
    @OneToMany(mappedBy = "file", cascade = CascadeType.ALL)
    private List<FileChunk> chunks = new ArrayList<>();
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        retentionStartDate = LocalDateTime.now();
        updateIntegrityChecksum();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        updateIntegrityChecksum();
    }
    
    private void updateIntegrityChecksum() {
        // Calculate checksum of critical fields to detect tampering
        String combined = String.join("|",
            id != null ? id : "",
            fileName,
            String.valueOf(fileSize),
            String.valueOf(version),
            ownerId,
            sha256Hash != null ? sha256Hash : ""
        );
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes());
            this.integrityChecksum = Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate integrity checksum", e);
        }
    }
}

// DataClassification.java
public enum DataClassification {
    PUBLIC,          // No restrictions
    INTERNAL,        // Company internal
    CONFIDENTIAL,    // Restricted access
    SECRET,          // Highly restricted
    TOP_SECRET       // Maximum security
}

// LegalHoldStatus.java
public enum LegalHoldStatus {
    NONE,           // No legal hold
    ACTIVE,         // Under legal hold
    RELEASED        // Legal hold released
}

// FileStatus.java
public enum FileStatus {
    ACTIVE,
    DELETED,
    LOCKED,
    QUARANTINED,    // Security scan failed
    PERMANENTLY_DELETED
}
```

```java
// FileAudit.java - Enhanced for compliance
package com.filestore.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "file_audits", indexes = {
    @Index(name = "idx_file_date", columnList = "file_id,created_at"),
    @Index(name = "idx_user_date", columnList = "user_id,created_at"),
    @Index(name = "idx_tenant_date", columnList = "tenant_id,created_at"),
    @Index(name = "idx_action_date", columnList = "action,created_at"),
    @Index(name = "idx_compliance_export", columnList = "exported_for_compliance,export_date")
})
@Data
public class FileAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String fileId;
    
    private String versionId;
    
    @Column(nullable = false)
    private String tenantId;
    
    @Column(nullable = false)
    private String userId;
    
    private String userName;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditResult result = AuditResult.SUCCESS;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    private String failureReason;
    
    // Network information
    @Column(nullable = false)
    private String ipAddress;
    
    private String userAgent;
    
    private String sessionId;
    
    private String deviceId;
    
    private String geolocation;
    
    // Compliance fields
    @Column(nullable = false)
    private String auditTrailHash; // Tamper-proof hash chain
    
    private String previousAuditHash; // Links to previous audit for blockchain-like integrity
    
    @Column(nullable = false)
    private Boolean exportedForCompliance = false;
    
    private LocalDateTime exportDate;
    
    private String exportBatchId;
    
    // Timestamps
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private Long processingTimeMs; // For performance monitoring
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        calculateAuditHash();
    }
    
    private void calculateAuditHash() {
        String combined = String.join("|",
            fileId,
            userId,
            action.toString(),
            createdAt.toString(),
            previousAuditHash != null ? previousAuditHash : ""
        );
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes());
            this.auditTrailHash = Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate audit hash", e);
        }
    }
}

public enum AuditAction {
    // File operations
    UPLOAD, DOWNLOAD, PREVIEW, UPDATE, DELETE, RESTORE, PERMANENT_DELETE,
    
    // Access operations
    VIEW, OPEN, SEARCH, LIST,
    
    // Sharing operations
    SHARE, UNSHARE, PERMISSION_CHANGE,
    
    // Compliance operations
    LEGAL_HOLD_APPLY, LEGAL_HOLD_RELEASE,
    RETENTION_POLICY_CHANGE, CLASSIFICATION_CHANGE,
    EXPORT_FOR_COMPLIANCE, INTEGRITY_CHECK,
    
    // Security operations
    ENCRYPTION_KEY_ROTATION, ACCESS_DENIED,
    MALWARE_SCAN, QUARANTINE, RELEASE_FROM_QUARANTINE,
    
    // Administrative operations
    METADATA_UPDATE, WATERMARK_APPLY, DLP_SCAN
}

public enum AuditResult {
    SUCCESS,
    FAILURE,
    PARTIAL_SUCCESS,
    ACCESS_DENIED,
    POLICY_VIOLATION
}
```

```java
// RetentionPolicy.java
package com.filestore.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "retention_policies")
@Data
public class RetentionPolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String tenantId;
    
    @Column(nullable = false, unique = true)
    private String policyName;
    
    private String description;
    
    @Column(nullable = false)
    private Integer retentionDays;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RetentionType retentionType;
    
    // Regulatory compliance tags
    @ElementCollection
    @CollectionTable(name = "retention_policy_regulations")
    private Set<String> applicableRegulations = new HashSet<>(); // e.g., "SOC2", "GDPR", "HIPAA"
    
    @Column(nullable = false)
    private Boolean allowEarlyDeletion = false;
    
    @Column(nullable = false)
    private Boolean requiresApproval = false;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

public enum RetentionType {
    REGULATORY,     // Required by regulation (cannot be reduced)
    BUSINESS,       // Business requirement (can be adjusted)
    LITIGATION,     // Legal hold (indefinite)
    CUSTOM          // Custom policy
}
```

### 2. Compliance Configuration

```java
// ComplianceConfig.java
package com.filestore.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.*;

@Configuration
@ConfigurationProperties(prefix = "compliance")
@Data
public class ComplianceConfig {
    
    // General compliance
    private Boolean enabled = true;
    private Set<String> enabledRegulations = new HashSet<>();
    
    // SOC 2
    private Soc2Config soc2 = new Soc2Config();
    
    // GDPR
    private GdprConfig gdpr = new GdprConfig();
    
    // HIPAA
    private HipaaConfig hipaa = new HipaaConfig();
    
    // SEC 17a-4
    private SecConfig sec = new SecConfig();
    
    // Audit trail
    private AuditConfig audit = new AuditConfig();
    
    @Data
    public static class Soc2Config {
        private Boolean enabled = true;
        private Boolean requireEncryptionAtRest = true;
        private Boolean requireEncryptionInTransit = true;
        private Boolean requireMfa = false;
        private Integer auditLogRetentionDays = 2555; // 7 years
        private Boolean requireChangeManagement = true;
    }
    
    @Data
    public static class GdprConfig {
        private Boolean enabled = true;
        private Boolean requireDataClassification = true;
        private Boolean enableRightToErasure = true;
        private Boolean enableRightToAccess = true;
        private Boolean requireConsentTracking = false;
        private Integer dataRetentionMaxDays = 2555; // 7 years max
        private Set<String> allowedRegions = new HashSet<>(Arrays.asList("EU", "EEA"));
    }
    
    @Data
    public static class HipaaConfig {
        private Boolean enabled = false;
        private Boolean requireAes256Encryption = true;
        private Boolean requireAccessLogs = true;
        private Integer auditLogRetentionDays = 2555; // 6 years minimum
        private Boolean requireBaa = true; // Business Associate Agreement
        private Boolean enablePhiDetection = true;
    }
    
    @Data
    public static class SecConfig {
        private Boolean enabled = false;
        private Boolean enableWorm = true;
        private Integer wormRetentionDays = 2555; // 7 years for broker-dealers
        private Boolean requireNonRewritable = true;
        private Boolean requireNonErasable = true;
    }
    
    @Data
    public static class AuditConfig {
        private Boolean enableHashChain = true;
        private Boolean enableTamperDetection = true;
        private Integer integrityCheckIntervalHours = 24;
        private Boolean exportToSiem = false;
        private String siemEndpoint;
        private Integer batchExportIntervalMinutes = 60;
    }
}
```

```yaml
# application.yml
compliance:
  enabled: true
  enabled-regulations:
    - SOC2
    - GDPR
    - ISO27001
  
  soc2:
    enabled: true
    require-encryption-at-rest: true
    require-encryption-in-transit: true
    require-mfa: true
    audit-log-retention-days: 2555
    require-change-management: true
  
  gdpr:
    enabled: true
    require-data-classification: true
    enable-right-to-erasure: true
    enable-right-to-access: true
    data-retention-max-days: 2555
    allowed-regions:
      - EU
      - EEA
      - US
  
  hipaa:
    enabled: false
    require-aes256-encryption: true
    require-access-logs: true
    audit-log-retention-days: 2555
    enable-phi-detection: true
  
  sec:
    enabled: false
    enable-worm: true
    worm-retention-days: 2555
    require-non-rewritable: true
    require-non-erasable: true
  
  audit:
    enable-hash-chain: true
    enable-tamper-detection: true
    integrity-check-interval-hours: 24
    export-to-siem: false
    batch-export-interval-minutes: 60

file:
  storage:
    base-path: /var/filestore/data
    chunk-size-bytes: 10485760
    single-file-threshold: 52428800
    max-versions-per-file: 100
    soft-delete-retention-days: 3650
    enable-watermark: true
    watermark-text: "CONFIDENTIAL"
    
    # Performance
    enable-compression: false # Don't compress encrypted data
    io-thread-pool-size: 10
    max-concurrent-uploads: 100
    
    # Security
    enable-virus-scan: true
    enable-dlp-scan: true
    
    # Encryption
    encryption:
      algorithm: AES-256-GCM
      key-rotation-days: 365
      master-key-path: /var/filestore/keys/master.key
      hsm-enabled: false # Set true for production with HSM
      
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/filestore
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
  
  jpa:
    properties:
      hibernate:
        jdbc:
          batch_size: 50
        order_inserts: true
        order_updates: true
        enable_lazy_load_no_trans: false
    show-sql: false
    
  servlet:
    multipart:
      max-file-size: 10GB
      max-request-size: 10GB

logging:
  level:
    com.filestore: INFO
    org.hibernate.SQL: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
  file:
    name: /var/log/filestore/application.log
    max-size: 100MB
    max-history: 90
```

### 3. Enhanced Services

```java
// ComplianceService.java
package com.filestore.service;

import com.filestore.config.ComplianceConfig;
import com.filestore.entity.*;
import com.filestore.exception.*;
import com.filestore.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplianceService {
    
    private final ComplianceConfig config;
    private final FileRepository fileRepository;
    private final RetentionPolicyRepository retentionPolicyRepository;
    private final AuditService auditService;
    
    /**
     * Apply legal hold to file
     */
    @Transactional
    public void applyLegalHold(
        String fileId,
        String legalHoldId,
        String reason,
        String appliedBy,
        String tenantId
    ) {
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        if (file.getLegalHoldStatus() == LegalHoldStatus.ACTIVE) {
            throw new ComplianceException("File already under legal hold");
        }
        
        file.setLegalHoldStatus(LegalHoldStatus.ACTIVE);
        file.setLegalHoldId(legalHoldId);
        file.setLegalHoldReason(reason);
        file.setLegalHoldAppliedAt(LocalDateTime.now());
        file.setLegalHoldAppliedBy(appliedBy);
        
        // Cancel any scheduled deletion
        file.setPermanentDeleteAt(null);
        
        fileRepository.save(file);
        
        auditService.logSuccess(fileId, tenantId, appliedBy, AuditAction.LEGAL_HOLD_APPLY,
            Map.of("legalHoldId", legalHoldId, "reason", reason));
        
        log.warn("Legal hold applied to file {} by {} - Reason: {}", fileId, appliedBy, reason);
    }
    
    /**
     * Release legal hold
     */
    @Transactional
    public void releaseLegalHold(
        String fileId,
        String releasedBy,
        String tenantId
    ) {
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        if (file.getLegalHoldStatus() != LegalHoldStatus.ACTIVE) {
            throw new ComplianceException("File not under legal hold");
        }
        
        String legalHoldId = file.getLegalHoldId();
        
        file.setLegalHoldStatus(LegalHoldStatus.RELEASED);
        
        // Recalculate retention if file was deleted
        if (file.getStatus() == FileStatus.DELETED) {
            RetentionPolicy policy = retentionPolicyRepository.findById(file.getRetentionPolicyId())
                .orElseThrow(() -> new ComplianceException("Retention policy not found"));
            
            file.setPermanentDeleteAt(
                file.getDeletedAt().plusDays(policy.getRetentionDays())
            );
        }
        
        fileRepository.save(file);
        
        auditService.logSuccess(fileId, tenantId, releasedBy, AuditAction.LEGAL_HOLD_RELEASE,
            Map.of("legalHoldId", legalHoldId));
        
        log.info("Legal hold {} released from file {} by {}", legalHoldId, fileId, releasedBy);
    }
    
    /**
     * Enable WORM (Write Once Read Many) for SEC 17a-4 compliance
     */
    @Transactional
    public void enableWorm(String fileId, String userId, String tenantId) {
        if (!config.getSec().getEnabled()) {
            throw new ComplianceException("SEC 17a-4 compliance not enabled");
        }
        
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        if (file.getWormEnabled()) {
            throw new ComplianceException("WORM already enabled for this file");
        }
        
        file.setWormEnabled(true);
        file.setWormLockedAt(LocalDateTime.now());
        file.setStatus(FileStatus.LOCKED);
        
        // Set retention based on SEC requirements
        RetentionPolicy secPolicy = retentionPolicyRepository
            .findByTenantIdAndApplicableRegulationsContaining(tenantId, "SEC17A4")
            .orElseThrow(() -> new ComplianceException("SEC 17a-4 retention policy not found"));
        
        file.setRetentionPolicyId(secPolicy.getId());
        file.setRetentionEndDate(
            LocalDateTime.now().plusDays(config.getSec().getWormRetentionDays())
        );
        
        fileRepository.save(file);
        
        auditService.logSuccess(fileId, tenantId, userId, AuditAction.LEGAL_HOLD_APPLY,
            Map.of("wormEnabled", true, "retentionDays", config.getSec().getWormRetentionDays()));
        
        log.info("WORM enabled for file {} - locked until {}", fileId, file.getRetentionEndDate());
    }
    
    /**
     * Verify file integrity (tamper detection)
     */
    @Transactional
    public IntegrityCheckResult verifyIntegrity(String fileId) throws Exception {
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        // Recalculate integrity checksum
        String originalChecksum = file.getIntegrityChecksum();
        String calculatedChecksum = calculateIntegrityChecksum(file);
        
        boolean isValid = originalChecksum.equals(calculatedChecksum);
        
        file.setLastIntegrityCheck(LocalDateTime.now());
        fileRepository.save(file);
        
        if (!isValid) {
            log.error("INTEGRITY VIOLATION DETECTED for file {}! Original: {}, Calculated: {}",
                fileId, originalChecksum, calculatedChecksum);
            
            auditService.logFailure(fileId, file.getTenantId(), "SYSTEM", 
                AuditAction.INTEGRITY_CHECK, "Integrity checksum mismatch");
            
            // Quarantine file
            file.setStatus(FileStatus.QUARANTINED);
            fileRepository.save(file);
        }
        
        return new IntegrityCheckResult(isValid, originalChecksum, calculatedChecksum);
    }
    
    /**
     * Check if file can be deleted based on compliance rules
     */
    public void validateDeletion(File file) {
        // Check legal hold
        if (file.getLegalHoldStatus() == LegalHoldStatus.ACTIVE) {
            throw new ComplianceException(
                "Cannot delete file under legal hold: " + file.getLegalHoldId()
            );
        }
        
        // Check WORM
        if (file.getWormEnabled()) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(file.getRetentionEndDate())) {
                throw new ComplianceException(
                    "Cannot delete WORM-protected file before retention end: " + 
                    file.getRetentionEndDate()
                );
            }
        }
        
        // Check retention policy
        RetentionPolicy policy = retentionPolicyRepository.findById(file.getRetentionPolicyId())
            .orElse(null);
        
        if (policy != null && policy.getRetentionType() == RetentionType.REGULATORY) {
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(file.getRetentionEndDate())) {
                throw new ComplianceException(
                    "Cannot delete file before regulatory retention end: " + 
                    file.getRetentionEndDate()
                );
            }
        }
    }
    
    /**
     * GDPR Right to Erasure (Right to be Forgotten)
     */
    @Transactional
    public void processRightToErasure(
        String userId,
        String tenantId,
        String requestedBy,
        String reason
    ) {
        if (!config.getGdpr().getEnabled() || !config.getGdpr().getEnableRightToErasure()) {
            throw new ComplianceException("GDPR Right to Erasure not enabled");
        }
        
        List<File> userFiles = fileRepository.findByTenantIdAndOwnerId(tenantId, userId);
        
        int deletedCount = 0;
        int skippedCount = 0;
        List<String> skippedReasons = new ArrayList<>();
        
        for (File file : userFiles) {
            try {
                validateDeletion(file);
                
                file.setStatus(FileStatus.PERMANENTLY_DELETED);
                file.setDeletedAt(LocalDateTime.now());
                file.setDeletedBy(requestedBy);
                file.setDeletionReason("GDPR Right to Erasure: " + reason);
                file.setPermanentDeleteAt(LocalDateTime.now());
                
                fileRepository.save(file);
                
                auditService.logSuccess(file.getId(), tenantId, requestedBy, 
                    AuditAction.PERMANENT_DELETE,
                    Map.of("gdprErasure", true, "reason", reason));
                
                deletedCount++;
                
            } catch (ComplianceException e) {
                skippedCount++;
                skippedReasons.add(file.getId() + ": " + e.getMessage());
                log.warn("Skipped file {} during GDPR erasure: {}", file.getId(), e.getMessage());
            }
        }
        
        log.info("GDPR Right to Erasure completed for user {}. Deleted: {}, Skipped: {}",
            userId, deletedCount, skippedCount);
    }
    
    /**
     * Export audit trail for compliance
     */
    @Transactional(readOnly = true)
    public ComplianceExport exportAuditTrail(
        String tenantId,
        LocalDateTime startDate,
        LocalDateTime endDate,
        Set<String> regulations
    ) {
        // Implementation for exporting audit trail
        // This would generate a tamper-proof export for auditors
        
        ComplianceExport export = new ComplianceExport();
        export.setTenantId(tenantId);
        export.setStartDate(startDate);
        export.setEndDate(endDate);
        export.setRegulations(regulations);
        export.setExportDate(LocalDateTime.now());
        export.setExportId(UUID.randomUUID().toString());
        
        // Calculate export hash for integrity
        String exportHash = calculateExportHash(export);
        export.setIntegrityHash(exportHash);
        
        return export;
    }
    
    // Helper methods
    
    private String calculateIntegrityChecksum(File file) {
        String combined = String.join("|",
            file.getId(),
            file.getFileName(),
            String.valueOf(file.getFileSize()),
            String.valueOf(file.getVersion()),
            file.getOwnerId(),
            file.getSha256Hash()
        );
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate checksum", e);
        }
    }
    
    private String calculateExportHash(ComplianceExport export) {
        // Implementation for calculating tamper-proof export hash
        return UUID.randomUUID().toString(); // Simplified
    }
    
    public record IntegrityCheckResult(
        boolean isValid,
        String originalChecksum,
        String calculatedChecksum
    ) {}
}
```

Due to length constraints, I'll provide the remaining critical components. Would you like me to continue with:

1. **PreviewService with Watermarking** (supports images, PDFs, documents)
2. **Enhanced FileService** with all compliance checks
3. **AuditService** with hash-chain for tamper-proof logs
4. **Scheduled Jobs** for cleanup, integrity checks, and audit export
5. **REST Controllers** with proper security
6. **Performance optimizations** (async processing, caching, connection pooling)
7. **DLP (Data Loss Prevention) Service** for PII/PHI detection
8. **Complete Docker deployment** with PostgreSQL

Which parts would you like me to detail next?


# 7,3,2

I'll continue with **7. DLP Service**, **3. AuditService**, and **2. Enhanced FileService**.

## 7. DLP (Data Loss Prevention) Service for PII/PHI Detection

```java
// DlpService.java
package com.filestore.service;

import com.filestore.config.ComplianceConfig;
import com.filestore.entity.*;
import com.filestore.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class DlpService {
    
    private final FileRepository fileRepository;
    private final ComplianceConfig complianceConfig;
    private final AuditService auditService;
    
    // PII Patterns (US-focused, extend for other jurisdictions)
    private static final Pattern SSN_PATTERN = Pattern.compile(
        "\\b\\d{3}-\\d{2}-\\d{4}\\b|\\b\\d{9}\\b"
    );
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b|\\(\\d{3}\\)\\s*\\d{3}[-.]?\\d{4}"
    );
    
    // Credit Card Patterns (Luhn algorithm validation recommended for production)
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
        "\\b(?:4[0-9]{12}(?:[0-9]{3})?|" +          // Visa
        "5[1-5][0-9]{14}|" +                         // MasterCard
        "3[47][0-9]{13}|" +                          // American Express
        "3(?:0[0-5]|[68][0-9])[0-9]{11}|" +         // Diners Club
        "6(?:011|5[0-9]{2})[0-9]{12}|" +            // Discover
        "(?:2131|1800|35\\d{3})\\d{11})\\b"         // JCB
    );
    
    // Bank Account Pattern
    private static final Pattern BANK_ACCOUNT_PATTERN = Pattern.compile(
        "\\b\\d{8,17}\\b"
    );
    
    // Driver's License (US format examples)
    private static final Pattern DRIVERS_LICENSE_PATTERN = Pattern.compile(
        "\\b[A-Z]{1,2}\\d{6,8}\\b"
    );
    
    // Passport Number
    private static final Pattern PASSPORT_PATTERN = Pattern.compile(
        "\\b[A-Z]{1,2}\\d{6,9}\\b"
    );
    
    // IP Address
    private static final Pattern IP_ADDRESS_PATTERN = Pattern.compile(
        "\\b(?:\\d{1,3}\\.){3}\\d{1,3}\\b"
    );
    
    // Medical Record Numbers (MRN)
    private static final Pattern MRN_PATTERN = Pattern.compile(
        "\\bMRN[:\\s-]?\\d{6,10}\\b|\\bMedical\\s+Record\\s+Number[:\\s-]?\\d{6,10}\\b",
        Pattern.CASE_INSENSITIVE
    );
    
    // Health Insurance Numbers
    private static final Pattern HEALTH_INSURANCE_PATTERN = Pattern.compile(
        "\\b[A-Z]{3}\\d{9}[A-Z]\\b" // Medicare format
    );
    
    // Drug/Prescription patterns
    private static final Set<String> MEDICAL_KEYWORDS = Set.of(
        "diagnosis", "prescription", "medication", "patient", "treatment",
        "symptom", "disease", "therapy", "clinical", "medical history"
    );
    
    /**
     * Scan file content for sensitive data
     */
    @Async
    @Transactional
    public void scanFileContent(String fileId, Path filePath) {
        try {
            File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
            
            log.info("Starting DLP scan for file: {}", fileId);
            long startTime = System.currentTimeMillis();
            
            DlpScanResult scanResult = performDlpScan(filePath, file.getMimeType());
            
            // Update file metadata
            file.setContainsPii(scanResult.containsPii);
            file.setContainsPhi(scanResult.containsPhi);
            file.setContainsPci(scanResult.containsPci);
            
            // Auto-classify based on findings
            if (scanResult.containsPhi) {
                file.setDataClassification(DataClassification.SECRET);
                log.warn("PHI detected in file {}, auto-classified as SECRET", fileId);
            } else if (scanResult.containsPci) {
                file.setDataClassification(DataClassification.SECRET);
                log.warn("PCI data detected in file {}, auto-classified as SECRET", fileId);
            } else if (scanResult.containsPii) {
                if (file.getDataClassification().ordinal() < DataClassification.CONFIDENTIAL.ordinal()) {
                    file.setDataClassification(DataClassification.CONFIDENTIAL);
                    log.warn("PII detected in file {}, auto-classified as CONFIDENTIAL", fileId);
                }
            }
            
            fileRepository.save(file);
            
            long duration = System.currentTimeMillis() - startTime;
            
            // Audit the scan
            Map<String, Object> details = new HashMap<>();
            details.put("containsPii", scanResult.containsPii);
            details.put("containsPhi", scanResult.containsPhi);
            details.put("containsPci", scanResult.containsPci);
            details.put("piiTypes", scanResult.piiTypes);
            details.put("confidenceScore", scanResult.confidenceScore);
            details.put("scanDurationMs", duration);
            
            auditService.logSuccess(fileId, file.getTenantId(), "SYSTEM", 
                AuditAction.DLP_SCAN, details);
            
            log.info("DLP scan completed for file {} in {}ms. PII: {}, PHI: {}, PCI: {}", 
                fileId, duration, scanResult.containsPii, scanResult.containsPhi, scanResult.containsPci);
            
        } catch (Exception e) {
            log.error("DLP scan failed for file {}: {}", fileId, e.getMessage(), e);
        }
    }
    
    /**
     * Perform actual DLP scan
     */
    private DlpScanResult performDlpScan(Path filePath, String mimeType) throws IOException {
        DlpScanResult result = new DlpScanResult();
        
        String content = extractTextContent(filePath, mimeType);
        
        if (content == null || content.isEmpty()) {
            log.warn("No text content extracted from file: {}", filePath);
            return result;
        }
        
        // Normalize content
        String normalizedContent = content.toLowerCase();
        
        // Scan for PII
        scanForPii(content, result);
        
        // Scan for PHI (HIPAA)
        scanForPhi(normalizedContent, result);
        
        // Scan for PCI
        scanForPci(content, result);
        
        // Calculate confidence score
        result.confidenceScore = calculateConfidenceScore(result);
        
        return result;
    }
    
    /**
     * Extract text from various file types
     */
    private String extractTextContent(Path filePath, String mimeType) throws IOException {
        // For demo purposes, reading as text
        // In production, use Apache Tika or specific libraries for:
        // - PDF: Apache PDFBox
        // - Word: Apache POI
        // - Excel: Apache POI
        // - Images: Tesseract OCR
        
        if (mimeType == null) {
            mimeType = Files.probeContentType(filePath);
        }
        
        // Text-based files
        if (mimeType != null && (
            mimeType.startsWith("text/") ||
            mimeType.equals("application/json") ||
            mimeType.equals("application/xml")
        )) {
            return Files.readString(filePath);
        }
        
        // For binary files, would use appropriate parsers
        // This is simplified for demonstration
        log.warn("Binary file type {} not fully supported for DLP scan", mimeType);
        return "";
    }
    
    /**
     * Scan for Personally Identifiable Information
     */
    private void scanForPii(String content, DlpScanResult result) {
        // SSN
        Matcher ssnMatcher = SSN_PATTERN.matcher(content);
        if (ssnMatcher.find()) {
            result.containsPii = true;
            result.piiTypes.add("SSN");
            result.findings.put("SSN_COUNT", countMatches(ssnMatcher));
        }
        
        // Email
        Matcher emailMatcher = EMAIL_PATTERN.matcher(content);
        if (emailMatcher.find()) {
            result.containsPii = true;
            result.piiTypes.add("EMAIL");
            result.findings.put("EMAIL_COUNT", countMatches(emailMatcher));
        }
        
        // Phone
        Matcher phoneMatcher = PHONE_PATTERN.matcher(content);
        if (phoneMatcher.find()) {
            result.containsPii = true;
            result.piiTypes.add("PHONE");
            result.findings.put("PHONE_COUNT", countMatches(phoneMatcher));
        }
        
        // Driver's License
        Matcher dlMatcher = DRIVERS_LICENSE_PATTERN.matcher(content);
        if (dlMatcher.find()) {
            result.containsPii = true;
            result.piiTypes.add("DRIVERS_LICENSE");
            result.findings.put("DL_COUNT", countMatches(dlMatcher));
        }
        
        // Passport
        Matcher passportMatcher = PASSPORT_PATTERN.matcher(content);
        if (passportMatcher.find()) {
            result.containsPii = true;
            result.piiTypes.add("PASSPORT");
            result.findings.put("PASSPORT_COUNT", countMatches(passportMatcher));
        }
        
        // IP Address
        Matcher ipMatcher = IP_ADDRESS_PATTERN.matcher(content);
        if (ipMatcher.find()) {
            result.containsPii = true;
            result.piiTypes.add("IP_ADDRESS");
            result.findings.put("IP_COUNT", countMatches(ipMatcher));
        }
    }
    
    /**
     * Scan for Protected Health Information (HIPAA)
     */
    private void scanForPhi(String content, DlpScanResult result) {
        // Medical Record Numbers
        Matcher mrnMatcher = MRN_PATTERN.matcher(content);
        if (mrnMatcher.find()) {
            result.containsPhi = true;
            result.phiTypes.add("MRN");
            result.findings.put("MRN_COUNT", countMatches(mrnMatcher));
        }
        
        // Health Insurance Numbers
        Matcher healthInsuranceMatcher = HEALTH_INSURANCE_PATTERN.matcher(content);
        if (healthInsuranceMatcher.find()) {
            result.containsPhi = true;
            result.phiTypes.add("HEALTH_INSURANCE");
            result.findings.put("HEALTH_INSURANCE_COUNT", countMatches(healthInsuranceMatcher));
        }
        
        // Medical keywords (context-based detection)
        int medicalKeywordCount = 0;
        for (String keyword : MEDICAL_KEYWORDS) {
            if (content.contains(keyword)) {
                medicalKeywordCount++;
            }
        }
        
        if (medicalKeywordCount >= 3) {
            result.containsPhi = true;
            result.phiTypes.add("MEDICAL_CONTEXT");
            result.findings.put("MEDICAL_KEYWORDS", medicalKeywordCount);
        }
        
        // If contains SSN + medical context, likely PHI
        if (result.piiTypes.contains("SSN") && medicalKeywordCount > 0) {
            result.containsPhi = true;
            result.phiTypes.add("SSN_WITH_MEDICAL_CONTEXT");
        }
    }
    
    /**
     * Scan for Payment Card Industry data
     */
    private void scanForPci(String content, DlpScanResult result) {
        // Credit Card Numbers
        Matcher ccMatcher = CREDIT_CARD_PATTERN.matcher(content);
        List<String> potentialCards = new ArrayList<>();
        
        while (ccMatcher.find()) {
            String cardNumber = ccMatcher.group().replaceAll("[\\s-]", "");
            if (isValidLuhn(cardNumber)) {
                potentialCards.add(cardNumber);
            }
        }
        
        if (!potentialCards.isEmpty()) {
            result.containsPci = true;
            result.pciTypes.add("CREDIT_CARD");
            result.findings.put("CREDIT_CARD_COUNT", potentialCards.size());
        }
        
        // Bank Account Numbers (less certain, need context)
        Matcher bankMatcher = BANK_ACCOUNT_PATTERN.matcher(content);
        if (bankMatcher.find() && content.toLowerCase().contains("account")) {
            result.containsPci = true;
            result.pciTypes.add("BANK_ACCOUNT");
            result.findings.put("BANK_ACCOUNT_COUNT", countMatches(bankMatcher));
        }
    }
    
    /**
     * Luhn algorithm validation for credit cards
     */
    private boolean isValidLuhn(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 13 || cardNumber.length() > 19) {
            return false;
        }
        
        int sum = 0;
        boolean alternate = false;
        
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));
            
            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            
            sum += digit;
            alternate = !alternate;
        }
        
        return sum % 10 == 0;
    }
    
    /**
     * Count pattern matches
     */
    private int countMatches(Matcher matcher) {
        matcher.reset();
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
    
    /**
     * Calculate confidence score for DLP findings
     */
    private double calculateConfidenceScore(DlpScanResult result) {
        int totalFindings = result.piiTypes.size() + result.phiTypes.size() + result.pciTypes.size();
        
        if (totalFindings == 0) {
            return 0.0;
        }
        
        // Base confidence on number and types of findings
        double confidence = Math.min(totalFindings * 0.2, 1.0);
        
        // High confidence patterns
        if (result.piiTypes.contains("SSN") || result.pciTypes.contains("CREDIT_CARD")) {
            confidence = Math.max(confidence, 0.9);
        }
        
        if (result.phiTypes.contains("MRN") || result.phiTypes.contains("HEALTH_INSURANCE")) {
            confidence = Math.max(confidence, 0.85);
        }
        
        return Math.round(confidence * 100.0) / 100.0;
    }
    
    /**
     * DLP Scan Result
     */
    public static class DlpScanResult {
        public boolean containsPii = false;
        public boolean containsPhi = false;
        public boolean containsPci = false;
        
        public Set<String> piiTypes = new HashSet<>();
        public Set<String> phiTypes = new HashSet<>();
        public Set<String> pciTypes = new HashSet<>();
        
        public Map<String, Integer> findings = new HashMap<>();
        public double confidenceScore = 0.0;
    }
    
    /**
     * Redact sensitive data from text
     */
    public String redactSensitiveData(String content) {
        String redacted = content;
        
        // Redact SSN
        redacted = SSN_PATTERN.matcher(redacted).replaceAll("***-**-****");
        
        // Redact Credit Cards
        redacted = CREDIT_CARD_PATTERN.matcher(redacted).replaceAll("****-****-****-****");
        
        // Redact Emails (partial)
        redacted = EMAIL_PATTERN.matcher(redacted).replaceAll(mr -> {
            String email = mr.group();
            int atIndex = email.indexOf('@');
            if (atIndex > 2) {
                return email.substring(0, 2) + "***@" + email.substring(atIndex + 1);
            }
            return "***@***";
        });
        
        // Redact Phone Numbers
        redacted = PHONE_PATTERN.matcher(redacted).replaceAll("***-***-****");
        
        return redacted;
    }
}
```

## 3. AuditService with Hash-Chain for Tamper-Proof Logs

```java
// AuditService.java
package com.filestore.service;

import com.filestore.config.ComplianceConfig;
import com.filestore.entity.*;
import com.filestore.repository.FileAuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {
    
    private final FileAuditRepository auditRepository;
    private final ComplianceConfig complianceConfig;
    
    // Thread-local for request context
    private final ThreadLocal<AuditContext> auditContext = new ThreadLocal<>();
    
    /**
     * Set audit context for current request
     */
    public void setAuditContext(String ipAddress, String userAgent, String sessionId, String deviceId, String geolocation) {
        AuditContext context = new AuditContext();
        context.ipAddress = ipAddress;
        context.userAgent = userAgent;
        context.sessionId = sessionId;
        context.deviceId = deviceId;
        context.geolocation = geolocation;
        auditContext.set(context);
    }
    
    /**
     * Clear audit context
     */
    public void clearAuditContext() {
        auditContext.remove();
    }
    
    /**
     * Log successful operation (async to not impact performance)
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logSuccess(
        String fileId,
        String tenantId,
        String userId,
        AuditAction action,
        Map<String, Object> details
    ) {
        log(fileId, null, tenantId, userId, action, AuditResult.SUCCESS, details, null);
    }
    
    /**
     * Log failed operation
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logFailure(
        String fileId,
        String tenantId,
        String userId,
        AuditAction action,
        String failureReason
    ) {
        log(fileId, null, tenantId, userId, action, AuditResult.FAILURE, null, failureReason);
    }
    
    /**
     * Log with version information
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logWithVersion(
        String fileId,
        String versionId,
        String tenantId,
        String userId,
        AuditAction action,
        AuditResult result,
        Map<String, Object> details
    ) {
        log(fileId, versionId, tenantId, userId, action, result, details, null);
    }
    
    /**
     * Core audit logging method with hash chain
     */
    private void log(
        String fileId,
        String versionId,
        String tenantId,
        String userId,
        AuditAction action,
        AuditResult result,
        Map<String, Object> details,
        String failureReason
    ) {
        long startTime = System.currentTimeMillis();
        
        try {
            FileAudit audit = new FileAudit();
            audit.setFileId(fileId);
            audit.setVersionId(versionId);
            audit.setTenantId(tenantId);
            audit.setUserId(userId);
            audit.setAction(action);
            audit.setResult(result);
            audit.setFailureReason(failureReason);
            
            // Convert details to JSON
            if (details != null && !details.isEmpty()) {
                audit.setDetails(convertToJson(details));
            }
            
            // Get audit context
            AuditContext context = auditContext.get();
            if (context != null) {
                audit.setIpAddress(context.ipAddress);
                audit.setUserAgent(context.userAgent);
                audit.setSessionId(context.sessionId);
                audit.setDeviceId(context.deviceId);
                audit.setGeolocation(context.geolocation);
            } else {
                audit.setIpAddress("UNKNOWN");
                audit.setUserAgent("SYSTEM");
            }
            
            // Hash chain for tamper-proof audit trail
            if (complianceConfig.getAudit().getEnableHashChain()) {
                String previousHash = getLastAuditHash(tenantId);
                audit.setPreviousAuditHash(previousHash);
            }
            
            long processingTime = System.currentTimeMillis() - startTime;
            audit.setProcessingTimeMs(processingTime);
            
            // Save audit (hash is calculated in @PrePersist)
            auditRepository.save(audit);
            
            // Log to external SIEM if configured
            if (complianceConfig.getAudit().getExportToSiem()) {
                exportToSiem(audit);
            }
            
        } catch (Exception e) {
            // Never fail the main operation due to audit logging
            log.error("Failed to create audit log for action {} on file {}: {}", 
                action, fileId, e.getMessage(), e);
        }
    }
    
    /**
     * Get last audit hash for hash chain
     */
    private String getLastAuditHash(String tenantId) {
        Optional<FileAudit> lastAudit = auditRepository.findFirstByTenantIdOrderByCreatedAtDesc(tenantId);
        return lastAudit.map(FileAudit::getAuditTrailHash).orElse("");
    }
    
    /**
     * Verify audit trail integrity
     */
    @Transactional(readOnly = true)
    public AuditIntegrityReport verifyAuditIntegrity(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        List<FileAudit> audits = auditRepository.findByTenantIdAndCreatedAtBetweenOrderByCreatedAtAsc(
            tenantId, startDate, endDate
        );
        
        AuditIntegrityReport report = new AuditIntegrityReport();
        report.tenantId = tenantId;
        report.startDate = startDate;
        report.endDate = endDate;
        report.totalRecords = audits.size();
        
        if (audits.isEmpty()) {
            report.isValid = true;
            return report;
        }
        
        String previousHash = "";
        
        for (int i = 0; i < audits.size(); i++) {
            FileAudit audit = audits.get(i);
            
            // Verify hash chain
            if (!previousHash.equals(audit.getPreviousAuditHash() != null ? audit.getPreviousAuditHash() : "")) {
                report.isValid = false;
                report.violations.add(new IntegrityViolation(
                    audit.getId(),
                    audit.getCreatedAt(),
                    "Hash chain broken: expected previous hash '" + previousHash + 
                    "' but found '" + audit.getPreviousAuditHash() + "'"
                ));
            }
            
            // Recalculate and verify audit hash
            String calculatedHash = calculateAuditHash(audit);
            if (!calculatedHash.equals(audit.getAuditTrailHash())) {
                report.isValid = false;
                report.violations.add(new IntegrityViolation(
                    audit.getId(),
                    audit.getCreatedAt(),
                    "Audit hash mismatch: record may have been tampered with"
                ));
            }
            
            previousHash = audit.getAuditTrailHash();
            report.verifiedRecords++;
        }
        
        if (report.violations.isEmpty()) {
            report.isValid = true;
        }
        
        log.info("Audit integrity verification completed for tenant {}. Valid: {}, Violations: {}", 
            tenantId, report.isValid, report.violations.size());
        
        return report;
    }
    
    /**
     * Recalculate audit hash for verification
     */
    private String calculateAuditHash(FileAudit audit) {
        String combined = String.join("|",
            audit.getFileId() != null ? audit.getFileId() : "",
            audit.getUserId() != null ? audit.getUserId() : "",
            audit.getAction() != null ? audit.getAction().toString() : "",
            audit.getCreatedAt() != null ? audit.getCreatedAt().toString() : "",
            audit.getPreviousAuditHash() != null ? audit.getPreviousAuditHash() : ""
        );
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(combined.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate audit hash", e);
        }
    }
    
    /**
     * Query audit logs with filtering
     */
    @Transactional(readOnly = true)
    public Page<FileAudit> queryAuditLogs(AuditQueryFilter filter, Pageable pageable) {
        // Build dynamic query based on filter
        // In production, use Specifications or QueryDSL for dynamic queries
        
        if (filter.fileId != null) {
            return auditRepository.findByFileIdOrderByCreatedAtDesc(filter.fileId, pageable);
        } else if (filter.userId != null && filter.startDate != null && filter.endDate != null) {
            return auditRepository.findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                filter.userId, filter.startDate, filter.endDate, pageable
            );
        } else if (filter.tenantId != null && filter.startDate != null && filter.endDate != null) {
            return auditRepository.findByTenantIdAndCreatedAtBetweenOrderByCreatedAtDesc(
                filter.tenantId, filter.startDate, filter.endDate, pageable
            );
        }
        
        return Page.empty(pageable);
    }
    
    /**
     * Get audit statistics for compliance reporting
     */
    @Transactional(readOnly = true)
    public AuditStatistics getAuditStatistics(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        List<FileAudit> audits = auditRepository.findByTenantIdAndCreatedAtBetweenOrderByCreatedAtAsc(
            tenantId, startDate, endDate
        );
        
        AuditStatistics stats = new AuditStatistics();
        stats.tenantId = tenantId;
        stats.startDate = startDate;
        stats.endDate = endDate;
        stats.totalEvents = audits.size();
        
        Map<AuditAction, Long> actionCounts = new HashMap<>();
        Map<String, Long> userActivityCounts = new HashMap<>();
        Map<AuditResult, Long> resultCounts = new HashMap<>();
        Set<String> uniqueUsers = new HashSet<>();
        Set<String> uniqueFiles = new HashSet<>();
        
        for (FileAudit audit : audits) {
            actionCounts.merge(audit.getAction(), 1L, Long::sum);
            userActivityCounts.merge(audit.getUserId(), 1L, Long::sum);
            resultCounts.merge(audit.getResult(), 1L, Long::sum);
            uniqueUsers.add(audit.getUserId());
            if (audit.getFileId() != null) {
                uniqueFiles.add(audit.getFileId());
            }
        }
        
        stats.actionCounts = actionCounts;
        stats.userActivityCounts = userActivityCounts;
        stats.resultCounts = resultCounts;
        stats.uniqueUsers = uniqueUsers.size();
        stats.uniqueFiles = uniqueFiles.size();
        stats.failedEvents = resultCounts.getOrDefault(AuditResult.FAILURE, 0L);
        stats.successfulEvents = resultCounts.getOrDefault(AuditResult.SUCCESS, 0L);
        
        return stats;
    }
    
    /**
     * Export audit trail to external SIEM
     */
    @Async
    private void exportToSiem(FileAudit audit) {
        // Implementation would send to SIEM (Splunk, ELK, etc.)
        // For demonstration, just log
        log.debug("Export to SIEM: {}", audit.getId());
    }
    
    /**
     * Convert map to JSON string
     */
    private String convertToJson(Map<String, Object> map) {
        try {
            // In production, use Jackson ObjectMapper
            StringBuilder json = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                if (!first) json.append(",");
                json.append("\"").append(entry.getKey()).append("\":\"")
                    .append(entry.getValue()).append("\"");
                first = false;
            }
            json.append("}");
            return json.toString();
        } catch (Exception e) {
            return "{}";
        }
    }
    
    // Inner classes
    
    private static class AuditContext {
        String ipAddress;
        String userAgent;
        String sessionId;
        String deviceId;
        String geolocation;
    }
    
    public static class AuditQueryFilter {
        public String tenantId;
        public String fileId;
        public String userId;
        public AuditAction action;
        public LocalDateTime startDate;
        public LocalDateTime endDate;
    }
    
    public static class AuditIntegrityReport {
        public String tenantId;
        public LocalDateTime startDate;
        public LocalDateTime endDate;
        public int totalRecords;
        public int verifiedRecords;
        public boolean isValid;
        public List<IntegrityViolation> violations = new ArrayList<>();
    }
    
    public static class IntegrityViolation {
        public String auditId;
        public LocalDateTime timestamp;
        public String description;
        
        public IntegrityViolation(String auditId, LocalDateTime timestamp, String description) {
            this.auditId = auditId;
            this.timestamp = timestamp;
            this.description = description;
        }
    }
    
    public static class AuditStatistics {
        public String tenantId;
        public LocalDateTime startDate;
        public LocalDateTime endDate;
        public long totalEvents;
        public long successfulEvents;
        public long failedEvents;
        public int uniqueUsers;
        public int uniqueFiles;
        public Map<AuditAction, Long> actionCounts;
        public Map<String, Long> userActivityCounts;
        public Map<AuditResult, Long> resultCounts;
    }
}
```

## 2. Enhanced FileService with All Compliance Checks

```java
// FileService.java
package com.filestore.service;

import com.filestore.config.ComplianceConfig;
import com.filestore.config.FileStorageConfig;
import com.filestore.entity.*;
import com.filestore.exception.*;
import com.filestore.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileService {
    
    private final FileRepository fileRepository;
    private final FileVersionRepository versionRepository;
    private final FileChunkRepository chunkRepository;
    private final RetentionPolicyRepository retentionPolicyRepository;
    private final FileStorageConfig config;
    private final ComplianceConfig complianceConfig;
    private final EncryptionService encryptionService;
    private final AuditService auditService;
    private final ComplianceService complianceService;
    private final DlpService dlpService;
    
    /**
     * Upload file with full compliance checks
     */
    @Transactional
    public File uploadFile(
        MultipartFile multipartFile,
        String tenantId,
        String userId,
        String parentId,
        DataClassification classification,
        String retentionPolicyId
    ) throws Exception {
        
        long startTime = System.currentTimeMillis();
        
        // Validate file
        validateFileUpload(multipartFile, tenantId);
        
        // Get retention policy
        RetentionPolicy retentionPolicy = retentionPolicyRepository.findById(retentionPolicyId)
            .orElseThrow(() -> new ComplianceException("Retention policy not found"));
        
        // Create file entity
        File file = new File();
        file.setTenantId(tenantId);
        file.setFileName(multipartFile.getOriginalFilename());
        file.setOwnerId(userId);
        file.setFileSize(multipartFile.getSize());
        file.setMimeType(multipartFile.getContentType());
        file.setParentId(parentId);
        file.setDataClassification(classification);
        file.setRetentionPolicyId(retentionPolicy.getId());
        file.setRetentionEndDate(LocalDateTime.now().plusDays(retentionPolicy.getRetentionDays()));
        file.setFullPath(buildFullPath(parentId, multipartFile.getOriginalFilename()));
        
        // Generate temporary file path
        Path tempPath = Files.createTempFile("upload-", ".tmp");
        
        try {
            // Save to temp location
            multipartFile.transferTo(tempPath.toFile());
            
            // Calculate file hashes before encryption
            FileHashes hashes = calculateFileHashes(tempPath);
            file.setSha256Hash(hashes.sha256);
            file.setSha512Hash(hashes.sha512);
            
            // Encrypt file and get encryption metadata
            EncryptionResult encryptionResult = encryptionService.encryptFile(tempPath, tenantId);
            file.setEncryptedDek(encryptionResult.encryptedDek);
            file.setDekIv(encryptionResult.dekIv);
            file.setKeyVersion(encryptionResult.keyVersion);
            
            // Determine storage strategy
            boolean needsChunking = multipartFile.getSize() > config.getSingleFileThreshold();
            
            if (needsChunking) {
                // Split and store chunks
                List<FileChunk> chunks = splitAndStoreChunks(
                    encryptionResult.encryptedPath,
                    file,
                    tenantId
                );
                file.setChunks(chunks);
                
                // Delete temporary encrypted file after chunking
                Files.deleteIfExists(encryptionResult.encryptedPath);
            } else {
                // Store as single file
                Path storagePath = buildStoragePath(tenantId, file.getId(), 0);
                Files.createDirectories(storagePath.getParent());
                Files.move(encryptionResult.encryptedPath, storagePath, StandardCopyOption.REPLACE_EXISTING);
                
                // Create single chunk entry
                FileChunk chunk = new FileChunk();
                chunk.setFile(file);
                chunk.setChunkIndex(0);
                chunk.setChunkSize(multipartFile.getSize());
                chunk.setStoragePath(storagePath.toString());
                chunk.setChecksum(hashes.sha256);
                file.getChunks().add(chunk);
            }
            
            // Create first version
            FileVersion version = createVersion(file, userId, "Initial upload");
            file.setCurrentVersionId(version.getId());
            file.getVersions().add(version);
            
            // Save file entity
            file = fileRepository.save(file);
            
            long uploadTime = System.currentTimeMillis() - startTime;
            
            // Audit successful upload
            Map<String, Object> auditDetails = new HashMap<>();
            auditDetails.put("fileName", file.getFileName());
            auditDetails.put("fileSize", file.getFileSize());
            auditDetails.put("classification", classification);
            auditDetails.put("uploadTimeMs", uploadTime);
            auditDetails.put("chunked", needsChunking);
            
            auditService.logSuccess(file.getId(), tenantId, userId, AuditAction.UPLOAD, auditDetails);
            
            // Async DLP scan
            if (config.isEnableDlpScan()) {
                Path scanPath = needsChunking ? reassembleChunksForScan(file) : buildStoragePath(tenantId, file.getId(), 0);
                dlpService.scanFileContent(file.getId(), scanPath);
            }
            
            log.info("File {} uploaded successfully by user {} in {}ms", 
                file.getId(), userId, uploadTime);
            
            return file;
            
        } finally {
            // Clean up temp file
            Files.deleteIfExists(tempPath);
        }
    }
    
    /**
     * Download file with compliance checks
     */
    @Transactional
    public FileDownloadResult downloadFile(String fileId, String userId, String tenantId) throws Exception {
        
        long startTime = System.currentTimeMillis();
        
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        // Validate access
        validateFileAccess(file, userId, tenantId, FilePermission.READ);
        
        // Check if file is deleted or quarantined
        if (file.getStatus() == FileStatus.DELETED) {
            throw new FileOperationException("File is deleted");
        }
        
        if (file.getStatus() == FileStatus.QUARANTINED) {
            throw new SecurityException("File is quarantined due to security scan failure");
        }
        
        // Reassemble file from chunks
        Path decryptedPath = reassembleAndDecryptFile(file, tenantId);
        
        long downloadTime = System.currentTimeMillis() - startTime;
        
        // Audit download
        Map<String, Object> auditDetails = new HashMap<>();
        auditDetails.put("fileName", file.getFileName());
        auditDetails.put("fileSize", file.getFileSize());
        auditDetails.put("downloadTimeMs", downloadTime);
        
        auditService.logSuccess(fileId, tenantId, userId, AuditAction.DOWNLOAD, auditDetails);
        
        FileDownloadResult result = new FileDownloadResult();
        result.file = file;
        result.filePath = decryptedPath;
        result.contentType = file.getMimeType();
        
        return result;
    }
    
    /**
     * Update file (creates new version)
     */
    @Transactional
    public File updateFile(
        String fileId,
        MultipartFile newContent,
        String userId,
        String tenantId,
        String changeDescription
    ) throws Exception {
        
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        // Validate access
        validateFileAccess(file, userId, tenantId, FilePermission.WRITE);
        
        // Check WORM
        if (file.getWormEnabled()) {
            throw new ComplianceException("Cannot modify WORM-protected file");
        }
        
        // Check if max versions reached
        if (file.getVersions().size() >= config.getMaxVersionsPerFile()) {
            throw new FileOperationException("Maximum number of versions reached");
        }
        
        // Upload new content (similar to upload)
        Path tempPath = Files.createTempFile("update-", ".tmp");
        
        try {
            newContent.transferTo(tempPath.toFile());
            
            // Calculate new hashes
            FileHashes hashes = calculateFileHashes(tempPath);
            
            // Encrypt
            EncryptionResult encryptionResult = encryptionService.encryptFile(tempPath, tenantId);
            
            // Update file metadata
            file.setFileSize(newContent.getSize());
            file.setSha256Hash(hashes.sha256);
            file.setSha512Hash(hashes.sha512);
            file.setEncryptedDek(encryptionResult.encryptedDek);
            file.setDekIv(encryptionResult.dekIv);
            file.setVersion(file.getVersion() + 1);
            
            // Clear old chunks
            chunkRepository.deleteByFileId(fileId);
            file.getChunks().clear();
            
            // Store new content
            boolean needsChunking = newContent.getSize() > config.getSingleFileThreshold();
            
            if (needsChunking) {
                List<FileChunk> chunks = splitAndStoreChunks(
                    encryptionResult.encryptedPath,
                    file,
                    tenantId
                );
                file.setChunks(chunks);
                Files.deleteIfExists(encryptionResult.encryptedPath);
            } else {
                Path storagePath = buildStoragePath(tenantId, file.getId(), file.getVersion());
                Files.createDirectories(storagePath.getParent());
                Files.move(encryptionResult.encryptedPath, storagePath, StandardCopyOption.REPLACE_EXISTING);
                
                FileChunk chunk = new FileChunk();
                chunk.setFile(file);
                chunk.setChunkIndex(0);
                chunk.setChunkSize(newContent.getSize());
                chunk.setStoragePath(storagePath.toString());
                chunk.setChecksum(hashes.sha256);
                file.getChunks().add(chunk);
            }
            
            // Create new version
            FileVersion version = createVersion(file, userId, changeDescription);
            file.setCurrentVersionId(version.getId());
            file.getVersions().add(version);
            
            file = fileRepository.save(file);
            
            // Audit
            auditService.logWithVersion(fileId, version.getId(), tenantId, userId, 
                AuditAction.UPDATE, AuditResult.SUCCESS,
                Map.of("version", file.getVersion(), "description", changeDescription));
            
            // DLP scan new content
            if (config.isEnableDlpScan()) {
                Path scanPath = needsChunking ? reassembleChunksForScan(file) : 
                    buildStoragePath(tenantId, file.getId(), file.getVersion());
                dlpService.scanFileContent(file.getId(), scanPath);
            }
            
            return file;
            
        } finally {
            Files.deleteIfExists(tempPath);
        }
    }
    
    /**
     * Soft delete file
     */
    @Transactional
    public void deleteFile(String fileId, String userId, String tenantId, String reason) {
        
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        // Validate access
        validateFileAccess(file, userId, tenantId, FilePermission.DELETE);
        
        // Compliance check
        complianceService.validateDeletion(file);
        
        // Get retention policy
        RetentionPolicy policy = retentionPolicyRepository.findById(file.getRetentionPolicyId())
            .orElseThrow(() -> new ComplianceException("Retention policy not found"));
        
        // Soft delete
        file.setStatus(FileStatus.DELETED);
        file.setDeletedAt(LocalDateTime.now());
        file.setDeletedBy(userId);
        file.setDeletionReason(reason);
        
        // Calculate permanent deletion date
        LocalDateTime permanentDeleteDate = LocalDateTime.now()
            .plusDays(Math.max(policy.getRetentionDays(), config.getSoftDeleteRetentionDays()));
        file.setPermanentDeleteAt(permanentDeleteDate);
        
        fileRepository.save(file);
        
        // Audit
        auditService.logSuccess(fileId, tenantId, userId, AuditAction.DELETE,
            Map.of("reason", reason, "permanentDeleteAt", permanentDeleteDate));
        
        log.info("File {} soft deleted by {}. Permanent deletion scheduled for {}", 
            fileId, userId, permanentDeleteDate);
    }
    
    /**
     * Restore deleted file
     */
    @Transactional
    public void restoreFile(String fileId, String userId, String tenantId) {
        
        File file = fileRepository.findById(fileId)
            .orElseThrow(() -> new ResourceNotFoundException("File not found"));
        
        if (file.getStatus() != FileStatus.DELETED) {
            throw new FileOperationException("File is not deleted");
        }
        
        // Validate access
        validateFileAccess(file, userId, tenantId, FilePermission.WRITE);
        
        // Check legal hold
        if (file.getLegalHoldStatus() == LegalHoldStatus.ACTIVE) {
            throw new ComplianceException("Cannot restore file under legal hold without proper authorization");
        }
        
        // Restore
        file.setStatus(FileStatus.ACTIVE);
        file.setDeletedAt(null);
        file.setDeletedBy(null);
        file.setDeletionReason(null);
        file.setPermanentDeleteAt(null);
        
        fileRepository.save(file);
        
        // Audit
        auditService.logSuccess(fileId, tenantId, userId, AuditAction.RESTORE, Map.of());
        
        log.info("File {} restored by {}", fileId, userId);
    }
    
    // Helper methods
    
    private void validateFileUpload(MultipartFile file, String tenantId) {
        if (file.isEmpty()) {
            throw new FileOperationException("File is empty");
        }
        
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new FileOperationException("File name is required");
        }
        
        // Additional validations...
    }
    
    private void validateFileAccess(File file, String userId, String tenantId, FilePermission permission) {
        if (!file.getTenantId().equals(tenantId)) {
            throw new AccessDeniedException("Access denied to file from different tenant");
        }
        
        // Check ownership
        boolean isOwner = file.getOwnerId().equals(userId);
        
        // Check shared permissions
        boolean hasPermission = file.getShares().stream()
            .anyMatch(share -> share.getSharedWithUserId().equals(userId) && 
                share.getPermission().hasPermission(permission));
        
        if (!isOwner && !hasPermission) {
            auditService.logFailure(file.getId(), tenantId, userId, 
                AuditAction.ACCESS_DENIED, "Insufficient permissions");
            throw new AccessDeniedException("Access denied");
        }
    }
    
    private FileHashes calculateFileHashes(Path filePath) throws Exception {
        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
        MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
        
        try (InputStream is = Files.newInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                sha256.update(buffer, 0, read);
                sha512.update(buffer, 0, read);
            }
        }
        
        FileHashes hashes = new FileHashes();
        hashes.sha256 = Base64.getEncoder().encodeToString(sha256.digest());
        hashes.sha512 = Base64.getEncoder().encodeToString(sha512.digest());
        return hashes;
    }
    
    private List<FileChunk> splitAndStoreChunks(Path encryptedPath, File file, String tenantId) throws IOException {
        List<FileChunk> chunks = new ArrayList<>();
        long fileSize = Files.size(encryptedPath);
        int chunkSize = config.getChunkSizeBytes();
        int chunkIndex = 0;
        
        try (InputStream is = Files.newInputStream(encryptedPath)) {
            byte[] buffer = new byte[chunkSize];
            int bytesRead;
            
            while ((bytesRead = is.read(buffer)) != -1) {
                Path chunkPath = buildChunkPath(tenantId, file.getId(), chunkIndex);
                Files.createDirectories(chunkPath.getParent());
                Files.write(chunkPath, Arrays.copyOf(buffer, bytesRead));
                
                FileChunk chunk = new FileChunk();
                chunk.setFile(file);
                chunk.setChunkIndex(chunkIndex);
                chunk.setChunkSize((long) bytesRead);
                chunk.setStoragePath(chunkPath.toString());
                
                // Calculate chunk checksum
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                chunk.setChecksum(Base64.getEncoder().encodeToString(
                    digest.digest(Arrays.copyOf(buffer, bytesRead))
                ));
                
                chunks.add(chunk);
                chunkIndex++;
            }
        } catch (Exception e) {
            throw new IOException("Failed to split file into chunks", e);
        }
        
        return chunks;
    }
    
    private Path reassembleAndDecryptFile(File file, String tenantId) throws Exception {
        // Reassemble chunks
        Path assembledPath = Files.createTempFile("assembled-", ".enc");
        
        List<FileChunk> chunks = file.getChunks();
        chunks.sort(Comparator.comparingInt(FileChunk::getChunkIndex));
        
        try (OutputStream os = Files.newOutputStream(assembledPath)) {
            for (FileChunk chunk : chunks) {
                Path chunkPath = Paths.get(chunk.getStoragePath());
                Files.copy(chunkPath, os);
            }
        }
        
        // Decrypt
        Path decryptedPath = encryptionService.decryptFile(
            assembledPath,
            file.getEncryptedDek(),
            file.getDekIv(),
            tenantId
        );
        
        Files.deleteIfExists(assembledPath);
        
        return decryptedPath;
    }
    
    private Path reassembleChunksForScan(File file) throws IOException {
        Path assembledPath = Files.createTempFile("scan-", ".tmp");
        
        List<FileChunk> chunks = file.getChunks();
        chunks.sort(Comparator.comparingInt(FileChunk::getChunkIndex));
        
        try (OutputStream os = Files.newOutputStream(assembledPath)) {
            for (FileChunk chunk : chunks) {
                Path chunkPath = Paths.get(chunk.getStoragePath());
                Files.copy(chunkPath, os);
            }
        }
        
        return assembledPath;
    }
    
    private FileVersion createVersion(File file, String userId, String description) {
        FileVersion version = new FileVersion();
        version.setFile(file);
        version.setVersionNumber(file.getVersion());
        version.setFileSize(file.getFileSize());
        version.setSha256Hash(file.getSha256Hash());
        version.setCreatedBy(userId);
        version.setChangeDescription(description);
        return version;
    }
    
    private Path buildStoragePath(String tenantId, String fileId, int version) {
        return Paths.get(config.getBasePath(), tenantId, 
            fileId.substring(0, 2), fileId.substring(2, 4), fileId, "v" + version);
    }
    
    private Path buildChunkPath(String tenantId, String fileId, int chunkIndex) {
        return Paths.get(config.getBasePath(), tenantId,
            fileId.substring(0, 2), fileId.substring(2, 4), fileId, "chunk_" + chunkIndex);
    }
    
    private String buildFullPath(String parentId, String fileName) {
        // Implementation for building hierarchical path
        return "/" + fileName; // Simplified
    }
    
    // Inner classes
    
    private static class FileHashes {
        String sha256;
        String sha512;
    }
    
    public static class FileDownloadResult {
        public File file;
        public Path filePath;
        public String contentType;
    }
}
```

Would you like me to continue with the remaining components (1, 4, 5, 6)?

# remaining

## What DLP (Data Loss Prevention) Should Do

**DLP Service Purpose:**
The DLP service scans file content to automatically detect sensitive data and enforce security policies. Here's what it accomplishes:

### 1. **Automatic Data Discovery**
- Scans uploaded files for sensitive patterns (SSN, credit cards, medical records, etc.)
- Identifies PII (Personally Identifiable Information)
- Detects PHI (Protected Health Information) - HIPAA compliance
- Finds PCI (Payment Card Industry) data - credit cards, bank accounts

### 2. **Auto-Classification**
- Automatically upgrades file classification based on content
- Example: If file contains SSN + medical keywords → Auto-classify as "SECRET" (PHI)
- Example: If file contains credit cards → Auto-classify as "SECRET" (PCI)
- Example: If file contains email/phone only → Auto-classify as "CONFIDENTIAL" (PII)

### 3. **Policy Enforcement**
- Blocks uploads containing restricted data types (if configured)
- Quarantines files with high-risk content
- Triggers alerts when sensitive data is detected
- Prevents accidental data leakage

### 4. **Compliance Reporting**
- Tracks which files contain sensitive data
- Generates reports for auditors showing PII/PHI/PCI inventory
- Helps answer: "Where is our customer SSN data stored?"
- Supports GDPR, HIPAA, PCI-DSS compliance requirements

### 5. **Content Redaction**
- Can redact sensitive data from file previews
- Masks SSNs, credit cards before displaying to unauthorized users
- Example: "123-45-6789" → "***-**-****"

### 6. **Risk Scoring**
- Assigns confidence scores to findings
- Helps prioritize which files need human review
- Reduces false positives

---

Now let me continue with the remaining components:

## 1. ComplianceService - Centralized Compliance Logic

```java
// ComplianceService.java
package com.filestore.service;

import com.filestore.config.ComplianceConfig;
import com.filestore.entity.*;
import com.filestore.exception.*;
import com.filestore.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ComplianceService {
    
    private final FileRepository fileRepository;
    private final RetentionPolicyRepository retentionPolicyRepository;
    private final LegalHoldRepository legalHoldRepository;
    private final ComplianceConfig complianceConfig;
    private final AuditService auditService;
    
    /**
     * Validate if file can be deleted based on compliance rules
     */
    @Transactional(readOnly = true)
    public void validateDeletion(File file) {
        // Check legal hold
        if (file.getLegalHoldStatus() == LegalHoldStatus.ACTIVE) {
            throw new ComplianceException(
                "Cannot delete file under active legal hold: " + file.getLegalHoldId()
            );
        }
        
        // Check retention policy
        RetentionPolicy policy = retentionPolicyRepository.findById(file.getRetentionPolicyId())
            .orElseThrow(() -> new ComplianceException("Retention policy not found"));
        
        if (policy.getEnforceRetention() && file.getRetentionEndDate().isAfter(LocalDateTime.now())) {
            long daysRemaining = ChronoUnit.DAYS.between(LocalDateTime.now(), file.getRetentionEndDate());
            throw new ComplianceException(
                String.format("File is under retention period. %d days remaining until %s",
                    daysRemaining, file.getRetentionEndDate())
            );
        }
        
        // Check WORM
        if (file.getWormEnabled()) {
            throw new ComplianceException("Cannot delete WORM-protected file");
        }
        
        // Check if file contains PHI and requires special deletion approval
        if (file.getContainsPhi() && complianceConfig.getHipaa().isRequireApprovalForPhiDeletion()) {
            // In production, check for deletion approval record
            log.warn("PHI file deletion requires approval: {}", file.getId());
        }
    }
    
    /**
     * Validate if file can be modified
     */
    @Transactional(readOnly = true)
    public void validateModification(File file) {
        // Check WORM
        if (file.getWormEnabled()) {
            throw new ComplianceException("Cannot modify WORM-protected file");
        }
        
        // Check legal hold
        if (file.getLegalHoldStatus() == LegalHoldStatus.ACTIVE) {
            throw new ComplianceException("Cannot modify file under active legal hold");
        }
        
        // Additional regulatory checks based on data classification
        if (file.getDataClassification() == DataClassification.SECRET) {
            log.warn("Modification of SECRET classified file: {}", file.getId());
            // Could require additional authorization
        }
    }
    
    /**
     * Validate if file can be shared
     */
    @Transactional(readOnly = true)
    public void validateSharing(File file, String targetUserId, String targetTenantId) {
        // Check if cross-tenant sharing is allowed
        if (!file.getTenantId().equals(targetTenantId)) {
            if (!complianceConfig.getGeneral().isAllowCrossTenantSharing()) {
                throw new ComplianceException("Cross-tenant file sharing is disabled");
            }
        }
        
        // Check data classification restrictions
        if (file.getDataClassification() == DataClassification.SECRET ||
            file.getDataClassification() == DataClassification.TOP_SECRET) {
            
            if (complianceConfig.getGeneral().isPreventHighClassificationSharing()) {
                throw new ComplianceException(
                    "Cannot share files classified as SECRET or TOP_SECRET"
                );
            }
        }
        
        // Check PHI sharing restrictions
        if (file.getContainsPhi() && complianceConfig.getHipaa().isEnablePhiRestrictions()) {
            // HIPAA requires Business Associate Agreements for PHI sharing
            log.warn("PHI file sharing requires BAA verification: {}", file.getId());
            // In production, verify BAA exists between parties
        }
        
        // Check PCI sharing restrictions
        if (file.getContainsPci()) {
            throw new ComplianceException("Cannot share files containing payment card data");
        }
        
        // Check legal hold
        if (file.getLegalHoldStatus() == LegalHoldStatus.ACTIVE) {
            log.warn("Sharing file under legal hold: {}", file.getId());
            // May require legal approval
        }
    }
    
    /**
     * Apply legal hold to files
     */
    @Transactional
    public LegalHold applyLegalHold(
        String tenantId,
        String holdName,
        String caseNumber,
        String requestedBy,
        LocalDateTime expirationDate,
        List<String> fileIds
    ) {
        // Create legal hold record
        LegalHold legalHold = new LegalHold();
        legalHold.setTenantId(tenantId);
        legalHold.setHoldName(holdName);
        legalHold.setCaseNumber(caseNumber);
        legalHold.setRequestedBy(requestedBy);
        legalHold.setStatus(LegalHoldStatus.ACTIVE);
        legalHold.setExpirationDate(expirationDate);
        
        legalHold = legalHoldRepository.save(legalHold);
        
        // Apply hold to files
        int filesAffected = 0;
        for (String fileId : fileIds) {
            try {
                File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new ResourceNotFoundException("File not found: " + fileId));
                
                if (!file.getTenantId().equals(tenantId)) {
                    log.warn("Skipping file from different tenant: {}", fileId);
                    continue;
                }
                
                file.setLegalHoldStatus(LegalHoldStatus.ACTIVE);
                file.setLegalHoldId(legalHold.getId());
                file.setLegalHoldAppliedAt(LocalDateTime.now());
                
                fileRepository.save(file);
                filesAffected++;
                
                // Audit
                auditService.logSuccess(fileId, tenantId, requestedBy, 
                    AuditAction.LEGAL_HOLD_APPLIED,
                    Map.of("holdId", legalHold.getId(), "caseNumber", caseNumber));
                
            } catch (Exception e) {
                log.error("Failed to apply legal hold to file {}: {}", fileId, e.getMessage());
            }
        }
        
        log.info("Legal hold {} applied to {} files for case {}", 
            legalHold.getId(), filesAffected, caseNumber);
        
        return legalHold;
    }
    
    /**
     * Release legal hold
     */
    @Transactional
    public void releaseLegalHold(String holdId, String releasedBy) {
        LegalHold legalHold = legalHoldRepository.findById(holdId)
            .orElseThrow(() -> new ResourceNotFoundException("Legal hold not found"));
        
        if (legalHold.getStatus() != LegalHoldStatus.ACTIVE) {
            throw new ComplianceException("Legal hold is not active");
        }
        
        // Release hold from all files
        List<File> affectedFiles = fileRepository.findByLegalHoldId(holdId);
        
        for (File file : affectedFiles) {
            file.setLegalHoldStatus(LegalHoldStatus.RELEASED);
            file.setLegalHoldReleasedAt(LocalDateTime.now());
            fileRepository.save(file);
            
            // Audit
            auditService.logSuccess(file.getId(), file.getTenantId(), releasedBy,
                AuditAction.LEGAL_HOLD_RELEASED,
                Map.of("holdId", holdId, "caseNumber", legalHold.getCaseNumber()));
        }
        
        // Update hold status
        legalHold.setStatus(LegalHoldStatus.RELEASED);
        legalHold.setReleasedAt(LocalDateTime.now());
        legalHold.setReleasedBy(releasedBy);
        legalHoldRepository.save(legalHold);
        
        log.info("Legal hold {} released by {}. Affected {} files", 
            holdId, releasedBy, affectedFiles.size());
    }
    
    /**
     * Generate compliance report
     */
    @Transactional(readOnly = true)
    public ComplianceReport generateComplianceReport(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        ComplianceReport report = new ComplianceReport();
        report.tenantId = tenantId;
        report.generatedAt = LocalDateTime.now();
        report.reportPeriodStart = startDate;
        report.reportPeriodEnd = endDate;
        
        // Get all files in date range
        List<File> files = fileRepository.findByTenantIdAndCreatedAtBetween(tenantId, startDate, endDate);
        
        report.totalFiles = files.size();
        report.totalStorageBytes = files.stream().mapToLong(File::getFileSize).sum();
        
        // Classification breakdown
        Map<DataClassification, Long> classificationCounts = new EnumMap<>(DataClassification.class);
        for (File file : files) {
            classificationCounts.merge(file.getDataClassification(), 1L, Long::sum);
        }
        report.filesByClassification = classificationCounts;
        
        // Sensitive data counts
        report.filesWithPii = (int) files.stream().filter(File::getContainsPii).count();
        report.filesWithPhi = (int) files.stream().filter(File::getContainsPhi).count();
        report.filesWithPci = (int) files.stream().filter(File::getContainsPci).count();
        
        // Encryption status
        report.encryptedFiles = files.size(); // All files are encrypted in this system
        report.encryptionCompliance = 100.0;
        
        // Legal holds
        report.filesUnderLegalHold = (int) files.stream()
            .filter(f -> f.getLegalHoldStatus() == LegalHoldStatus.ACTIVE)
            .count();
        
        // WORM protected
        report.wormProtectedFiles = (int) files.stream()
            .filter(File::getWormEnabled)
            .count();
        
        // Retention compliance
        long filesWithActiveRetention = files.stream()
            .filter(f -> f.getRetentionEndDate() != null && 
                        f.getRetentionEndDate().isAfter(LocalDateTime.now()))
            .count();
        report.retentionCompliance = files.isEmpty() ? 100.0 : 
            (filesWithActiveRetention * 100.0 / files.size());
        
        // Access violations (from audit logs)
        report.accessViolations = countAccessViolations(tenantId, startDate, endDate);
        
        // Files pending deletion
        report.filesPendingDeletion = (int) files.stream()
            .filter(f -> f.getStatus() == FileStatus.DELETED)
            .count();
        
        // Calculate overall compliance score
        report.overallComplianceScore = calculateComplianceScore(report);
        
        // Recommendations
        report.recommendations = generateRecommendations(report);
        
        return report;
    }
    
    /**
     * Count access violations from audit logs
     */
    private int countAccessViolations(String tenantId, LocalDateTime startDate, LocalDateTime endDate) {
        AuditService.AuditQueryFilter filter = new AuditService.AuditQueryFilter();
        filter.tenantId = tenantId;
        filter.startDate = startDate;
        filter.endDate = endDate;
        filter.action = AuditAction.ACCESS_DENIED;
        
        // In production, would query audit repository
        return 0; // Placeholder
    }
    
    /**
     * Calculate overall compliance score
     */
    private double calculateComplianceScore(ComplianceReport report) {
        double score = 0.0;
        int factors = 0;
        
        // Encryption compliance (weight: 30%)
        score += report.encryptionCompliance * 0.3;
        factors++;
        
        // Retention compliance (weight: 25%)
        score += report.retentionCompliance * 0.25;
        factors++;
        
        // Data classification (weight: 20%)
        // Score based on proper classification of sensitive data
        double classificationScore = 100.0;
        if (report.filesWithPhi > 0 || report.filesWithPci > 0) {
            long secretOrHigher = report.filesByClassification.getOrDefault(DataClassification.SECRET, 0L) +
                                  report.filesByClassification.getOrDefault(DataClassification.TOP_SECRET, 0L);
            long sensitive = report.filesWithPhi + report.filesWithPci;
            classificationScore = (secretOrHigher >= sensitive) ? 100.0 : (secretOrHigher * 100.0 / sensitive);
        }
        score += classificationScore * 0.2;
        factors++;
        
        // Access control (weight: 15%)
        // Lower score if many access violations
        double accessScore = Math.max(0, 100.0 - (report.accessViolations * 5.0));
        score += accessScore * 0.15;
        factors++;
        
        // Legal hold compliance (weight: 10%)
        // All files under hold should be properly marked
        score += 100.0 * 0.1; // Assuming compliance
        factors++;
        
        return Math.round(score * 100.0) / 100.0;
    }
    
    /**
     * Generate compliance recommendations
     */
    private List<String> generateRecommendations(ComplianceReport report) {
        List<String> recommendations = new ArrayList<>();
        
        if (report.overallComplianceScore < 80.0) {
            recommendations.add("CRITICAL: Overall compliance score is below 80%. Immediate action required.");
        }
        
        if (report.encryptionCompliance < 100.0) {
            recommendations.add("Encrypt all files at rest to meet compliance requirements.");
        }
        
        if (report.filesWithPii > 0 && report.filesByClassification.getOrDefault(DataClassification.PUBLIC, 0L) > 0) {
            recommendations.add("Review files with PII - some may be incorrectly classified as PUBLIC.");
        }
        
        if (report.filesWithPhi > 0) {
            recommendations.add("Ensure HIPAA Business Associate Agreements are in place for PHI data sharing.");
        }
        
        if (report.filesWithPci > 0) {
            recommendations.add("Implement PCI-DSS controls for files containing payment card data.");
        }
        
        if (report.accessViolations > 10) {
            recommendations.add("High number of access violations detected. Review access control policies.");
        }
        
        if (report.retentionCompliance < 95.0) {
            recommendations.add("Improve retention policy compliance. Ensure all files have valid retention policies.");
        }
        
        if (report.filesPendingDeletion > 50) {
            recommendations.add("Large number of files pending permanent deletion. Review deletion queue.");
        }
        
        return recommendations;
    }
    
    /**
     * Compliance Report DTO
     */
    public static class ComplianceReport {
        public String tenantId;
        public LocalDateTime generatedAt;
        public LocalDateTime reportPeriodStart;
        public LocalDateTime reportPeriodEnd;
        
        public int totalFiles;
        public long totalStorageBytes;
        
        public Map<DataClassification, Long> filesByClassification;
        
        public int filesWithPii;
        public int filesWithPhi;
        public int filesWithPci;
        
        public int encryptedFiles;
        public double encryptionCompliance;
        
        public int filesUnderLegalHold;
        public int wormProtectedFiles;
        public double retentionCompliance;
        
        public int accessViolations;
        public int filesPendingDeletion;
        
        public double overallComplianceScore;
        public List<String> recommendations;
    }
}
```

## 4. RetentionService - Automated Retention Management

```java
// RetentionService.java
package com.filestore.service;

import com.filestore.config.ComplianceConfig;
import com.filestore.entity.*;
import com.filestore.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class RetentionService {
    
    private final FileRepository fileRepository;
    private final RetentionPolicyRepository retentionPolicyRepository;
    private final FileChunkRepository chunkRepository;
    private final AuditService auditService;
    private final ComplianceConfig complianceConfig;
    
    /**
     * Scheduled job to process retention policies
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void processRetentionPolicies() {
        log.info("Starting retention policy processing");
        long startTime = System.currentTimeMillis();
        
        RetentionProcessingResult result = new RetentionProcessingResult();
        
        try {
            // Find all active retention policies
            List<RetentionPolicy> policies = retentionPolicyRepository.findByStatus(PolicyStatus.ACTIVE);
            
            for (RetentionPolicy policy : policies) {
                processPolicy(policy, result);
            }
            
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("Retention policy processing completed in {}ms. " +
                    "Expired: {}, Deleted: {}, Errors: {}",
                duration, result.filesExpired, result.filesDeleted, result.errors);
            
        } catch (Exception e) {
            log.error("Retention policy processing failed: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Process individual retention policy
     */
    private void processPolicy(RetentionPolicy policy, RetentionProcessingResult result) {
        log.debug("Processing retention policy: {} - {}", policy.getId(), policy.getPolicyName());
        
        // Find files covered by this policy where retention has expired
        List<File> expiredFiles = fileRepository.findByRetentionPolicyIdAndRetentionEndDateBefore(
            policy.getId(),
            LocalDateTime.now()
        );
        
        for (File file : expiredFiles) {
            try {
                processExpiredFile(file, policy, result);
            } catch (Exception e) {
                log.error("Failed to process expired file {}: {}", file.getId(), e.getMessage());
                result.errors++;
            }
        }
    }
    
    /**
     * Process individual expired file
     */
    private void processExpiredFile(File file, RetentionPolicy policy, RetentionProcessingResult result) {
        log.debug("Processing expired file: {} (retention ended: {})", 
            file.getId(), file.getRetentionEndDate());
        
        // Check if file is under legal hold
        if (file.getLegalHoldStatus() == LegalHoldStatus.ACTIVE) {
            log.info("Skipping file {} - under legal hold", file.getId());
            return;
        }
        
        // Check if file is WORM protected
        if (file.getWormEnabled()) {
            log.info("Skipping file {} - WORM protected", file.getId());
            return;
        }
        
        // Apply retention action based on policy
        switch (policy.getRetentionAction()) {
            case DELETE:
                handleDeleteAction(file, policy, result);
                break;
                
            case ARCHIVE:
                handleArchiveAction(file, policy, result);
                break;
                
            case REVIEW:
                handleReviewAction(file, policy, result);
                break;
                
            case NONE:
                log.debug("No action for file {} under policy {}", file.getId(), policy.getId());
                break;
        }
        
        result.filesExpired++;
    }
    
    /**
     * Handle DELETE retention action
     */
    private void handleDeleteAction(File file, RetentionPolicy policy, RetentionProcessingResult result) {
        if (file.getStatus() == FileStatus.DELETED) {
            // Already soft-deleted, check if ready for permanent deletion
            if (file.getPermanentDeleteAt() != null && 
                file.getPermanentDeleteAt().isBefore(LocalDateTime.now())) {
                
                permanentlyDeleteFile(file, result);
            }
        } else {
            // Soft delete the file
            file.setStatus(FileStatus.DELETED);
            file.setDeletedAt(LocalDateTime.now());
            file.setDeletedBy("SYSTEM");
            file.setDeletionReason("Retention period expired");
            
            // Set permanent deletion date
            file.setPermanentDeleteAt(LocalDateTime.now()
                .plusDays(complianceConfig.getRetention().getSoftDeleteRetentionDays()));
            
            fileRepository.save(file);
            
            // Audit
            auditService.logSuccess(file.getId(), file.getTenantId(), "SYSTEM",
                AuditAction.RETENTION_DELETE,
                Map.of(
                    "policyId", policy.getId(),
                    "retentionEndDate", file.getRetentionEndDate(),
                    "permanentDeleteAt", file.getPermanentDeleteAt()
                ));
            
            log.info("File {} soft-deleted due to retention policy. Permanent deletion: {}", 
                file.getId(), file.getPermanentDeleteAt());
        }
    }
    
    /**
     * Permanently delete file and all data
     */
    private void permanentlyDeleteFile(File file, RetentionProcessingResult result) {
        log.info("Permanently deleting file: {}", file.getId());
        
        try {
            // Delete physical files
            List<FileChunk> chunks = file.getChunks();
            for (FileChunk chunk : chunks) {
                try {
                    Path chunkPath = Paths.get(chunk.getStoragePath());
                    Files.deleteIfExists(chunkPath);
                    log.debug("Deleted chunk: {}", chunkPath);
                } catch (IOException e) {
                    log.error("Failed to delete chunk {}: {}", chunk.getStoragePath(), e.getMessage());
                }
            }
            
            // Delete chunk records
            chunkRepository.deleteByFileId(file.getId());
            
            // Delete file record
            fileRepository.delete(file);
            
            // Audit
            auditService.logSuccess(file.getId(), file.getTenantId(), "SYSTEM",
                AuditAction.PERMANENT_DELETE,
                Map.of("deletedAt", LocalDateTime.now()));
            
            result.filesDeleted++;
            
            log.info("File {} permanently deleted", file.getId());
            
        } catch (Exception e) {
            log.error("Failed to permanently delete file {}: {}", file.getId(), e.getMessage(), e);
            result.errors++;
        }
    }
    
    /**
     * Handle ARCHIVE retention action
     */
    private void handleArchiveAction(File file, RetentionPolicy policy, RetentionProcessingResult result) {
        if (file.getStatus() == FileStatus.ACTIVE) {
            file.setStatus(FileStatus.ARCHIVED);
            fileRepository.save(file);
            
            // Audit
            auditService.logSuccess(file.getId(), file.getTenantId(), "SYSTEM",
                AuditAction.ARCHIVE,
                Map.of("policyId", policy.getId(), "retentionEndDate", file.getRetentionEndDate()));
            
            log.info("File {} archived due to retention policy", file.getId());
        }
    }
    
    /**
     * Handle REVIEW retention action
     */
    private void handleReviewAction(File file, RetentionPolicy policy, RetentionProcessingResult result) {
        if (file.getStatus() != FileStatus.PENDING_REVIEW) {
            file.setStatus(FileStatus.PENDING_REVIEW);
            fileRepository.save(file);
            
            // In production, would send notification to file owner or compliance team
            
            // Audit
            auditService.logSuccess(file.getId(), file.getTenantId(), "SYSTEM",
                AuditAction.RETENTION_REVIEW_REQUIRED,
                Map.of("policyId", policy.getId(), "retentionEndDate", file.getRetentionEndDate()));
            
            log.info("File {} marked for review due to retention policy", file.getId());
        }
    }
    
    /**
     * Create retention policy
     */
    @Transactional
    public RetentionPolicy createRetentionPolicy(RetentionPolicyRequest request) {
        RetentionPolicy policy = new RetentionPolicy();
        policy.setTenantId(request.tenantId);
        policy.setPolicyName(request.policyName);
        policy.setDescription(request.description);
        policy.setRetentionDays(request.retentionDays);
        policy.setRetentionAction(request.retentionAction);
        policy.setEnforceRetention(request.enforceRetention);
        policy.setApplicableClassifications(request.applicableClassifications);
        policy.setApplicableFileTypes(request.applicableFileTypes);
        policy.setStatus(PolicyStatus.ACTIVE);
        
        policy = retentionPolicyRepository.save(policy);
        
        log.info("Created retention policy: {} - {}", policy.getId(), policy.getPolicyName());
        
        return policy;
    }
    
    /**
     * Apply retention policy to existing files
     */
    @Transactional
    public int applyRetentionPolicy(String policyId) {
        RetentionPolicy policy = retentionPolicyRepository.findById(policyId)
            .orElseThrow(() -> new IllegalArgumentException("Policy not found"));
        
        // Find files matching policy criteria
        List<File> matchingFiles = findFilesMatchingPolicy(policy);
        
        int appliedCount = 0;
        for (File file : matchingFiles) {
            // Skip if file already has a retention policy
            if (file.getRetentionPolicyId() != null) {
                continue;
            }
            
            file.setRetentionPolicyId(policy.getId());
            file.setRetentionEndDate(file.getCreatedAt().plusDays(policy.getRetentionDays()));
            
            fileRepository.save(file);
            appliedCount++;
        }
        
        log.info("Applied retention policy {} to {} files", policyId, appliedCount);
        
        return appliedCount;
    }
    
    /**
     * Find files matching policy criteria
     */
    private List<File> findFilesMatchingPolicy(RetentionPolicy policy) {
        // In production, use Specifications or QueryDSL for complex queries
        List<File> allFiles = fileRepository.findByTenantId(policy.getTenantId());
        List<File> matchingFiles = new ArrayList<>();
        
        for (File file : allFiles) {
            boolean matches = true;
            
            // Check classification
            if (policy.getApplicableClassifications() != null && 
                !policy.getApplicableClassifications().isEmpty()) {
                if (!policy.getApplicableClassifications().contains(file.getDataClassification())) {
                    matches = false;
                }
            }
            
            // Check file type (extension)
            if (matches && policy.getApplicableFileTypes() != null && 
                !policy.getApplicableFileTypes().isEmpty()) {
                String extension = getFileExtension(file.getFileName());
                if (!policy.getApplicableFileTypes().contains(extension)) {
                    matches = false;
                }
            }
            
            if (matches) {
                matchingFiles.add(file);
            }
        }
        
        return matchingFiles;
    }
    
    /**
     * Get file extension
     */
    private String getFileExtension(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        return lastDot > 0 ? fileName.substring(lastDot + 1).toLowerCase() : "";
    }
    
    /**
     * Get retention statistics
     */
    @Transactional(readOnly = true)
    public RetentionStatistics getRetentionStatistics(String tenantId) {
        RetentionStatistics stats = new RetentionStatistics();
        stats.tenantId = tenantId;
        
        List<File> allFiles = fileRepository.findByTenantId(tenantId);
        
        stats.totalFiles = allFiles.size();
        
        LocalDateTime now = LocalDateTime.now();
        
        for (File file : allFiles) {
            if (file.getRetentionEndDate() != null) {
                if (file.getRetentionEndDate().isBefore(now)) {
                    stats.filesExpired++;
                } else if (file.getRetentionEndDate().isBefore(now.plusDays(30))) {
                    stats.filesExpiringIn30Days++;
                }
            } else {
                stats.filesWithoutPolicy++;
            }
            
            if (file.getStatus() == FileStatus.DELETED) {
                stats.filesSoftDeleted++;
            }
            
            if (file.getLegalHoldStatus() == LegalHoldStatus.ACTIVE) {
                stats.filesUnderLegalHold++;
            }
        }
        
        return stats;
    }
    
    // DTOs
    
    public static class RetentionPolicyRequest {
        public String tenantId;
        public String policyName;
        public String description;
        public int retentionDays;
        public RetentionAction retentionAction;
        public boolean enforceRetention;
        public Set<DataClassification> applicableClassifications;
        public Set<String> applicableFileTypes;
    }
    
    private static class RetentionProcessingResult {
        int filesExpired = 0;
        int filesDeleted = 0;
        int errors = 0;
    }
    
    public static class RetentionStatistics {
        public String tenantId;
        public int totalFiles;
        public int filesWithoutPolicy;
        public int filesExpired;
        public int filesExpiringIn30Days;
        public int filesSoftDeleted;
        public int filesUnderLegalHold;
    }
}
```

## 5. REST Controllers

```java
// FileController.java
package com.filestore.controller;

import com.filestore.entity.*;
import com.filestore.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.*;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {
    
    private final FileService fileService;
    private final AuditService auditService;
    
    /**
     * Upload file
     */
    @PostMapping("/upload")
    public ResponseEntity<FileUploadResponse> uploadFile(
        @RequestParam("file") MultipartFile file,
        @RequestParam(required = false) String parentId,
        @RequestParam DataClassification classification,
        @RequestParam String retentionPolicyId,
        @RequestHeader("X-Tenant-ID") String tenantId,
        @AuthenticationPrincipal UserDetails userDetails,
        HttpServletRequest request
    ) {
        try {
            // Set audit context
            setAuditContext(request);
            
            File uploadedFile = fileService.uploadFile(
                file,
                tenantId,
                userDetails.getUsername(),
                parentId,
                classification,
                retentionPolicyId
            );
            
            FileUploadResponse response = new FileUploadResponse();
            response.fileId = uploadedFile.getId();
            response.fileName = uploadedFile.getFileName();
            response.fileSize = uploadedFile.getFileSize();
            response.uploadedAt = uploadedFile.getCreatedAt();
            response.classification = uploadedFile.getDataClassification();
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("File upload failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            auditService.clearAuditContext();
        }
    }
    
    /**
     * Download file
     */
    @GetMapping("/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(
        @PathVariable String fileId,
        @RequestHeader("X-Tenant-ID") String tenantId,
        @AuthenticationPrincipal UserDetails userDetails,
        HttpServletRequest request
    ) {
        try {
            setAuditContext(request);
            
            FileService.FileDownloadResult result = fileService.downloadFile(
                fileId,
                userDetails.getUsername(),
                tenantId
            );
            
            Resource resource = new UrlResource(result.filePath.toUri());
            
            return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(result.contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + result.file.getFileName() + "\"")
                .body(resource);
                
        } catch (Exception e) {
            log.error("File download failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            auditService.clearAuditContext();
        }
    }
    
    /**
     * Delete file
     */
    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
        @PathVariable String fileId,
        @RequestParam(required = false) String reason,
        @RequestHeader("X-Tenant-ID") String tenantId,
        @AuthenticationPrincipal UserDetails userDetails,
        HttpServletRequest request
    ) {
        try {
            setAuditContext(request);
            
            fileService.deleteFile(
                fileId,
                userDetails.getUsername(),
                tenantId,
                reason != null ? reason : "User requested deletion"
            );
            
            return ResponseEntity.noContent().build();
            
        } catch (Exception e) {
            log.error("File deletion failed: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            auditService.clearAuditContext();
        }
    }
    
    /**
     * Set audit context from HTTP request
     */
    private void setAuditContext(HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String sessionId = request.getSession().getId();
        String deviceId = request.getHeader("X-Device-ID");
        String geolocation = request.getHeader("X-Geo-Location");
        
        auditService.setAuditContext(ipAddress, userAgent, sessionId, deviceId, geolocation);
    }
    
    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
    
    // Response DTOs
    
    public static class FileUploadResponse {
        public String fileId;
        public String fileName;
        public Long fileSize;
        public LocalDateTime uploadedAt;
        public DataClassification classification;
    }
}
```

```java
// ComplianceController.java
package com.filestore.controller;

import com.filestore.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/compliance")
@RequiredArgsConstructor
public class ComplianceController {
    
    private final ComplianceService complianceService;
    private final AuditService auditService;
    private final RetentionService retentionService;
    
    /**
     * Generate compliance report
     */
    @GetMapping("/reports")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<ComplianceService.ComplianceReport> getComplianceReport(
        @RequestHeader("X-Tenant-ID") String tenantId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        ComplianceService.ComplianceReport report = 
            complianceService.generateComplianceReport(tenantId, startDate, endDate);
        
        return ResponseEntity.ok(report);
    }
    
    /**
     * Verify audit trail integrity
     */
    @GetMapping("/audit/verify")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<AuditService.AuditIntegrityReport> verifyAuditIntegrity(
        @RequestHeader("X-Tenant-ID") String tenantId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        AuditService.AuditIntegrityReport report = 
            auditService.verifyAuditIntegrity(tenantId, startDate, endDate);
        
        return ResponseEntity.ok(report);
    }
    
    /**
     * Get retention statistics
     */
    @GetMapping("/retention/statistics")
    @PreAuthorize("hasRole('COMPLIANCE_OFFICER')")
    public ResponseEntity<RetentionService.RetentionStatistics> getRetentionStatistics(
        @RequestHeader("X-Tenant-ID") String tenantId
    ) {
        RetentionService.RetentionStatistics stats = 
            retentionService.getRetentionStatistics(tenantId);
        
        return ResponseEntity.ok(stats);
    }
    
    /**
     * Apply legal hold
     */
    @PostMapping("/legal-hold")
    @PreAuthorize("hasRole('LEGAL')")
    public ResponseEntity<LegalHold> applyLegalHold(
        @RequestBody LegalHoldRequest request,
        @RequestHeader("X-Tenant-ID") String tenantId
    ) {
        LegalHold hold = complianceService.applyLegalHold(
            tenantId,
            request.holdName,
            request.caseNumber,
            request.requestedBy,
            request.expirationDate,
            request.fileIds
        );
        
        return ResponseEntity.ok(hold);
    }
    
    public static class LegalHoldRequest {
        public String holdName;
        public String caseNumber;
        public String requestedBy;
        public LocalDateTime expirationDate;
        public List<String> fileIds;
    }
}
```

## 6. Configuration Updates

```java
// SecurityConfig.java
package com.filestore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf().disable()
            .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                .antMatchers("/api/files/**").authenticated()
                .antMatchers("/api/compliance/**").hasAnyRole("COMPLIANCE_OFFICER", "LEGAL")
                .anyRequest().permitAll()
            .and()
            .httpBasic();
        
        return http.build();
    }
}
```

```java
// AsyncConfig.java
package com.filestore.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    // Async configuration for background tasks
}
```

This completes all the major components! Would you like me to add anything else, such as:
- Docker configuration
- Integration tests
- Monitoring setup
- API documentation (Swagger)
- Database migration scripts?