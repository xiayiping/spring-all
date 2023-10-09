package org.xyp.demo.call;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.baggage.propagation.W3CBaggagePropagator;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.logs.SdkLoggerProvider;
import io.opentelemetry.sdk.logs.export.BatchLogRecordProcessor;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.hc.client5.http.ssl.TrustAllStrategy;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.ssl.JksSslBundleProperties;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientSsl;
import org.springframework.boot.ssl.SslBundle;
import org.springframework.boot.ssl.SslBundles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import javax.net.ssl.SSLContext;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
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
//    dev/paradise/keystore
//    String vaultPemPath = "d:/tools/vault/1.14/ca.pem";
    String vaultPemPath = "d:/tools/vault/1.14/tcghl-com-crt.pem";

//    String vaultRoot = "https://127.0.0.1:8180";
    String vaultRoot = "https://vault.tcghl.com";

    //    String vaultToken = "hvs.CAESIH66nAoa6gU05CN1CIpKIpaP3pkNYM2gbMEjmo7szQ4WGh4KHGh2cy4wQlB3Z25tMnFFS2NodjhPZzhpak9XSkQ";
    String vaultToken = "hvs.CAESIOuIuBjV-viraSq1zb6A7F5Aeg4icLbz9HyfEXTZMlaXGh4KHGh2cy5EWFNadDVOM1VtbEJkUWhIb1FpZjQ3QU4";

    //    String secretPath = vaultRoot + "/v1/kv_xyp/data/dev";
    String secretPath = vaultRoot + "/v1/secret/data/dev/paradise/keystore";

//    String passwordField = "keystore_key";
    String passwordField = "password";

