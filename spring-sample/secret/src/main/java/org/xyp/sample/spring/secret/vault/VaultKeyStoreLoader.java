package org.xyp.sample.spring.secret.vault;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.vault.config.VaultProperties;
import org.springframework.util.StringUtils;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;
import org.xyp.sample.spring.secret.AbstractKeyStoreLoader;
import org.xyp.sample.spring.secret.SecretReader;
import org.xyp.sample.spring.secret.config.KeyStoreSecretProperty;

import java.util.Map;
import java.util.Objects;

@Slf4j
@AllArgsConstructor
public class VaultKeyStoreLoader extends AbstractKeyStoreLoader<Map<String, Object>> {

    final VaultTemplate vaultTemplate;
    final VaultProperties vaultProperties;

    @Override
    public boolean isSupported(KeyStoreSecretProperty.KeyStoreEntry entry) {
        return StringUtils.hasText(entry.getUrl()) &&
                !StringUtils.hasText(entry.getRegion());
    }

    @Override
    protected SecretReader<Map<String, Object>> getReader(KeyStoreSecretProperty.KeyStoreEntry entry) {
        return path -> Objects.requireNonNull(vaultTemplate
                        .opsForKeyValue(entry.getUrl(), VaultKeyValueOperationsSupport.KeyValueBackend.KV_2)
                        .get(path))
                .getData();
    }

    @Override
    protected boolean shouldLoadData() {
        String vaultToken = vaultProperties.getToken();
        if (!StringUtils.hasText(vaultToken)) {
            log.info("${vault_token} not provided when starting up, so skip load key store from vault");
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Map<String, Object> getData(Object resp) {

        if (resp instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        } else {
            throw new IllegalStateException("vault template should return value of map, but actually " + resp.getClass());
        }
    }
}
