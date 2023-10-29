package org.xyp.demo.api.secret.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.config.ConfigDataEnvironmentPostProcessor;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.cloud.bootstrap.BootstrapApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

import static org.xyp.demo.api.secret.config.vault.VaultKeyStoreLoader.VAULT_TOKEN;

@Slf4j
public class EnvProcessor implements EnvironmentPostProcessor, Ordered {

    private final String prefix = "secret.vault.";
    private final String suffix = "yml";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment,
                                       SpringApplication application) {
        var configFile = prefix + suffix;
        ClassPathResource resource = new ClassPathResource(configFile);

        val activeProfiles = environment.getProperty("spring.profiles.active");
        if (StringUtils.hasText(activeProfiles)) {
            var actives = environment.getProperty("spring.profiles.active").split(",");
            val profileConfig = Stream.of(actives)
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .map(p -> prefix + p + "." + suffix)
                    .map(ClassPathResource::new)
                    .filter(Resource::exists)
                    .toList();
            if (!profileConfig.isEmpty()) {
                resource = profileConfig.get(profileConfig.size() - 1);
            }
        }


        List<PropertySource<?>> rootLoaded = load(resource.getFilename(), resource);

        String vaultToken = System.getenv(VAULT_TOKEN);
        if (!StringUtils.hasText(vaultToken)) {
            log.warn("${vault_token} not provided when starting up, so skip load key store from vault");
            return;
        }

        Map<String, String> sourceMap = new HashMap<>();

        rootLoaded.stream().findFirst().ifPresent(ps -> {
            processVariableFromVault(ps, vaultToken, sourceMap);
        });

        PropertySource<?> ppSource = new OriginTrackedMapPropertySource("secret-vault", sourceMap, false);
        environment.getPropertySources().addLast(ppSource);
    }

    private void processVariableFromVault(PropertySource<?> ps, String vaultToken, Map<String, String> sourceMap) {
        val url = Objects.requireNonNull(ps.getProperty("vault.url")).toString();
        val caPath = Objects.requireNonNull(ps.getProperty("vault.ca-path")).toString();

        int eIdx = 0;
        var entryPrefix = "vault.entries[" + eIdx + "]";
        var sPathObj = ps.getProperty(entryPrefix + ".secret-path");
        try {
            while (null != sPathObj) {
                val sPath = sPathObj.toString();

                VaultClient client = new VaultClient(url, vaultToken, caPath);
                val dataString = client.read(sPath);
                val data = getDataSection(dataString);

                putEnvironment(ps, sourceMap, data, entryPrefix);

                eIdx++;
                entryPrefix = "vault.entries[" + eIdx + "]";
                sPathObj = ps.getProperty(entryPrefix + ".secret-path");
            }
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static void putEnvironment(
            PropertySource<?> ps, Map<String, String> sourceMap,
            Map<String, ?> data, String entryPrefix) {
        int kIdx = 0;
        var kFieldObj = ps.getProperty(entryPrefix + ".keys[" + kIdx + "].field");
        var kValueObj = ps.getProperty(entryPrefix + ".keys[" + kIdx + "].value");
        while (null != kFieldObj && null != kValueObj) {
            val kField = kFieldObj.toString();
            val kValue = kValueObj.toString();

            Optional.of(data.get(kField)).ifPresent(v ->
                    sourceMap.put(kValue, v.toString()));

            kIdx++;
            kFieldObj = ps.getProperty(entryPrefix + ".keys[" + kIdx + "].field");
            kValueObj = ps.getProperty(entryPrefix + ".keys[" + kIdx + "].value");
        }
    }

    @Override
    public int getOrder() {
        return ConfigDataEnvironmentPostProcessor.ORDER - 1;
    }

    private List<PropertySource<?>> load(String name, Resource resource) {
        try {
            return new YamlPropertySourceLoader().load(name, resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, ?> getDataSection(String dataString) throws JsonProcessingException {

        ObjectMapper mapper = new ObjectMapper();
        val map = mapper.readValue(dataString, Map.class);
        if (map.get("errors") != null) {
            throw new IllegalStateException("load vault error " + map.get("errors"));
        }
        return (Map<String, ?>) ((Map<String, ?>) map.get("data")).get("data");
    }

}
