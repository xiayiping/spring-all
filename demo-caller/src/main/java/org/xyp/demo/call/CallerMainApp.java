package org.xyp.demo.call;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.logging.LoggingMetricExporter;
import io.opentelemetry.exporter.logging.LoggingSpanExporter;
import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.instrumentation.spring.webmvc.v6_0.SpringWebMvcTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.OpenTelemetrySdkBuilder;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import jakarta.servlet.Filter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.slf4j.bridge.SLF4JBridgeHandler;
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

    @Bean
    public OpenTelemetry openTelemetry() {
//        SdkLoggerProvider sdkLoggerProvider = SdkLoggerProvider.builder()
//            .setResource(
//                Resource.getDefault().toBuilder()
//                    .put(ResourceAttributes.SERVICE_NAME, "log4j-example")
//                    .build())
//            .addLogRecordProcessor(
//                BatchLogRecordProcessor.builder(
//                        OtlpGrpcLogRecordExporter.builder()
//                            .setEndpoint("http://localhost:5555")
//                            .build())
//                    .build())
//            .build();
//
//        Resource resource =
//            Resource.getDefault().toBuilder().put(ResourceAttributes.SERVICE_NAME,
//                "dice-server").put(ResourceAttributes.SERVICE_VERSION, "0.1.0").build();
//
//        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
//            .addSpanProcessor(SimpleSpanProcessor.create(
//                OtlpGrpcSpanExporter.builder().setEndpoint("http://localhost:5555")
//                    .build()))
//            .setResource(resource)
//            .build();
//
//        OpenTelemetrySdk openTelemetry = OpenTelemetrySdk.builder()
////            .setTracerProvider(sdkTracerProvider)
////            .setMeterProvider(sdkMeterProvider)
//            .setLoggerProvider(sdkLoggerProvider)
//            .setPropagators(ContextPropagators.create(
//                TextMapPropagator.composite(
//                    W3CTraceContextPropagator.getInstance(),
//                    W3CBaggagePropagator.getInstance())))
//            .buildAndRegisterGlobal();
////        OpenTelemetryAppender;
//
//
//        return openTelemetry;

        /////////////////////////

//        Resource resource = Resource.getDefault().toBuilder().put(ResourceAttributes.SERVICE_NAME, "dice-server").put(ResourceAttributes.SERVICE_VERSION, "0.1.0").build();

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(
//                LoggingSpanExporter
//                    .create(),
                OtlpGrpcSpanExporter.builder().setEndpoint("http://127.0.0.1:4317")
                    .build()
            ).build())
//            .setResource(resource)
            .build();

//        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
//            .registerMetricReader(PeriodicMetricReader.builder(LoggingMetricExporter.create()).build())
//            .setResource(resource)
//            .build();

        SdkLoggerProvider sdkLoggerProvider = SdkLoggerProvider.builder()
            .addLogRecordProcessor(BatchLogRecordProcessor.builder(SystemOutLogRecordExporter.create()).build())
//            .setResource(resource)
            .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
            .setTracerProvider(sdkTracerProvider)
//            .setMeterProvider(sdkMeterProvider)
            .setLoggerProvider(sdkLoggerProvider)
            .setPropagators(ContextPropagators.create(TextMapPropagator.composite(W3CTraceContextPropagator.getInstance(), W3CBaggagePropagator.getInstance())))
            .buildAndRegisterGlobal();

        return openTelemetry;

    }

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
    public RestTemplate getRestTemplate(RestTemplateBuilder builder,
                                        SslBundles sslBundles) {
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
//        Optional<MeterRegistry> otelRegistry = Metrics.globalRegistry.getRegistries()
//        .stream()
//                .filter(r -> r.getClass().getName().contains
//                ("OpenTelemetryMeterRegistry"))
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
