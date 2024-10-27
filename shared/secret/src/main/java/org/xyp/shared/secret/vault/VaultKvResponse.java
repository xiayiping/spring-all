package org.xyp.shared.secret.vault;

import lombok.Data;

@Data
public class VaultKvResponse {
    String request_id = "";
    VaultKvData data = new VaultKvData();
}
