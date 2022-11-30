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

import fr.gouv.vitamui.collect.external.client.SearchCriteriaHistoryExternalRestClient;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class SearchCriteriaHistoryServiceTest {

    private SearchCriteriaHistoryService searchCriteriaHistoryService;

    @Mock
    private SearchCriteriaHistoryExternalRestClient searchCriteriaHistoryExternalRestClient;

    @Before
    public void init() {
        searchCriteriaHistoryService = new SearchCriteriaHistoryService(searchCriteriaHistoryExternalRestClient);
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    public void searchGetSearchCriteriaHistoryWithSuccess() {
        // Given
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        when(searchCriteriaHistoryExternalRestClient.getSearchCriteriaHistory(ArgumentMatchers.any()))
            .thenReturn(List.of(new SearchCriteriaHistoryDto()));

        // When
        List<SearchCriteriaHistoryDto> response =
            searchCriteriaHistoryService.getSearchCritriaHistory(context);

        // Then
        verify(searchCriteriaHistoryExternalRestClient, times(1))
            .getSearchCriteriaHistory(ArgumentMatchers.any());
        assertNotNull(response);
    }

    @Test
    public void searchUpdateCriteriaHistoryWithSuccess() {
        // Given
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        when(searchCriteriaHistoryExternalRestClient.update(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(new SearchCriteriaHistoryDto());
        SearchCriteriaHistoryDto newSearchCriteria = new SearchCriteriaHistoryDto();
        newSearchCriteria.setId("ID");
        newSearchCriteria.setName("NAME");
        newSearchCriteria.setSearchCriteriaList(new ArrayList<>());
        // When
        SearchCriteriaHistoryDto response =
            searchCriteriaHistoryService.update(context, newSearchCriteria);

        // Then
        verify(searchCriteriaHistoryExternalRestClient, times(1))
            .update(ArgumentMatchers.any(), ArgumentMatchers.any());
        assertNotNull(response);
    }
}
