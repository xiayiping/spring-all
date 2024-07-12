package org.xyp.sample.spring.secret;

import java.io.IOException;

@FunctionalInterface
public interface  SecretReader<R> {
    R read(String path) throws IOException, InterruptedException;
}
