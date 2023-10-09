package org.xyp.demo.api.secret.config;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import software.amazon.awssdk.services.lightsail.model.Bundle;

import java.util.HashMap;
import java.util.Map;


@NoArgsConstructor
@Data
@ConfigurationProperties(prefix = "secret.keystore")
public class VaultKeyStoreSecretProperty {

    private final Map<String, VaultSecurityEntry> entries = new HashMap<>();

    //
    @NoArgsConstructor
    @Data
    public static class VaultSecurityEntry {
        String type;
        String url;
        String caPath;
        String secretPath;
        String region;
        private final Map<String, Bundle> bundles = new HashMap<>();

    }

    @NoArgsConstructor
    @Data
    public static class Secret {
        String path;
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
