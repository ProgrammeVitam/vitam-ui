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

import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.collect.external.client.CollectTransactionExternalRestClient;
import fr.gouv.vitamui.collect.external.client.UpdateUnitsMetadataExternalRestClient;
import fr.gouv.vitamui.commons.api.dtos.OntologyDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class TransactionServiceTest {

    private TransactionService transactionService;

    private static final String OBJECT_ID = "objectId";

    @Mock
    private UpdateUnitsMetadataExternalRestClient updateUnitsMetadataExternalRestClient;

    @Mock
    private CollectTransactionExternalRestClient collectTransactionExternalRestClient;

    @Mock
    private CommonService commonService;

    @Before
    public void init() {
        transactionService = new TransactionService(
            collectTransactionExternalRestClient,
            updateUnitsMetadataExternalRestClient,
            commonService
        );
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    public void search_collect_units_should_call_appropriate_rest_client() {
        // Given
        SearchCriteriaDto searchCriteriaDto = new SearchCriteriaDto();
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        Mockito.when(
            transactionService.searchArchiveUnitsByTransactionAndSearchQuery(
                ArgumentMatchers.any(),
                ArgumentMatchers.any(),
                any(SearchCriteriaDto.class)
            )
        ).thenReturn(new ArchiveUnitsDto());

        // When
        transactionService.searchArchiveUnitsByTransactionAndSearchQuery(context, "projectId", searchCriteriaDto);

        // Then
        verify(collectTransactionExternalRestClient, Mockito.times(1)).searchArchiveUnitsByProjectAndSearchQuery(
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            any(SearchCriteriaDto.class)
        );
    }

    @Test
    public void when_abortTransaction_ok() {
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");

        Mockito.doNothing().when(collectTransactionExternalRestClient).abortTransaction(context, "transactionId");
        transactionService.abortTransaction(context, "transactionId");
        verify(collectTransactionExternalRestClient, times(1)).abortTransaction(context, "transactionId");
    }

    @Test
    public void when_repenTransaction_ok() {
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");

        Mockito.doNothing().when(collectTransactionExternalRestClient).reopenTransaction(context, "transactionId");
        transactionService.reopenTransaction(context, "transactionId");
        verify(collectTransactionExternalRestClient, times(1)).reopenTransaction(context, "transactionId");
    }

    @Test
    public void update_archive_units_metadata_should_call_appropriate_client() {
        // Given
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        Mockito.when(
            updateUnitsMetadataExternalRestClient.updateArchiveUnitsMetadataFromFile(
                any(ExternalHttpContext.class),
                anyString(),
                anyString(),
                any(InputStream.class)
            )
        ).thenReturn(new ResponseEntity<>("responseFromVitam", HttpStatus.OK));

        String transactionId = "1";
        InputStream anyInputStream = new ByteArrayInputStream("test data".getBytes());

        // When
        transactionService.updateArchiveUnitsMetadataFromFile(
            eq(transactionId),
            eq("fileName"),
            eq(anyInputStream),
            eq(context)
        );

        // Then
        verify(updateUnitsMetadataExternalRestClient, Mockito.times(1)).updateArchiveUnitsMetadataFromFile(
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any(),
            ArgumentMatchers.any()
        );
    }

    @Test
    public void find_object_group_by_id_should_call_appropriate_client() {
        // Given
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        when(
            collectTransactionExternalRestClient.findObjectGroupById(ArgumentMatchers.any(), ArgumentMatchers.any())
        ).thenReturn(new ResponseEntity<>(new ResultsDto(), HttpStatus.OK));

        // When
        ResponseEntity<ResultsDto> response = transactionService.getObjectGroupById(OBJECT_ID, context);

        // Then
        verify(collectTransactionExternalRestClient, times(1)).findObjectGroupById(
            ArgumentMatchers.any(),
            ArgumentMatchers.any()
        );
        assertNotNull(response);
        assertThat(response).isInstanceOf(ResponseEntity.class);
        assertThat(response.getBody()).isInstanceOf(ResultsDto.class);
    }

    @Test
    public void get_ontologies_list_should_call_appropriate_rest_client_one_time() {
        // Given
        List<OntologyDto> ontologiesList = new ArrayList<>();
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        Mockito.when(collectTransactionExternalRestClient.getExternalOntologiesList(ArgumentMatchers.any())).thenReturn(
            ontologiesList
        );

        // When
        transactionService.getExternalOntologiesList(eq(context));

        // Then
        verify(collectTransactionExternalRestClient, Mockito.times(1)).getExternalOntologiesList(
            ArgumentMatchers.any()
        );
    }
}
