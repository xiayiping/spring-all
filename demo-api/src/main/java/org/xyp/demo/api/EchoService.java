package org.xyp.demo.api;

//import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "echoServer", configuration = ServiceConfig.class)
public interface EchoService {

    @GetMapping("/echo/echo")
    public String echo();
}
