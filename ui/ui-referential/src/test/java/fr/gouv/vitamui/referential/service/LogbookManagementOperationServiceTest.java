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

package fr.gouv.vitamui.referential.service;

import fr.gouv.vitam.common.model.ProcessQuery;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUIProcessDetailResponseDto;
import fr.gouv.vitamui.referential.external.client.LogbookManagementOperationExternalRestClient;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
public class LogbookManagementOperationServiceTest {

    @Mock
    private LogbookManagementOperationExternalRestClient client;
    @Mock
    private CommonService commonService;

    private LogbookManagementOperationService service;


    @Before
    public void setUp() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        service = new LogbookManagementOperationService(client, commonService);
    }

    @Test
    public void list_operations_details_should_call_appropriate_rest_client_once() {
        // Given
        Mockito.when(client.searchOperationsDetails(isNull(), any(ProcessQuery.class)))
            .thenReturn(new ResponseEntity<>(new VitamUIProcessDetailResponseDto(), HttpStatus.OK));

        // When
        service.searchOperationsDetails(null, new ProcessQuery());

        // Then
        verify(client, Mockito.times(1))
            .searchOperationsDetails(isNull(), any(ProcessQuery.class));

    }
}
