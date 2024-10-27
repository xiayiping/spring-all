package org.xyp.shared.secret.loader.cert.vault;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.xyp.shared.secret.loader.cert.AbstractKeyStoreLoader;
import org.xyp.shared.secret.loader.cert.SecretReader;
import org.xyp.shared.secret.loader.cert.config.KeyStoreFetchSource;
import org.xyp.shared.secret.loader.cert.config.KeyStoreSecretProperty;
import org.xyp.shared.secret.vault.VaultClient;
import org.xyp.shared.secret.vault.VaultKvData;
import org.xyp.shared.secret.vault.VaultKvResponse;

import java.util.Map;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
public class VaultKeyStoreLoader extends AbstractKeyStoreLoader<Map<String, Object>> {

    final VaultClient vaultClient;

    @Override
    public boolean isSupported(KeyStoreSecretProperty.KeyStoreEntry entry) {
        return entry.getFetchSource().equals(KeyStoreFetchSource.VAULT);
    }

    @Override
    protected SecretReader getReader(KeyStoreSecretProperty.KeyStoreEntry entry) {
        return path -> Optional.ofNullable(vaultClient.readKvFromPath(path.getEngine(), path.getPath()))
            .map(VaultKvResponse::getData)
            .map(VaultKvData::getData)
            .orElse(Map.of());
    }

    @Override
    protected boolean shouldLoadData() {
        return null != vaultClient;
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
