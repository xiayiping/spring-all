package org.xyp.demo.call;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "echoServer")
public interface EchoService {

    @GetMapping("/echo/echo")
    public String echo();
}
