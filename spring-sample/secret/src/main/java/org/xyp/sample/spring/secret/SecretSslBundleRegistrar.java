package org.xyp.sample.spring.secret;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.autoconfigure.ssl.JksSslBundleProperties;
import org.springframework.boot.autoconfigure.ssl.SslBundleRegistrar;
import org.springframework.boot.ssl.DefaultSslBundleRegistry;
import org.springframework.boot.ssl.NoSuchSslBundleException;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundleRegistry;
import org.xyp.function.wrapper.ResultOrError;
import org.xyp.sample.spring.secret.config.KeyStoreSecretProperty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SecretSslBundleRegistrar implements SslBundleRegistrar {

    static class FullbackRegistry implements SslBundleRegistry {
        final Map<String, SslBundle> registeredBundles = new ConcurrentHashMap<>();
        boolean backInited = false;

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

    public SecretSslBundleRegistrar(
        SslBundleRegistrar baseRegistrar,
        List<KeyStoreLoader> keyStoreLoaders,
        KeyStoreSecretProperty property) {
        this.baseRegistrar = baseRegistrar;
        this.keyStoreLoaders = keyStoreLoaders;
        this.property = property;
    }

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
                val supportedLoader = keyStoreLoaders.stream()
                    .filter(loader -> loader.isSupported(entry))
                    .findAny();
                if (supportedLoader.isEmpty()) {
                    bundleReferredStore(entry, bundles);
                }
            }
        }
    }

    private void bundleReferredStore(KeyStoreSecretProperty.KeyStoreEntry entry, DefaultSslBundleRegistry defaultReg) {
        val bundles = entry.getPaths().stream()
            .map(KeyStoreSecretProperty.SecretPath::getBundles)
            .toList();
        for (val bundleSetting : bundles) {
            bundleReferredStore(bundleSetting, defaultReg);
        }
    }

    private void bundleReferredStore(Map<String, KeyStoreSecretProperty.Bundle> bundleSettingMap,
                                     DefaultSslBundleRegistry defaultReg) {
        for (val mapEnt : bundleSettingMap.entrySet()) {
            bundleReferredStore(mapEnt.getKey(), mapEnt.getValue(), defaultReg);
        }
    }

    private void bundleReferredStore(
        String newBundleId, KeyStoreSecretProperty.Bundle bundleDetail,
        DefaultSslBundleRegistry defaultReg
    ) {
        try {
            String newPassword = generateRandomPassword();
            if (!StringUtils.isBlank(bundleDetail.getKeyStoreField())
                && StringUtils.isBlank(bundleDetail.getTrustStoreField())) {
                log.warn("{} has no trust store bundle but has key store, which no allowed", newBundleId);
                return;
            }
            byte[] keyStoreBytes = null;
            if (!StringUtils.isBlank(bundleDetail.getKeyStoreField())) {
                KeyStore mergedKeystore = KeyStore.getInstance("JKS"); // You can change the type if needed
                mergedKeystore.load(null, null);// the load null/null is
                for (val keyRef : bundleDetail.getKeyStoreField().split(",")) {
                    mergeKeyStore(mergedKeystore, keyRef, defaultReg, newPassword);
                }
                keyStoreBytes = keyStoreToByte(mergedKeystore, newPassword);
            }
            byte[] trustStoreBytes = null;
            if (!StringUtils.isBlank(bundleDetail.getTrustStoreField())) {
                KeyStore mergedTruststore = KeyStore.getInstance("JKS"); // You can change the type if needed
                mergedTruststore.load(null, null);// the load null/null is
                for (val keyRef : bundleDetail.getTrustStoreField().split(",")) {
                    mergeTrustStore(mergedTruststore, keyRef, defaultReg);
                }
                trustStoreBytes = keyStoreToByte(mergedTruststore, newPassword);
            }
            val passwordByte = newPassword.getBytes();
            var sslBundle = createSslBundle(newBundleId + "-key", newBundleId + "-trust",
                keyStoreBytes, passwordByte, trustStoreBytes, passwordByte);

            defaultReg.registerBundle(newBundleId, sslBundle);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private void mergeKeyStore(
        KeyStore mergedKeystore, String keyRef,
        DefaultSslBundleRegistry defaultReg, String newPassword
    ) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {

        val theOne = getBundle(keyRef, defaultReg);
        KeyStore keyStore = theOne.getStores().getKeyStore();
        String keyStorePassword = theOne.getStores().getKeyStorePassword();

        for (val aliasK : Collections.list(keyStore.aliases())) {
            if (keyStore.isKeyEntry(aliasK)) {
                Key key = keyStore.getKey(aliasK, keyStorePassword.toCharArray());
                Certificate[] certChain = keyStore.getCertificateChain(aliasK);
                mergedKeystore.setKeyEntry(aliasK, key, newPassword.toCharArray(), certChain);
                mergedKeystore.setCertificateEntry(aliasK + "-cert", keyStore.getCertificate(aliasK));
            } else {
                Certificate cert1 = keyStore.getCertificate(aliasK);
                mergedKeystore.setCertificateEntry(aliasK, cert1);
            }
        }
    }

    private void mergeTrustStore(
        KeyStore mergedKeystore, String keyRef,
        DefaultSslBundleRegistry defaultReg
    ) throws KeyStoreException {

        val theOne = getBundle(keyRef, defaultReg);
        KeyStore keyStore = theOne.getStores().getTrustStore();
        for (val aliasK : Collections.list(keyStore.aliases())) {
            Certificate cert1 = keyStore.getCertificate(aliasK);
            mergedKeystore.setCertificateEntry(aliasK, cert1);
        }
    }

    private void loadKeyStoreByLoader(SslBundleRegistry registry, KeyStoreLoader loader,
                                      List<KeyStoreSecretProperty.KeyStoreEntry> sslStoreEntries) {
        for (KeyStoreSecretProperty.KeyStoreEntry ent : sslStoreEntries) {
            val bundles = ent.getPaths().stream()
                .flatMap(p -> p.getBundles().entrySet().stream()).toList();
            loadAndRegisterBundles(registry, loader, ent, bundles);
        }
    }

    private void loadAndRegisterBundles(SslBundleRegistry registry, KeyStoreLoader loader,
                                        KeyStoreSecretProperty.KeyStoreEntry ent, List<Map.Entry<String, KeyStoreSecretProperty.Bundle>> bundles) {
        for (Map.Entry<String, KeyStoreSecretProperty.Bundle> bd : bundles) {
            try {
                byte[] keyPassword = loader.loadKeyStorePassword(bd.getKey());
                byte[] trustPassword = loader.loadTrustStorePassword(bd.getKey());
                byte[] keyStoreContent = loader.loadKeyStore(bd.getKey());
                byte[] trustStoreContent = loader.loadTrustStore(bd.getKey());
                val keyStoreLoc = System.identityHashCode(ent) + "-" + bd.getKey() + "-key";
                val trustStoreDetail = System.identityHashCode(ent) + "-" + bd.getKey() + "-trust";
                var sslBundle = createSslBundle(keyStoreLoc, trustStoreDetail, keyStoreContent, keyPassword,
                    trustStoreContent, trustPassword);
                registry.registerBundle(bd.getKey(), sslBundle);
            } catch (Exception e) {
                log.warn("create bundle" + bd.getKey() + " failed because : " + e);
            }
        }
    }

    private InMemoryPropertiesSslBundle createSslBundle(
        String keyStoreLoc, String trustStoreLoc,
        byte[] keyStoreContent, byte[] keyPassword, byte[] trustStoreContent, byte[] trustPassword) {
        var keyStoreDetail = null == keyStoreContent ? null
            : StoreUtil.createStoreDetails(
            keyStoreLoc, keyStoreContent, keyPassword);
        var trustStoreDetail = null == trustStoreContent ? null
            : StoreUtil.createStoreDetails(
            trustStoreLoc, trustStoreContent, trustPassword);

        var sslStoreBundle = new InMemoryJksSslStoreBundle(keyStoreDetail, trustStoreDetail);
        return new InMemoryPropertiesSslBundle(sslStoreBundle, new JksSslBundleProperties());
    }

    private byte[] keyStoreToByte(KeyStore keyStore, String password) {
        try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
            keyStore.store(bout, password.toCharArray());
            return bout.toByteArray();
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
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

    private SslBundle getBundle(String keyRef, DefaultSslBundleRegistry defaultReg) {
        return ResultOrError.on(() -> defaultReg.getBundle(keyRef))
            .getResult()
            .getOptionEvenErr()
            .orElseGet(() -> getFromFallback(keyRef));
    }

    private SslBundle getFromFallback(String keyRef) {
        if (!fallbackRegistry.backInited) {
            baseRegistrar.registerBundles(fallbackRegistry);
            fallbackRegistry.backInited = true;
        }
        return Optional.ofNullable(fallbackRegistry.registeredBundles.get(keyRef))
            .orElseThrow(() -> new NoSuchSslBundleException(
                keyRef,
                "SSL bundle name '%s' cannot be found".formatted(keyRef)));
    }
}
