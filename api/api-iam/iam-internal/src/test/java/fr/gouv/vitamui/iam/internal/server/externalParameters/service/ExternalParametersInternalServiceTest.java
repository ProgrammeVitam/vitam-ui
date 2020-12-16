package fr.gouv.vitamui.iam.internal.server.externalParameters.service;

import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.test.context.junit4.SpringRunner;

import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.mongo.dao.CustomSequenceRepository;
import fr.gouv.vitamui.commons.mongo.repository.impl.VitamUIRepositoryImpl;
import fr.gouv.vitamui.iam.internal.server.externalParameters.converter.ExternalParametersConverter;
import fr.gouv.vitamui.iam.internal.server.externalParameters.dao.ExternalParametersRepository;
import fr.gouv.vitamui.iam.internal.server.externalParameters.domain.ExternalParameters;
import fr.gouv.vitamui.iam.internal.server.group.converter.GroupConverter;
import fr.gouv.vitamui.iam.internal.server.logbook.service.AbstractLogbookIntegrationTest;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;

@RunWith(SpringRunner.class)
@EnableMongoRepositories(
	basePackageClasses =  {ExternalParametersRepository.class, CustomSequenceRepository.class }, 
	repositoryBaseClass = VitamUIRepositoryImpl.class)
public class ExternalParametersInternalServiceTest extends AbstractLogbookIntegrationTest {
	
	private ExternalParametersInternalService service;
	
    @MockBean
    private ExternalParametersRepository externalParametersRepository;
    
    @Autowired
    private CustomSequenceRepository sequenceRepository;
	
    @Autowired
    private ExternalParametersConverter externalParametersConverter;
    
    @Autowired
    private InternalSecurityService internalSecurityService;


    private static final String ID = "ID";

    @Before
    public void setup() {
        service = new ExternalParametersInternalService(
        		sequenceRepository, externalParametersRepository, externalParametersConverter, internalSecurityService);    
    }

    @Test
    public void testGetOne() {
        ExternalParameters externalParameters = new ExternalParameters();
        externalParameters.setId(ID);

    	when(externalParametersRepository.findOne(ArgumentMatchers.any(Query.class))).thenReturn(Optional.of(externalParameters));

        ExternalParametersDto res = this.service.getMyExternalParameters();
        Assert.assertNotNull("ExternalParameters should be returned.", res);
        Assert.assertTrue(res.getId().equals(ID));
    }
}
