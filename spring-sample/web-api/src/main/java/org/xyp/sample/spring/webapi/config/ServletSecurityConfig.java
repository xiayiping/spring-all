package org.xyp.sample.spring.webapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.autoconfigure.security.servlet.StaticResourceRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;

//import org.xyp.sample.spring.web.filter.SomeFilter;
//import org.xyp.sample.spring.web.filter.SomeServletFilter;
//
@Slf4j
@Configuration
//@ConditionalOnClass(name="org.apache.catalina.filters.RequestFilter")
@EnableWebSecurity // this is for traditional web app
//@EnableWebFluxSecurity // this is for webflux
public class ServletSecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.introspection-uri}")
    String introspectionUri;

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-id}")
    String clientId;

    @Value("${spring.security.oauth2.resourceserver.opaquetoken.client-secret}")
    String clientSecret;

    final StaticResourceRequest.StaticResourceRequestMatcher staticMatcher = PathRequest.toStaticResources().atCommonLocations();

    //
    public ServletSecurityConfig() {
        log.info("using servlet SecurityConfig ......");
    }

    @Bean(name = "iconFilter")
    @Order(1)
    SecurityFilterChain springSecurityFilterChain(
        HttpSecurity http
    ) throws Exception {
        log.info("use customized ico filter chain 2 ......");
        http
            .securityMatcher(staticMatcher)
            .authorizeHttpRequests(exchange -> exchange
                .anyRequest().permitAll()
            );
        return http.build();
    }

    @Bean(name = "allFilter")
    SecurityFilterChain apiSecurity(
        HttpSecurity http,
        Oauth2AuthenticationFailureHandler authenticationFailureHandler
    ) throws Exception {
        log.info("creating servlet springSecurityFilterChain ......");
//        http.csrf(AbstractHttpConfigurer::disable);
        http.csrf(csrfSpec -> csrfSpec
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
//            .csrfTokenRequestHandler(new XorServerCsrfTokenRequestAttributeHandler())
        );
        http
            .securityMatcher(d -> !staticMatcher.matches(d))
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()
            )
//            .oauth2ResourceServer(
//                config -> config.jwt(Customizer.withDefaults())
//            )
//            .oauth2ResourceServer(configurer -> configurer.opaqueToken(opaqueToken -> opaqueToken
//                        .introspectionUri(this.introspectionUri)
//                        .introspectionClientCredentials(this.clientId, this.clientSecret)
//                    )
//                    .addObjectPostProcessor(new ObjectPostProcessor<BearerTokenAuthenticationFilter>() {
//                        @Override
//                        public <O extends BearerTokenAuthenticationFilter> O postProcess(O object) {
//                            object.setAuthenticationFailureHandler(authenticationFailureHandler);
//                            return object;
//                        }
//                    })
//            )
        ;

//        bearerTokenAuthenticationFilter.setAuthenticationFailureHandler(authenticationFailureHandler);

//        http.oauth2Login(login -> login.failureHandler(authenticationFailureHandler));
        return http.build();
    }
}
