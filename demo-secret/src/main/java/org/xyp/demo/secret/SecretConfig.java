package org.xyp.demo.secret;

import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(VaultSecretProperty.class)
public class SecretConfig {


    //    String vaultPemPath = "d:/tools/vault/1.14/ca.pem";
    String vaultPemPath = "d:/tools/vault/1.14/tcghl-com-crt.pem";

    //    String vaultRoot = "https://127.0.0.1:8180";
    String vaultRoot = "https://vault.tcghl.com";

    //    String vaultToken = "hvs.CAESIH66nAoa6gU05CN1CIpKIpaP3pkNYM2gbMEjmo7szQ4WGh4KHGh2cy4wQlB3Z25tMnFFS2NodjhPZzhpak9XSkQ";
    String vaultToken = "hvs.CAESIOuIuBjV-viraSq1zb6A7F5Aeg4icLbz9HyfEXTZMlaXGh4KHGh2cy5EWFNadDVOM1VtbEJkUWhIb1FpZjQ3QU4";

    //    String secretPath = vaultRoot + "/v1/kv_xyp/data/dev";
    String secretPath = vaultRoot + "/v1/secret/data/dev/paradise/keystore";

    //    String passwordField = "keystore_key";
    String passwordField = "password";

    //    String keyStoreField = "private_keystoe";
    String keyStoreField = "key_store";

    String trustStoreField = "trust_store";

}
