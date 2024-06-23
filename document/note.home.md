<!-- Wrap your entire content in a div with a style attribute -->
<div style="font-size: 1.2em;">

# JPA

## PrePersist action and domain events

```java
import org.springframework.data.domain.DomainEvents;

@Entity
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String customerName;

    private LocalDateTime creationDate;

    @Transient
    private List<Object> domainEvents = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.creationDate = LocalDateTime.now();
        // Add the domain event to the list
        domainEvents.add(new OrderCreatedEvent(this));
    }

    @DomainEvents
    protected Collection<Object> domainEvents() {
        return domainEvents;
    }

    @AfterDomainEventPublication
    protected void clearDomainEvents() {
        domainEvents.clear();
    }
}
```

```java
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class OrderEventListener {

    @EventListener
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        Order order = event.getOrder();
        // Handle the event (e.g., send a notification, update a status, etc.)
        System.out.println("Order created: " + order.getId() + " for customer " + order.getCustomerName());
    }
}
```

# Encryption

## PKCE (Proof Key for Code Exchange) and SCRAM-SHA-256

PKCE (Proof Key for Code Exchange) and SCRAM-SHA-256 (Salted Challenge Response Authentication Mechanism using SHA-256) are both mechanisms used to enhance security in authentication processes, but they serve different purposes and are used in different contexts.

### PKCE (Proof Key for Code Exchange)

PKCE is an extension to the OAuth 2.0 authorization code flow designed to improve the security of public clients (clients that cannot securely store secrets, such as single-page applications or mobile apps). PKCE helps protect against certain attacks, such as authorization code interception, by ensuring that the authorization code issued to the client is exchanged in a secure manner.

#### How PKCE Works:

1. **Code Challenge Generation**:
    - The client generates a random string called the code verifier.
    - The client then creates a code challenge by applying a transformation (e.g., SHA-256 hashing) to the code verifier.

2. **Authorization Request**:
    - The client initiates the authorization request to the authorization server, including the code challenge and the method used to create it (e.g., `S256` for SHA-256).

3. **Authorization Server Response**:
    - The authorization server validates the request and returns an authorization code to the client.

4. **Token Request**:
    - The client sends a token request to the token endpoint, including the authorization code and the original code verifier.

5. **Token Response**:
    - The authorization server verifies the code challenge by applying the same transformation to the code verifier and comparing it with the original code challenge.
    - If they match, the server issues an access token to the client.

### SCRAM-SHA-256 (Salted Challenge Response Authentication Mechanism using SHA-256)

SCRAM-SHA-256 is an authentication mechanism used primarily in protocols like SASL (Simple Authentication and Security Layer) to securely authenticate clients and servers. It provides a way to authenticate a user without sending passwords in plaintext over the network.

#### How SCRAM-SHA-256 Works:

1. **Client First Message**:
    - The client sends a message to the server including a random nonce and the username.

2. **Server First Message**:
    - The server responds with a salt, a server nonce, and other necessary parameters.

3. **Client Final Message**:
    - The client uses the salt and nonces to compute a client proof, including evidence that it knows the password, and sends it to the server.

4. **Server Final Message**:
    - The server verifies the client's proof and, if correct, responds with its own proof to the client.

### Comparison and Relation:

Though PKCE and SCRAM-SHA-256 are both used to enhance security in authentication, they are employed in different contexts and solve different problems:

- **PKCE** is specific to OAuth 2.0 and is designed to securely exchange authorization codes between public clients and authorization servers.
- **SCRAM-SHA-256** is a general authentication mechanism used in protocols like SASL to securely authenticate users without transmitting passwords in plaintext.

#### Specific Differences:

- **Context**:
    - PKCE is used in the context of OAuth 2.0 authorization flows.
    - SCRAM-SHA-256 is used in the context of client-server authentication, often within protocols like SASL.

- **Purpose**:
    - PKCE enhances the security of the OAuth 2.0 authorization code flow, particularly for public clients.
    - SCRAM-SHA-256 provides secure password-based authentication.

- **Mechanism**:
    - PKCE uses a code challenge and code verifier to secure the authorization code exchange.
    - SCRAM-SHA-256 uses salted, hashed passwords and nonces to perform secure challenge-response authentication.

### Summary

While PKCE and SCRAM-SHA-256 both enhance security in different authentication scenarios, they are not directly related. PKCE is specific to OAuth 2.0 and secures the authorization code flow, whereas SCRAM-SHA-256 is a general authentication mechanism used to securely authenticate users and servers in various protocols. Each addresses different aspects of security in their respective domains.



## The GitHub OAUTH steps sample

- GET   http://localhost:8080/hello
  - 302 /oauth2/authorization/github
- GET   http://localhost:8080/oauth2/authorization/github
  - 302 https://github.com/login/oauth/authorize?response_type=code&client_id=cc30276322adbf59445b&scope=read:user&state=nvHgO46MwGDn1aT9QaJuL_nI1-6ytjmrhMKMk9u_oSQ%3D&redirect_uri=http://localhost:8080/login/oauth2/code/github
- GET   https://github.com/login/oauth/authorize?response_type=code&client_id=cc30276322adbf59445b&scope=read:user&state=nvHgO46MwGDn1aT9QaJuL_nI1-6ytjmrhMKMk9u_oSQ=&redirect_uri=http://localhost:8080/login/oauth2/code/github
  - 302 https://github.com/login?client_id=cc30276322adbf59445b&return_to=%2Flogin%2Foauth%2Fauthorize%3Fclient_id%3Dcc30276322adbf59445b%26redirect_uri%3Dhttp%253A%252F%252Flocalhost%253A8080%252Flogin%252Foauth2%252Fcode%252Fgithub%26response_type%3Dcode%26scope%3Dread%253Auser%26state%3DnvHgO46MwGDn1aT9QaJuL_nI1-6ytjmrhMKMk9u_oSQ%253D
- GET   https://github.com/login?client_id=cc30276322adbf59445b&return_to=/login/oauth/authorize?client_id=cc30276322adbf59445b&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Flogin%2Foauth2%2Fcode%2Fgithub&response_type=code&scope=read%3Auser&state=nvHgO46MwGDn1aT9QaJuL_nI1-6ytjmrhMKMk9u_oSQ%3D
  - this is login page
  - enter login 
- POST  https://github.com/session
  - post login username/password , password not hashed.
  - 302 https://github.com/login/oauth/authorize?client_id=cc30276322adbf59445b&redirect_uri=http%3A%2F%2Flocalhost%3A8080%2Flogin%2Foauth2%2Fcode%2Fgithub&response_type=code&scope=read%3Auser&state=nvHgO46MwGDn1aT9QaJuL_nI1-6ytjmrhMKMk9u_oSQ%3D
- GET   https://github.com/login/oauth/authorize?client_id=cc30276322adbf59445b&redirect_uri=http://localhost:8080/login/oauth2/code/github&response_type=code&scope=read:user&state=nvHgO46MwGDn1aT9QaJuL_nI1-6ytjmrhMKMk9u_oSQ=
  - the return page make me href to another page below
- GET   http://localhost:8080/login/oauth2/code/github?code=1b809a220db60f18607a&state=nvHgO46MwGDn1aT9QaJuL_nI1-6ytjmrhMKMk9u_oSQ=
  - 302 /hello
- GET   http://localhost:8080/hello




org.springframework.boot.autoconfigure.web.reactive.WebSessionIdResolverAutoConfiguration
org.springframework.boot.autoconfigure.web.reactive.WebSessionIdResolverAutoConfiguration

</div>
