package org.xyp.sample.spring.oauth2client;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.security.reactive.PathRequest;
import org.springframework.boot.autoconfigure.security.reactive.StaticResourceRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher;

import java.net.URI;

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
        log.info("use customized ico filter chain 2 ......");
        http
            .securityMatcher(staticMatcher)
            .authorizeExchange(exchange -> exchange
                .anyExchange().permitAll()
            );
        return http.build();
    }

    boolean use403ThanLoginPage = false;

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
        ;

        if (use403ThanLoginPage) {
            http.exceptionHandling(cus -> cus.authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.FORBIDDEN)));
        }
        http.oauth2Login(login -> login.authenticationMatcher(new PathPatternParserServerWebExchangeMatcher("/oauth2/login/code/{registrationId}")));
        http.oauth2Client(withDefaults());

//        http.addFilterAfter((exchange, chain) -> {
//            System.out.println("ssss");
//            return exchange.getSession()
//                .map(s -> {
//                    System.out.println(s.getAttributes());
//                    return s;
//                })
//                .flatMap(s -> {
//
//                    if (!s.isExpired()) {
//                        System.out.println("valid");
//                        System.out.println("valid");
//                        System.out.println("valid");
//                        System.out.println("valid");
//                        return chain.filter(exchange);
//                    } else {
//                        System.out.println("expired ");
//                        System.out.println("expired ");
//                        System.out.println("expired ");
//                        System.out.println("expired ");
//
//                        val resp = exchange.getResponse();
//                        resp.setStatusCode(HttpStatus.FOUND);
//                        resp.getHeaders().setLocation(URI.create("/ttt/oauth2/authorization/{registrationId}"));
//                        return resp.setComplete();
//                    }
//                });
//        }, SecurityWebFiltersOrder.FORM_LOGIN);

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
//        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/oauth2/authorization/{registrationId}");

        return oidcLogoutSuccessHandler;
    }
}
