package org.xyp.demo.call;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "echoService", url = "${echo.url}", configuration = {FeignConfig.class})
public interface EchoService {

    @GetMapping("/echo")
    public String echo();
}
