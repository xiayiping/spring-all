package org.xyp.sample.spring.webapi.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@ConditionalOnClass(name = "jakata.servlet.Servlet")
public class SomeServletFilter extends OncePerRequestFilter {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {
        log.info("Some Servlet Filter ... ...");
        try {
            filterChain.doFilter(request, response);
        } catch (InvalidBearerTokenException invalidBearerTokenException) {
            log.error("invalid token {}", invalidBearerTokenException.getMessage());

            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);

            val pd = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
            pd.setTitle("unauthenticated");
            pd.setDetail("unauthenticated");
            try (val writer = response.getWriter()) {
                writer.write(mapper.writeValueAsString(pd));
                writer.flush();
            }
        } catch (Exception exception) {
            log.error(exception.getMessage());
        }
    }
}
