package org.xyp.demo.api.maptransfer;

import java.util.Map;

public class MapTransfer {
    static Map<?, ?> demoMap = Map.of(
            "name", "${name}",
            "age", "${age}",
            "numbers", "2"
    );

    static String demoJson = """
{
    "name": "${name}",
    
}
            """;
}
