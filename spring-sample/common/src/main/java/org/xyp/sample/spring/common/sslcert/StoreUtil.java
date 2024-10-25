package org.xyp.sample.spring.common.sslcert;

import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.xyp.sample.spring.common.sslcert.model.InMemoryJksStoreDetails;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Optional;

public class StoreUtil {
    private StoreUtil() {
    }

    public static InMemoryJksStoreDetails createStoreDetails(String location, byte[] content, byte[] password) {
        return new InMemoryJksStoreDetails(null, null, location, content, password);
    }

    public static boolean isHardwareKeystoreType(String type) {
        return Optional.ofNullable(type).orElse("").equalsIgnoreCase("PKCS11");
    }

    public static boolean isDetailsEmpty(InMemoryJksStoreDetails details) {
        return null == details.content() || details.content().length == 0;
    }

    public static Optional<KeyStore> createKeyStore(String name, InMemoryJksStoreDetails details) {
        if (details == null || isDetailsEmpty(details)) {
            return Optional.empty();
        }

        try {
            String type = (!StringUtils.hasText(details.type())) ?
                KeyStore.getDefaultType() : details.type();
            char[] password = (null != details.password()) ?
                new String(details.password()).toCharArray() : null;

            String location = details.location();
            KeyStore store = getKeyStoreInstance(type, details.provider());
            if (isHardwareKeystoreType(type)) {
                loadHardwareKeyStore(store, location, password);
            } else {
                loadKeyStore(store, location, details.content(), password);
            }
            return Optional.of(store);
        } catch (RuntimeException rt) {
            throw rt;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to create %s store: %s".formatted(name, e.getMessage()), e);
        }
    }

    private static KeyStore getKeyStoreInstance(String type, String provider)
        throws KeyStoreException, NoSuchProviderException {
        return (!StringUtils.hasText(provider)) ?
            KeyStore.getInstance(type) : KeyStore.getInstance(type, provider);
    }

    private static void loadHardwareKeyStore(KeyStore store, String location, char[] password)
        throws CertificateException, IOException, NoSuchAlgorithmException {
        Assert.state(!StringUtils.hasText(location),
            () -> "Location is '%s', but must be empty or null for PKCS11 hardware key store".formatted(location));
        store.load(null, password);
    }

    private static void loadKeyStore(KeyStore store, String location, byte[] content, char[] password) {
        Assert.state(StringUtils.hasText(location), () -> "store alias must not be empty or null");
        try {
            try (ByteArrayInputStream stream = new ByteArrayInputStream(content)) {
                if (null != password && password.length > 0) {
                    store.load(stream, password);
                } else {
                    final CertificateFactory certificateFactory =
                        CertificateFactory.getInstance("X.509");
                    final X509Certificate certificate = (X509Certificate) certificateFactory
                        .generateCertificate(stream);
                    store.load(null, null);
                    store.setCertificateEntry(location, certificate);

                }
            }
        } catch (RuntimeException rt) {
            throw rt;
        } catch (Exception e) {
            throw new IllegalStateException("Could not load store from %s".formatted(location), e);
        }
    }

}
