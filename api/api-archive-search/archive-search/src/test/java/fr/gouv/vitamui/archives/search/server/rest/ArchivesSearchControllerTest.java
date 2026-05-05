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

package fr.gouv.vitamui.archives.search.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.export.transfer.TransferRequestParameters;
import fr.gouv.vitamui.archives.search.common.dto.RuleSearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.TransferRequestDto;
import fr.gouv.vitamui.archives.search.common.dto.VitamUIArchiveUnitResponseDto;
import fr.gouv.vitamui.archives.search.common.rest.RestApi;
import fr.gouv.vitamui.archives.search.server.service.ArchiveSearchEliminationService;
import fr.gouv.vitamui.archives.search.server.service.ArchiveSearchMgtRulesService;
import fr.gouv.vitamui.archives.search.server.service.ArchiveSearchService;
import fr.gouv.vitamui.archives.search.server.service.ArchiveSearchUnitExportCsvService;
import fr.gouv.vitamui.archives.search.server.service.ExportDipService;
import fr.gouv.vitamui.archives.search.server.service.TransferVitamOperationsService;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.dtos.CriteriaValue;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.dtos.VitamUiOntologyDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
import fr.gouv.vitamui.commons.vitam.api.dto.PersistentIdentifierResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = { ArchivesSearchController.class })
class ArchivesSearchControllerTest extends ApiArchiveSearchControllerTest<IdDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchivesSearchControllerTest.class);

    public final String ARCHIVE_UNITS_RESULTS_CSV = "data/vitam_archive_units_response.csv";

    public final String EXPECTED_RESPONSE = "expected_response";

    @MockitoBean
    private ArchiveSearchService archiveSearchService;

    @MockitoBean
    private TransferVitamOperationsService transferVitamOperationsService;

    @MockitoBean
    private ExportDipService exportDipService;

    @MockitoBean
    private ArchiveSearchEliminationService archiveSearchEliminationService;

    @MockitoBean
    private ArchiveSearchMgtRulesService archiveSearchMgtRulesService;

    @MockitoBean
    private ArchiveSearchUnitExportCsvService archiveSearchUnitExportCsvService;

    @Test
    void testArchiveController() {
        assertNotNull(archiveSearchMgtRulesService);
    }

    private ArchivesSearchController archivesSearchController;

    @BeforeEach
    public void setUp() {
        archivesSearchController = new ArchivesSearchController(
            archiveSearchService,
            archiveSearchUnitExportCsvService,
            exportDipService,
            transferVitamOperationsService,
            archiveSearchEliminationService,
            archiveSearchMgtRulesService
        );
    }

    @Override
    protected String[] getServices() {
        return new String[] { ServicesData.SERVICE_ARCHIVE };
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
    protected Logger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {}

    @Override
    protected String getRessourcePrefix() {
        return RestApi.ARCHIVE_SEARCH_PATH;
    }

    @Test
    void test_searchArchiveUnitsByCriteria_with_ok_criteria_should_return_ok()
        throws InvalidParseOperationException, PreconditionFailedException, VitamClientException, IOException {
        SearchCriteriaDto query = new SearchCriteriaDto();
        VitamUIArchiveUnitResponseDto expectedResponse = new VitamUIArchiveUnitResponseDto();
        Mockito.when(archiveSearchService.searchArchiveUnitsByCriteria(query)).thenReturn(expectedResponse);
        VitamUIArchiveUnitResponseDto responseDto = archivesSearchController.searchArchiveUnitsByCriteria(query);
        assertEquals(responseDto, expectedResponse);
    }

    @Test
    void test_searchArchiveUnitsByCriteria_with_invalid_criteria_should_return_ko()
        throws VitamClientException, IOException {
        SearchCriteriaDto query = new SearchCriteriaDto();
        SearchCriteriaEltDto nodeCriteria = new SearchCriteriaEltDto();
        nodeCriteria.setCriteria("NODES");
        nodeCriteria.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
        nodeCriteria.setCategory(ArchiveSearchConsts.CriteriaCategory.NODES);
        nodeCriteria.setValues(List.of(new CriteriaValue("<s>insecure</s>")));
        query.setCriteriaList(List.of(nodeCriteria));
        VitamUIArchiveUnitResponseDto expectedResponse = new VitamUIArchiveUnitResponseDto();
        Mockito.when(archiveSearchService.searchArchiveUnitsByCriteria(query)).thenReturn(expectedResponse);

        assertThatCode(() -> archivesSearchController.searchArchiveUnitsByCriteria(query))
            .isInstanceOf(PreconditionFailedException.class)
            .hasMessage("The object is not valid ");
    }

    @Test
    void testSearchFilingHoldingSchemeResultsThanReturnVitamUISearchResponseDto() throws VitamClientException {
        // Given
        VitamUISearchResponseDto expectedResponse = new VitamUISearchResponseDto();
        when(archiveSearchService.getFillingHoldingScheme()).thenReturn(expectedResponse);
        // When
        VitamUISearchResponseDto filingHoldingSchemeResults = archivesSearchController.getFillingHoldingScheme();
        // Then
        assertNotNull(filingHoldingSchemeResults);
        assertEquals(filingHoldingSchemeResults, expectedResponse);
    }

    @Test
    void test_exportCsvArchiveUnitsByCriteria_with_valid_criteria_should_return_ok()
        throws InvalidParseOperationException, PreconditionFailedException, IOException, VitamClientException {
        // Given
        SearchCriteriaDto query = new SearchCriteriaDto();
        query.setLanguage(Locale.FRENCH.getLanguage());

        Resource resource = new ByteArrayResource(
            Objects.requireNonNull(
                ArchivesSearchControllerTest.class.getClassLoader().getResourceAsStream(ARCHIVE_UNITS_RESULTS_CSV)
            ).readAllBytes()
        );

        when(archiveSearchUnitExportCsvService.exportToCsvSearchArchiveUnitsByCriteria(query)).thenReturn(resource);
        // When
        Resource responseCsv = archivesSearchController.exportCsvArchiveUnitsByCriteria(query);
        // Then
        assertNotNull(responseCsv);
        assertEquals(responseCsv, resource);
    }

    @Test
    void when_transferRequest_Srvc_ok_should_return_ok()
        throws InvalidParseOperationException, PreconditionFailedException, IOException, VitamClientException {
        // Given
        TransferRequestDto transferRequestDto = new TransferRequestDto()
            .setTransferRequestParameters(new TransferRequestParameters())
            .setSearchCriteria(new SearchCriteriaDto())
            .setDataObjectVersionsPatterns(Map.of())
            .setLifeCycleLogs(true);
        when(transferVitamOperationsService.transferRequest(transferRequestDto)).thenReturn("OK");
        // When
        String response = archivesSearchController.transferRequest(transferRequestDto);
        // Then
        assertThat(response).isNotNull().isEqualTo("OK");
    }

    @Test
    void testArchiveUnitsRulesMassUpdateResultsThanReturnVitamOperationId()
        throws InvalidParseOperationException, PreconditionFailedException, VitamClientException {
        RuleSearchCriteriaDto ruleSearchCriteriaDto = new RuleSearchCriteriaDto();
        String expectedResponse = EXPECTED_RESPONSE;

        Mockito.when(archiveSearchMgtRulesService.updateArchiveUnitsRules(ruleSearchCriteriaDto)).thenReturn(
            expectedResponse
        );

        String response = archivesSearchController.updateArchiveUnitsRules(ruleSearchCriteriaDto);
        assertEquals(response, expectedResponse);
    }

    @Test
    void testLaunchComputedInheritedRulesThenReturnVitamOperationId()
        throws InvalidParseOperationException, PreconditionFailedException, VitamClientException {
        // Given
        SearchCriteriaDto searchCriteriaDto = new SearchCriteriaDto();
        String expectedResponse = EXPECTED_RESPONSE;

        // When
        Mockito.when(archiveSearchService.computedInheritedRules(searchCriteriaDto)).thenReturn(expectedResponse);
        String response = archivesSearchController.computedInheritedRules(searchCriteriaDto);

        // Then
        assertEquals(response, expectedResponse);
    }

    @Test
    void testSelectUnitWithInheritedRulesThenReturnVitamOperationId()
        throws InvalidParseOperationException, PreconditionFailedException, VitamClientException, IOException {
        // Given
        SearchCriteriaDto searchCriteriaDto = new SearchCriteriaDto();
        ResultsDto expectedResponse = new ResultsDto();

        // When
        Mockito.when(archiveSearchService.selectUnitWithInheritedRules(searchCriteriaDto)).thenReturn(expectedResponse);
        ResultsDto response = archivesSearchController.selectUnitWithInheritedRules(searchCriteriaDto);

        // Then
        assertEquals(response, expectedResponse);
    }

    @Test
    void testTransferAcknowledgmentThenReturnVitamOperationDetails()
        throws InvalidParseOperationException, PreconditionFailedException, VitamClientException {
        // Given
        String fileName = "FileName";
        String expectedResponse = "operationId";
        String initialString = "atr xml text";
        InputStream atrFile = new ByteArrayInputStream(initialString.getBytes());

        // When
        Mockito.when(transferVitamOperationsService.transferAcknowledgment(atrFile)).thenReturn(expectedResponse);
        String response = archivesSearchController.transferAcknowledgment(atrFile);

        // Then
        assertEquals(response, expectedResponse);
    }

    @Test
    void testGetOntologiesListThenReturnOntologiesValuesList() throws PreconditionFailedException, IOException {
        // Given
        List<VitamUiOntologyDto> expectedResponse = new ArrayList<>();

        // When
        Mockito.when(archiveSearchService.readExternalOntologiesFromFile()).thenReturn(expectedResponse);
        List<VitamUiOntologyDto> response = archivesSearchController.getExternalOntologiesList();

        // Then
        assertEquals(response, expectedResponse);
    }

    @Test
    void testFindUnitsByPersistentIdentifier() throws PreconditionFailedException, VitamClientException {
        // Given
        final String arkId = "ark:/225867/001a9d7db5eghxac";
        final PersistentIdentifierResponseDto expectedResponse = new PersistentIdentifierResponseDto();

        // When
        Mockito.when(archiveSearchService.findUnitsByPersistentIdentifier(arkId)).thenReturn(expectedResponse);
        final PersistentIdentifierResponseDto response = archivesSearchController.findUnitsByPersistentIdentifier(
            arkId
        );

        // Then
        verify(archiveSearchService, times(1)).findUnitsByPersistentIdentifier(arkId);
        assertEquals(response, expectedResponse);
    }

    @Test
    void testFindObjectsByPersistentIdentifier() throws PreconditionFailedException, VitamClientException {
        // Given
        final String arkId = "ark:/225867/001a9d7db5eghxac_binary_master";
        final PersistentIdentifierResponseDto expectedResponse = new PersistentIdentifierResponseDto();

        // When
        Mockito.when(archiveSearchService.findObjectsByPersistentIdentifier(arkId)).thenReturn(expectedResponse);
        final PersistentIdentifierResponseDto response = archivesSearchController.findObjectsByPersistentIdentifier(
            arkId
        );

        // Then
        verify(archiveSearchService, times(1)).findObjectsByPersistentIdentifier(arkId);
        assertEquals(response, expectedResponse);
    }
}
