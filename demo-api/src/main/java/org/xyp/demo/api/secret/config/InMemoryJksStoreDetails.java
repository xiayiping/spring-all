package org.xyp.demo.api.secret.config;

public record InMemoryJksStoreDetails(String type, String provider, String location,
                                      byte[] content,
                                      String password) {
}
