
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


##
- GET   http://127.0.0.1/oauth2/authorization/msa
    - 302 https://uat-sso.mysmartadvisor.com/connect/authorize?response_type=code&client_id=msa-app&scope=openid%20role%20esop%20fund%20stock%20offline_access%20ids%20msa%20forex&state=XdkNgE-kFJK2jhEqvoWjG1xKXqbHDy5sjaQPvqXKrlc%3D&redirect_uri=http://127.0.0.1/oauth2/login/code/msa&nonce=yS_F4huLWWmPb1mEGl37mGXOc799HB5YzW_l5qFMibs
- GET   https://uat-sso.mysmartadvisor.com/connect/authorize?response_type=code&client_id=msa-app&scope=openid%20role%20esop%20fund%20stock%20offline_access%20ids%20msa%20forex&state=XdkNgE-kFJK2jhEqvoWjG1xKXqbHDy5sjaQPvqXKrlc%3D&redirect_uri=http://127.0.0.1/oauth2/login/code/msa&nonce=yS_F4huLWWmPb1mEGl37mGXOc799HB5YzW_l5qFMibs
    - 302 https://uat-sso.mysmartadvisor.com/Account/Login?ReturnUrl=%2Fconnect%2Fauthorize%2Fcallback%3Fresponse_type%3Dcode%26client_id%3Dmsa-app%26scope%3Dopenid%2520role%2520esop%2520fund%2520stock%2520offline_access%2520ids%2520msa%2520forex%26state%3DXdkNgE-kFJK2jhEqvoWjG1xKXqbHDy5sjaQPvqXKrlc%253D%26redirect_uri%3Dhttp%253A%252F%252F127.0.0.1%252Foauth2%252Flogin%252Fcode%252Fmsa%26nonce%3DyS_F4huLWWmPb1mEGl37mGXOc799HB5YzW_l5qFMibs
    - set-cookie: .AspNetCore.Culture=c%3Den-US%7Cuic%3Den-US; expires=Fri, 22 Aug 2025 06:35:35 GMT; path=/
- GET   https://uat-sso.mysmartadvisor.com/Account/Login?ReturnUrl=%2Fconnect%2Fauthorize%2Fcallback%3Fresponse_type%3Dcode%26client_id%3Dmsa-app%26scope%3Dopenid%2520role%2520esop%2520fund%2520stock%2520offline_access%2520ids%2520msa%2520forex%26state%3DXdkNgE-kFJK2jhEqvoWjG1xKXqbHDy5sjaQPvqXKrlc%253D%26redirect_uri%3Dhttp%253A%252F%252F127.0.0.1%252Foauth2%252Flogin%252Fcode%252Fmsa%26nonce%3DyS_F4huLWWmPb1mEGl37mGXOc799HB5YzW_l5qFMibs
    - Page
    - Set-Cookie: .AspNetCore.Antiforgery.p7G02r1aI4k=CfDJ8EfQ81fdiRhHrYpqReyHftAXm_gg6YMHTXV_nvCWh7T8LnVDuDdGPQ0tXTtx3TZKwGAI1mTxNtCqVKlkfrNL9KZyu8joOqDagMD3Givjl1WvRzQHaVAjz4S_23kiK4Hsj5KXdMgrGh4eBeRCSzVFMPY; path=/; samesite=strict; httponly
    - Set-Cookie: .AspNetCore.Mvc.CookieTempDataProvider=; expires=Thu, 01 Jan 1970 00:00:00 GMT; path=/; samesite=lax; httponly
