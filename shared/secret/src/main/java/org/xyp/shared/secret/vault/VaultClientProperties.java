package org.xyp.shared.secret.vault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.io.Resource;
import org.xyp.shared.secret.model.CertificateFileType;
import org.xyp.shared.secret.model.UrlSchema;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(VaultClientProperties.PREFIX)
public class VaultClientProperties {
    public static final String PREFIX = "org.xyp.shared.vault";

    private UrlSchema schema;
    private String host;
    private int port;
    private String token;

    private CertificateFileType trustStoreType;
    private Resource trustStore;

}
