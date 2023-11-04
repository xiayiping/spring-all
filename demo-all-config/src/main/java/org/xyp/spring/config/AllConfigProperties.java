package org.xyp.spring.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "all-configs")
public class AllConfigProperties {
    private Map<String, String> serverPort = new HashMap<>();
    private Map<String, String> appName = new HashMap<>();
}
