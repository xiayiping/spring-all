package org.xyp.demo.echo;

import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EchoMainApp {
    int a = 99;
    public EchoMainApp() {
        a = 100;
    }

    public static void main(String[] args) {
        val aa = new EchoMainApp();
    }
}
