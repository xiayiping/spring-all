package org.xyp.shared.secret.loader.prop.vault;

import lombok.val;
import org.springframework.boot.context.config.*;
import org.xyp.shared.secret.vault.VaultClientProperties;

import java.util.Collections;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * <br/> vault-xyp://{engine}:{spring-profile}/path/to/kv
 */
public class VaultConfigDataLocationResolver implements ConfigDataLocationResolver<VaultConfigLocation> {

    @Override
    public boolean isResolvable(
        ConfigDataLocationResolverContext context,
        ConfigDataLocation location
    ) {
        return location.getValue().startsWith(VaultConfigLocation.PREFIX);
    }

    @Override
    public List<VaultConfigLocation> resolve(
        ConfigDataLocationResolverContext context,
        ConfigDataLocation location
    ) throws ConfigDataLocationNotFoundException, ConfigDataResourceNotFoundException {
        return List.of();
    }

    @Override
    public List<VaultConfigLocation> resolveProfileSpecific(
        ConfigDataLocationResolverContext context,
        ConfigDataLocation location,
        Profiles profiles
    ) throws ConfigDataLocationNotFoundException {

        if (!location.getValue().startsWith(VaultConfigLocation.PREFIX)) {
            return Collections.emptyList();
        }

        val bootContext = context.getBootstrapContext();

        bootContext.registerIfAbsent(VaultClientProperties.class,
            ignore -> context.getBinder().bindOrCreate(
                VaultClientProperties.PREFIX, VaultClientProperties.class));

        val tail = location.getValue().replace(
            VaultConfigLocation.PREFIX, ""
        );
        val engineSplitIdx = tail.indexOf(":");
        val engine = tail.substring(0, engineSplitIdx);
        val path = tail.substring(engineSplitIdx + 1);

        return StreamSupport.stream(profiles.spliterator(), false)
            .map(profile -> new VaultConfigLocation(
                engine,
                profile,
                path
            ))
            .toList();
    }

}
