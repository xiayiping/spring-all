package org.xyp.sample.spring.webapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("org.xyp.web-api")
public class WebApiProperties {
    boolean needAuthentication = false;
    boolean needCsrf = false;
}
