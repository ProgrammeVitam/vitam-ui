/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2021)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
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

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.ItemStatus;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.administration.VitamOperationService;
import fr.gouv.vitamui.referential.internal.server.logbookmanagement.LogbookManagementOperationInternalService;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.Response;


import static org.assertj.core.api.Assertions.assertThatCode;
import static org.easymock.EasyMock.*;

public class LogbookManagementOperationInternalServiceTest {

    private ObjectMapper objectMapper;
    private VitamOperationService vitamOperationService;
    private LogbookManagementOperationInternalService logbookManagementOperationInternalService;

    @Before
    public void setUp() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        objectMapper = mock(ObjectMapper.class);
        vitamOperationService = mock(VitamOperationService.class);
        logbookManagementOperationInternalService = new LogbookManagementOperationInternalService(objectMapper, vitamOperationService);
    }

    @Test
    public void updateOperationActionProcess_should_not_throw_VitamClientException_when_actionId_is_correct() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String actionId = "REPLAY";
        String operationId = "id";

        expect(vitamOperationService.updateOperationActionProcess(isA(VitamContext.class),isA(String.class), isA(String.class)))
            .andReturn(new RequestResponse<ItemStatus>() {
                @Override
                public Response toResponse() {
                    return null;
                }
            }.setHttpCode(200));
        EasyMock.replay(vitamOperationService);

        assertThatCode(() -> {
            logbookManagementOperationInternalService.updateOperationActionProcess(vitamContext, actionId, operationId);
        }).doesNotThrowAnyException();
    }

    @Test
    public void updateOperationActionProcess_should_throw_VitamClientException_when_actionId_is_not_correct() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String actionId = "action";
        String operationId = "id";

        expect(vitamOperationService.updateOperationActionProcess(isA(VitamContext.class),isA(String.class), isA(String.class)))
            .andReturn(new RequestResponse<ItemStatus>() {
                @Override
                public Response toResponse() {
                    return null;
                }
            }.setHttpCode(200));

        EasyMock.replay(vitamOperationService);

        assertThatCode(() -> {
            logbookManagementOperationInternalService.updateOperationActionProcess(vitamContext, actionId, operationId);
        }).hasMessage("Cannot update  the operation, because the actionId given is not correct");
    }

    @Test
    public void cancelOperationProcessExecution_should_throw_VitamClientException_when_vitamclient_throw_VitamClientException() throws VitamClientException {
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        expect(vitamOperationService.cancelOperationProcessExecution(isA(VitamContext.class), isA(String.class)))
            .andThrow(new VitamClientException("Exception thrown by vitam"));
        EasyMock.replay(vitamOperationService);

        assertThatCode(() -> {
            logbookManagementOperationInternalService.cancelOperationProcessExecution(vitamContext, identifier);
        }).isInstanceOf(VitamClientException.class);
    }
}
