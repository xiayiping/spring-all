package org.xyp.sample.spring.common.sslcert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.ssl.JksSslBundleProperties;
import org.springframework.boot.autoconfigure.ssl.SslBundleRegistrar;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.boot.ssl.NoSuchSslBundleException;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundleRegistry;
import org.springframework.util.StringUtils;
import org.xyp.function.wrapper.ResultOrError;
import org.xyp.sample.spring.common.sslcert.config.KeyStoreSecretProperty;

import java.io.ByteArrayOutputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class SecretSslBundleRegister implements SslBundleRegistrar {
    static class FullbackRegistry implements SslBundleRegistry {
        final Map<String, SslBundle> registeredBundles = new ConcurrentHashMap<>();
        boolean backInitiated = false;

        @Override
        public void registerBundle(String name, SslBundle bundle) {
            this.registeredBundles.put(name, bundle);
        }

        @Override
        public void updateBundle(String name, SslBundle updatedBundle) throws NoSuchSslBundleException {
            this.registeredBundles.put(name, updatedBundle);
        }
    }

    private final SslBundleRegistrar baseRegistrar;
    private final List<KeyStoreLoader> keyStoreLoaders;
    private final KeyStoreSecretProperty property;
    private final FullbackRegistry fallbackRegistry = new FullbackRegistry();

    @Override
    public void registerBundles(SslBundleRegistry registry) {
        log.info("starting registration secret ssl bundles");
        for (KeyStoreLoader loader : keyStoreLoaders) {
            val supportedEntries = property.getEntries().stream()
                .filter(loader::isSupported)
                .toList();
            loadKeyStoreByLoader(registry, loader, supportedEntries);
        }
        if (registry instanceof DefaultSslBundleRegistry bundles) {
            for (val entry : property.getEntries()) {
                val hasSupportedLoader = keyStoreLoaders.stream()
                    .anyMatch(loader -> loader.isSupported(entry));
                if (!hasSupportedLoader) {
                    bundleReferredStore(entry, bundles);
                }

            }
        }
    }

    private void bundleReferredStore(
        KeyStoreSecretProperty.KeyStoreEntry entry,
        DefaultSslBundleRegistry defaultSslBundleRegistry
    ) {
        val bundles = entry.getPaths().stream()
            .map(KeyStoreSecretProperty.SecretPath::getBundles)
            .toList();
        for (val bundleSetting : bundles) {
            bundleReferredStore(bundleSetting, defaultSslBundleRegistry);
        }
    }

    private void bundleReferredStore(
        Map<String, KeyStoreSecretProperty.Bundle> bundleSettingMap,
        DefaultSslBundleRegistry defaultSslBundleRegistry
    ) {
        for (val mapEnt : bundleSettingMap.entrySet()) {
            bundleReferredStore(mapEnt.getKey(), mapEnt.getValue(), defaultSslBundleRegistry);
        }
    }

    private void bundleReferredStore(
        String newBundleId,
        KeyStoreSecretProperty.Bundle bundle,
        DefaultSslBundleRegistry defaultSslBundleRegistry
    ) {
        try {
            String newPassword = generateRandomPassword();
            byte[] keyStoreBytes = null;
            if (StringUtils.hasText(bundle.getKeyStoreField())) {
                val mergedKeyStore = KeyStore.getInstance("JKS");
                mergedKeyStore.load(null, null);// empty load is MUST
                for (val keRef : bundle.getKeyStoreField().split(",")) {
                    mergeKeyStore(mergedKeyStore, keRef.trim(), defaultSslBundleRegistry, newPassword);
                }
                keyStoreBytes = keyStoreToByte(mergedKeyStore, newPassword);
            }
            byte[] trustStoreBytes = null;
            if (StringUtils.hasText(bundle.getTrustStoreField())) {
                val mergedTrustStore = KeyStore.getInstance("JKS");
                mergedTrustStore.load(null, null);// empty load is MUST
                for (val keRef : bundle.getTrustStoreField().split(",")) {
                    mergeTrustStore(mergedTrustStore, keRef.trim(), defaultSslBundleRegistry);
                }
                trustStoreBytes = keyStoreToByte(mergedTrustStore, newPassword);
            }
            val passwordByte = newPassword.getBytes();
            val sslBundle = createSslBundle(
                newBundleId + "-key", newBundleId + "-trust",
                keyStoreBytes, passwordByte,
                trustStoreBytes, passwordByte
            );
            defaultSslBundleRegistry.registerBundle(newBundleId, sslBundle);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void mergeKeyStore(
        KeyStore mergedKeyStore,
        String keyRef,
        DefaultSslBundleRegistry defaultReg,
        String newPassword
    ) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        val theBundle = getBundle(keyRef, defaultReg);
        val keyStore = theBundle.getStores().getTrustStore();
        if (null == keyStore) {
            return;
        }
        String keyStorePassword = theBundle.getStores().getKeyStorePassword();

        for (val aliasK : Collections.list(keyStore.aliases())) {
            if (keyStore.isKeyEntry(aliasK)) {
                Key key = keyStore.getKey(aliasK, keyStorePassword.toCharArray());
                Certificate[] certChain = keyStore.getCertificateChain(aliasK);
                mergedKeyStore.setKeyEntry(aliasK, key, newPassword.toCharArray(), certChain);
                mergedKeyStore.setCertificateEntry(aliasK + "-cert", keyStore.getCertificate(aliasK));
            } else {
                Certificate cert1 = keyStore.getCertificate(aliasK);
                mergedKeyStore.setCertificateEntry(aliasK, cert1);
            }
        }
    }

    private void mergeTrustStore(
        KeyStore mergedTrustStore,
        String keyRef,
        DefaultSslBundleRegistry defaultReg
    ) throws KeyStoreException {
        val theBundle = getBundle(keyRef, defaultReg);
        val trustStore = theBundle.getStores().getTrustStore();
        if (null == trustStore) {
            return;
        }

        for (val aliasT : Collections.list(trustStore.aliases())) {
            Certificate cert1 = trustStore.getCertificate(aliasT);
            mergedTrustStore.setCertificateEntry(aliasT, cert1);
        }
    }

    private void loadKeyStoreByLoader(
        SslBundleRegistry registry,
        KeyStoreLoader loader,
        List<KeyStoreSecretProperty.KeyStoreEntry> sslStoreEntries
    ) {
        for (KeyStoreSecretProperty.KeyStoreEntry ent : sslStoreEntries) {
            val bundles = ent.getPaths().stream()
                .flatMap(p -> p.getBundles().entrySet().stream())
                .toList();
            loadAndRegisterBundles(registry, loader, ent, bundles);
        }
    }

    private void loadAndRegisterBundles(
        SslBundleRegistry registry,
        KeyStoreLoader loader,
        KeyStoreSecretProperty.KeyStoreEntry ent,
        List<Map.Entry<String, KeyStoreSecretProperty.Bundle>> bundles
    ) {
        for (Map.Entry<String, KeyStoreSecretProperty.Bundle> bd : bundles) {
            try {
                byte[] keyPassword = loader.loadKeyStorePassword(bd.getKey());
                byte[] trustPassword = loader.loadTrustStorePassword(bd.getKey());
                byte[] keyStoreContent = loader.loadKeyStore(bd.getKey());
                byte[] trustStoreContent = loader.loadTrustStore(bd.getKey());
                val keyStoreLoc = System.identityHashCode(ent) + "-" + bd.getKey() + "-key";
                val trustStoreLoc = System.identityHashCode(ent) + "-" + bd.getKey() + "-trust";
                val sslBundle = createSslBundle(
                    keyStoreLoc,
                    trustStoreLoc,
                    keyStoreContent,
                    keyPassword,
                    trustStoreContent,
                    trustPassword
                );
                registry.registerBundle(bd.getKey(), sslBundle);
            } catch (Exception e) {
                log.warn("create bundle {} failed because {}", bd.getKey(), e.getMessage(), e);
            }
        }

    }

    private InMemoryPropertiesSslBundle createSslBundle(
        String keyStoreLoc, String trustStoreLoc,
        byte[] keyStoreContent, byte[] keyPassword,
        byte[] trustStoreContent, byte[] trustPassword
    ) {
        val keyStoreDetail = null == keyStoreContent ? null :
            StoreUtil.createStoreDetails(keyStoreLoc, keyStoreContent, keyPassword);
        val trustStoreDetail = null == trustStoreContent ? null :
            StoreUtil.createStoreDetails(trustStoreLoc, trustStoreContent, trustPassword);
        var sslStoreBundle = new InMemoryJksSslStoreBundle(keyStoreDetail, trustStoreDetail);
        return new InMemoryPropertiesSslBundle(sslStoreBundle, new JksSslBundleProperties());
    }

    private byte[] keyStoreToByte(KeyStore keyStore, String password) {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            keyStore.store(bout, password.toCharArray());
            return bout.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String generateRandomPassword() {
        val random = new SecureRandom();
        val len = 64;
        byte[] passwd = new byte[len];
        for (int i = 0; i < len; i++) {
            passwd[i] = (byte) (random.nextInt(48, 123));
        }
        return new String(passwd);
    }

    private SslBundle getBundle(String keyRef, DefaultSslBundleRegistry defaultSslBundleRegistry) {
        return ResultOrError.on(() -> defaultSslBundleRegistry.getBundle(keyRef))
            .getResult()
            .getOrFallBackForError(err -> {
                log.warn("use default bundle because error {}", err.getMessage(), err);
                return getFromFallback(keyRef);
            });
    }

    private SslBundle getFromFallback(String keyRef) {
        if (!fallbackRegistry.backInitiated) {
            baseRegistrar.registerBundles(fallbackRegistry);
            fallbackRegistry.backInitiated = true;
        }
        return Optional.ofNullable(fallbackRegistry.registeredBundles.get(keyRef))
            .orElseThrow(() -> new NoSuchSslBundleException(
                keyRef,
                "SSL bundle name '%s' cannot be found".formatted(keyRef)
            ));
    }
}
