package fr.gouv.vitamui.referential.server.service.externalreferential;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitam.access.external.client.AdminExternalClientRest;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.commons.rest.client.configuration.SSLConfiguration;
import fr.gouv.vitamui.referential.common.dto.ExternalReferentialConfigDto;
import fr.gouv.vitamui.referential.server.config.ApiReferentialApplicationProperties;
import fr.gouv.vitamui.referential.server.config.ExternalArchivingSystemClientConfig;
import fr.gouv.vitamui.referential.server.config.ExternalArchivingSystemReferenceConfig;
import fr.gouv.vitamui.referential.server.config.ExternalArchivingSystemTenantConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static fr.gouv.vitamui.referential.server.service.externalreferential.ExternalArchivingSystemConfigurationManager.LOCAL_ARCHIVING_SYSTEM_ID;
import static fr.gouv.vitamui.referential.server.service.externalreferential.ExternalArchivingSystemConfigurationManager.LOCAL_ARCHIVING_SYSTEM_NAME;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(SpringExtension.class)
class ExternalArchivingSystemConfigurationManagerTest {

    private static final String UNKNOWN_ARCHIVING_SYSTEM_ID = "unknown_id";
    private static final int SOME_TENANT_IDENTIFIER = 5;
    private static final String LOCAL_ARCHIVING_SYSTEM = "local";
    private static final String ARCHIVING_SYSTEM_ID1 = "ext_archiving_system_id1";
    private static final String ARCHIVING_SYSTEM_ID2 = "ext_archiving_system_id2";
    private static final String ARCHIVING_SYSTEM_NAME1 = "Ext 1";
    private static final String ARCHIVING_SYSTEM_NAME2 = "Ext 2";

    @Test
    public void testEmptyConfiguration() {
        ApiReferentialApplicationProperties emptyConfig = new ApiReferentialApplicationProperties()
            .setExternalArchivingSystemClients(Collections.emptyList())
            .setExternalArchivingSystemTenants(Collections.emptyList());

        ExternalArchivingSystemConfigurationManager configurationManager =
            new ExternalArchivingSystemConfigurationManager(emptyConfig);

        assertThat(configurationManager.listExternalReferentialConfig(SOME_TENANT_IDENTIFIER))
            .usingRecursiveFieldByFieldElementComparator()
            .isEqualTo(
                List.of(
                    new ExternalReferentialConfigDto()
                        .setArchivingSystemId(LOCAL_ARCHIVING_SYSTEM_ID)
                        .setName(LOCAL_ARCHIVING_SYSTEM_NAME)
                        .setTenantIds(List.of(SOME_TENANT_IDENTIFIER))
                )
            );

        assertThat(configurationManager.getClient(LOCAL_ARCHIVING_SYSTEM_ID)).isNotNull();

        assertThatThrownBy(() -> configurationManager.getClient(UNKNOWN_ARCHIVING_SYSTEM_ID)).isInstanceOf(
            IllegalArgumentException.class
        );
    }

