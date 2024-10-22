package org.xyp.sample.spring.common.sslcert;

@FunctionalInterface
public interface SecretReader<R> {
    R read(String path);
}
