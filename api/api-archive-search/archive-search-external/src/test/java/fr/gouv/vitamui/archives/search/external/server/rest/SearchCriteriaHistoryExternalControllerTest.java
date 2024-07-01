/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.archives.search.external.server.rest;

import fr.gouv.vitamui.archives.search.common.rest.RestApi;
import fr.gouv.vitamui.archives.search.external.server.service.SearchCriteriaHistoryExternalService;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaHistoryDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import java.util.Arrays;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = { SearchCriteriaHistoryExternalController.class })
public class SearchCriteriaHistoryExternalControllerTest
    extends ApiArchiveSearchExternalControllerTest<SearchCriteriaHistoryDto> {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        SearchCriteriaHistoryExternalControllerTest.class
    );

    @MockBean
    private SearchCriteriaHistoryExternalService service;

    private SearchCriteriaHistoryExternalController mockedController = MvcUriComponentsBuilder.on(
        SearchCriteriaHistoryExternalController.class
    );

    @Test
    public void testGetSearchCriteriaHistories() throws Exception {
        LOGGER.debug("get search criterias");
        Mockito.when(service.getSearchCriteriaHistory()).thenReturn(Arrays.asList(new SearchCriteriaHistoryDto()));

        ResultActions result = super.performGet(CommonConstants.PATH_ME);
        result.andExpect(MockMvcResultMatchers.handler().methodCall(mockedController.getSearchCriteriaHistory()));
    }

    @Test
    public void testUpdateSearchCriteriaHistory() {
        super.testUpdateEntity();
    }

    @Test
    public void testDeleteSearchCriteriaHistory() {
        super.performDelete("/1");
    }

    @Override
    protected Class<SearchCriteriaHistoryDto> getDtoClass() {
        return SearchCriteriaHistoryDto.class;
    }

    @Override
    protected SearchCriteriaHistoryDto buildDto() {
        return new SearchCriteriaHistoryDto();
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {}

    @Override
    protected String getRessourcePrefix() {
        return RestApi.SEARCH_CRITERIA_HISTORY;
    }

    @Override
    protected String[] getServices() {
        return new String[] {
            ServicesData.ROLE_GET_ALL_ARCHIVE,
            ServicesData.ROLE_CREATE_ARCHIVE,
            ServicesData.ROLE_GET_ARCHIVE,
        };
    }
}
