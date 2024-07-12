package org.xyp.sample.spring.secret.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Data
@ConfigurationProperties(prefix = "org.xyp.secret.keystore")
public class KeyStoreSecretProperty {

    private final List<KeyStoreEntry> entries = new ArrayList<>();

    @NoArgsConstructor
    @Data
    public static class KeyStoreEntry {
        String type;
        String url;
        String caPath;
        String region;
        private final List<SecretPath> paths = new ArrayList<>();

    }

    @NoArgsConstructor
    @Data
    public static class SecretPath {
        String path;
        private final Map<String, Bundle> bundles = new HashMap<>();
    }

    @NoArgsConstructor
    @Data
    public static class Bundle {
        String trustStorePasswordField;
        String trustStoreField;
        String keyStorePasswordField;
        String keyStoreField;
    }
}
