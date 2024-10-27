package org.xyp.shared.secret.vault;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class VaultKvData {
    Map<String, String> data = new HashMap<>();
}
