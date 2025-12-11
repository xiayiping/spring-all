# Jwt normal Fields

Here are all the standard JWT claims defined in RFC 7519 and related OAuth2/OIDC specifications:

## Registered Claims (RFC 7519)

These are the official JWT standard claims:

### `iss` (Issuer)
**Purpose**: Identifies who created and signed the token

**Type**: String (case-sensitive)

**Example**:
```json
{
  "iss": "https://auth.myapp.com"
}
```

**Use**: Validates the token came from a trusted authority. Critical in federated systems where multiple identity providers exist.

---

### `sub` (Subject)
**Purpose**: Identifies the principal (user) the token is about

**Type**: String (case-sensitive, unique within issuer context)

**Example**:
```json
{
  "sub": "user123",
  "sub": "auth0|507f1f77bcf86cd799439011"  // Common format
}
```

**Use**: Usually the user ID. Should be immutable (don't use email as subject).

---

### `aud` (Audience)
**Purpose**: Identifies the recipients the token is intended for

**Type**: String or Array of strings (case-sensitive)

**Example**:
```json
{
  "aud": "api.myapp.com"
}

{
  "aud": ["api.myapp.com", "cdn.myapp.com"]
}
```

**Use**: Prevents tokens from being used on unintended services. Each service validates its identifier is in the audience.

---

### `exp` (Expiration Time)
**Purpose**: Time after which the token is no longer valid

**Type**: NumericDate (seconds since Unix epoch)

**Example**:
```json
{
  "exp": 1702300000  // December 11, 2023, 13:33:20 GMT
}
```

**Use**: Critical security feature. Tokens auto-expire. Validators reject expired tokens.

---

### `nbf` (Not Before)
**Purpose**: Time before which the token must not be accepted

**Type**: NumericDate (seconds since Unix epoch)

**Example**:
```json
{
  "nbf": 1702296400,  // Token valid starting from this time
  "exp": 1702300000
}
```

**Use**: For scheduled token activation. Rarely used in practice. Useful for pre-generated tokens that activate later.

---

### `iat` (Issued At)
**Purpose**: Time when the token was issued

**Type**: NumericDate (seconds since Unix epoch)

**Example**:
```json
{
  "iat": 1702296400
}
```

**Use**: Auditing, determining token age, rejecting old tokens even if not expired. Some systems reject tokens issued before a certain time (e.g., after password change).

---

### `jti` (JWT ID)
**Purpose**: Unique identifier for the token

**Type**: String (case-sensitive)

**Example**:
```json
{
  "jti": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Use**: Prevents replay attacks, enables token revocation (store JTI in blacklist/database), tracking individual tokens.

---

## OAuth 2.0 / OpenID Connect Claims

These are standard in OAuth2 and OIDC (RFC 6749, OpenID Connect Core):

### `scope`
**Purpose**: Permissions granted to the token

**Type**: String (space-separated list)

**Example**:
```json
{
  "scope": "read write profile email"
}
```

**Use**: Authorization decisions. API checks if required scope is present.

---

### `client_id`
**Purpose**: Identifier of the client application

**Type**: String

**Example**:
```json
{
  "client_id": "mobile_app_abc123"
}
```

**Use**: Identifies which application requested the token. Useful for auditing and different permissions per client.

---

### `azp` (Authorized Party)
**Purpose**: Client ID of the party the token was issued to

**Type**: String

**Example**:
```json
{
  "aud": ["api.myapp.com", "other-service.com"],
  "azp": "mobile_app_abc123"
}
```

**Use**: When token has multiple audiences, `azp` identifies which client it was issued to. Required when `aud` is an array in OIDC.

---

## OpenID Connect ID Token Claims

Additional claims for OIDC ID tokens:

### `name`
**Purpose**: User's full name

**Type**: String

**Example**: `"name": "John Doe"`

---

### `given_name`
**Purpose**: User's first name

**Type**: String

**Example**: `"given_name": "John"`

---

### `family_name`
**Purpose**: User's last name

**Type**: String

**Example**: `"family_name": "Doe"`

---

### `middle_name`
**Purpose**: User's middle name

**Type**: String

**Example**: `"middle_name": "Robert"`

---

### `nickname`
**Purpose**: User's casual name

**Type**: String

**Example**: `"nickname": "Johnny"`

---

### `preferred_username`
**Purpose**: Username the user prefers

**Type**: String

**Example**: `"preferred_username": "johndoe"`

---

### `profile`
**Purpose**: URL of user's profile page

**Type**: String (URL)

**Example**: `"profile": "https://myapp.com/users/johndoe"`

---

### `picture`
**Purpose**: URL of user's profile picture

**Type**: String (URL)

**Example**: `"picture": "https://cdn.myapp.com/avatars/johndoe.jpg"`

---

### `website`
**Purpose**: URL of user's website

**Type**: String (URL)

**Example**: `"website": "https://johndoe.com"`

---

### `email`
**Purpose**: User's email address

**Type**: String

**Example**: `"email": "john.doe@example.com"`

---

### `email_verified`
**Purpose**: Whether email has been verified

**Type**: Boolean

**Example**: `"email_verified": true`

**Use**: Critical for security. Don't trust unverified emails for account recovery.

---

### `gender`
**Purpose**: User's gender

**Type**: String

**Example**: `"gender": "male"`

---

### `birthdate`
**Purpose**: User's birthday

**Type**: String (ISO 8601 YYYY-MM-DD format)

**Example**: `"birthdate": "1990-12-25"`

---

### `zoneinfo`
**Purpose**: User's timezone

**Type**: String (IANA timezone)

**Example**: `"zoneinfo": "America/New_York"`

---

### `locale`
**Purpose**: User's locale/language preference

**Type**: String (BCP47 language tag)

**Example**: `"locale": "en-US"`

---

### `phone_number`
**Purpose**: User's phone number

**Type**: String (E.164 format recommended)

**Example**: `"phone_number": "+1 (425) 555-1212"`

---

### `phone_number_verified`
**Purpose**: Whether phone has been verified

**Type**: Boolean

**Example**: `"phone_number_verified": false`

---

### `address`
**Purpose**: User's postal address

**Type**: JSON object

**Example**:
```json
{
  "address": {
    "formatted": "123 Main St, Anytown, USA",
    "street_address": "123 Main St",
    "locality": "Anytown",
    "region": "CA",
    "postal_code": "12345",
    "country": "USA"
  }
}
```

---

### `updated_at`
**Purpose**: Last time user's info was updated

**Type**: NumericDate

**Example**: `"updated_at": 1702296400`

---

### `nonce`
**Purpose**: String to associate client session with ID token

**Type**: String

**Example**: `"nonce": "n-0S6_WzA2Mj"`

**Use**: Prevents replay attacks in OIDC flows. Client sends nonce, validates it matches in returned token.

---

### `acr` (Authentication Context Class Reference)
**Purpose**: Indicates strength of authentication

**Type**: String

**Example**: `"acr": "urn:mace:incommon:iap:silver"`

**Use**: Indicates MFA was used, biometric auth, etc.

---

### `amr` (Authentication Methods References)
**Purpose**: Methods used to authenticate

**Type**: Array of strings

**Example**: `"amr": ["pwd", "mfa", "otp"]`

**Values**: `pwd` (password), `mfa`, `otp`, `sms`, `face`, `fpt` (fingerprint), etc.

---

### `auth_time`
**Purpose**: Time when user authentication occurred

**Type**: NumericDate

**Example**: `"auth_time": 1702296000`

**Use**: For requiring recent authentication (e.g., re-auth for sensitive operations).

---

## Common Custom Claims (Not Standard but Widely Used)

### `roles` or `role`
**Purpose**: User's roles

**Type**: Array of strings or string

**Example**:
```json
{
  "roles": ["admin", "user"]
}
```

---

### `permissions`
**Purpose**: Granular permissions

**Type**: Array of strings

**Example**:
```json
{
  "permissions": ["read:users", "write:posts", "delete:comments"]
}
```

---

### `groups`
**Purpose**: User's group memberships

**Type**: Array of strings

**Example**:
```json
{
  "groups": ["admins", "developers", "managers"]
}
```

---

### `tenant_id` or `org_id`
**Purpose**: Multi-tenant identifier

**Type**: String

**Example**: `"tenant_id": "org_abc123"`

---

### `token_type`
**Purpose**: Distinguishes access vs refresh token

**Type**: String

**Example**: `"token_type": "access"` or `"token_type": "refresh"`

---

### `session_id`
**Purpose**: Links tokens to a session

**Type**: String

**Example**: `"session_id": "sess_xyz789"`

---

## Complete Example

```json
{
  "iss": "https://auth.myapp.com",
  "sub": "auth0|507f1f77bcf86cd799439011",
  "aud": ["api.myapp.com", "cdn.myapp.com"],
  "exp": 1702300000,
  "nbf": 1702296400,
  "iat": 1702296400,
  "jti": "550e8400-e29b-41d4-a716-446655440000",
  
  "scope": "read write profile email",
  "client_id": "mobile_app_v2",
  "azp": "mobile_app_v2",
  
  "name": "John Doe",
  "given_name": "John",
  "family_name": "Doe",
  "email": "john.doe@example.com",
  "email_verified": true,
  "picture": "https://cdn.myapp.com/avatars/john.jpg",
  "locale": "en-US",
  
  "roles": ["user", "premium"],
  "tenant_id": "org_abc123",
  "token_type": "access"
}
```

## Key Takeaways

**Always include**: `iss`, `sub`, `aud`, `exp`, `iat`

**For revocation**: `jti`

**For authorization**: `scope`, `roles`, `permissions`

**For user info**: OIDC claims (`name`, `email`, etc.)

**For security**: `nbf`, `nonce`, `auth_time`, `acr`, `amr`

Keep JWTs small - only include claims needed by the recipient. Sensitive data should not be in JWTs (they're not encrypted, just base64 encoded).