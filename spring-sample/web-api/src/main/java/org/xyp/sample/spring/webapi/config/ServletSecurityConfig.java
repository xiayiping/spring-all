package org.xyp.sample.spring.webapi.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;

import static org.springframework.security.config.Customizer.withDefaults;

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

    //
    public ServletSecurityConfig() {
        log.info("using servlet SecurityConfig ......");
    }

    //
//    @Bean
    public SecurityFilterChain createSecurityFilterChain(
        HttpSecurity http
    ) throws Exception {
        log.info("creating webflux springSecurityFilterChain ......");
        http.authorizeHttpRequests(
            cus -> cus.requestMatchers("/hello").authenticated()
                .requestMatchers("/*").permitAll()
        )
//            .oauth2Login(Customizer.withDefaults())
//            .formLogin(Customizer.withDefaults())
        ;
//        http.addFilterBefore(new SomeServletFilter(), AuthorizationFilter.class);
//        http.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests(
            c -> c.requestMatchers("/favicon.ico").permitAll()
        );
        return http.build();
    }


    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        log.info("creating servlet springSecurityFilterChain ......");
        http.csrf(AbstractHttpConfigurer::disable);
        http
            .authorizeHttpRequests((authorize) -> authorize
                .anyRequest().permitAll()
            )
//            .oauth2ResourceServer(
//                config -> config.jwt(Customizer.withDefaults())
//            )
            .oauth2ResourceServer(configurer -> configurer.opaqueToken(opaqueToken -> opaqueToken
                        .introspectionUri(this.introspectionUri)
                        .introspectionClientCredentials(this.clientId, this.clientSecret)
                )
            )
        ;

        return http.build();
    }
//
//
}
