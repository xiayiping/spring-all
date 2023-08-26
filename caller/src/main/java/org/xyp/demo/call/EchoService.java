package org.xyp.demo.call;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
//import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignAutoConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "echoServer1"
    , url="https://localhost:8094/"
//    , configuration = {FeignAutoConfiguration.class, FeignConfig.class}
)
public interface EchoService {

    @GetMapping("/echo/echo")
    public String echo();
}
