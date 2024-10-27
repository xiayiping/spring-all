package org.xyp.shared.secret.vault;

public interface VaultClient {
    VaultKvResponse readKvFromPath(String engine, String path);
}
