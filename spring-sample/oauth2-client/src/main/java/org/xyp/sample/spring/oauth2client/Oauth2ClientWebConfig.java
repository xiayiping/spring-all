package org.xyp.sample.spring.oauth2client;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.springframework.web.server.session.WebSessionManager;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult;

@Slf4j
@Configuration
//@EnableWebFluxSecurity
//@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class Oauth2ClientWebConfig {

//    @Bean
//    @Order(1)
//    SecurityWebFilterChain springSecurityFilterChain(
//        ServerHttpSecurity http,
//        ReactiveClientRegistrationRepository clientRegistrationRepository
//    ) {
//        log.info("use customized ico filter chain ......");
//        http
//            .securityMatcher(
//                PathRequest.toStaticResources().atCommonLocations()
//            )
//
//            .authorizeExchange((exchange) -> exchange
//                .anyExchange().permitAll()
//            );
//        return http.build();
//    }
//
//    @Bean
//    @Order(2)
//    SecurityWebFilterChain springSecurityFilterChainAll(
//        ServerHttpSecurity http,
//        ReactiveClientRegistrationRepository clientRegistrationRepository
//    ) {
//        log.info("use customized filter chain ......");
//        http
////            .securityMatcher(PathRequest.toStaticResources().atCommonLocations().matches())
//            .authorizeExchange((exchange) -> exchange
//                .anyExchange().authenticated()
//            );
//        http.oauth2Login(withDefaults());
//        http.oauth2Client(withDefaults());
//
//        http.logout((logout) -> logout
//            .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
//        );
//        return http.build();
//    }

    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(
        ReactiveClientRegistrationRepository clientRegistrationRepository
    ) {
        OidcClientInitiatedServerLogoutSuccessHandler oidcLogoutSuccessHandler =
            new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);

        // Sets the location that the End-User's User Agent will be redirected to
        // after the logout has been performed at the Provider
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");

        return oidcLogoutSuccessHandler;
    }
}
