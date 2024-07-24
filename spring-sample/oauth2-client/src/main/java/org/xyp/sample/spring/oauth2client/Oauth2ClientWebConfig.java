package org.xyp.sample.spring.oauth2client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
import org.springframework.boot.autoconfigure.security.reactive.StaticResourceRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher.MatchResult;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class Oauth2ClientWebConfig {

    final StaticResourceRequest.StaticResourceServerWebExchange staticMatcher = PathRequest.toStaticResources().atCommonLocations();

    @Bean(name = "iconFilter")
    @Order(1)
    SecurityWebFilterChain springSecurityFilterChain(
        ServerHttpSecurity http
    ) {
        log.info("use customized ico filter chain ......");
        http
            .securityMatcher(staticMatcher)
            .authorizeExchange(exchange -> exchange
                .anyExchange().permitAll()
            );
        return http.build();
    }

    @Bean(name = "allFilter")
    @Order(2)
    SecurityWebFilterChain springSecurityFilterChainAll(
        ServerHttpSecurity http,
        ReactiveClientRegistrationRepository clientRegistrationRepository
    ) {
        log.info("use customized filter chain 4 ......");
        http.csrf(ServerHttpSecurity.CsrfSpec::disable);
        http
            .securityMatcher(ex -> staticMatcher
                .matches(ex)
                .filter(r -> !r.isMatch())
                .flatMap(ignored -> MatchResult.match())
                .switchIfEmpty(MatchResult.notMatch())
            )
            .authorizeExchange((exchange) -> exchange
                .pathMatchers("logout").permitAll()
                .anyExchange().authenticated()
            )
            .exceptionHandling(cus -> cus.authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.FORBIDDEN)))
        ;
        http.oauth2Login(withDefaults());
        http.oauth2Client(withDefaults());

        http.logout((logout) -> logout
            .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
        );
        return http.build();
    }

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
