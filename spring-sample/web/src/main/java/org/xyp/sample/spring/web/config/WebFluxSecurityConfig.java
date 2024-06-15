//package org.xyp.sample.spring.web.config;
//
//import lombok.extern.slf4j.Slf4j;
//import org.apache.catalina.filters.RequestFilter;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
//import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
//import org.springframework.security.config.web.server.ServerHttpSecurity;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.server.SecurityWebFilterChain;
//import org.xyp.sample.spring.web.filter.SomeFilter;
//
//@Slf4j
//@Configuration
//@ConditionalOnMissingClass(value = "org.apache.catalina.filters.RequestFilter")
//@EnableWebSecurity // this is for traditional web app
////@EnableWebFluxSecurity // this is for webflux
//public class WebFluxSecurityConfig {
//
//    public WebFluxSecurityConfig() {
//        log.info("using webflux SecurityConfig ......");
//    }
////
////    @Bean
////    public MapReactiveUserDetailsService userDetailsService() {
////        UserDetails user = User.withDefaultPasswordEncoder()
////            .username("user")
////            .password("user")
////            .roles("USER")
////            .build();
////        return new MapReactiveUserDetailsService(user);
////    }
////
//
//    @Bean
//    public SecurityWebFilterChain fChain(ServerHttpSecurity http) {
//        ;
//        log.info("creating webflux springSecurityFilterChain ......");
//        http
//            .authorizeExchange(exchanges -> exchanges
//                .pathMatchers("/hello").authenticated()
//                .pathMatchers("/*").permitAll()
//            )
////            .oauth2Login(Customizer.withDefaults())
////            .formLogin(Customizer.withDefaults())
//        ;
//        http.addFilterBefore(new SomeFilter(), SecurityWebFiltersOrder.AUTHORIZATION);
//        return http.build();
//    }
//}
