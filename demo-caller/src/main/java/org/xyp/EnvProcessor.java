package org.xyp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.cloud.bootstrap.BootstrapApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;

import java.util.HashMap;
import java.util.Map;

public class EnvProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        System.out.println("11111111");
        System.out.println("11111111");
        System.out.println("11111111");
        System.out.println("11111111");
        System.out.println("11111111");
        System.out.println("11111111");
        System.out.println("11111111");
        System.out.println("11111111");
        System.out.println("11111111");
        var asssss = "sdfsfd";
        System.out.println(application.getAdditionalProfiles());
        System.out.println("--- default: ");
        for (String defaultProfile : environment.getDefaultProfiles()) {
            System.out.println(defaultProfile);
        }
        System.out.println("--- activate: ");
        for (String defaultProfile : environment.getActiveProfiles()) {
            System.out.println(defaultProfile);
        }
        if (environment.getPropertySources().contains(BootstrapApplicationListener.BOOTSTRAP_PROPERTY_SOURCE_NAME)) {
            System.out.println("ooooooooo");
            System.out.println("ooooooooo");
            System.out.println("ooooooooo");
            System.out.println("this is in bootstrap listener");

        } else {
            System.out.println("xxxxxxxxx");
            System.out.println("xxxxxxxxx");
            System.out.println("xxxxxxxxx");
            System.out.println("this is in non bootstrap listener");

            System.out.println("create source ");
            Map<String, String> sourceMap = new HashMap<>();
//        sourceMap.put("a.b.c", "abc");
            sourceMap.put("nameab", "xxx nameAB");
            PropertySource<?> ppSource = new OriginTrackedMapPropertySource("xypSource", sourceMap, false);
            environment.getPropertySources().addLast(ppSource);
        }

        System.out.println(asssss + " " + this);
    }

    @Override
    public int getOrder() {
        return 1;
    }
}
