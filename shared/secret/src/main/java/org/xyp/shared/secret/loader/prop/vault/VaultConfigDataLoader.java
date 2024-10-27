package org.xyp.shared.secret.loader.prop.vault;

import lombok.val;
import org.apache.commons.logging.Log;
import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.logging.DeferredLogFactory;
import org.springframework.core.env.PropertySource;
import org.xyp.shared.secret.vault.VaultClient;
import org.xyp.shared.secret.vault.VaultClientProperties;
import org.xyp.shared.secret.vault.VaultClientRestImpl;
import org.xyp.shared.secret.vault.VaultKvData;
import org.xyp.shared.secret.vault.VaultKvResponse;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class VaultConfigDataLoader implements ConfigDataLoader<VaultConfigLocation> {

    private final Log log;

    public VaultConfigDataLoader(DeferredLogFactory logFactory) {
        log = logFactory.getLog(VaultConfigDataLoader.class);
    }


    @Override
    public boolean isLoadable(ConfigDataLoaderContext context, VaultConfigLocation resource) {
        return ConfigDataLoader.super.isLoadable(context, resource);
    }

    @Override
    public ConfigData load(ConfigDataLoaderContext context, VaultConfigLocation resource) throws IOException, ConfigDataResourceNotFoundException {
        log.debug("loading properties from tcg vault ");

        ConfigurableBootstrapContext bootstrap = context.getBootstrapContext();

        VaultClientProperties vaultClientProperties = bootstrap.get(VaultClientProperties.class);

        if (log.isDebugEnabled()) {
            log.debug("vault backend engine: " + vaultClientProperties.getHost());
        }

        VaultClient vaultClient = new VaultClientRestImpl(vaultClientProperties);

        val respData = vaultClient.readKvFromPath(
            resource.getBackend(),
            resource.getProfile() +
                "/" +
                resource.getPathWithoutProfile()
        );

        val prefix = resource.getPathWithoutProfile().replace("/", ".") + ".";

        val sourceList = Optional.ofNullable(respData)
            .map(VaultKvResponse::getData)
            .map(VaultKvData::getData)
            .map(map ->
                map.entrySet().stream()
                    .map(es -> new AbstractMap.SimpleImmutableEntry<>(prefix + es.getKey(), es.getValue()))
                    .collect(Collectors.toMap(
                        AbstractMap.SimpleImmutableEntry::getKey,
                        AbstractMap.SimpleImmutableEntry::getValue))
            )
            .map(map -> new VaultPropertySource(prefix, map))
            .map(List::of)
            .orElseGet(List::of);

        if (log.isDebugEnabled()) {
            log.debug("load " + sourceList.stream().mapToInt(i -> i.map.size()).sum() + " properties from " + resource.getPathWithoutProfile());
        }
        return new ConfigData(sourceList);
    }

    public static class VaultPropertySource extends PropertySource<Map<String, String>> {

        private final Map<String, String> map = new HashMap<>();
        private final String sName;

        public VaultPropertySource(String name, Map<String, String> source) {
            super(name);
            this.sName = name;
            this.map.putAll(source);
        }

        @Override
        public Object getProperty(String name) {
            return map.get(name);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            if (!super.equals(o)) return false;
            VaultPropertySource that = (VaultPropertySource) o;
            return Objects.equals(map, that.map) && Objects.equals(sName, that.sName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), map, sName);
        }
    }
}
