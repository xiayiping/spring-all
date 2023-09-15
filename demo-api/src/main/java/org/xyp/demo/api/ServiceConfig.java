package org.xyp.demo.api;

import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(clients = {EchoService.class})
public class ServiceConfig {
    public ServiceConfig() {
        System.out.println("sdf------------------------ init Service Config");
    }

}
