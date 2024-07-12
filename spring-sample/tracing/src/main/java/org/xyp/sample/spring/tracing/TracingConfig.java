package org.xyp.sample.spring.tracing;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackageClasses = TraceAspect.class)
public class TracingConfig {
}
