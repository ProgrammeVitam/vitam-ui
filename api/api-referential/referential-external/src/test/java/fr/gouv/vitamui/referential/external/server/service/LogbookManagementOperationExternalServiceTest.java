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

package fr.gouv.vitamui.referential.external.server.service;

import fr.gouv.vitam.common.model.ProcessQuery;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.vitam.api.dto.ProcessDetailDto;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import fr.gouv.vitamui.referential.internal.client.LogbookManagementOperationInternalRestClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class LogbookManagementOperationExternalServiceTest extends ExternalServiceTest {

    @Mock
    private LogbookManagementOperationInternalRestClient logbookManagementOperationInternalRestClient;

    @Mock
    private ExternalSecurityService externalSecurityService;

    private LogbookManagementOperationExternalService logbookManagementOperationExternalService;

    @Before
    public void init() {
        final String userCustomerId = "customerIdAllowed";
        mockSecurityContext(externalSecurityService, userCustomerId, 10);
        logbookManagementOperationExternalService = new LogbookManagementOperationExternalService(
            externalSecurityService,
            logbookManagementOperationInternalRestClient
        );
    }

    @Test
    public void list_operations_details_should_call_appropriate_rest_client_once() {
        // Given
        when(
            logbookManagementOperationInternalRestClient.searchOperationsDetails(
                any(InternalHttpContext.class),
                any(ProcessQuery.class)
            )
        ).thenReturn(new ProcessDetailDto());

        // When
        logbookManagementOperationExternalService.searchOperationsDetails(new ProcessQuery());

        // Then
        verify(logbookManagementOperationInternalRestClient, Mockito.times(1)).searchOperationsDetails(
            any(InternalHttpContext.class),
            any(ProcessQuery.class)
        );
    }

    @Test
    public void cancelOperationProcessExecution_should_call_appropriate_rest_client_once() {
        // Given
        when(
            logbookManagementOperationInternalRestClient.cancelOperationProcessExecution(
                any(InternalHttpContext.class),
                any(String.class)
            )
        ).thenReturn(new ProcessDetailDto());

        // When
        logbookManagementOperationExternalService.cancelOperationProcessExecution(new String());

        // Then
        verify(logbookManagementOperationInternalRestClient, Mockito.times(1)).cancelOperationProcessExecution(
            any(InternalHttpContext.class),
            any(String.class)
        );
    }

    @Test
    public void updateOperationActionProcess_should_call_appropriate_rest_client_once() {
        // Given
        when(
            logbookManagementOperationInternalRestClient.updateOperationActionProcess(
                any(InternalHttpContext.class),
                any(String.class),
                any(String.class)
            )
        ).thenReturn(new ProcessDetailDto());

        // When
        logbookManagementOperationExternalService.updateOperationActionProcess(new String(), new String());

        // Then
        verify(logbookManagementOperationInternalRestClient, Mockito.times(1)).updateOperationActionProcess(
            any(InternalHttpContext.class),
            any(String.class),
            any(String.class)
        );
    }
}
