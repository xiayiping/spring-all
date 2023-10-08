package org.xyp.demo.secret;


import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;


@NoArgsConstructor
@Data
@ConfigurationProperties(prefix = "secret.keystore.vault")
public class VaultSecretProperty {

    private String url;
    private String caPath;
    private String secretPath;
    private final Map<String, VaultSecurityEntry> entries = new HashMap<>();

    @NoArgsConstructor
    @Data
    public static class VaultSecurityEntry {


        String passwordField;
        String storeField;
    }

}
