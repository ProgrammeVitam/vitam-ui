package fr.gouv.vitamui.iam.server.logbook.service;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitamui.commons.logbook.config.LogbookAutoConfiguration;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.config.ConverterConfig;
import fr.gouv.vitamui.iam.server.logbook.config.LogbookConfiguration;
import fr.gouv.vitamui.iam.server.provisioning.config.ProvisioningClientConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import(
    {
        LogbookAutoConfiguration.class,
        ConverterConfig.class,
        LogbookConfiguration.class,
        AbstractLogbookIntegrationTest.TestProvisioningConfig.class,
    }
)
public class AbstractLogbookIntegrationTest extends AbstractMongoTests {

    @TestConfiguration
    public static class TestProvisioningConfig {

        @Bean
        public ProvisioningClientConfiguration provisioningClientConfiguration() {
            return new ProvisioningClientConfiguration();
        }
    }

    @MockitoBean
    protected SecurityService securityService;

    @Autowired
    protected EventRepository eventRepository;

    @Autowired
    protected IamLogbookService iamLogbookService;

    @MockitoBean
    private AdminExternalClient adminExternalClient;
}
