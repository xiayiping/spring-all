package org.xyp.sample.spring.webapi.service;

import lombok.val;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class GreetService {

    @PreAuthorize("hasAnyAuthority('SCOPE_user.read')")
    public Map<String, Object> greet() {
        val jwt = SecurityContextHolder.getContext()
            .getAuthentication().getPrincipal();
        return Map.of("message", "hello, " + jwt);
    }
}
