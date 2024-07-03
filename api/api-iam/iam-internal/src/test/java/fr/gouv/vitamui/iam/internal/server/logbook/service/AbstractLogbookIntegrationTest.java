package fr.gouv.vitamui.iam.internal.server.logbook.service;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitamui.commons.logbook.config.LogbookAutoConfiguration;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import fr.gouv.vitamui.iam.internal.server.config.ConverterConfig;
import fr.gouv.vitamui.iam.internal.server.logbook.config.LogbookConfiguration;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

@Import({ LogbookAutoConfiguration.class, ConverterConfig.class, LogbookConfiguration.class })
public class AbstractLogbookIntegrationTest extends AbstractMongoTests {

    @MockBean
    protected InternalSecurityService internalSecurityService;

    @Autowired
    protected EventRepository eventRepository;

    @Autowired
    protected IamLogbookService iamLogbookService;

    @MockBean
    private AdminExternalClient adminExternalClient;
}
