package org.xyp.demo.oauth2.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    //
//    @Bean
//    @Order(1)
//    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
//        throws Exception {
//
////        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);
////
////        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
////            .oidc(Customizer.withDefaults());	// Enable OpenID Connect 1.0
//
//        http
//            // Redirect to the login page when not authenticated from the
//            // authorization endpoint
//            .exceptionHandling((exceptions) -> exceptions
//                .defaultAuthenticationEntryPointFor(
//                    new LoginUrlAuthenticationEntryPoint("/login"),
//                    new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
//                )
//            )
//            // Accept access tokens for User Info and/or Client Registration
//            .oauth2ResourceServer((resourceServer) -> resourceServer
//                .jwt(Customizer.withDefaults()));
//
//        return http.build();
//    }
//
    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
        throws Exception {
        http
            .authorizeHttpRequests((authorize) -> authorize
                .anyRequest().authenticated()
            )
            // Form login handles the redirect to the login page from the
            // authorization server filter chain
            .oauth2Login(Customizer.withDefaults())
            .formLogin(Customizer.withDefaults())
        /**/
        ;
        return http.build();
    }
//
//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails userDetails = User.withDefaultPasswordEncoder()
//            .username("user")
//            .password("password")
//            .roles("USER")
//            .build();
//
//        return new InMemoryUserDetailsManager(userDetails);
//    }
//
////    @Bean
////    public RegisteredClientRepository registeredClientRepository() {
////        RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
////            .clientId("oidc-client")
////            .clientSecret("{noop}secret")
////            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
////            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
////            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
////            .redirectUri("http://127.0.0.1:8080/login/oauth2/code/oidc-client")
////            .postLogoutRedirectUri("http://127.0.0.1:8080/")
////            .scope(OidcScopes.OPENID)
////            .scope(OidcScopes.PROFILE)
////            .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
////            .build();
////
////        return new InMemoryRegisteredClientRepository(oidcClient);
////    }
//
//    @Bean
//    public JWKSource<SecurityContext> jwkSource() {
//        KeyPair keyPair = generateRsaKey();
//        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
//        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
//        RSAKey rsaKey = new RSAKey.Builder(publicKey)
//            .privateKey(privateKey)
//            .keyID(UUID.randomUUID().toString())
//            .build();
//        JWKSet jwkSet = new JWKSet(rsaKey);
//        return new ImmutableJWKSet<>(jwkSet);
//    }
//
//    private static KeyPair generateRsaKey() {
//        KeyPair keyPair;
//        try {
//            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//            keyPairGenerator.initialize(2048);
//            keyPair = keyPairGenerator.generateKeyPair();
//        }
//        catch (Exception ex) {
//            throw new IllegalStateException(ex);
//        }
//        return keyPair;
//    }
//
////    @Bean
////    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
////        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
////    }
////
////    @Bean
////    public AuthorizationServerSettings authorizationServerSettings() {
////        return AuthorizationServerSettings.builder().build();
////    }
}
