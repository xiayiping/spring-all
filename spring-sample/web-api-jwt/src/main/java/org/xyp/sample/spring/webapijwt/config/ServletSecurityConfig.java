package org.xyp.sample.spring.webapijwt.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.xyp.sample.spring.common.jwt.JwtConverter;
import org.xyp.sample.spring.webapijwt.filter.JwtFilter;

@Slf4j
@Configuration
@EnableWebSecurity // this is for traditional web app
public class ServletSecurityConfig {

    public ServletSecurityConfig() {
        log.info("using servlet SecurityConfig ......");
    }

    @Bean
    SecurityFilterChain apiSecurity(HttpSecurity http) throws Exception {
        log.info("creating servlet springSecurityFilterChain ......");
//        http.csrf(AbstractHttpConfigurer::disable);
        http.csrf(csrfSpec -> csrfSpec.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
            ;
        http.sessionManagement(sessionCustomize -> sessionCustomize.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http
            .authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()
            )
        ;
        http.addFilterBefore(new JwtFilter(new JwtConverter(null)), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