    @Test
    public void testComplexConfiguration() {
        // Given
        ApiReferentialApplicationProperties complexConfig = new ApiReferentialApplicationProperties()
            .setExternalArchivingSystemClients(
                List.of(
                    new ExternalArchivingSystemClientConfig()
                        .setArchivingSystemId(ARCHIVING_SYSTEM_ID1)
                        .setName(ARCHIVING_SYSTEM_NAME1)
                        .setAccessExternalClient(
                            new RestClientConfiguration()
                                .setServerHost("localhost")
                                .setServerPort(1111)
                                .setSslConfiguration(
                                    new SSLConfiguration()
                                        .setKeystore(
                                            new SSLConfiguration.CertificateStoreConfiguration()
                                                .setKeyPath("ssl/test_keystore.p12")
                                                .setKeyPassword("azerty4")
                                        )
                                        .setTruststore(
                                            new SSLConfiguration.CertificateStoreConfiguration()
                                                .setKeyPath("ssl/test_truststore.jks")
                                                .setKeyPassword("azerty10")
                                        )
                                )
                        ),
                    new ExternalArchivingSystemClientConfig()
                        .setArchivingSystemId(ARCHIVING_SYSTEM_ID2)
                        .setName("Ext 2")
                        .setAccessExternalClient(
                            new RestClientConfiguration()
                                .setServerHost("localhost")
                                .setServerPort(2222)
                                .setSslConfiguration(
                                    new SSLConfiguration()
                                        .setKeystore(
                                            new SSLConfiguration.CertificateStoreConfiguration()
                                                .setKeyPath("ssl/test_keystore.p12")
                                                .setKeyPassword("azerty4")
                                        )
                                        .setTruststore(
                                            new SSLConfiguration.CertificateStoreConfiguration()
                                                .setKeyPath("ssl/test_truststore.jks")
                                                .setKeyPassword("azerty10")
                                        )
                                )
                        )
                )
            )
            .setExternalArchivingSystemTenants(
                List.of(
                    new ExternalArchivingSystemTenantConfig()
                        .setTenant(0)
                        .setExternalArchivingSystemReferences(
                            List.of(
                                new ExternalArchivingSystemReferenceConfig()
                                    .setArchivingSystemId(LOCAL_ARCHIVING_SYSTEM)
                                    .setTenantIds(Set.of(0))
                            )
                        ),
                    new ExternalArchivingSystemTenantConfig()
                        .setTenant(1)
                        .setExternalArchivingSystemReferences(
                            List.of(
                                new ExternalArchivingSystemReferenceConfig()
                                    .setArchivingSystemId(ARCHIVING_SYSTEM_ID1)
                                    .setTenantIds(Set.of(0, 2)),
                                new ExternalArchivingSystemReferenceConfig()
                                    .setArchivingSystemId(ARCHIVING_SYSTEM_ID2)
                                    .setTenantIds(Set.of(3))
                            )
                        ),
                    new ExternalArchivingSystemTenantConfig()
                        .setTenant(2)
                        .setExternalArchivingSystemReferences(
                            List.of(
                                new ExternalArchivingSystemReferenceConfig()
                                    .setArchivingSystemId(LOCAL_ARCHIVING_SYSTEM)
                                    .setTenantIds(Set.of(0, 2)),
                                new ExternalArchivingSystemReferenceConfig()
                                    .setArchivingSystemId(ARCHIVING_SYSTEM_ID1)
                                    .setTenantIds(Set.of(0, 2))
                            )
                        )
                )
            );

        // When
        ExternalArchivingSystemConfigurationManager configurationManager =
            new ExternalArchivingSystemConfigurationManager(complexConfig);

        // Then
        assertThat(configurationManager.listExternalReferentialConfig(SOME_TENANT_IDENTIFIER))
            .usingRecursiveFieldByFieldElementComparator()
            .isEqualTo(
                List.of(
                    new ExternalReferentialConfigDto()
                        .setArchivingSystemId(LOCAL_ARCHIVING_SYSTEM_ID)
                        .setName(LOCAL_ARCHIVING_SYSTEM_NAME)
                        .setTenantIds(List.of(SOME_TENANT_IDENTIFIER))
                )
            );

        assertThat(configurationManager.getClient(LOCAL_ARCHIVING_SYSTEM_ID)).isNotNull();
        AdminExternalClient extClient1 = configurationManager.getClient(ARCHIVING_SYSTEM_ID1);
        AdminExternalClient extClient2 = configurationManager.getClient(ARCHIVING_SYSTEM_ID2);
        assertThat(extClient1).isNotNull();
        assertThat(((AdminExternalClientRest) extClient1).getClientFactory().getServiceUrl()).startsWith(
            "https://localhost:1111/"
        );
        assertThat(extClient2).isNotNull();
        assertThat(((AdminExternalClientRest) extClient2).getClientFactory().getServiceUrl()).startsWith(
            "https://localhost:2222/"
        );
        assertThatThrownBy(() -> configurationManager.getClient(UNKNOWN_ARCHIVING_SYSTEM_ID)).isInstanceOf(
            IllegalArgumentException.class
        );

        assertThat(configurationManager.listExternalReferentialConfig(0))
            .usingRecursiveFieldByFieldElementComparator()
            .isEqualTo(
                List.of(
                    new ExternalReferentialConfigDto()
                        .setArchivingSystemId(LOCAL_ARCHIVING_SYSTEM_ID)
                        .setName(LOCAL_ARCHIVING_SYSTEM_NAME)
                        .setTenantIds(List.of(0))
                )
            );

        assertThat(configurationManager.listExternalReferentialConfig(1))
            .usingRecursiveFieldByFieldElementComparator()
            .isEqualTo(
                List.of(
                    new ExternalReferentialConfigDto()
                        .setArchivingSystemId(LOCAL_ARCHIVING_SYSTEM_ID)
                        .setName(LOCAL_ARCHIVING_SYSTEM_NAME)
                        .setTenantIds(List.of(1)),
                    new ExternalReferentialConfigDto()
                        .setArchivingSystemId(ARCHIVING_SYSTEM_ID1)
                        .setName(ARCHIVING_SYSTEM_NAME1)
                        .setTenantIds(List.of(0, 2)),
                    new ExternalReferentialConfigDto()
                        .setArchivingSystemId(ARCHIVING_SYSTEM_ID2)
                        .setName(ARCHIVING_SYSTEM_NAME2)
                        .setTenantIds(List.of(3))
                )
            );

        assertThat(configurationManager.listExternalReferentialConfig(2))
            .usingRecursiveFieldByFieldElementComparator()
            .isEqualTo(
                List.of(
                    new ExternalReferentialConfigDto()
                        .setArchivingSystemId(LOCAL_ARCHIVING_SYSTEM_ID)
                        .setName(LOCAL_ARCHIVING_SYSTEM_NAME)
                        .setTenantIds(List.of(0, 2)),
                    new ExternalReferentialConfigDto()
                        .setArchivingSystemId(ARCHIVING_SYSTEM_ID1)
                        .setName(ARCHIVING_SYSTEM_NAME1)
                        .setTenantIds(List.of(0, 2))
                )
            );

        assertThat(configurationManager.listExternalReferentialConfig(SOME_TENANT_IDENTIFIER))
            .usingRecursiveFieldByFieldElementComparator()
            .isEqualTo(
                List.of(
                    new ExternalReferentialConfigDto()
                        .setArchivingSystemId(LOCAL_ARCHIVING_SYSTEM_ID)
                        .setName(LOCAL_ARCHIVING_SYSTEM_NAME)
                        .setTenantIds(List.of(SOME_TENANT_IDENTIFIER))
                )
            );
    }
}
