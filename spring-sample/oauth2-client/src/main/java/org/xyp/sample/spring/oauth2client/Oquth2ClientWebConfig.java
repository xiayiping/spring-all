package org.xyp.sample.spring.oauth2client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.ResponseCookie;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import org.springframework.web.server.session.WebSessionManager;
import reactor.core.publisher.Mono;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.springframework.security.config.Customizer.withDefaults;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class Oquth2ClientWebConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(
        ServerHttpSecurity http,
        ReactiveClientRegistrationRepository clientRegistrationRepository
    ) {
        log.info("use customized filter chain ......");
        http.authorizeExchange((exchange) -> exchange
            .pathMatchers("/favicon.ico").permitAll()
            .anyExchange().authenticated()
        );
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

    static final String SESSION_NAME = "tsSESSION";

    //        @Bean(name = WebHttpHandlerBuilder.WEB_SESSION_MANAGER_BEAN_NAME)
    WebSessionManager timeStampSessionManager() {
        log.info("create Timestamp WebSessionManager");
        return new WebSessionManager() {
            @Override
            public Mono<WebSession> getSession(ServerWebExchange exchange) {
                log.info("get timeStamp session");
                return Mono.just(exchange.getRequest().getCookies())
                    .map(cookies -> Optional.ofNullable(cookies.get(SESSION_NAME)).orElseGet(List::of))
                    .flatMapIterable(l -> l)
                    .map(cookie -> cookie.getValue())
                    .next()
                    .map(ignored -> new TimeStampSession(ignored))
                    .switchIfEmpty(Mono.fromSupplier(() -> new TimeStampSession(Instant.now())))
                    .cast(WebSession.class)
                    .doOnNext(session -> exchange.getResponse().beforeCommit(() ->
                        Mono.fromRunnable(() ->
                            exchange.getResponse().getCookies().set(
                                SESSION_NAME,
                                ResponseCookie.from(SESSION_NAME, session.getId()).build()
                            )
                        )
                    ));
            }
        };

    }


    static class TimeStampSession implements WebSession {

        Clock clock = Clock.system(ZoneId.of("GMT"));

        private final AtomicReference<String> id = new AtomicReference<>(String.valueOf("" + System.currentTimeMillis()));

        private final Map<String, Object> attributes = new ConcurrentHashMap<>();

        private final Instant creationTime;

        private volatile Instant lastAccessTime;

        private volatile Duration maxIdleTime = Duration.ofMinutes(30);

        private final AtomicReference<State> state = new AtomicReference<>(State.NEW);

        public TimeStampSession(Instant creationTime) {
            this.creationTime = creationTime;
            this.lastAccessTime = this.creationTime;
            this.id.set(creationTime.toEpochMilli() + "");
        }

        public TimeStampSession(String id) {
            this.creationTime = Instant.now();
            this.id.set(id);
        }

        @Override
        public String getId() {
            return this.id.get();
        }

        @Override
        public Map<String, Object> getAttributes() {
            return this.attributes;
        }

        @Override
        public Instant getCreationTime() {
            return this.creationTime;
        }

        @Override
        public Instant getLastAccessTime() {
            return this.lastAccessTime;
        }

        @Override
        public void setMaxIdleTime(Duration maxIdleTime) {
            this.maxIdleTime = maxIdleTime;
        }

        @Override
        public Duration getMaxIdleTime() {
            return this.maxIdleTime;
        }

        @Override
        public void start() {
            this.state.compareAndSet(State.NEW, State.STARTED);
        }

        @Override
        public boolean isStarted() {
            return this.state.get().equals(State.STARTED) || !getAttributes().isEmpty();
        }

        @Override
        public Mono<Void> changeSessionId() {
            return Mono.empty();
//            return Mono.<Void>defer(() -> {
//                    String currentId = this.id.get();
//                    this.sessions.remove(currentId);
//                    String newId = String.valueOf(idGenerator.generateId());
//                    this.id.set(newId);
//                    this.sessions.put(this.getId(), this);
//                    return Mono.empty();
//                })
//                .subscribeOn(Schedulers.boundedElastic())
//                .publishOn(Schedulers.parallel())
//                .then();
        }

        @Override
        public Mono<Void> invalidate() {
            this.state.set(State.EXPIRED);
            getAttributes().clear();
            return Mono.empty();
        }

        @Override
        public Mono<Void> save() {

            checkMaxSessionsLimit();

            // Implicitly started session..
            if (!getAttributes().isEmpty()) {
                this.state.compareAndSet(State.NEW, State.STARTED);
            }

            if (isStarted()) {
                // Save
//                this.sessions.put(this.getId(), this);

                // Unless it was invalidated
                if (this.state.get().equals(State.EXPIRED)) {
//                    this.sessions.remove(this.getId());
                    return Mono.error(new IllegalStateException("Session was invalidated"));
                }
            }

            return Mono.empty();
        }

        private void checkMaxSessionsLimit() {

        }

        @Override
        public boolean isExpired() {
            return isExpired(clock.instant());
        }

        private boolean isExpired(Instant now) {
            if (this.state.get().equals(State.EXPIRED)) {
                return true;
            }
            if (checkExpired(now)) {
                this.state.set(State.EXPIRED);
                return true;
            }
            return false;
        }

        private boolean checkExpired(Instant currentTime) {
            return isStarted() && !this.maxIdleTime.isNegative() &&
                currentTime.minus(this.maxIdleTime).isAfter(this.lastAccessTime);
        }

        private void updateLastAccessTime(Instant currentTime) {
            this.lastAccessTime = currentTime;
        }

    }

    private enum State {NEW, STARTED, EXPIRED}

}
