package org.xyp.sample.spring.webvaadin.domain;


import lombok.Builder;
import org.springframework.http.ProblemDetail;

@Builder
public record HttpResp<T>(
    T data,
    ProblemDetail problem
) {
    public static <R> HttpResp<R> ofData(R data) {
        return new HttpResp<>(data, null);
    }

    public static <R> HttpResp<R> ofError(R data, ProblemDetail problem) {
        return new HttpResp<>(data, problem);
    }
}
