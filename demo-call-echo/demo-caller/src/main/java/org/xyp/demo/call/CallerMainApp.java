package org.xyp.demo.call;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Optional;

@Slf4j
@SpringBootApplication
// @LoadBalancerClients({
// @LoadBalancerClient("echoServer")
// }) |
public class CallerMainApp {
    /*
    PS C:\Users\terry.xiao> vault token create -policy=matt_policy
Key Value
--- -----
token hvs.CAESIOuIuBjV-viraSq1zb6A7F5Aeg4icLbz9HyfEXTZMlaXGh4KHGh2cy5EWFNadDVOM1VtbEJkUWhIb1FpZjQ3QU4
token_accessor vnOOvwwoDBV3c7fpOZse8rru
token_duration 768h
token_renewable true
token_policies ["default" "matt_policy"]
identity_policies []
policies ["default" "matt_policy"]
     */


//    @Bean
//    public OpenTelemetry openTelemetry() {
//
//        /////////////////////////
//
//        Resource resource = Resource.getDefault().toBuilder()
//                .put(ResourceAttributes.SERVICE_NAME, "caller-server")
//                .put(ResourceAttributes.SERVICE_VERSION, "0.1.0")
//                .build();
//
//        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
//                .addSpanProcessor(BatchSpanProcessor.builder(
////                LoggingSpanExporter
////                    .create(),
//                        OtlpGrpcSpanExporter.builder()
//                                .setEndpoint("http://127.0.0.1:5555")
//                                .build()).build())
//                .setResource(resource)
//                .build();
//
////        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
////            .registerMetricReader(PeriodicMetricReader.builder(LoggingMetricExporter
////            .create()).build())
////            .setResource(resource)
////            .build();
//
//        SdkLoggerProvider sdkLoggerProvider = SdkLoggerProvider
//                .builder()
//                .addLogRecordProcessor(
//                        BatchLogRecordProcessor.builder(
//                                OtlpGrpcLogRecordExporter.builder()
//                                        .setEndpoint("http://127.0.0.1:5556")
//                                        .build()).build())
//                .setResource(resource)
//                .build();
//
//        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
//                .setTracerProvider(sdkTracerProvider)
////            .setMeterProvider(sdkMeterProvider)
//                .setLoggerProvider(sdkLoggerProvider)
//                .setPropagators(ContextPropagators.create(
//                        TextMapPropagator.composite(
//                                W3CTraceContextPropagator.getInstance(),
//                                W3CBaggagePropagator.getInstance())))
//                .buildAndRegisterGlobal();
//
//        return openTelemetry;
//
//    }

    public static void main(String[] args) {
        SpringApplication.run(CallerMainApp.class, args);
    }
    //
    // @Autowired
    // EchoService echoService;

    @Value("${echo.url}")
    String echoUrl;

//    @Value("${server.ssl.bundle}")
    String sslBundleKey = "defaultBundle";

    private boolean isHttps() {
        return echoUrl.startsWith("https");
    }

    @Bean("echo")
    public RestTemplate getRestTemplate(RestTemplateBuilder builder,
                                        SslBundles sslBundles)
            throws Exception {

        System.out.println("xxxxxxxxxxxxxxxxxxxxxx");
        System.out.println("xxxxxxxxxxxxxxxxxxxxxx");
        System.out.println("xxxxxxxxxxxxxxxxxxxxxx");
        System.out.println("xxxxxxxxxxxxxxxxxxxxxx");
        System.out.println("xxxxxxxxxxxxxxxxxxxxxx");
        System.out.println("xxxxxxxxxxxxxxxxxxxxxx");
        System.out.println("xxxxxxxxxxxxxxxxxxxxxx");
        System.out.println("xxxxxxxxxxxxxxxxxxxxxx");
        return Optional.of(isHttps()).filter(i -> i)
                .map(i -> builder
                        .rootUri(echoUrl)
//                        .setSslBundle(bundle)
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

    @Bean
    public ApplicationRunner runner() {
        int aa = 0;
        int bb = 0;
        bb = 4;
        return args -> {
            try {
                System.out.println(aa);
                Assert.isTrue(aa >= 0, "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
    }

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
//
//    @Bean
//    public Filter telemetryFilter(OpenTelemetry openTelemetry) {
//        return SpringWebMvcTelemetry.create(openTelemetry).createServletFilter();
//    }

    private String abc() {

        return "sdfsdf";
    }



}
