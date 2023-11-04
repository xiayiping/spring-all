package org.xyp.spring.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.server.Ssl;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "all-configs")
public class AllConfigProperties {
    private Map<String, String> serverPort = new HashMap<>();
    private Map<String, String> appName = new HashMap<>();
    private Map<String, ServerSsl> serverSsl = new HashMap<>();

    @Data
    @NoArgsConstructor
    public static class ServerSsl {
        private boolean enabled;
        private String bundle;
        private Ssl.ClientAuth clientAuth;
    }
}
