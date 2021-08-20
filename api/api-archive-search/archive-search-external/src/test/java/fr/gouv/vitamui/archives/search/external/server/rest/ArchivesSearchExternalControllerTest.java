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
import fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.CriteriaValue;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaEltDto;
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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = {ArchivesSearchExternalController.class})
public class ArchivesSearchExternalControllerTest extends ApiArchiveSearchExternalControllerTest<IdDto> {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchivesSearchExternalControllerTest.class);

    public final String ARCHIVE_UNITS_RESULTS_CSV = "data/vitam_archive_units_response.csv";

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
        Assertions.assertNotNull(archivesSearchExternalService);
    }


    private ArchivesSearchExternalController archivesSearchExternalController;

    @BeforeEach
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
        Assertions.assertEquals(responseDto, expectedResponse);
    }

    @Test
    public void when_searchArchiveUnitsByCriteria_Srvc_ok_should_return_ko() {

        SearchCriteriaDto query = new SearchCriteriaDto();
        SearchCriteriaEltDto nodeCriteria = new SearchCriteriaEltDto();
        nodeCriteria.setCriteria("NODES");
        nodeCriteria.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        nodeCriteria.setCategory(ArchiveSearchConsts.CriteriaCategory.NODES);
        nodeCriteria.setValues(Arrays.asList(new CriteriaValue("<s>insecure</s>")));
        query.setCriteriaList(List.of(nodeCriteria));
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
        Assertions.assertNotNull(filingHoldingSchemeResults);
        Assertions.assertEquals(filingHoldingSchemeResults, expectedResponse);
    }



    @Test
    public void when_exportCsvArchiveUnitsByCriteria_Srvc_ok_should_return_ok() throws IOException {
        // Given
        SearchCriteriaDto query = new SearchCriteriaDto();
        query.setLanguage(Locale.FRENCH.getLanguage());

        Resource resource = new ByteArrayResource(ArchivesSearchExternalControllerTest.class.getClassLoader()
            .getResourceAsStream(ARCHIVE_UNITS_RESULTS_CSV).readAllBytes());

        when(archivesSearchExternalService.exportCsvArchiveUnitsByCriteria(Mockito.eq(query)))
            .thenReturn(resource);
        // When
        Resource responseCsv =
            archivesSearchExternalController.exportCsvArchiveUnitsByCriteria(query);
        // Then
        Assertions.assertNotNull(responseCsv);
        Assertions.assertEquals(responseCsv, resource);
    }
}
