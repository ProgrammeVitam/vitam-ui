package fr.gouv.vitamui.iam.server.logbook.service;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitamui.commons.logbook.config.LogbookAutoConfiguration;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.test.AbstractMongoTests;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.config.ConverterConfig;
import fr.gouv.vitamui.iam.server.logbook.config.LogbookConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@Import({ LogbookAutoConfiguration.class, ConverterConfig.class, LogbookConfiguration.class })
public class AbstractLogbookIntegrationTest extends AbstractMongoTests {

    @MockitoBean
    protected SecurityService securityService;

    @Autowired
    protected EventRepository eventRepository;

    @Autowired
    protected IamLogbookService iamLogbookService;

    @MockitoBean
    private AdminExternalClient adminExternalClient;
}
