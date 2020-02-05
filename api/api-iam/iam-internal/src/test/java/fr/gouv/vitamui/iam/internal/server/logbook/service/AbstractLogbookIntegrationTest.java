package fr.gouv.vitamui.iam.internal.server.logbook.service;

import org.junit.BeforeClass;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import fr.gouv.vitam.access.external.client.AdminExternalClient;
import fr.gouv.vitamui.commons.logbook.config.LogbookAutoConfiguration;
import fr.gouv.vitamui.commons.logbook.dao.EventRepository;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.commons.test.utils.AbstractServerIdentityBuilder;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.iam.internal.server.TestMongoConfig;
import fr.gouv.vitamui.iam.internal.server.config.ConverterConfig;
import fr.gouv.vitamui.iam.internal.server.logbook.config.LogbookConfiguration;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;

@Import({ TestMongoConfig.class, LogbookAutoConfiguration.class, ConverterConfig.class, LogbookConfiguration.class })
@EnableMongoRepositories(basePackageClasses = { EventRepository.class, }, repositoryBaseClass = VitamUIRepositoryImpl.class)
public class AbstractLogbookIntegrationTest extends AbstractServerIdentityBuilder {

    @MockBean
    protected InternalSecurityService internalSecurityService;

    @Autowired
    protected EventRepository eventRepository;

    @Autowired
    protected IamLogbookService iamLogbookService;

    @MockBean
    private AdminExternalClient adminExternalClient;

    @BeforeClass
    public static void beforeClass() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

}