- POST  https://uat-sso.mysmartadvisor.com/Account/Login?returnurl=%2Fconnect%2Fauthorize%2Fcallback%3Fresponse_type%3Dcode%26client_id%3Dmsa-app%26scope%3Dopenid%2520role%2520esop%2520fund%2520stock%2520offline_access%2520ids%2520msa%2520forex%26state%3DXdkNgE-kFJK2jhEqvoWjG1xKXqbHDy5sjaQPvqXKrlc%253D%26redirect_uri%3Dhttp%253A%252F%252F127.0.0.1%252Foauth2%252Flogin%252Fcode%252Fmsa%26nonce%3DyS_F4huLWWmPb1mEGl37mGXOc799HB5YzW_l5qFMibs
    - Set-Cookie: idsrv.session=B2C8F679C52DC096CA6E6AC867C0835E; path=/; secure; samesite=none
    - Set-Cookie: .AspNetCore.Identity.Application=CfDJ8EfQ81fdiRhHrYpqReyHftAewzCASGQopYvWwfoxlHGiKishfL7wgGBTlcn92Bi8XCx4aX_vseHSiL6_wuA0NeUglcNfOjdsr8On3_lPtp8GFDvEi_cCccpf1o4W46ObRqz5v5GDl3AhY6UgWW806NiLcIB4CKfo_V0vBefjJN-uCi7ycF23LKh-HTgZ5cXKZkAh8qmtylN1cfPssz-Q7pqlBNI0QaCP4QqwXkRwUP6OKUU5j6_eZ1KvKzj4tFWF6-EZ4QX9B7oDsPFR1JM7PunlWBNIWei67UnoZR57aaM2iJEG97Rqw30D59ttK0nwZXWFxFQ9GFfueWjSEFWHVc3D22BtnO9LeGGukDNnKn2ooQuPCWKHDNmdF7eLKsEsdjHFcUBzW84U7SkKz53WbkmGmoqJJrbgWPV_5yRtCch6qX91G_2NOxQDl4vQOh0dYSwAq24pXEZonb3TMxTQ_mEKVw2yVUNeuO4JwoFAc0Q4IG-DbkxM69Nc0c-aPRMlEYynRWTJtSNSSp2t2h4luxVUaYUjdQbuHUT17aqh7i0lIZsy1QYSwFpd-rk7SzGq6OV7wQCKUIJMrMaMrzlZ8PaiMFalDF6aZNl343NswkARH4UbAmOGvUGV2_UbM60wvZa9ynq89a6qpUtdTPhipsoKDRPA3wir7fdXPMXMUcGyZYr50qZ9V1RG0sG8C0Tecnj4N17U6u_2TNn-SntxPb-mpG6-nJx6qUuFeOLJDDnYUQGXa8PdWN4Q5U9gtOBuy4Kd8edkRIa01PQPlWMoWt_I-UFZ8ab31p3T7opAqTz5-fjTNrNB7qp6x12VMHKcyXkMYxPqQr-fWclS_AZxLjFWci_pVFR5vj8tsgMuGus3H8SiI_yKS_LzVTkLXf4G2wIkci1rlB3CsmO4vhVUpkWBR7rMVR7RivMZsm1B_M1aitts0MGilth3k4YWmmMP4eZp3USa6BVJgkaKkLpLYQDIE79SsA-XHEcBFdIf8RRfMCo46eyuq_axqTQme7Q3-whOUVeuSDwVz5MvQleJMobZz3WXJee8fIck6SOo2YJ3UfpLZJMOdJkBveel4DiTnlfJA_BcRPteiRUpc-B4mpItLR4_4WMVb46Oc7DwjLzlKBgRKdud22Vfp1GcPm4boT4nCQYe5bcyiO-Jyet4-NfhxjFdtJdb6MsjG4em4m3Jeo03-djVwpbITgZK5MYaUND9ydiXtyp6uwf7tHmiP7r33QQWcmqhNl417FCA0lL5tObshAQSajJw47uGiezyFkj0XAGOgaU7EJ7XXQZLNAm6V6n8VCCeI6-UkzC9SQ6ieGoixGrwV-NyHrH9Vu0jQNipHgweRayDnbfBbP5wZiE8x0Vrbsh5XB1JycuphvxXqnP0AvvSZ81mQv1oxBj0ttbLZTFEqZtNzaIM_ZyzGsY3yi-ApNLXY9OJ1mldGsAy; path=/; secure; samesite=none; httponly
    - 302 https://uat-sso.mysmartadvisor.com/connect/authorize/callback?response_type=code&client_id=msa-app&scope=openid%20role%20esop%20fund%20stock%20offline_access%20ids%20msa%20forex&state=XdkNgE-kFJK2jhEqvoWjG1xKXqbHDy5sjaQPvqXKrlc%3D&redirect_uri=http%3A%2F%2F127.0.0.1%2Foauth2%2Flogin%2Fcode%2Fmsa&nonce=yS_F4huLWWmPb1mEGl37mGXOc799HB5YzW_l5qFMibs
