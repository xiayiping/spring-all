package org.xyp.demo.api.secret.config.vault;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.util.StringUtils;
import org.xyp.demo.api.secret.config.KeyStoreLoader;
import org.xyp.demo.api.secret.config.VaultClient;
import org.xyp.demo.api.secret.config.VaultKeyStoreSecretProperty;

import java.io.IOException;
import java.net.http.HttpClient;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class VaultKeyStoreLoader implements KeyStoreLoader {
    private static final String SupportedType = "vault";

    private final VaultKeyStoreSecretProperty property;

    private Map<String, StoreBundle> storeBundleMap = new HashMap<>();

    @PostConstruct
    public void loadData() {
        String vaultToken = System.getenv("vault_token");
        if (!StringUtils.hasText(vaultToken)) {
            log.warn("${vault_token} not provided when starting up, so skip load key store from vault");
            return;
        }
        log.info("start loading key store data from vault");

        val vaultEntries = property.getEntries().entrySet().stream()
                .filter(e -> SupportedType.equals(e.getValue().getType()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        val vaultClient = vaultEntries.entrySet().stream()
                .map(e -> {
                    try {
                        return new Tuple<>(e.getKey(), new VaultClient(
                                e.getValue().getUrl(),
                                vaultToken,
                                e.getValue().getCaPath(),
                                e.getValue().getSecretPath()));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                })
                .collect(Collectors.toMap(e -> e.v1, e -> e.v2));

        val bundled = vaultEntries.entrySet().stream()
                .map(e-> {
                    val client = vaultClient.get(e.getKey());
                    val bundleCfgs = e.getValue().getBundles();
                    try {
                        return readBundle(client, bundleCfgs);
                    } catch (IOException | InterruptedException ex) {
                        throw new RuntimeException(ex);
                    }
                }).toList();

        for(Map<String, StoreBundle > map : bundled) {
            this.storeBundleMap.putAll(map);
        }

        System.out.println(this.storeBundleMap);

    }

    private Map<String, StoreBundle> readBundle(
            VaultClient client,
            Map<String, VaultKeyStoreSecretProperty.Bundle> bundles)
            throws IOException, InterruptedException {
        String resp = client.read();
        ObjectMapper mapper = new ObjectMapper();
        val responseMap = mapper.readValue(resp.getBytes(), Map.class);
        val dataMap = getDataSection(responseMap);

        return bundles.entrySet().stream()
                .map(e -> new Tuple<>(
                        e.getKey(),
                        creatStoreBundleFromMap(dataMap, e.getValue())))
                .collect(Collectors.toMap(e -> e.v1, e -> e.v2));
    }

    private StoreBundle creatStoreBundleFromMap(
            Map<String, ?> map,
            VaultKeyStoreSecretProperty.Bundle bundle) {

        return new StoreBundle(
                Base64.getDecoder().decode(map.get(bundle.getKeyStoreField()).toString()),
                Base64.getDecoder().decode(map.get(bundle.getTrustStoreField()).toString()),
                map.get(bundle.getKeyStorePasswordField()).toString(),
                map.get(bundle.getTrustStorePasswordField()).toString()
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, ?> getDataSection(Map<String, Object> map) {
        return (Map<String, ?>) ((Map<String, ?>) map.get("data")).get("data");
    }

    class Tuple<T1, T2> {
        T1 v1;
        T2 v2;

        Tuple(T1 v1, T2 v2) {
            this.v1 = v1;
            this.v2 = v2;
        }
    }

    public VaultKeyStoreLoader(VaultKeyStoreSecretProperty property) {
        this.property = property;
    }

    @Override
    public byte[] loadKeyStore(String bundleName) {
        return new byte[0];
    }

    @Override
    public byte[] loadTrustStore(String bundleName) {
        return new byte[0];
    }

    @Override
    public String loadKeyStorePassword(String bundle) {
        return null;
    }

    @Override
    public String loadTrustStorePassword(String bundle) {
        return null;
    }

    class StoreBundle {
        byte[] keyStore;
        byte[] trustStore;
        String keyPassword;
        String trustPassword;

        public StoreBundle(byte[] keyStore, byte[] trustStore,
                           String keyPassword, String trustPassword) {
            this.keyStore = keyStore;
            this.trustStore = trustStore;
            this.keyPassword = keyPassword;
            this.trustPassword = trustPassword;
        }
    }
}
