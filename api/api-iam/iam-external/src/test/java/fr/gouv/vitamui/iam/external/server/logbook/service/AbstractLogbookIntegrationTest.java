package fr.gouv.vitamui.iam.external.server.logbook.service;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitamui.commons.logbook.config.LogbookAutoConfiguration;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import fr.gouv.vitamui.iam.external.server.config.ConverterConfig;
import fr.gouv.vitamui.iam.external.server.logbook.config.LogbookConfiguration;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import({ LogbookAutoConfiguration.class, ConverterConfig.class, LogbookConfiguration.class })
public class AbstractLogbookIntegrationTest extends AbstractMongoTests {

    @MockBean
    protected ExternalSecurityService externalSecurityService;

    @Autowired
    protected EventRepository eventRepository;

    @Autowired
    protected IamLogbookService iamLogbookService;

    @MockBean
    private AdminExternalClient adminExternalClient;
}
