package org.xyp.sample.spring.webapijwt.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.xyp.sample.spring.common.exception.UnAuthenticatedException;
import org.xyp.sample.spring.common.jwt.JwtConverter;

import java.io.IOException;

@Slf4j
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    final JwtConverter jwtConverter;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request, HttpServletResponse response, FilterChain filterChain
    ) throws ServletException, IOException {
//        val authHeader = request.getHeader("Authorization");
//        if (!StringUtils.hasText(authHeader)) {
//            throw new UnAuthenticatedException();
//        }
//
//        val token = authHeader.replace("Bearer ", "");
//        log.info("jwtToken: {}", token);
//        val auth = jwtConverter.convertJwtToken(token);
//        SecurityContextHolder.getContext().setAuthentication(auth);
        filterChain.doFilter(request, response);
    }
}