//    String keyStoreField = "private_keystoe";
    String keyStoreField = "key_store";

    String trustStoreField = "trust_store";


    @Bean
    public OpenTelemetry openTelemetry() {

        /////////////////////////

        Resource resource = Resource.getDefault().toBuilder()
                .put(ResourceAttributes.SERVICE_NAME, "caller-server")
                .put(ResourceAttributes.SERVICE_VERSION, "0.1.0")
                .build();

        SdkTracerProvider sdkTracerProvider = SdkTracerProvider.builder()
                .addSpanProcessor(BatchSpanProcessor.builder(
//                LoggingSpanExporter
//                    .create(),
                        OtlpGrpcSpanExporter.builder()
                                .setEndpoint("http://127.0.0.1:5555")
                                .build()).build())
                .setResource(resource)
                .build();

//        SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder()
//            .registerMetricReader(PeriodicMetricReader.builder(LoggingMetricExporter
//            .create()).build())
//            .setResource(resource)
//            .build();

        SdkLoggerProvider sdkLoggerProvider = SdkLoggerProvider
                .builder()
                .addLogRecordProcessor(
                        BatchLogRecordProcessor.builder(
                                OtlpGrpcLogRecordExporter.builder()
                                        .setEndpoint("http://127.0.0.1:5556")
                                        .build()).build())
                .setResource(resource)
                .build();

        OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
//            .setMeterProvider(sdkMeterProvider)
                .setLoggerProvider(sdkLoggerProvider)
                .setPropagators(ContextPropagators.create(
                        TextMapPropagator.composite(
                                W3CTraceContextPropagator.getInstance(),
                                W3CBaggagePropagator.getInstance())))
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

//    @Value("${server.ssl.bundle}")
    String sslBundleKey = "secretBundle";

    private boolean isHttps() {
        return echoUrl.startsWith("https");
    }

    @Bean("echo")
    public RestTemplate getRestTemplate(RestTemplateBuilder builder,
                                        SslBundles sslBundles)
            throws Exception {

        var bundle = createSslBundle();
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

    @Value("${name.a.b}")
    String theName;

    @Bean
    public ApplicationRunner runner() {
        int aa = 0;
        int bb = 0;
        bb = 4;
        return args -> {
            try {
                System.out.println(aa);
                Assert.isTrue(aa >= 0, "");
                System.out.println(theName + " +++++++++++++++++++++++++++");
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


    SSLContext createSSLContextFromPem() throws Exception {

        val pemBytes = Files.readAllBytes(Path.of(vaultPemPath));
        System.out.println("------------------ read vault pem from " + vaultPemPath);

        // Convert PEM to X509Certificate
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        X509Certificate certificate = (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(pemBytes));

        // Create a new JKS and add the certificate
        KeyStore trustStore = KeyStore.getInstance("JKS");
        trustStore.load(null, null); // the load null/null is MUST
        trustStore.setCertificateEntry("alias", certificate);

        return new SSLContextBuilder()
                .loadTrustMaterial(trustStore, new TrustAllStrategy())
                .build();
    }

    private byte[] getPrivateKeyFromVault() throws Exception {
        System.out.println(createSSLContextFromPem() + " create ssl context from pem");
        HttpClient client = HttpClient.newBuilder()
                .sslContext(createSSLContextFromPem())
                .build();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(secretPath))
                .headers(
                        "X-Vault-Request", "true",
                        "X-Vault-Token", vaultToken)
                .timeout(Duration.ofMillis(5009))
                .build();

        val body = client.send(request, HttpResponse.BodyHandlers.ofString());
        ObjectMapper mapper = new ObjectMapper();
        val map = mapper.readValue(body.body().getBytes(), Map.class);
        System.out.println(map.get("errors"));

        System.out.println("---- map ----");
        System.out.println(map);
        Map<?, ?> m2 = (Map) map.get("data");
        Map<?, ?> m3 = (Map) m2.get("data");
        Object m4 = m3.get(keyStoreField);
        System.out.println("---- private key store ----");
        System.out.println("[" + m4 + "]");

        return Base64.getDecoder().decode(m4.toString());//.getBytes();
    }

    private byte[] getCertificateFromVault() throws Exception {
        System.out.println(createSSLContextFromPem() + " create ssl context from pem");
        HttpClient client = HttpClient.newBuilder()
                .sslContext(createSSLContextFromPem())
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(secretPath))
                .headers(
                        "X-Vault-Request", "true",
                        "X-Vault-Token", vaultToken)
                .timeout(Duration.ofMillis(5009))
                .build();


        val body = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(body.body());
        ObjectMapper mapper = new ObjectMapper();
        val map = mapper.readValue(body.body().getBytes(), Map.class);
        System.out.println(map.get("errors"));

        Map<?, ?> m2 = (Map) map.get("data");
        Map<?, ?> m3 = (Map) m2.get("data");
        Object m4 = m3.get(trustStoreField);
        System.out.println("---- trust store ----");
        System.out.println(m4);

        return Base64.getDecoder().decode(m4.toString());
    }

    private String getKeyPasswordFromVault() throws Exception {
        System.out.println(createSSLContextFromPem() + " create ssl context from pem");
        HttpClient client = HttpClient.newBuilder()
                .sslContext(createSSLContextFromPem())
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(secretPath))
                .headers(
                        "X-Vault-Request", "true",
                        "X-Vault-Token", vaultToken)
                .timeout(Duration.ofMillis(5009))
                .build();


        val body = client.send(request, HttpResponse.BodyHandlers.ofString());
        System.out.println(body.body());
        ObjectMapper mapper = new ObjectMapper();
        val map = mapper.readValue(body.body().getBytes(), Map.class);
        System.out.println(map.get("errors"));

        System.out.println("---- map ----");
        System.out.println(map);
        Map<?, ?> m2 = (Map) map.get("data");
        System.out.println("---- m2 ----");
        System.out.println(m2);
        System.out.println();
        Map m3 = (Map) m2.get("data");
        Object m4 = m3.get(passwordField);
        System.out.println("---- password ----");
        System.out.println(m4);

        return m4.toString();
    }

    private InMemoryJksStoreDetails getStoreDetails(String location, byte[] content, String password) {
        return new InMemoryJksStoreDetails(null, null, location, content, password);
    }

    SslBundle createSslBundle() throws Exception {

        val password = getKeyPasswordFromVault();

        val privateKey = getPrivateKeyFromVault();
        var keyStoreDetail = getStoreDetails("keystore", privateKey, password);

        val trustCertificate = getCertificateFromVault();
        var trustStoreDetail = getStoreDetails("trustStore", trustCertificate, password);

        var sslStoreBundle = new InMemoryJksSslStoreBundle(keyStoreDetail, trustStoreDetail);
        var sslBundle = new InMemoryPropertiesSslBundle(
                sslStoreBundle, new JksSslBundleProperties());

        return sslBundle;
    }

}
