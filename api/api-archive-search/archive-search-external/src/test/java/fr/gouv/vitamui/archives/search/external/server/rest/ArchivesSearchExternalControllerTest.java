/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2020)
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

package fr.gouv.vitamui.archives.search.external.server.rest;


import fr.gouv.archive.internal.client.ArchiveInternalRestClient;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.rest.RestApi;
import fr.gouv.vitamui.archives.search.external.client.ArchiveSearchExternalRestClient;
import fr.gouv.vitamui.archives.search.external.server.service.ArchivesSearchExternalService;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.InvalidSanitizeCriteriaException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@WebMvcTest(controllers = {ArchivesSearchExternalController.class})
public class ArchivesSearchExternalControllerTest extends ApiArchivesSearchExternalControllerTest<IdDto> {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchivesSearchExternalControllerTest.class);

    @MockBean
    private ArchivesSearchExternalService archivesSearchExternalService;

    @MockBean
    private ArchiveSearchExternalRestClient archiveSearchExternalRestClient;

    @MockBean
    private ArchiveInternalRestClient archiveInternalRestClient;

    @MockBean
    private ExternalSecurityService externalSecurityService;

    @Test
    public void testArchiveController() {
        Assert.assertNotNull(archivesSearchExternalService);
    }


    private ArchivesSearchExternalController archivesSearchExternalController;

    @Before
    public void setUp() {
        archivesSearchExternalController = new ArchivesSearchExternalController(archivesSearchExternalService);
    }

    @Override
    protected String[] getServices() {
        return new String[] {ServicesData.SERVICE_ARCHIVE};
    }

    @Override
    protected Class<IdDto> getDtoClass() {
        return IdDto.class;
    }

    @Override
    protected IdDto buildDto() {
        return null;
    }

    @Override
    protected VitamUILogger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {

    }

    @Override
    protected String getRessourcePrefix() {
        return RestApi.ARCHIVE_SEARCH_PATH;
    }



    @Test
    public void when_searchArchiveUnitsByCriteria_Srvc_ok_should_return_ok() {

        SearchCriteriaDto query = new SearchCriteriaDto();
        ArchiveUnitsDto expectedResponse = new ArchiveUnitsDto();
        Mockito
            .when(archivesSearchExternalService.searchArchiveUnitsByCriteria(Mockito.eq(query)))
            .thenReturn(expectedResponse);
        ArchiveUnitsDto responseDto = archivesSearchExternalController.searchArchiveUnitsByCriteria(query);
        Assert.assertEquals(responseDto, expectedResponse);
    }

    @Test
    public void when_searchArchiveUnitsByCriteria_Srvc_ok_should_return_ko() {

        SearchCriteriaDto query = new SearchCriteriaDto();
        query.setNodes(Arrays.asList("<s>insecure</s>"));
        ArchiveUnitsDto expectedResponse = new ArchiveUnitsDto();
        Mockito
            .when(archivesSearchExternalService.searchArchiveUnitsByCriteria(Mockito.eq(query)))
            .thenReturn(expectedResponse);

        assertThatCode(() -> archivesSearchExternalController.searchArchiveUnitsByCriteria(query))
            .isInstanceOf(InvalidSanitizeCriteriaException.class);
    }

    @Test
    public void testSearchFilingHoldingSchemeResultsThanReturnVitamUISearchResponseDto() {
        // Given
        VitamUISearchResponseDto expectedResponse = new VitamUISearchResponseDto();
        when(archivesSearchExternalService.getFilingHoldingScheme())
            .thenReturn(expectedResponse);
        // When
        VitamUISearchResponseDto filingHoldingSchemeResults =
            archivesSearchExternalController.getFillingHoldingScheme();
        // Then
        Assertions.assertThat(filingHoldingSchemeResults).isNotNull();
        Assertions.assertThat(filingHoldingSchemeResults).isEqualTo(expectedResponse);
    }

}
