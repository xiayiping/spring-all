package org.xyp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

public class EnvProcessor2 implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {

        System.out.println("22222222");
        System.out.println("22222222");
        System.out.println("22222222");
        System.out.println("22222222");
        System.out.println("22222222");
        System.out.println("22222222");
        System.out.println("22222222");
        System.out.println("22222222");
        System.out.println("22222222");
        var asssss = "sdfsfd";
        System.out.println(asssss);
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
