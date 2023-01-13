/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */

package fr.gouv.vitamui.referential.internal.server.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.ManagementContractModel;
import fr.gouv.vitam.common.model.logbook.LogbookOperation;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.administration.ManagementContractService;
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
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.mock;

@RunWith(org.powermock.modules.junit4.PowerMockRunner.class)
@PrepareForTest({ ServerIdentityConfiguration.class })
public class ManagementContractInternalServiceTest {

    private LogbookService logbookService;

    private ManagementContractInternalService managementContractInternalService;

    private ManagementContractService managementContractService;

    @Before
    public void setUp() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        ManagementContractConverter converter = new ManagementContractConverter();
        logbookService = mock(LogbookService.class);
        VitamUIManagementContractService vitamUIManagementContractService =
            mock(VitamUIManagementContractService.class);
        managementContractService = mock(ManagementContractService.class);
        managementContractInternalService = new ManagementContractInternalService(managementContractService,
            vitamUIManagementContractService, objectMapper, converter, logbookService);

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

        assertThatCode(() ->
            managementContractInternalService.getOne(vitamContext,identifier)
        ).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_throw_InternalServerException_when_vitamclient_400() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(managementContractService.findManagementContractById(isA(VitamContext.class), isA(String.class)))
            .andReturn(new RequestResponseOK<ManagementContractModel>().setHttpCode(400));
        EasyMock.replay(managementContractService);

        assertThatCode(() ->
            managementContractInternalService.getOne(vitamContext, identifier)
        ).doesNotThrowAnyException();
    }

    @Test
    public void getOne_should_throw_InternalServerException_when_vitamclient_throws_vitamclientexception() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(managementContractService.findManagementContractById(isA(VitamContext.class), isA(String.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(managementContractService);

        assertThatCode(() ->
            managementContractInternalService.getOne(vitamContext, identifier)
        ).isInstanceOf(InternalServerException.class);
    }

    @Test
    public void getAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);

        expect(managementContractService.findManagementContracts(vitamContext, new Select().getFinalSelect()))
            .andReturn(new RequestResponseOK<ManagementContractModel>().setHttpCode(400));
        EasyMock.replay(managementContractService);

        assertThatCode(() ->
            managementContractInternalService.getAll(vitamContext)
        ).doesNotThrowAnyException();
    }

    @Test
    public void findHistoryByIdentifier_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";
        expect(logbookService.selectOperations(isA(JsonNode.class), isA(VitamContext.class)))
            .andReturn(new RequestResponseOK<LogbookOperation>().setHttpCode(200));
        EasyMock.replay(logbookService);

        assertThatCode(() ->
            managementContractInternalService.findHistoryByIdentifier(vitamContext,identifier)
        ).doesNotThrowAnyException();
    }

    @Test
    public void findAll_should_return_ok_when_vitamclient_ok() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        JsonNode jsonQuery = new Select().getFinalSelect();

        expect(managementContractService.findManagementContracts(vitamContext, new Select().getFinalSelect()))
            .andReturn(new RequestResponseOK<ManagementContractModel>().setHttpCode(400));
        EasyMock.replay(managementContractService);

        assertThatCode(() ->
            managementContractInternalService.findAll(vitamContext, jsonQuery)
        ).doesNotThrowAnyException();
    }

}
