package org.xyp.demo.echo;

public record InMemoryJksStoreDetails(String type, String provider, String location,
                                      byte[] content,
                                      String password) {
}
