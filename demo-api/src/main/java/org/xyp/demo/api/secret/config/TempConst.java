package org.xyp.demo.api.secret.config;

public class TempConst {

    public static String vaultPemPath = "d:/tools/vault/1.14/tcghl-com-crt.pem";

    //    String vaultRoot = "https://127.0.0.1:8180";
    public static String vaultRoot = "https://vault.tcghl.com";

    //    String vaultToken = "hvs.CAESIH66nAoa6gU05CN1CIpKIpaP3pkNYM2gbMEjmo7szQ4WGh4KHGh2cy4wQlB3Z25tMnFFS2NodjhPZzhpak9XSkQ";
    public static String vaultToken = "hvs.CAESIOuIuBjV-viraSq1zb6A7F5Aeg4icLbz9HyfEXTZMlaXGh4KHGh2cy5EWFNadDVOM1VtbEJkUWhIb1FpZjQ3QU4";

    //    String secretPath = vaultRoot + "/v1/kv_xyp/data/dev";
    public static String secretPath = vaultRoot + "/v1/secret/data/dev/paradise/keystore";

    //    String passwordField = "keystore_key";
    public static String passwordField = "password";

    //    String keyStoreField = "private_keystoe";
    public static String keyStoreField = "key_store";

    public static String trustStoreField = "trust_store";

}
