package org.xyp.sample.spring.common.sslcert.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
public class InMemoryJksStoreDetails {
    private String type;
    private String provider;
    private String location;
    private byte[] content;
    private byte[] password;

    public String type() {
        return type;
    }

    public String provider() {
        return provider;
    }

    public String location() {
        return location;
    }

    public byte[] content() {
        return content;
    }

    public byte[] password() {
        return password;
    }

}
