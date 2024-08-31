package org.xyp.sample.spring.webvaadin.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.xyp.sample.spring.webvaadin.domain.HttpResp;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResp<Void>> handleGlobalException(
        Exception ex,
        HttpServletRequest request
    ) {
        log.error(ex.getMessage(), ex);
        log.info("{}", request);
        val pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        pd.setTitle("Internal Server Error");
        return new ResponseEntity<>(HttpResp.ofError(null, pd), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
