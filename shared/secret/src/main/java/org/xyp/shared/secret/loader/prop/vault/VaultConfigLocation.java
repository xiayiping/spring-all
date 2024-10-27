package org.xyp.shared.secret.loader.prop.vault;

import lombok.Getter;
import org.springframework.boot.context.config.ConfigDataResource;

@Getter
public class VaultConfigLocation extends ConfigDataResource {

    public static final String PREFIX = "vault-customized://";

    private final String backend;
    private final String profile;
    private final String pathWithoutProfile;

    public VaultConfigLocation(
        String backend,
        String profile,
        String pathWithoutProfile
    ) {
        super();
        this.backend = backend;
        this.profile = profile;
        this.pathWithoutProfile = pathWithoutProfile.startsWith("/") ?
            pathWithoutProfile.substring(1) : pathWithoutProfile;
    }

}
