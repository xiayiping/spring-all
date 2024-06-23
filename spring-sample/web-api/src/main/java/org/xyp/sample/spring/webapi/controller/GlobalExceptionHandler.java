package org.xyp.sample.spring.webapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    // Handle global exceptions
//    @ExceptionHandler(Exception.class)

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGlobalException(
        Exception ex,
        HttpServletRequest request
    ) {
        log.error(ex.getMessage(), ex);
        log.info("{}", request);
        val pd = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(pd, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
