package org.xyp.shared.secret.loader.cert;


import org.xyp.shared.secret.loader.cert.config.KeyStoreSecretProperty;

import java.io.IOException;
import java.util.Map;

public interface SecretReader {
    Map<String, String> read(KeyStoreSecretProperty.SecretPath path) throws IOException, InterruptedException;
}
