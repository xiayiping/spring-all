package org.xyp.shared.secret.loader.cert.config;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@Data
@ConfigurationProperties(prefix = "org.xyp.shared.secret.keystore")
public class KeyStoreSecretProperty {

    private boolean enabled = true;

    private final List<KeyStoreEntry> entries = new ArrayList<>();

    @NoArgsConstructor
    @Data
    public static class KeyStoreEntry {
        KeyStoreFetchSource fetchSource;

        /**
         * AWS region
         */
        String region;
        private final List<SecretPath> paths = new ArrayList<>();

    }

    @NoArgsConstructor
    @Data
    public static class SecretPath {
        /**
         * vault engine
         */
        String engine;
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
