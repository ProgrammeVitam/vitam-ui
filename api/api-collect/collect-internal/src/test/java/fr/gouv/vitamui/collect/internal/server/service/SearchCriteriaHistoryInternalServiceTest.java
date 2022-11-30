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

package fr.gouv.vitamui.collect.internal.server.service;

import fr.gouv.vitamui.collect.internal.server.dao.SearchCriteriaHistoryRepository;
import fr.gouv.vitamui.commons.api.domain.QueryDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
class SearchCriteriaHistoryInternalServiceTest {
    @InjectMocks
    SearchCriteriaHistoryInternalService searchCriteriaHistoryInternalService;
    @Mock
    InternalSecurityService internalSecurityService;
    @Mock
    SearchCriteriaHistoryRepository searchCriteriaHistoryRepository;
    final PodamFactory factory = new PodamFactoryImpl();


    @BeforeEach
    public void beforeEach() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    void shouldGetSearchCriteriaHistoryDtosWithSuccess() {
        // GIVEN
        final AuthUserDto authUserDto = factory.manufacturePojo(AuthUserDto.class);
        ArrayList resultedValues =
            factory.manufacturePojo(ArrayList.class, SearchCriteriaHistoryDto.class);
        final QueryDto queryDto = new QueryDto();
        Mockito.when(internalSecurityService.getUser()).thenReturn(authUserDto);
        Mockito.when(searchCriteriaHistoryInternalService.getAll(queryDto)).thenReturn(resultedValues);

        // WHEN
        List<SearchCriteriaHistoryDto> results =
            searchCriteriaHistoryInternalService.getSearchCriteriaHistoryDtos();

        // THEN
        assertNotNull(results);
    }
}
