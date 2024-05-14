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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.ItemStatus;
import fr.gouv.vitam.common.model.ProcessQuery;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.processing.ProcessDetail;
import fr.gouv.vitamui.commons.api.enums.OperationActionStatus;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.administration.VitamOperationService;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamSearchRequestDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUIProcessDetailResponseDto;
import fr.gouv.vitamui.referential.internal.server.logbookmanagement.LogbookManagementOperationInternalService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class LogbookManagementOperationInternalServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private VitamOperationService vitamOperationService;

    @InjectMocks
    private LogbookManagementOperationInternalService logbookManagementOperationInternalService;

    private DummyData dummyData;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        dummyData = new DummyData();
        logbookManagementOperationInternalService = new LogbookManagementOperationInternalService(
            objectMapper,
            vitamOperationService
        );
    }

    @Test
    public void updateOperationActionProcess_should_not_throw_VitamClientException_when_actionId_is_correct()
        throws VitamClientException, JsonProcessingException { // Given
        VitamContext vitamContext = new VitamContext(0);
        OperationActionStatus replay = OperationActionStatus.REPLAY;
        String operationId = "id";

        when(vitamOperationService.listOperationsDetails(any(VitamContext.class), any(ProcessQuery.class))).thenReturn(
            dummyData.listOperationsDetailsRequestResponse()
        );
        RequestResponse<ItemStatus> updateResponse = dummyData.updateOperationActionProcessRequestResponse();
        when(
            vitamOperationService.updateOperationActionProcess(
                any(VitamContext.class),
                any(String.class),
                any(String.class)
            )
        ).thenReturn(updateResponse);
        when(objectMapper.treeToValue(updateResponse.toJsonNode(), VitamUIProcessDetailResponseDto.class)).thenReturn(
            dummyData.vitamUIProcessDetailResponseDto()
        );

        //When //Then
        assertThatCode(() -> {
            logbookManagementOperationInternalService.updateOperationActionProcess(
                vitamContext,
                replay.toString(),
                operationId
            );
        }).doesNotThrowAnyException();
    }

    @Test
    public void updateOperationActionProcess_should_throw_VitamClientException_when_actionId_is_not_correct()
        throws VitamClientException {
        //Given
        VitamContext vitamContext = new VitamContext(0);
        String actionId = "action";
        String operationId = "id";

        when(
            vitamOperationService.updateOperationActionProcess(
                any(VitamContext.class),
                any(String.class),
                any(String.class)
            )
        ).thenReturn(dummyData.updateOperationActionProcessRequestResponse());

        //When //Then
        assertThatCode(() -> {
            logbookManagementOperationInternalService.updateOperationActionProcess(vitamContext, actionId, operationId);
        }).hasMessage("Cannot update  the operation, because the actionId given is not correct");
    }

    @Test
    public void cancelOperationProcessExecution_should_throw_VitamClientException_when_vitamclient_throw_VitamClientException()
        throws VitamClientException {
        //Given
        VitamContext vitamContext = new VitamContext(0);
        String identifier = "identifier";

        when(
            vitamOperationService.cancelOperationProcessExecution(any(VitamContext.class), any(String.class))
        ).thenThrow(new VitamClientException("Exception thrown by vitam"));

        //When //Then
        assertThatCode(() -> {
            logbookManagementOperationInternalService.cancelOperationProcessExecution(vitamContext, identifier);
        }).isInstanceOf(VitamClientException.class);
    }

    private static class DummyData {

        RequestResponse<ItemStatus> updateOperationActionProcessRequestResponse() {
            return new RequestResponse<ItemStatus>() {
                @Override
                public Response toResponse() {
                    ProcessDetail processDetail = new ProcessDetail();
                    return new RequestResponseOK().addResult(processDetail).toResponse();
                }
            }.setHttpCode(200);
        }

        RequestResponse<ProcessDetail> listOperationsDetailsRequestResponse() {
            return new RequestResponse<ProcessDetail>() {
                @Override
                public Response toResponse() {
                    return null;
                }
            }.setHttpCode(200);
        }

        VitamUIProcessDetailResponseDto vitamUIProcessDetailResponseDto() {
            VitamUIProcessDetailResponseDto vitamUIProcessDetailResponseDto = new VitamUIProcessDetailResponseDto();
            vitamUIProcessDetailResponseDto.setContext(new VitamSearchRequestDto());
            return vitamUIProcessDetailResponseDto;
        }
    }
}
