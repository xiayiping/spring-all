package org.xyp.shared.excel;

import java.io.File;
import java.util.function.Consumer;

public class ConfigFunctions {
    private ConfigFunctions() {
    }

    public static void processFile(File file,
                                   Consumer<File> parser
    ) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                processFile(child, parser);
            }
        } else {
            parser.accept(file);
        }
    }

    public static String getConfigName(File file, String rootPath) {
        var name = file.getAbsolutePath().replace("\\", "/")
            .substring(rootPath.length());
        if (name.endsWith(".xml")) {
            name = name.substring(0, name.length() - ".xml".length());
        }
        while (name.startsWith("/")) {
            name = name.substring(1);
        }
        return name;
    }
}
