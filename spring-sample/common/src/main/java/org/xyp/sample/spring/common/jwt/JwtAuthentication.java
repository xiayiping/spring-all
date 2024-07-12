package org.xyp.sample.spring.common.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

@AllArgsConstructor
public class JwtAuthentication implements Authentication {

    @Getter
    private final String token;

    @Getter
    private final long subject;

    private final String name;

    private final List<SimpleGrantedAuthority> authorities;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public JwtAuthentication getPrincipal() {
        return this;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "JwtAuthentication{" +
            "subject=" + subject +
            ", name='" + name + '\'' +
            ", authorities=" + authorities +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JwtAuthentication that = (JwtAuthentication) o;
        return subject == that.subject && Objects.equals(token, that.token) && Objects.equals(name, that.name) && Objects.equals(authorities, that.authorities);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, subject, name, authorities);
    }
}
