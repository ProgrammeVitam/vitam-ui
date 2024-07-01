/*
 *
 *  * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *  *
 *  * contact.vitam@culture.gouv.fr
 *  *
 *  * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 *  * high volumetry securely and efficiently.
 *  *
 *  * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 *  * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 *  * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *  *
 *  * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 *  * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 *  * successive licensors have only limited liability.
 *  *
 *  * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 *  * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 *  * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 *  * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 *  * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 *  * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *  *
 *  * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 *  * accept its terms.
 *
 */

package fr.gouv.vitamui.collect.service;

import fr.gouv.vitamui.collect.common.dto.CollectProjectDto;
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.collect.external.client.CollectExternalRestClient;
import fr.gouv.vitamui.collect.external.client.CollectStreamingExternalRestClient;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class ProjectServiceTest {

    private ProjectService projectService;

    @Mock
    private CollectExternalRestClient collectExternalRestClient;

    @Mock
    private CollectStreamingExternalRestClient collectStreamingExternalRestClient;

    @Mock
    private CommonService commonService;

    private static final String PROJECT_ID = "projectID";
    private static final String TRANSACTION_ID = "transactionId";

    @Before
    public void init() {
        projectService = new ProjectService(
            commonService,
            collectExternalRestClient,
            collectStreamingExternalRestClient
        );
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    public void shouldCreateProjectWithSuccess() {
        // Given
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        when(collectExternalRestClient.create(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(
            new CollectProjectDto()
        );

        // When
        CollectProjectDto response = projectService.createProject(context, new CollectProjectDto());

        // Then
        verify(collectExternalRestClient, times(1)).create(ArgumentMatchers.any(), ArgumentMatchers.any());
        assertNotNull(response);
        assertThat(response).isInstanceOf(CollectProjectDto.class);
    }

    @Test
    public void shouldGetAllProjectsPaginatedWithSuccess() {
        // Given
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        when(
            collectExternalRestClient.getAllPaginated(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            )
        ).thenReturn(new PaginatedValuesDto<>());

        // When
        PaginatedValuesDto<CollectProjectDto> response = projectService.getAllProjectsPaginated(
            context,
            1,
            1,
            Optional.empty(),
            Optional.empty(),
            Optional.empty()
        );

        // Then
        verify(collectExternalRestClient, times(1)).getAllPaginated(
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any()
        );
        assertNotNull(response);
        assertThat(response).isInstanceOf(PaginatedValuesDto.class);
    }

    @Test
    public void shouldStreamingUploadWithSuccess() {
        // Given
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        when(
            collectStreamingExternalRestClient.streamingUpload(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            )
        ).thenReturn(ResponseEntity.ok().build());

        // When
        ResponseEntity<Void> response = projectService.streamingUpload(
            context,
            "filename",
            TRANSACTION_ID,
            new ByteArrayInputStream("Fake file".getBytes())
        );

        // Then
        verify(collectStreamingExternalRestClient, times(1)).streamingUpload(
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any()
        );
        assertNotNull(response);
    }

    @Test
    public void shouldDeleteProjectWithSuccess() {
        // Given
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        // When
        assertDoesNotThrow(() -> projectService.deleteProject(PROJECT_ID, context));
    }

    @Test
    public void shouldCreateTransactionForProjectWithSuccess() {
        // Given
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        when(
            collectExternalRestClient.createTransactionForProject(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            )
        ).thenReturn(new CollectTransactionDto());

        // When
        CollectTransactionDto response = projectService.createTransactionForProject(
            context,
            new CollectTransactionDto(),
            TRANSACTION_ID
        );

        // Then
        verify(collectExternalRestClient, times(1)).createTransactionForProject(
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any()
        );
        assertNotNull(response);
        assertThat(response).isInstanceOf(CollectTransactionDto.class);
    }

    @Test
    public void shouldGetLastTransactionForProjectIdWithSuccess() {
        // Given
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        when(
            collectExternalRestClient.getLastTransactionForProjectId(ArgumentMatchers.any(), ArgumentMatchers.any())
        ).thenReturn(new CollectTransactionDto());

        // When
        CollectTransactionDto response = projectService.getLastTransactionForProjectId(context, PROJECT_ID);

        // Then
        verify(collectExternalRestClient, times(1)).getLastTransactionForProjectId(
            ArgumentMatchers.any(),
            ArgumentMatchers.any()
        );
        assertNotNull(response);
        assertThat(response).isInstanceOf(CollectTransactionDto.class);
    }

    @Test
    public void shouldGetTransactionsByProjectPaginatedWithSuccess() {
        // Given
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        when(
            collectExternalRestClient.getTransactionsByProjectPaginated(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                ArgumentMatchers.any()
            )
        ).thenReturn(new PaginatedValuesDto<>());

        // When
        PaginatedValuesDto<CollectTransactionDto> response = projectService.getTransactionsByProjectPaginated(
            1,
            1,
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            context,
            PROJECT_ID
        );

        // Then
        verify(collectExternalRestClient, times(1)).getTransactionsByProjectPaginated(
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any()
        );
        assertNotNull(response);
        assertThat(response).isInstanceOf(PaginatedValuesDto.class);
    }
}
