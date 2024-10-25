package org.xyp.sample.spring.common.sslcert.vault;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.vault.config.VaultProperties;
import org.springframework.util.StringUtils;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultTemplate;
import org.xyp.sample.spring.common.sslcert.AbstractKeyStoreLoader;
import org.xyp.sample.spring.common.sslcert.SecretReader;
import org.xyp.sample.spring.common.sslcert.config.KeyStoreSecretProperty;
import org.xyp.sample.spring.common.sslcert.config.VaultApiVersion;

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
        return path -> Objects.requireNonNull(
                vaultTemplate.opsForKeyValue(
                        entry.getUrl(),
                        VaultApiVersion.V1.equals(entry.getVaultApiVersion()) ?
                            VaultKeyValueOperationsSupport.KeyValueBackend.KV_1 :
                            VaultKeyValueOperationsSupport.KeyValueBackend.KV_2
                    )
                    .get(path)
            )
            .getData();
    }

    @Override
    protected Map<String, Object> getData(Object resp) {
        if (resp instanceof Map<?, ?> map)
            return (Map<String, Object>) map;
        else throw new IllegalStateException("expect map but of type " + resp.getClass());
    }

    @Override
    protected boolean shouldLoadData() {
        return vaultProperties.isEnabled();
    }
}
