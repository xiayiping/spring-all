package org.xyp.demo.jwt.config;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.xyp.demo.jwt.dao.UserRepository;

@Configuration
@RequiredArgsConstructor
public class JwtAppConfig {

    private final UserRepository userRepository;

    @Bean
    public UserDetailsService createUserDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username)
                    throws UsernameNotFoundException {
                return userRepository.findByEmail(username).orElseThrow();
            }
        };
    }

    @Bean
    public AuthenticationProvider createAuthenticationProvider() {

        val authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(createUserDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager createAuthenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
