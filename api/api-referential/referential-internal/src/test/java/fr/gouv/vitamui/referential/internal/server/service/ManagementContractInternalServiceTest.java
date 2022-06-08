package fr.gouv.vitamui.referential.internal.server.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.ManagementContractModel;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.ManagementContractService;
import fr.gouv.vitamui.referential.common.dto.ManagementContractDto;
import fr.gouv.vitamui.referential.common.service.VitamUIManagementContractService;
import fr.gouv.vitamui.referential.internal.server.managementcontract.ManagementContractConverter;
import fr.gouv.vitamui.referential.internal.server.managementcontract.ManagementContractInternalService;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.easymock.EasyMock.*;

@RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@PrepareForTest({ ServerIdentityConfiguration.class })
public class ManagementContractInternalServiceTest {

    private ObjectMapper objectMapper;

    private ManagementContractConverter converter;

    private LogbookService logbookService;

    private VitamUIManagementContractService vitamUIManagementContractService;

    private ManagementContractInternalService managementContractInternalService;

    private ManagementContractService managementContractService;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        converter = new ManagementContractConverter();
        logbookService = mock(LogbookService.class);
        vitamUIManagementContractService = mock(VitamUIManagementContractService.class);
        managementContractService = mock(ManagementContractService.class);
        managementContractInternalService = new ManagementContractInternalService(managementContractService,vitamUIManagementContractService, objectMapper, converter, logbookService);

        // Mock server identity for Logs when not using spring
        PowerMock.suppress(PowerMock.constructor(ServerIdentityConfiguration.class));
        PowerMock.mockStatic(ServerIdentityConfiguration.class);
        ServerIdentityConfiguration serverIdentityConfigurationMock = PowerMock.createMock(ServerIdentityConfiguration.class);
        expect(ServerIdentityConfiguration.getInstance()).andReturn(serverIdentityConfigurationMock).anyTimes();
        expect(serverIdentityConfigurationMock.getLoggerMessagePrepend()).andReturn("LOG TESTS FileFormatInternalServiceTest - ").anyTimes();
        PowerMock.replay(ServerIdentityConfiguration.class);
        PowerMock.replay(serverIdentityConfigurationMock);

    }

    @Test
    public void getOne_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";
        expect(managementContractService.findManagementContractById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<ManagementContractModel>().setHttpCode(200));
        EasyMock.replay(managementContractService);

        assertThatCode(() -> {
            managementContractInternalService.getOne(vitamContext,identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_throw_InternalServerException_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(managementContractService.findManagementContractById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<ManagementContractModel>().setHttpCode(200));
        EasyMock.replay(managementContractService);

        assertThatCode(() -> {
            managementContractInternalService.getOne(vitamContext, identifier);
        }).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_throw_InternalServerException_when_vitamclient_throws_vitamclientexception() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(managementContractService.findManagementContractById(isA(VitamContext.class), isA(String.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(managementContractService);

        assertThatCode(() -> {
            managementContractInternalService.getOne(vitamContext, identifier);
        }).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(managementContractService.findManagementContracts(vitamContext, new Select().getFinalSelect()))
            .andReturn(new RequestResponseOK<ManagementContractModel>().setHttpCode(400));
        EasyMock.replay(managementContractService);

        assertThatCode(() -> {
            managementContractInternalService.getAll(vitamContext);
        }).doesNotThrowAnyException();
    }


}
