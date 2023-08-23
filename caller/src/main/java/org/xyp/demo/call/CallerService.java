package org.xyp.demo.call;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.FeignClientsConfiguration;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "callEcho", url = "https://localhost:8094/echo"/*, contextId = "userSSL"*/,
        configuration = {FeignClientsConfiguration.class, FeignConfig.class}/**/)
public interface CallerService {
    @GetMapping("/echo")
    public String echo();
}
