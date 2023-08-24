package org.xyp.demo.call.auth;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.kerberos.authentication.KerberosAuthenticationProvider;
import org.springframework.security.kerberos.authentication.KerberosServiceAuthenticationProvider;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosClient;
import org.springframework.security.kerberos.authentication.sun.SunJaasKerberosTicketValidator;
import org.springframework.security.kerberos.web.authentication.SpnegoAuthenticationProcessingFilter;
import org.springframework.security.kerberos.web.authentication.SpnegoEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

@EnableWebSecurity
@Configuration
@NoArgsConstructor
@Slf4j
public class SecurityConfig {

    @Value("${app.service-principal}")
    private String servicePrincipal;

    @Value("${app.keytab-location}")
    private String keytabLocation;

    private AuthenticationFilter authenticationFilter;

    SecurityConfig(AuthenticationFilter authenticationFilter) {
        this.authenticationFilter = authenticationFilter;
    }

    @Bean
    public SecurityFilterChain getSecurityFilterChain(HttpSecurity http) throws Exception {


//        KerberosAuthenticationProvider kerberosAuthenticationProvider =
//        kerberosAuthenticationProvider();
//        KerberosServiceAuthenticationProvider kerberosServiceAuthenticationProvider =
//        kerberosServiceAuthenticationProvider();
//        ProviderManager providerManager = new ProviderManager(kerberosAuthenticationProvider,
//            kerberosServiceAuthenticationProvider);

        /////////////////////////////////////////////////////////
//        http.csrf(AbstractHttpConfigurer::disable);
//        http.addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter.class);
        /*http
            .authorizeHttpRequests((authz) -> authz
                .requestMatchers("/", "/home").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling()
            .authenticationEntryPoint(spnegoEntryPoint())
            .and()
            .formLogin()
            .loginPage("/login").permitAll()
            .and()
            .logout()
            .permitAll()
            .and()
            .authenticationProvider(kerberosAuthenticationProvider())
            .authenticationProvider(kerberosServiceAuthenticationProvider())
            .addFilterBefore(spnegoAuthenticationProcessingFilter(providerManager),
                BasicAuthenticationFilter.class);
        return http.build();*/

        return http.build();
    }

    @Bean
    public KerberosAuthenticationProvider kerberosAuthenticationProvider() {
        KerberosAuthenticationProvider provider = new KerberosAuthenticationProvider();
        SunJaasKerberosClient client = new SunJaasKerberosClient();
        client.setDebug(true);
        provider.setKerberosClient(client);
        provider.setUserDetailsService(dummyUserDetailsService());
        return provider;
    }

    @Bean
    public SpnegoEntryPoint spnegoEntryPoint() {
        return new SpnegoEntryPoint("/login");
    }

    public SpnegoAuthenticationProcessingFilter spnegoAuthenticationProcessingFilter(AuthenticationManager authenticationManager) {
        log.info("-------- {} ", authenticationManager.getClass());
        SpnegoAuthenticationProcessingFilter filter = new SpnegoAuthenticationProcessingFilter();
        filter.setAuthenticationManager(authenticationManager);
        return filter;
    }

//    @Bean
//    public KerberosServiceAuthenticationProvider kerberosServiceAuthenticationProvider() {
//        KerberosServiceAuthenticationProvider provider = new
//        KerberosServiceAuthenticationProvider();
//        provider.setTicketValidator(sunJaasKerberosTicketValidator());
//        provider.setUserDetailsService(dummyUserDetailsService());
//        return provider;
//    }

//    @Bean
//    public SunJaasKerberosTicketValidator sunJaasKerberosTicketValidator() {
//        SunJaasKerberosTicketValidator ticketValidator = new SunJaasKerberosTicketValidator();
////        ticketValidator.setServicePrincipal(servicePrincipal);
////        ticketValidator.setKeyTabLocation(new FileSystemResource(keytabLocation));
////        ticketValidator.setDebug(true);
//        return ticketValidator;
//    }

    @Bean
    public DummyUserDetailsService dummyUserDetailsService() {
        return new DummyUserDetailsService();
    }
}
