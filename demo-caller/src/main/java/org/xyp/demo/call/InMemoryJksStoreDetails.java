package org.xyp.demo.call;

public record InMemoryJksStoreDetails(String type, String provider, String location,
                                      byte[] content,
                                      String password) {
}
