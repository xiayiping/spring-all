package a.b.c.d;

import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.xyp.demo.call.CallerMainApp;

@Configuration
@Import(CallerMainApp.class)
public class CallerTestRootApplication {
    public static void main(String[] args) {
        SpringApplication.from(CallerMainApp::main)
            .with(CallerTestRootApplication.class)
            .run(args);
    }
}
