package org.xyp.demo.call;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.instrumentation.spring.webmvc.v6_0.SpringWebMvcTelemetry;
import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Slf4j
@SpringBootApplication
// @LoadBalancerClients({
// @LoadBalancerClient("echoServer")
// })
public class CallerMainApp {
    public static void main(String[] args) {
        SpringApplication.run(CallerMainApp.class, args);
    }
    //
    // @Autowired
    // EchoService echoService;

    @Value("${echo.url}")
    String echoUrl;

    @Value("${server.ssl.bundle}")
    String sslBundleKey;

    private boolean isHttps() {
        return echoUrl.startsWith("https");
    }

    @Bean("echo")
    public RestTemplate getRestTemplate(RestTemplateBuilder builder, SslBundles sslBundles) {
        return Optional.of(isHttps()).filter(i -> i)
                .map(i -> builder
                        .rootUri(echoUrl)
                        .setSslBundle(sslBundles.getBundle(sslBundleKey))
                        .build())
                .orElseGet(() -> builder
                        .rootUri(echoUrl)
                        .build());
    }

    @Bean("otel")
    public RestTemplate getOtelRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public WebClient getHttpClient(WebClientSsl ssl) throws Exception {
        val b = WebClient.builder().baseUrl(echoUrl);
        if (isHttps())
            return b.apply(ssl.fromBundle(sslBundleKey))
                    .build();
        else
            return b.build();
    }

    // @Bean
    // public ApplicationRunner runner(EchoService service) {
    // return args -> {
    // try {
    // System.out.println(service.echo());
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // };
    // }

//    @Bean
//    @ConditionalOnClass(name = "io.opentelemetry.javaagent.OpenTelemetryAgent")
//    public MeterRegistry otelRegistry() {
//        log.warn("register open tel metrics");
//        Optional<MeterRegistry> otelRegistry = Metrics.globalRegistry.getRegistries().stream()
//                .filter(r -> r.getClass().getName().contains("OpenTelemetryMeterRegistry"))
//                .findAny();
//        otelRegistry.ifPresent(Metrics.globalRegistry::remove);
//        return otelRegistry.orElse(null);
//    }

//    @Bean
//    ObservationFilter orgFilter() {
//        return context -> {
//            log.warn("add org add org -----------------");
//            context.addLowCardinalityKeyValue(KeyValue.of("org", "xyp"));
//            return context;
//        };
//    }
//
//    @Bean
//    public SpanExporter spanExporter() {
//        log.warn("creating http span exporter");
//        return
////                OtlpHttpSpanExporter
//                OtlpGrpcSpanExporter
//                        .builder()
//                        .setEndpoint("http://localhost:5555").build();
//    }

    @Bean
    public Filter telemetryFilter(OpenTelemetry openTelemetry) {
        return SpringWebMvcTelemetry.create(openTelemetry).createServletFilter();
    }

    private String abc() {
        return "sdfsdf";
    }
}
