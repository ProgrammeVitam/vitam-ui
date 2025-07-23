package fr.gouv.vitamui.referential.server.service.externalreferential;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClientFactory;
import fr.gouv.vitam.common.external.client.configuration.SSLConfiguration;
import fr.gouv.vitam.common.external.client.configuration.SSLKey;
import fr.gouv.vitam.common.external.client.configuration.SecureClientConfiguration;
import fr.gouv.vitam.common.external.client.configuration.SecureClientConfigurationImpl;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.referential.common.dto.ExternalReferentialConfigDto;
import fr.gouv.vitamui.referential.server.config.ApiReferentialApplicationProperties;
import fr.gouv.vitamui.referential.server.config.ExternalArchivingSystemClientConfig;
import fr.gouv.vitamui.referential.server.config.ExternalArchivingSystemReferenceConfig;
import fr.gouv.vitamui.referential.server.config.ExternalArchivingSystemTenantConfig;
import org.apache.commons.collections4.map.LazyMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class ExternalArchivingSystemConfigurationManager {

    public static final String LOCAL_ARCHIVING_SYSTEM_ID = "local";
    public static final String LOCAL_ARCHIVING_SYSTEM_NAME = "Local EAS";

    private final Map<String, AdminExternalClientFactory> adminExternalClientFactories;
    private final Map<Integer, Map<String, ExternalReferentialConfigDto>> configMapByTenantAndArchivingSystemId;

    @Autowired
    public ExternalArchivingSystemConfigurationManager(ApiReferentialApplicationProperties referentialConfig) {
        List<ExternalArchivingSystemClientConfig> clientConfig = referentialConfig.getExternalArchivingSystemClients();
        List<ExternalArchivingSystemTenantConfig> tenantConfig = referentialConfig.getExternalArchivingSystemTenants();
        validateClientConfiguration(clientConfig);
        Set<String> externalArchivingSystems = clientConfig
            .stream()
            .map(ExternalArchivingSystemClientConfig::getArchivingSystemId)
            .collect(Collectors.toSet());
        validateTenantConfig(tenantConfig, externalArchivingSystems);
        this.configMapByTenantAndArchivingSystemId = initConfig(clientConfig, tenantConfig);
        this.adminExternalClientFactories = initAdminExternalClients(clientConfig);
    }

    private void validateClientConfiguration(List<ExternalArchivingSystemClientConfig> clientConfig) {
        List<String> archivingSystemIds = clientConfig
            .stream()
            .map(ExternalArchivingSystemClientConfig::getArchivingSystemId)
            .toList();
        findDuplicateEntry(archivingSystemIds).ifPresent(duplicateArchivingSystemId -> {
            throw new IllegalArgumentException(
                "Duplicate archiving system id found '" + duplicateArchivingSystemId + "'"
            );
        });

        if (archivingSystemIds.contains(LOCAL_ARCHIVING_SYSTEM_ID)) {
            throw new IllegalArgumentException(
                "Invalid external archiving system clients configuration. Cannot defined reserved 'local' archiving system id."
            );
        }
    }

    private void validateTenantConfig(
        List<ExternalArchivingSystemTenantConfig> tenantConfig,
        Set<String> externalArchivingSystems
    ) {
        findDuplicateEntry(
            tenantConfig.stream().map(ExternalArchivingSystemTenantConfig::getTenant).toList()
        ).ifPresent(tenant -> {
            throw new IllegalArgumentException(
                "Invalid external archiving system tenant configuration. Duplicate tenant found '" + tenant + "'"
            );
        });

        for (ExternalArchivingSystemTenantConfig externalArchivingSystemTenant : tenantConfig) {
            findDuplicateEntry(
                externalArchivingSystemTenant
                    .getExternalArchivingSystemReferences()
                    .stream()
                    .map(ExternalArchivingSystemReferenceConfig::getArchivingSystemId)
                    .toList()
            ).ifPresent(archivingSystemId -> {
                throw new IllegalArgumentException(
                    "Invalid external archiving system tenant configuration. Duplicate archiving system id declaration '" +
                    archivingSystemId +
                    "' for tenant " +
                    externalArchivingSystemTenant.getTenant()
                );
            });

            for (ExternalArchivingSystemReferenceConfig externalArchivingSystemReference : externalArchivingSystemTenant.getExternalArchivingSystemReferences()) {
                if (
                    !LOCAL_ARCHIVING_SYSTEM_ID.equals(externalArchivingSystemReference.getArchivingSystemId()) &&
                    !externalArchivingSystems.contains(externalArchivingSystemReference.getArchivingSystemId())
                ) {
                    throw new IllegalArgumentException(
                        "Invalid external archiving system id '" +
                        externalArchivingSystemReference.getArchivingSystemId() +
                        "'"
                    );
                }
            }
        }
    }

    private static <T> Optional<T> findDuplicateEntry(Collection<T> collection) {
        Set<T> found = new HashSet<>();
        return collection.stream().filter(e -> !found.add(e)).findFirst();
    }

    private Map<Integer, Map<String, ExternalReferentialConfigDto>> initConfig(
        List<ExternalArchivingSystemClientConfig> clientConfig,
        List<ExternalArchivingSystemTenantConfig> tenantConfig
    ) {
        Map<String, String> archivingSystemNameMap = mapArchivingSystemNameById(clientConfig);

        Map<Integer, Map<String, ExternalReferentialConfigDto>> configMap = tenantConfig
            .stream()
            .collect(
                Collectors.toMap(
                    ExternalArchivingSystemTenantConfig::getTenant,
                    tenantConf -> initTenantConfig(tenantConf, archivingSystemNameMap)
                )
            );

        return Collections.synchronizedMap(
            LazyMap.lazyMap(
                configMap,
                // Add default/implicit <"local" system Id / tenantId> config entry, if missing explicit config for tenant
                tenantId ->
                    Map.of(
                        LOCAL_ARCHIVING_SYSTEM_ID,
                        new ExternalReferentialConfigDto()
                            .setArchivingSystemId(LOCAL_ARCHIVING_SYSTEM_ID)
                            .setName(LOCAL_ARCHIVING_SYSTEM_NAME)
                            .setTenantIds(List.of(tenantId))
                    )
            )
        );
    }

    private Map<String, String> mapArchivingSystemNameById(List<ExternalArchivingSystemClientConfig> clientConfig) {
        Map<String, String> archivingSystemNameMap = new HashMap<>();
        archivingSystemNameMap.put(LOCAL_ARCHIVING_SYSTEM_ID, LOCAL_ARCHIVING_SYSTEM_NAME);
        for (ExternalArchivingSystemClientConfig externalArchivingSystemClient : clientConfig) {
            archivingSystemNameMap.put(
                externalArchivingSystemClient.getArchivingSystemId(),
                externalArchivingSystemClient.getName()
            );
        }
        return archivingSystemNameMap;
    }

    private Map<String, ExternalReferentialConfigDto> initTenantConfig(
        ExternalArchivingSystemTenantConfig tenantConfig,
        Map<String, String> archivingSystemNameMap
    ) {
        Map<String, ExternalReferentialConfigDto> configMap = new LinkedHashMap<>();

        if (
            tenantConfig
                .getExternalArchivingSystemReferences()
                .stream()
                .noneMatch(ref -> LOCAL_ARCHIVING_SYSTEM_ID.equals(ref.getArchivingSystemId()))
        ) {
            // Add default/implicit <"local" system Id / tenantId> entry, if missing
            configMap.put(
                LOCAL_ARCHIVING_SYSTEM_ID,
                new ExternalReferentialConfigDto()
                    .setArchivingSystemId(LOCAL_ARCHIVING_SYSTEM_ID)
                    .setName(LOCAL_ARCHIVING_SYSTEM_NAME)
                    .setTenantIds(List.of(tenantConfig.getTenant()))
            );
        }

        for (ExternalArchivingSystemReferenceConfig referenceConfig : tenantConfig.getExternalArchivingSystemReferences()) {
            List<Integer> tenantIds = computeTenantIds(
                referenceConfig.getArchivingSystemId(),
                tenantConfig.getTenant(),
                referenceConfig.getTenantIds()
            );
            configMap.put(
                referenceConfig.getArchivingSystemId(),
                new ExternalReferentialConfigDto()
                    .setArchivingSystemId(referenceConfig.getArchivingSystemId())
                    .setName(archivingSystemNameMap.get(referenceConfig.getArchivingSystemId()))
                    .setTenantIds(tenantIds)
            );
        }

        return configMap;
    }

    private List<Integer> computeTenantIds(
        String archivingSystemId,
        int tenantId,
        Collection<Integer> configuredTenantIds
    ) {
        // Ensures tenants are sorted
        SortedSet<Integer> sortedTenantIds = new TreeSet<>(configuredTenantIds);
        // Add tenantId as an implicit tenant for "local" archiving system.
        if (LOCAL_ARCHIVING_SYSTEM_ID.equals(archivingSystemId)) {
            sortedTenantIds.add(tenantId);
        }
        return new ArrayList<>(sortedTenantIds);
    }

    private Map<String, AdminExternalClientFactory> initAdminExternalClients(
        List<ExternalArchivingSystemClientConfig> clientConfig
    ) {
        Map<String, AdminExternalClientFactory> adminExternalClientFactories = new HashMap<>();

        // Add default AdminExternalClientFactory instance as "local"
        adminExternalClientFactories.put(LOCAL_ARCHIVING_SYSTEM_ID, AdminExternalClientFactory.getInstance());

        for (ExternalArchivingSystemClientConfig externalArchivingSystemClient : clientConfig) {
            RestClientConfiguration accessExternalConf = externalArchivingSystemClient.getAccessExternalClient();
            SecureClientConfiguration secureClientConfiguration = getSecureClientConfiguration(accessExternalConf);

            adminExternalClientFactories.put(
                externalArchivingSystemClient.getArchivingSystemId(),
                new AdminExternalClientFactory(secureClientConfiguration)
            );
        }
        return adminExternalClientFactories;
    }

    private SecureClientConfiguration getSecureClientConfiguration(RestClientConfiguration accessExternalConf) {
        return new SecureClientConfigurationImpl(
            accessExternalConf.getServerHost(),
            accessExternalConf.getServerPort(),
            true,
            new SSLConfiguration(
                List.of(
                    new SSLKey(
                        accessExternalConf.getSslConfiguration().getKeystore().getKeyPath(),
                        accessExternalConf.getSslConfiguration().getKeystore().getKeyPassword()
                    )
                ),
                List.of(
                    new SSLKey(
                        accessExternalConf.getSslConfiguration().getTruststore().getKeyPath(),
                        accessExternalConf.getSslConfiguration().getTruststore().getKeyPassword()
                    )
                )
            ),
            accessExternalConf.getSslConfiguration().isHostnameVerification()
        );
    }

    public void validateExternalReferential(
        int tenantIdentifier,
        String archivingSystemId,
        Integer targetTenantIdentifier
    ) {
        Map<String, ExternalReferentialConfigDto> tenantConfigMap =
            this.configMapByTenantAndArchivingSystemId.get(tenantIdentifier);
        if (!tenantConfigMap.containsKey(archivingSystemId)) {
            throw new BadRequestException("No such external archiving system id '" + archivingSystemId + "'");
        }
        if (!tenantConfigMap.get(archivingSystemId).getTenantIds().contains(targetTenantIdentifier)) {
            throw new BadRequestException(
                "No such tenant " + targetTenantIdentifier + " for archiving system id '" + archivingSystemId + "'"
            );
        }
    }

    public Collection<ExternalReferentialConfigDto> listExternalReferentialConfig(int tenantIdentifier) {
        return this.configMapByTenantAndArchivingSystemId.get(tenantIdentifier).values();
    }

    public AdminExternalClient getClient(String archivingSystemId) {
        if (!adminExternalClientFactories.containsKey(archivingSystemId)) {
            throw new IllegalArgumentException("Invalid archivingSystemId '" + archivingSystemId + "'");
        }
        return this.adminExternalClientFactories.get(archivingSystemId).getClient();
    }
}