- GET   https://uat-sso.mysmartadvisor.com/connect/authorize/callback?response_type=code&client_id=msa-app&scope=openid%20role%20esop%20fund%20stock%20offline_access%20ids%20msa%20forex&state=XdkNgE-kFJK2jhEqvoWjG1xKXqbHDy5sjaQPvqXKrlc%3D&redirect_uri=http%3A%2F%2F127.0.0.1%2Foauth2%2Flogin%2Fcode%2Fmsa&nonce=yS_F4huLWWmPb1mEGl37mGXOc799HB5YzW_l5qFMibs
    - 302 http://127.0.0.1/oauth2/login/code/msa?code=253CC99E8155FBFC9FFC8F671673E5CABB651B73EB1D461CEB329325CBEBC423&scope=openid%20role%20esop%20fund%20stock%20offline_access%20ids%20msa%20forex&state=XdkNgE-kFJK2jhEqvoWjG1xKXqbHDy5sjaQPvqXKrlc%3D&session_state=2fJd3cXSfJgzMWDQaA_13Vb4CCns9TBTilc0NX6pris.63589A6809D0D86CB048BF98D3127D73
      -GET    http://127.0.0.1/oauth2/login/code/msa?code=253CC99E8155FBFC9FFC8F671673E5CABB651B73EB1D461CEB329325CBEBC423&scope=openid%20role%20esop%20fund%20stock%20offline_access%20ids%20msa%20forex&state=XdkNgE-kFJK2jhEqvoWjG1xKXqbHDy5sjaQPvqXKrlc%3D&session_state=2fJd3cXSfJgzMWDQaA_13Vb4CCns9TBTilc0NX6pris.63589A6809D0D86CB048BF98D3127D73
    - 302  /
    - set-cookie: SESSION=36ca330b-65de-457a-8917-a42cd1cb2604; Path=/; Domain=127.0.0.1; HTTPOnly; SameSite=Lax



org.springframework.boot.autoconfigure.web.reactive.WebSessionIdResolverAutoConfiguration


## Oauth

https://spring.io/guides/tutorials/spring-boot-oauth2

## OIDC
An OIDC client (OpenID Connect client) is a software application or service that interacts with an OpenID Connect (OIDC)
provider to authenticate users and obtain user identity information.
OpenID Connect (OIDC) is an identity layer built on top of the OAuth 2.0 protocol,
providing a standardized way to authenticate users and obtain identity tokens.

### Key Components and Flow of an OIDC Client
1. Authorization Request:
    - The OIDC client initiates the authentication process by redirecting the user to the OpenID provider's authorization endpoint. This request includes parameters such as:
        - client_id: The unique identifier of the client application.
        - redirect_uri: The URI where the authorization server will send the user after authorization.
        - response_type: Typically set to code for the Authorization Code Flow.
        - scope: Specifies the level of access requested (e.g., openid, profile, email).
        - state: A value used to maintain state between the request and callback to prevent CSRF attacks.
2. User Authentication and Consent:
    - The user authenticates with the OpenID provider (if not already authenticated) and may be prompted to consent to the requested scopes.
3. Authorization Response:
    - After successful authentication and consent, the OpenID provider redirects the user back to the OIDC client with an authorization code and the state parameter (if provided).
4. Token Exchange:
    - The OIDC client exchanges the authorization code for tokens (ID token, access token, and optionally a refresh token) by making a request to the OpenID provider's token
5. Token Validation:
    - The OIDC client validates the ID token to ensure its authenticity and integrity. This involves verifying the
      token’s signature and checking claims such as:
        - iss (issuer): The issuer of the token.
        - aud (audience): The audience for which the token is intended (should match the client_id).
        - exp (expiration): The expiration time of the token.
6. User Information:
    - The OIDC client can use the access token to request additional user information from the OpenID provider's userinfo endpoint.

SpringOpaqueTokenIntrospector is used for obtain opaque token


## grant type (flow)

- Web application with server backend: `authorization code flow` 
- Native mobile app: `authorization code flow with PKCE`
- Javascript app with API backend `implicit flow`
- microservices and APIs: `client credentials flow`
