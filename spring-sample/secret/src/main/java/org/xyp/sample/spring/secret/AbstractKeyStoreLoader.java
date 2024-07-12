package org.xyp.sample.spring.secret;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.xyp.function.wrapper.ResultOrError;
import org.xyp.sample.spring.secret.config.KeyStoreSecretProperty;
import org.xyp.sample.spring.secret.pojo.SecretStoreBundle;
import org.xyp.sample.spring.secret.pojo.SecretTuple;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractKeyStoreLoader<T> implements KeyStoreLoader {

    protected final Map<String, SecretStoreBundle> storeBundleMap = new HashMap<>();

    private SecretStoreBundle getBundle(String bundleName) {
        val b = this.storeBundleMap.get(bundleName);
        if (null == b) {
            log.error("can't find bundle of name {}", bundleName);
            throw new IllegalStateException("can't find bundle of name [" + bundleName + "]");
        }
        return b;
    }

    @Override
    public byte[] loadKeyStore(String bundleName) {
        return getBundle(bundleName).keyStore();
    }

    @Override
    public byte[] loadTrustStore(String bundleName) {
        return getBundle(bundleName).trustStore();
    }

    @Override
    public byte[] loadKeyStorePassword(String bundleName) {
        return getBundle(bundleName).keyPassword();
    }

    @Override
    public byte[] loadTrustStorePassword(String bundleName) {
        return getBundle(bundleName).trustPassword();
    }

    protected abstract SecretReader<T> getReader(
        KeyStoreSecretProperty.KeyStoreEntry entry
    );

    protected abstract Map<String, Object> getData(Object resp);

    protected abstract boolean shouldLoadData();

    public void loadData(KeyStoreSecretProperty property) {
        if (!shouldLoadData()) {
            return;
        }

        // entry: url, capath, token
        // path: secret path
        // bundle fields
        val vaultEntries = property.getEntries().stream()
            .filter(this::isSupported)
            .toList();

        val clientAndPaths = vaultEntries.stream()
            .map(entry -> ResultOrError.on(() ->
                new SecretTuple<>(getReader(entry), entry.getPaths())).get())
            .toList();

        val bundled = clientAndPaths.stream()
            .flatMap(tu -> {
                val client = tu.v1();
                return tu.v2().stream()
                    .map(path -> new SecretTuple<>(client, path));
            })
            .map(tu -> ResultOrError.on(() ->
                readBundle(tu.v1(),
                    tu.v2().getPath(),
                    tu.v2().getBundles())).get())
            .toList();

        for (Map<String, SecretStoreBundle> map : bundled) {
            this.storeBundleMap.putAll(map);
        }
    }

    protected Map<String, SecretStoreBundle> readBundle(
        SecretReader<?> client,
        String sPath,
        Map<String, KeyStoreSecretProperty.Bundle> bundles
    ) throws IOException, InterruptedException {
        log.info("load from secret key store");
        val resp = client.read(sPath);
        val dataMap = getData(resp);

        return bundles.entrySet().stream()
            .map(e -> new SecretTuple<>(
                e.getKey(),
                creatStoreBundleFromMap(dataMap, e.getValue())))
            .collect(Collectors.toMap(SecretTuple::v1, SecretTuple::v2));
    }

    protected SecretStoreBundle creatStoreBundleFromMap(
        Map<String, ?> map,
        KeyStoreSecretProperty.Bundle bundle
    ) {

        return new SecretStoreBundle(
            Optional.ofNullable(map.get(bundle.getKeyStoreField()))
                .map(s -> Base64.getDecoder().decode(s.toString()))
                .orElse(null),
            Optional.ofNullable(map.get(bundle.getTrustStoreField()))
                .map(s -> Base64.getDecoder().decode(s.toString()))
                .orElse(null),
            Optional.ofNullable(map.get(bundle.getKeyStorePasswordField()))
                .map(s -> s.toString().getBytes())
                .orElse(null),
            Optional.ofNullable(map.get(bundle.getTrustStorePasswordField()))
                .map(s -> s.toString().getBytes())
                .orElse(null)
        );
    }
}
