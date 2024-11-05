package org.xyp.shared.utils;

import org.xyp.shared.function.wrapper.ResultOrError;

import java.io.File;
import java.net.URL;

public class ResourceUtils {
    private ResourceUtils() {
    }

    private static final String FILE = "file:";
    private static final String CLASSPATH = "classpath:";

    public static File getFile(String location) {
        if (location.startsWith(FILE)) {
            return new File(location.substring(FILE.length()));
        } else if (location.startsWith(CLASSPATH)) {
            final var normalized = ResultOrError.on(() -> location.substring(CLASSPATH.length()))
                .map(loc -> {
                    var tmp = loc;
                    if (tmp.startsWith("/")) {
                        tmp = tmp.substring(1);
                    }
                    return tmp;
                }).get();
            URL resource = ResourceUtils.class.getClassLoader().getResource(normalized);
            if (resource == null) {
                throw new IllegalArgumentException("File not found: " + location);
            }
            // Convert URL to File
            return new File(resource.getFile());
        }
        throw new IllegalArgumentException(location + " is not support for resource");
    }
}
