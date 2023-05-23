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
package fr.gouv.vitamui.archive.internal.server.service;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.io.ByteStreams;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.configuration.BuilderToken;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitam.common.model.elimination.EliminationRequestBody;
import fr.gouv.vitamui.archive.internal.server.rulesupdate.converter.RuleOperationsConverter;
import fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnit;
import fr.gouv.vitamui.archives.search.common.dto.CriteriaValue;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationAction;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationQueryActionType;
import fr.gouv.vitamui.archives.search.common.dto.RuleSearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaEltDto;
import fr.gouv.vitamui.archives.search.common.dto.UnitDescriptiveMetadataDto;
import fr.gouv.vitamui.commons.api.domain.AccessContractModelDto;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.access.EliminationService;
import fr.gouv.vitamui.commons.vitam.api.access.ExportDipV2Service;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractService;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import fr.gouv.vitamui.commons.vitam.api.dto.AccessContractResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.iam.common.dto.AccessContractsResponseDto;
import fr.gouv.vitamui.iam.common.dto.AccessContractsVitamDto;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ArchiveSearchInternalServiceTest {

    @MockBean(name = "objectMapper")
    private ObjectMapper objectMapper;

    @MockBean(name = "unitService")
    private UnitService unitService;

    @MockBean(name = "agencyService")
    private AgencyService agencyService;

    @MockBean(name = "archiveSearchAgenciesInternalService")
    private ArchiveSearchAgenciesInternalService archiveSearchAgenciesInternalService;

    @InjectMocks
    private ArchiveSearchAgenciesInternalService archiveSearchAgenciesInternalServiceInjected;

    @MockBean(name = "archiveSearchRulesInternalService")
    private ArchiveSearchRulesInternalService archiveSearchRulesInternalService;

    @InjectMocks
    private ArchivesSearchFieldsQueryBuilderService archivesSearchFieldsQueryBuilderService;

    @InjectMocks
    private ArchivesSearchManagementRulesQueryBuilderService archivesSearchManagementRulesQueryBuilderService;

    @InjectMocks
    private ArchiveSearchInternalService archiveSearchInternalService;

    @MockBean(name = "exportDipV2Service")
    private ExportDipV2Service exportDipV2Service;

    @MockBean(name = "eliminationService")
    private EliminationService eliminationService;

    @MockBean(name = "ruleOperationsConverter")
    private RuleOperationsConverter ruleOperationsConverter;

    @MockBean(name = "accessContractService")
    private AccessContractService accessContractService;

    public final String FILING_HOLDING_SCHEME_RESULTS = "data/vitam_filing_holding_units_response.json";
    public final String UPDATE_RULES_ASYNC_RESPONSE = "data/update_rules_async_response.json";
    public final String UPDATE_UNIT_DESCRIPTIVE_METADATA_RESPONSE =
        "data/update_unit_descriptive_metadata_response.json";
    public final String ELIMINATION_ANALYSIS_QUERY = "data/elimination/query.json";
    public final String ELIMINATION_ANALYSIS_FINAL_QUERY = "data/elimination/expected_query.json";
    public final String FILLING_HOLDING_SCHEME_EXPECTED_QUERY = "data/fillingholding/expected_query.json";

    // CSV Export data tests
    public final String FR_ARCHIVE_UNITS_RESULTS_CSV = "data/export-csv/vitam_archive_units_response_fr.csv";
    public final String EN_ARCHIVE_UNITS_RESULTS_CSV = "data/export-csv/vitam_archive_units_response_en.csv";
    public final String VITAM_UNIT_RESULTS = "data/export-csv/vitam_units_response.json";
    public final String VITAM_UNIT_ONE_RESULTS = "data/export-csv/vitam_units_one_response.json";

    // Tests encode special weired chars
    public final String VITAM_UNIT_ONE_RESULT_TO_ENCODE =
        "data/export-csv/vitam_units_one_response_to_encode_end_to_end.json";
    public final String ARCHIVE_UNITS_WITH_CONTENT_TO_ENCODE_CORRECTLY =
        "data/export-csv/vitam_archive_units_response_correctly_encoded.csv";
    public final String VITAM_UNIT_RESULTS_TO_ENCODE = "data/export-csv/vitam_units_response_to_encode.json";

    // Filing Unit
    public final String ARCHIVE_UNITS_WITH_CONTENT_FILING_UNIT =
        "data/export-csv/filing-unit/vitam_archive_units_response_fr.csv";
    public final String VITAM_UNIT_RESULTS_FILING_UNIT = "data/export-csv/filing-unit/vitam_units_one_response.json";
    public final String VITAM_UNIT_ONE_RESULT_FILING_UNIT = "data/export-csv/filing-unit/vitam_units_response.json";

    // Holding Unit
    public final String ARCHIVE_UNITS_WITH_CONTENT_HOLDING_UNIT =
        "data/export-csv/holding-unit/vitam_archive_units_response_fr.csv";
    public final String VITAM_UNIT_RESULTS_HOLDING_UNIT = "data/export-csv/holding-unit/vitam_units_one_response.json";
    public final String VITAM_UNIT_ONE_RESULT_HOLDING_UNIT = "data/export-csv/holding-unit/vitam_units_response.json";

    // Unit With Object
    public final String ARCHIVE_UNITS_WITH_CONTENT_UNIT_WITH_OBJECT =
        "data/export-csv/unit-with-object/vitam_archive_units_response_fr.csv";
    public final String VITAM_UNIT_RESULTS_UNIT_WITH_OBJECT =
        "data/export-csv/unit-with-object/vitam_units_one_response.json";
    public final String VITAM_UNIT_ONE_RESULT_UNIT_WITH_OBJECT =
        "data/export-csv/unit-with-object/vitam_units_response.json";

    // Unit Without Object
    public final String ARCHIVE_UNITS_WITH_CONTENT_UNIT_WITHOUT_OBJECT =
        "data/export-csv/unit-without-object/vitam_archive_units_response_fr.csv";
    public final String VITAM_UNIT_RESULTS_UNIT_WITHOUT_OBJECT =
        "data/export-csv/unit-without-object/vitam_units_one_response.json";
    public final String VITAM_UNIT_ONE_RESULT_UNIT_WITHOUT_OBJECT =
        "data/export-csv/unit-without-object/vitam_units_response.json";

    @BeforeEach
    public void setUp() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        archiveSearchInternalService =
            new ArchiveSearchInternalService(objectMapper, unitService, archiveSearchAgenciesInternalService,
                archiveSearchRulesInternalService, archivesSearchFieldsQueryBuilderService,
                exportDipV2Service, archivesSearchManagementRulesQueryBuilderService, eliminationService,
                ruleOperationsConverter, accessContractService);
    }

    @Test
    public void testSearchFilingHoldingSchemeResultsThanReturnVitamUISearchResponseDto() throws VitamClientException,
        IOException, InvalidParseOperationException {
        // Given
        when(unitService.searchUnits(any(), any()))
            .thenReturn(buildUnitMetadataResponse(FILING_HOLDING_SCHEME_RESULTS));
        // When
        JsonNode jsonNode = archiveSearchInternalService.searchArchiveUnits(any(), any());

        // Configure the mapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        VitamUISearchResponseDto vitamUISearchResponseDto =
            objectMapper.treeToValue(jsonNode, VitamUISearchResponseDto.class);

        // Then
        Assertions.assertThat(vitamUISearchResponseDto).isNotNull();
        Assertions.assertThat(vitamUISearchResponseDto.getResults().size()).isEqualTo(20);
    }

    private RequestResponse<JsonNode> buildUnitMetadataResponse(String filename)
        throws IOException, InvalidParseOperationException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream inputStream = ArchiveSearchInternalServiceTest.class.getClassLoader()
            .getResourceAsStream(filename);
        return RequestResponseOK
            .getFromJsonNode(objectMapper.readValue(ByteStreams.toByteArray(inputStream), JsonNode.class));
    }

    @Test
    public void getFinalEliminationConstructedQuery() throws Exception {
        JsonNode fromString = JsonHandler.getFromFile(PropertiesUtils.findFile(ELIMINATION_ANALYSIS_QUERY));
        EliminationRequestBody eliminationRequestBody2 =
            archiveSearchInternalService.getEliminationRequestBody(fromString);

        JsonNode resultExpected = JsonHandler.getFromFile(PropertiesUtils.findFile(ELIMINATION_ANALYSIS_FINAL_QUERY));
        Assertions.assertThat(eliminationRequestBody2.getDslRequest()).isEqualTo(resultExpected);
    }


    @Test
    public void getFinalFillingHoldingSchemeQuery() throws Exception {
        // Given
        JsonNode expectedQuery =
            JsonHandler.getFromFile(PropertiesUtils.findFile(FILLING_HOLDING_SCHEME_EXPECTED_QUERY));

        // When
        JsonNode givenQuery =
            archiveSearchInternalService.createQueryForHoldingFillingUnit();

        // Then
        Assertions.assertThat(expectedQuery.toString()).isEqualTo(String.valueOf(givenQuery));
        Assertions.assertThat(
            givenQuery.get(BuilderToken.GLOBAL.FILTER.exactToken()).get(BuilderToken.SELECTFILTER.ORDERBY.exactToken())
                .has("Title")).isTrue();
    }

    @Test
    public void testUpdateArchiveUnitsRulesWithCorrectAccessContractThenReturnSuccess() throws Exception {
        // Given
        when(unitService.massUpdateUnitsRules(any(), any()))
            .thenReturn(buildUnitMetadataResponse(UPDATE_RULES_ASYNC_RESPONSE));

        RequestResponseOK<AccessContractModel> response1 = new RequestResponseOK<>();
        response1.setHttpCode(200);
        response1.setHits(1, 1, 1, 1);
        response1.addResult(createAccessContractModel("contratTNR", "contrat d acces", 0, true));

        when(accessContractService.findAccessContractById(any(), any()))
            .thenReturn(response1);

        RequestResponse<AccessContractModel> requestResponse = Mockito.mock(RequestResponse.class);
        Mockito.when(accessContractService.findAccessContracts(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(requestResponse);
        List<AccessContractModelDto> results =
            List.of(createAccessContractModelDto("contratTNR", "contrat d acces", 0, true));
        JsonHandler.toJsonNode(results);
        AccessContractResponseDto response = new AccessContractResponseDto();
        response.setResults(results);
        Mockito.when(objectMapper.treeToValue(requestResponse.toJsonNode(), AccessContractResponseDto.class))
            .thenReturn(response);

        // Configure the mapper
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        when(objectMapper.treeToValue(any(), any()))
            .thenReturn(createAccessContractsResponseDto("contratTNR", "contrat d acces", 0, true));

        SearchCriteriaDto searchQuery = new SearchCriteriaDto();
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto agencyCodeCriteria = new SearchCriteriaEltDto();
        agencyCodeCriteria.setCriteria(ArchiveSearchConsts.ORIGINATING_AGENCY_ID_FIELD);
        agencyCodeCriteria.setValues(List.of(new CriteriaValue("CODE1")));
        agencyCodeCriteria.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        criteriaList.add(agencyCodeCriteria);
        agencyCodeCriteria = new SearchCriteriaEltDto();
        agencyCodeCriteria.setCriteria(ArchiveSearchConsts.ORIGINATING_AGENCY_LABEL_FIELD);
        agencyCodeCriteria.setValues(List.of(new CriteriaValue("ANY_LABEL")));
        agencyCodeCriteria.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);

        SearchCriteriaEltDto searchCriteriaElementsNodes = new SearchCriteriaEltDto();
        searchCriteriaElementsNodes.setCriteria("NODE");
        searchCriteriaElementsNodes.setCategory(ArchiveSearchConsts.CriteriaCategory.NODES);
        searchCriteriaElementsNodes.setValues(
            Arrays.asList(new CriteriaValue("node1"), new CriteriaValue("node2"), new CriteriaValue("node3")));
        criteriaList.add(agencyCodeCriteria);
        criteriaList.add(searchCriteriaElementsNodes);
        searchQuery.setSize(20);
        searchQuery.setPageNumber(20);
        searchQuery.setCriteriaList(criteriaList);
        RuleSearchCriteriaDto ruleSearchCriteriaDto = new RuleSearchCriteriaDto();
        ruleSearchCriteriaDto.setSearchCriteriaDto(searchQuery);

        //When //Then
        String expectingGuid =
            archiveSearchInternalService.updateArchiveUnitsRules(new VitamContext(1), ruleSearchCriteriaDto);
        assertThatCode(() -> {
            archiveSearchInternalService.updateArchiveUnitsRules(new VitamContext(1), ruleSearchCriteriaDto);
        }).doesNotThrowAnyException();

        Assertions.assertThat(expectingGuid).isEqualTo("aeeaaaaaagh23tjvabz5gal6qlt6iaaaaaaq");
    }

    @Test
    public void testReclassificationThenOK() throws Exception {
        // Given
        when(unitService.reclassification(any(), any()))
            .thenReturn(buildUnitMetadataResponse(UPDATE_RULES_ASYNC_RESPONSE));

        SearchCriteriaDto searchQuery = new SearchCriteriaDto();
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto agencyCodeCriteria = new SearchCriteriaEltDto();
        agencyCodeCriteria.setCriteria(ArchiveSearchConsts.ORIGINATING_AGENCY_ID_FIELD);
        agencyCodeCriteria.setValues(List.of(new CriteriaValue("CODE1")));
        agencyCodeCriteria.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        criteriaList.add(agencyCodeCriteria);
        agencyCodeCriteria = new SearchCriteriaEltDto();
        agencyCodeCriteria.setCriteria(ArchiveSearchConsts.ORIGINATING_AGENCY_LABEL_FIELD);
        agencyCodeCriteria.setValues(List.of(new CriteriaValue("ANY_LABEL")));
        agencyCodeCriteria.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);

        SearchCriteriaEltDto searchCriteriaElementsNodes = new SearchCriteriaEltDto();
        searchCriteriaElementsNodes.setCriteria("NODE");
        searchCriteriaElementsNodes.setCategory(ArchiveSearchConsts.CriteriaCategory.NODES);
        searchCriteriaElementsNodes.setValues(
            Arrays.asList(new CriteriaValue("node1"), new CriteriaValue("node2"), new CriteriaValue("node3")));
        criteriaList.add(agencyCodeCriteria);
        criteriaList.add(searchCriteriaElementsNodes);
        searchQuery.setSize(20);
        searchQuery.setPageNumber(20);
        searchQuery.setCriteriaList(criteriaList);

        ReclassificationCriteriaDto reclassificationCriteriaDto = buildReclassificationCriteriaDto();
        reclassificationCriteriaDto.setSearchCriteriaDto(searchQuery);

        //When //Then
        String expectingGuid =
            archiveSearchInternalService.reclassification(new VitamContext(1), reclassificationCriteriaDto);
        assertThatCode(() -> {
            archiveSearchInternalService.reclassification(new VitamContext(1), reclassificationCriteriaDto);
        }).doesNotThrowAnyException();

        Assertions.assertThat(expectingGuid).isEqualTo("aeeaaaaaagh23tjvabz5gal6qlt6iaaaaaaq");
    }

    private ReclassificationCriteriaDto buildReclassificationCriteriaDto() {
        ReclassificationCriteriaDto reclassificationCriteriaDto = new ReclassificationCriteriaDto();

        List<ReclassificationAction> reclassificationActionsList = new ArrayList<>();

        ReclassificationAction reclassificationActions = new ReclassificationAction();

        ReclassificationQueryActionType attr = new ReclassificationQueryActionType();
        attr.setUnitups(Arrays.asList("guid1", "guid2"));

        reclassificationActions.setAdd(attr);
        reclassificationActions.setPull(attr);

        reclassificationActionsList.add(reclassificationActions);

        reclassificationCriteriaDto.setAction(reclassificationActionsList);

        return reclassificationCriteriaDto;
    }

    @Test
    public void testUpdateUnitDescriptiveMetadataWithUnsetFieldsTest1() throws Exception {
        // Given
        when(unitService.updateUnitById(any(), any(), any()))
            .thenReturn(buildUnitMetadataResponse(UPDATE_UNIT_DESCRIPTIVE_METADATA_RESPONSE));

        UnitDescriptiveMetadataDto unitDescriptiveMetadataDto =
            buildUnitDescriptiveMetadataDto(null, null, null, null, "01/01/2023", Arrays.asList("StartDate"));

        //When //Then
        ObjectNode expectingQuery = archiveSearchInternalService.createUpdateQuery(unitDescriptiveMetadataDto);
        JsonNode fromFile = JsonHandler.getFromFile(PropertiesUtils.findFile("data/queries/updateUnits/query_1.json"));
        Assertions.assertThat(expectingQuery.toPrettyString()).isEqualTo(fromFile.toPrettyString());
    }

    @Test
    public void testUpdateUnitDescriptiveMetadataWithUnsetFieldsTest2() throws Exception {
        // Given
        when(unitService.updateUnitById(any(), any(), any()))
            .thenReturn(buildUnitMetadataResponse(UPDATE_UNIT_DESCRIPTIVE_METADATA_RESPONSE));

        UnitDescriptiveMetadataDto unitDescriptiveMetadataDto =
            buildUnitDescriptiveMetadataDto("some title", null, null, null, "01/01/2023",
                Arrays.asList("StartDate", "Description"));

        //When //Then
        ObjectNode expectingQuery = archiveSearchInternalService.createUpdateQuery(unitDescriptiveMetadataDto);

        JsonNode fromFile = JsonHandler.getFromFile(PropertiesUtils.findFile("data/queries/updateUnits/query_2.json"));
        Assertions.assertThat(expectingQuery.toPrettyString()).isEqualTo(fromFile.toPrettyString());
    }

    @Test
    public void testUpdateUnitDescriptiveMetadataWithUnsetFieldsTest3() throws Exception {
        // Given
        when(unitService.updateUnitById(any(), any(), any()))
            .thenReturn(buildUnitMetadataResponse(UPDATE_UNIT_DESCRIPTIVE_METADATA_RESPONSE));

        UnitDescriptiveMetadataDto unitDescriptiveMetadataDto =
            buildUnitDescriptiveMetadataDto("some title", "some description", "Item", null, null,
                Arrays.asList("StartDate", "EndDate"));

        //When //Then
        ObjectNode expectingQuery = archiveSearchInternalService.createUpdateQuery(unitDescriptiveMetadataDto);

        JsonNode fromFile = JsonHandler.getFromFile(PropertiesUtils.findFile("data/queries/updateUnits/query_3.json"));
        Assertions.assertThat(expectingQuery.toPrettyString()).isEqualTo(fromFile.toPrettyString());
    }

    @Test
    public void testUpdateUnitDescriptiveMetadataWithUnsetFieldsTest4() throws Exception {
        // Given
        when(unitService.updateUnitById(any(), any(), any()))
            .thenReturn(buildUnitMetadataResponse(UPDATE_UNIT_DESCRIPTIVE_METADATA_RESPONSE));

        UnitDescriptiveMetadataDto unitDescriptiveMetadataDto =
            buildUnitDescriptiveMetadataDto(null, null, null, null, null,
                Arrays.asList("StartDate", "EndDate", "Description"));

        //When //Then
        ObjectNode expectingQuery = archiveSearchInternalService.createUpdateQuery(unitDescriptiveMetadataDto);

        JsonNode fromFile = JsonHandler.getFromFile(PropertiesUtils.findFile("data/queries/updateUnits/query_4.json"));
        Assertions.assertThat(expectingQuery.toPrettyString()).isEqualTo(fromFile.toPrettyString());
    }

    @Test
    public void testUpdateUnitDescriptiveMetadataWithUnsetFieldsTest5() throws Exception {
        // Given
        when(unitService.updateUnitById(any(), any(), any()))
            .thenReturn(buildUnitMetadataResponse(UPDATE_UNIT_DESCRIPTIVE_METADATA_RESPONSE));

        UnitDescriptiveMetadataDto unitDescriptiveMetadataDto =
            buildUnitDescriptiveMetadataDto("Title", null, "Item", null, null,
                Arrays.asList("StartDate", "EndDate", "Description"));

        //When //Then
        ObjectNode expectingQuery = archiveSearchInternalService.createUpdateQuery(unitDescriptiveMetadataDto);

        JsonNode fromFile = JsonHandler.getFromFile(PropertiesUtils.findFile("data/queries/updateUnits/query_5.json"));
        Assertions.assertThat(expectingQuery.toPrettyString()).isEqualTo(fromFile.toPrettyString());
    }

    @Test
    public void testUpdateUnitDescriptiveMetadataWithUnsetFieldsTest6() throws Exception {
        // Given
        when(unitService.updateUnitById(any(), any(), any()))
            .thenReturn(buildUnitMetadataResponse(UPDATE_UNIT_DESCRIPTIVE_METADATA_RESPONSE));

        UnitDescriptiveMetadataDto unitDescriptiveMetadataDto =
            buildFullUnitDescriptiveMetadataDto(
                "french title", "english title", "french description", "english description", "Item", null, null,
                Arrays.asList("StartDate", "EndDate"));

        //When //Then
        ObjectNode expectingQuery = archiveSearchInternalService.createUpdateQuery(unitDescriptiveMetadataDto);

        JsonNode fromFile = JsonHandler.getFromFile(PropertiesUtils.findFile("data/queries/updateUnits/query_6.json"));

        Assertions.assertThat(expectingQuery.toPrettyString()).isEqualTo(fromFile.toPrettyString());
    }

    @Test
    public void testUpdateUnitDescriptiveMetadataWithUnsetFieldsTest7() throws Exception {
        // Given
        when(unitService.updateUnitById(any(), any(), any()))
            .thenReturn(buildUnitMetadataResponse(UPDATE_UNIT_DESCRIPTIVE_METADATA_RESPONSE));

        UnitDescriptiveMetadataDto unitDescriptiveMetadataDto =
            buildFullUnitDescriptiveMetadataDto(
                null, "english title", "french description", null, "Item", null, null,
                Arrays.asList("StartDate", "EndDate", "Title_.fr", "Description_.en"));

        //When //Then
        ObjectNode expectingQuery = archiveSearchInternalService.createUpdateQuery(unitDescriptiveMetadataDto);

        JsonNode fromFile = JsonHandler.getFromFile(PropertiesUtils.findFile("data/queries/updateUnits/query_7.json"));

        Assertions.assertThat(expectingQuery.toPrettyString()).isEqualTo(fromFile.toPrettyString());
    }

    private UnitDescriptiveMetadataDto buildUnitDescriptiveMetadataDto
        (String title, String description, String descriptionLevel, String startDate, String endDate,
            List<String> unsetAction) {
        UnitDescriptiveMetadataDto unitDescriptiveMetadataDto = new UnitDescriptiveMetadataDto();
        unitDescriptiveMetadataDto.setId("aeeaaaaaagh23tjvabz5gal6qlt6iaaaaaaq");
        unitDescriptiveMetadataDto.setTitle(title);
        unitDescriptiveMetadataDto.setDescription(description);
        unitDescriptiveMetadataDto.setDescriptionLevel(descriptionLevel);
        unitDescriptiveMetadataDto.setStartDate(startDate);
        unitDescriptiveMetadataDto.setEndDate(endDate);
        unitDescriptiveMetadataDto.setUnsetAction(unsetAction);
        return unitDescriptiveMetadataDto;
    }

    private UnitDescriptiveMetadataDto buildFullUnitDescriptiveMetadataDto
        (String title_fr, String title_en, String description_fr, String description_en, String descriptionLevel,
            String startDate, String endDate, List<String> unsetAction) {
        UnitDescriptiveMetadataDto unitDescriptiveMetadataDto = new UnitDescriptiveMetadataDto();
        unitDescriptiveMetadataDto.setId("aeeaaaaaagh23tjvabz5gal6qlt6iaaaaaaq");
        unitDescriptiveMetadataDto.setTitle_fr(title_fr);
        unitDescriptiveMetadataDto.setTitle_en(title_en);
        unitDescriptiveMetadataDto.setDescription_fr(description_fr);
        unitDescriptiveMetadataDto.setDescription_en(description_en);
        unitDescriptiveMetadataDto.setDescriptionLevel(descriptionLevel);
        unitDescriptiveMetadataDto.setStartDate(startDate);
        unitDescriptiveMetadataDto.setEndDate(endDate);
        unitDescriptiveMetadataDto.setUnsetAction(unsetAction);
        return unitDescriptiveMetadataDto;
    }

    @Test
    public void testUpdateArchiveUnitsRulesWithInCorrectAccessContractThenReturBadRequest() throws Exception {
        // Given
        when(unitService.massUpdateUnitsRules(any(), any()))
            .thenReturn(buildUnitMetadataResponse(UPDATE_RULES_ASYNC_RESPONSE));

        RequestResponseOK<AccessContractModel> response1 = new RequestResponseOK<>();
        response1.setHttpCode(200);
        response1.setHits(1, 1, 1, 1);
        response1.addResult(createAccessContractModel("contratTNR", "contrat d acces", 0, false));

        when(accessContractService.findAccessContractById(any(), any()))
            .thenReturn(response1);

        RequestResponse<AccessContractModel> requestResponse = Mockito.mock(RequestResponse.class);
        Mockito.when(accessContractService.findAccessContracts(ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(requestResponse);
        List<AccessContractModelDto> results =
            List.of(createAccessContractModelDto("contratTNR", "contrat d acces", 0, false));
        JsonHandler.toJsonNode(results);
        AccessContractResponseDto response = new AccessContractResponseDto();
        response.setResults(results);
        Mockito.when(objectMapper.treeToValue(requestResponse.toJsonNode(), AccessContractResponseDto.class))
            .thenReturn(response);

        // Configure the mapper
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        when(objectMapper.treeToValue(any(), any()))
            .thenReturn(createAccessContractsResponseDto("contratTNR", "contrat d acces", 0, false));

        SearchCriteriaDto searchQuery = new SearchCriteriaDto();
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto agencyCodeCriteria = new SearchCriteriaEltDto();
        agencyCodeCriteria.setCriteria(ArchiveSearchConsts.ORIGINATING_AGENCY_ID_FIELD);
        agencyCodeCriteria.setValues(List.of(new CriteriaValue("CODE1")));
        agencyCodeCriteria.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        criteriaList.add(agencyCodeCriteria);
        agencyCodeCriteria = new SearchCriteriaEltDto();
        agencyCodeCriteria.setCriteria(ArchiveSearchConsts.ORIGINATING_AGENCY_LABEL_FIELD);
        agencyCodeCriteria.setValues(List.of(new CriteriaValue("ANY_LABEL")));
        agencyCodeCriteria.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);

        SearchCriteriaEltDto searchCriteriaElementsNodes = new SearchCriteriaEltDto();
        searchCriteriaElementsNodes.setCriteria("NODE");
        searchCriteriaElementsNodes.setCategory(ArchiveSearchConsts.CriteriaCategory.NODES);
        searchCriteriaElementsNodes.setValues(
            Arrays.asList(new CriteriaValue("node1"), new CriteriaValue("node2"), new CriteriaValue("node3")));
        criteriaList.add(agencyCodeCriteria);
        criteriaList.add(searchCriteriaElementsNodes);
        searchQuery.setSize(20);
        searchQuery.setPageNumber(20);
        searchQuery.setCriteriaList(criteriaList);
        RuleSearchCriteriaDto ruleSearchCriteriaDto = new RuleSearchCriteriaDto();
        ruleSearchCriteriaDto.setSearchCriteriaDto(searchQuery);

        //When //Then
        assertThatCode(() -> {
            archiveSearchInternalService.updateArchiveUnitsRules(new VitamContext(1), ruleSearchCriteriaDto);
        }).hasMessage("the access contract using to update unit rules has no writing permission to update units");

    }

    AccessContractModel createAccessContractModel(String identifier, String name, Integer tenant,
        Boolean writingPermission) {
        AccessContractModel accessContractModel = new AccessContractModel();
        accessContractModel.setIdentifier(identifier);
        accessContractModel.setName(name);
        accessContractModel.setTenant(tenant);
        accessContractModel.setWritingPermission(writingPermission);
        return accessContractModel;
    }

    AccessContractsResponseDto createAccessContractsResponseDto(String identifier, String name, Integer tenant,
        Boolean writingPermission) {
        AccessContractsResponseDto accessContractModel = new AccessContractsResponseDto();
        accessContractModel
            .setResults(List.of(createAccessContractsVitamDto(identifier, name, tenant, writingPermission)));
        return accessContractModel;
    }

    AccessContractsVitamDto createAccessContractsVitamDto(String identifier, String name, Integer tenant,
        Boolean writingPermission) {
        AccessContractsVitamDto accessContractModel = new AccessContractsVitamDto();
        accessContractModel.setIdentifier(identifier);
        accessContractModel.setName(name);
        accessContractModel.setTenant(tenant);
        accessContractModel.setWritingPermission(writingPermission);
        return accessContractModel;
    }

    AccessContractModelDto createAccessContractModelDto(String identifier, String name, Integer tenant,
        Boolean writingPermission) {
        AccessContractModelDto accessContractModel = new AccessContractModelDto();
        accessContractModel.setIdentifier(identifier);
        accessContractModel.setName(name);
        accessContractModel.setTenant(tenant);
        accessContractModel.setWritingPermission(writingPermission);
        return accessContractModel;
    }



    @Test
    public void testExportCSVWithFrThenReturnTheExactExpectedFile() throws Exception {
        // Given
        setUpData();
        // query
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue("ARCHIVE_UNIT_WITH_OBJECTS"), new CriteriaValue("ARCHIVE_UNIT_WITHOUT_OBJECTS")));
        criteriaList.add(searchCriteriaEltDto);

        SearchCriteriaDto query = new SearchCriteriaDto();
        query.setLanguage(Locale.FRENCH.getLanguage());
        query.setSize(20);
        query.setPageNumber(20);
        query.setCriteriaList(criteriaList);

        Resource GivenResourceCsv = new ByteArrayResource(ArchiveSearchInternalService.class.getClassLoader()
            .getResourceAsStream(FR_ARCHIVE_UNITS_RESULTS_CSV).readAllBytes());
        // When
        Resource responseCsv =
            archiveSearchInternalService.exportToCsvSearchArchiveUnitsByCriteria(query, new VitamContext(1));

        // Then
        Assertions.assertThat(responseCsv).isNotNull();
        InputStream inputStream = responseCsv.getInputStream();

        // The exact match of available bytes number
        Assertions.assertThat(GivenResourceCsv.getInputStream().available())
            .isEqualTo(responseCsv.getInputStream().available());

        // Read the CSV InputStream
        List<String[]> results = readCSVFromInputStream(inputStream);
        List<String[]> resultsExpected = readCSVFromInputStream(GivenResourceCsv.getInputStream());

        // Two expecting entries here
        //1: The CSV Header
        Assertions.assertThat(results.get(0)[0]).isEqualTo(resultsExpected.get(0)[0]);
        //2: The first line content
        Assertions.assertThat(results.get(1)[0]).isEqualTo(resultsExpected.get(1)[0]);
        Assertions.assertThat(responseCsv).isNotNull();
    }

    @Test
    public void testExportCSVWithEnThenReturnTheExactExpectedFile() throws Exception {
        // Given
        setUpData();
        // query
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue("ARCHIVE_UNIT_WITH_OBJECTS"), new CriteriaValue("ARCHIVE_UNIT_WITHOUT_OBJECTS")));
        criteriaList.add(searchCriteriaEltDto);

        SearchCriteriaDto query = new SearchCriteriaDto();
        query.setLanguage(Locale.ENGLISH.getLanguage());
        query.setSize(20);
        query.setPageNumber(20);
        query.setCriteriaList(criteriaList);

        Resource GivenResourceCsv = new ByteArrayResource(ArchiveSearchInternalService.class.getClassLoader()
            .getResourceAsStream(EN_ARCHIVE_UNITS_RESULTS_CSV).readAllBytes());
        // When
        Resource responseCsv =
            archiveSearchInternalService.exportToCsvSearchArchiveUnitsByCriteria(query, new VitamContext(1));

        // Then
        Assertions.assertThat(responseCsv).isNotNull();
        InputStream inputStream = responseCsv.getInputStream();

        // The exact match of available bytes number
        Assertions.assertThat(GivenResourceCsv.getInputStream().available())
            .isEqualTo(responseCsv.getInputStream().available());

        // Read the CSV InputStream
        List<String[]> results = readCSVFromInputStream(inputStream);
        List<String[]> resultsExpected = readCSVFromInputStream(GivenResourceCsv.getInputStream());

        // Two expecting entries here
        //1: The CSV Header
        Assertions.assertThat(results.get(0)[0]).isEqualTo(resultsExpected.get(0)[0]);
        //2: The first line content
        Assertions.assertThat(results.get(1)[0]).isEqualTo(resultsExpected.get(1)[0]);
        Assertions.assertThat(responseCsv).isNotNull();
    }

    @Test
    public void testExportCSVWithFrAndSpecialCharsThenReturnTheExactExpectedContentCorrectlyEncoded() throws Exception {
        // Given
        when(unitService.searchUnits(any(), any()))
            .thenReturn(buildUnitMetadataResponse(VITAM_UNIT_ONE_RESULT_TO_ENCODE));
        when(agencyService.findAgencies(any(), any())).thenReturn(buildAgenciesResponse());

        when(archiveSearchAgenciesInternalService.findOriginAgenciesByCodes(any(), any()))
            .thenReturn(List.of(buildAgencyModelDtos()));

        RequestResponse<JsonNode> jsonNodeRequestResponse = buildArchiveUnit(VITAM_UNIT_RESULTS_TO_ENCODE);
        ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);

        when(archiveSearchAgenciesInternalService.fillOriginatingAgencyName(any(), any()))
            .thenReturn(buildArchiveUnits(resultsDto));

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        when(objectMapper.treeToValue(any(), any()))
            .thenReturn(buildVitamUISearchResponseDto(VITAM_UNIT_ONE_RESULT_TO_ENCODE));
        // query
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue("ARCHIVE_UNIT_WITH_OBJECTS"), new CriteriaValue("ARCHIVE_UNIT_WITHOUT_OBJECTS")));
        criteriaList.add(searchCriteriaEltDto);

        SearchCriteriaDto query = new SearchCriteriaDto();
        query.setLanguage(Locale.FRENCH.getLanguage());
        query.setSize(20);
        query.setPageNumber(20);
        query.setCriteriaList(criteriaList);

        Resource GivenResourceCsv = new ByteArrayResource(ArchiveSearchInternalService.class.getClassLoader()
            .getResourceAsStream(ARCHIVE_UNITS_WITH_CONTENT_TO_ENCODE_CORRECTLY).readAllBytes());
        // When
        Resource responseCsv =
            archiveSearchInternalService.exportToCsvSearchArchiveUnitsByCriteria(query, new VitamContext(1));

        // Then
        Assertions.assertThat(responseCsv).isNotNull();
        InputStream inputStream = responseCsv.getInputStream();


        // The exact match of available bytes number
        Assertions.assertThat(GivenResourceCsv.getInputStream().available())
            .isEqualTo(responseCsv.getInputStream().available());
        Assertions.assertThat(inputStream.available()).isPositive();
        // Read the CSV InputStream
        List<String[]> results = readCSVFromInputStream(inputStream);
        List<String[]> resultsExpected = readCSVFromInputStream(GivenResourceCsv.getInputStream());

        // Two expecting entries here
        //1: The CSV Header
        Assertions.assertThat(results.get(0)[0]).isEqualTo(resultsExpected.get(0)[0]);
        //2: The first line content
        Assertions.assertThat(results.get(1)[0]).isEqualTo(resultsExpected.get(1)[0]);
        Assertions.assertThat(responseCsv).isNotNull();
    }

    @Test
    public void testExportCSVWithEnThenReturnTheExactExpectedFileAsFilingUnit() throws Exception {
        // Given
        when(unitService.searchUnits(any(), any()))
            .thenReturn(buildUnitMetadataResponse(VITAM_UNIT_ONE_RESULT_FILING_UNIT));
        when(agencyService.findAgencies(any(), any())).thenReturn(buildAgenciesResponse());

        when(archiveSearchAgenciesInternalService.findOriginAgenciesByCodes(any(), any()))
            .thenReturn(List.of(buildAgencyModelDtos()));

        RequestResponse<JsonNode> jsonNodeRequestResponse = buildArchiveUnit(VITAM_UNIT_RESULTS_FILING_UNIT);
        ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);

        when(archiveSearchAgenciesInternalService.fillOriginatingAgencyName(any(), any()))
            .thenReturn(buildArchiveUnits(resultsDto));

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        when(objectMapper.treeToValue(any(), any()))
            .thenReturn(buildVitamUISearchResponseDto(VITAM_UNIT_ONE_RESULT_FILING_UNIT));
        // query
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue("ARCHIVE_UNIT_WITH_OBJECTS"), new CriteriaValue("ARCHIVE_UNIT_WITHOUT_OBJECTS")));
        criteriaList.add(searchCriteriaEltDto);

        SearchCriteriaDto query = new SearchCriteriaDto();
        query.setLanguage(Locale.FRENCH.getLanguage());
        query.setSize(20);
        query.setPageNumber(20);
        query.setCriteriaList(criteriaList);

        Resource GivenResourceCsv = new ByteArrayResource(ArchiveSearchInternalService.class.getClassLoader()
            .getResourceAsStream(ARCHIVE_UNITS_WITH_CONTENT_FILING_UNIT).readAllBytes());
        // When
        Resource responseCsv =
            archiveSearchInternalService.exportToCsvSearchArchiveUnitsByCriteria(query, new VitamContext(1));

        // Then
        Assertions.assertThat(responseCsv).isNotNull();
        InputStream inputStream = responseCsv.getInputStream();


        // The exact match of available bytes number
        Assertions.assertThat(GivenResourceCsv.getInputStream().available())
            .isEqualTo(responseCsv.getInputStream().available());
        Assertions.assertThat(inputStream.available()).isPositive();
        // Read the CSV InputStream
        List<String[]> results = readCSVFromInputStream(inputStream);
        List<String[]> resultsExpected = readCSVFromInputStream(GivenResourceCsv.getInputStream());

        // Two expecting entries here
        //1: The CSV Header
        Assertions.assertThat(results.get(0)[0]).isEqualTo(resultsExpected.get(0)[0]);
        //2: The first line content
        Assertions.assertThat(results.get(1)[0]).isEqualTo(resultsExpected.get(1)[0]);
        Assertions.assertThat(responseCsv).isNotNull();
    }

    @Test
    public void testExportCSVWithEnThenReturnTheExactExpectedFileAsHoldingUnit() throws Exception {
        // Given
        when(unitService.searchUnits(any(), any()))
            .thenReturn(buildUnitMetadataResponse(VITAM_UNIT_ONE_RESULT_HOLDING_UNIT));
        when(agencyService.findAgencies(any(), any())).thenReturn(buildAgenciesResponse());

        when(archiveSearchAgenciesInternalService.findOriginAgenciesByCodes(any(), any()))
            .thenReturn(List.of(buildAgencyModelDtos()));

        RequestResponse<JsonNode> jsonNodeRequestResponse = buildArchiveUnit(VITAM_UNIT_RESULTS_HOLDING_UNIT);
        ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);

        when(archiveSearchAgenciesInternalService.fillOriginatingAgencyName(any(), any()))
            .thenReturn(buildArchiveUnits(resultsDto));

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        when(objectMapper.treeToValue(any(), any()))
            .thenReturn(buildVitamUISearchResponseDto(VITAM_UNIT_ONE_RESULT_HOLDING_UNIT));
        // query
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue("ARCHIVE_UNIT_WITH_OBJECTS"), new CriteriaValue("ARCHIVE_UNIT_WITHOUT_OBJECTS")));
        criteriaList.add(searchCriteriaEltDto);

        SearchCriteriaDto query = new SearchCriteriaDto();
        query.setLanguage(Locale.FRENCH.getLanguage());
        query.setSize(20);
        query.setPageNumber(20);
        query.setCriteriaList(criteriaList);

        Resource GivenResourceCsv = new ByteArrayResource(ArchiveSearchInternalService.class.getClassLoader()
            .getResourceAsStream(ARCHIVE_UNITS_WITH_CONTENT_HOLDING_UNIT).readAllBytes());
        // When
        Resource responseCsv =
            archiveSearchInternalService.exportToCsvSearchArchiveUnitsByCriteria(query, new VitamContext(1));

        // Then
        Assertions.assertThat(responseCsv).isNotNull();
        InputStream inputStream = responseCsv.getInputStream();


        // The exact match of available bytes number
        Assertions.assertThat(GivenResourceCsv.getInputStream().available())
            .isEqualTo(responseCsv.getInputStream().available());
        Assertions.assertThat(inputStream.available()).isPositive();
        // Read the CSV InputStream
        List<String[]> results = readCSVFromInputStream(inputStream);
        List<String[]> resultsExpected = readCSVFromInputStream(GivenResourceCsv.getInputStream());

        // Two expecting entries here
        //1: The CSV Header
        Assertions.assertThat(results.get(0)[0]).isEqualTo(resultsExpected.get(0)[0]);
        //2: The first line content
        Assertions.assertThat(results.get(1)[0]).isEqualTo(resultsExpected.get(1)[0]);
        Assertions.assertThat(responseCsv).isNotNull();
    }

    @Test
    public void testExportCSVWithEnThenReturnTheExactExpectedFileAsUnitWithObject() throws Exception {
        // Given
        when(unitService.searchUnits(any(), any()))
            .thenReturn(buildUnitMetadataResponse(VITAM_UNIT_ONE_RESULT_UNIT_WITH_OBJECT));
        when(agencyService.findAgencies(any(), any())).thenReturn(buildAgenciesResponse());

        when(archiveSearchAgenciesInternalService.findOriginAgenciesByCodes(any(), any()))
            .thenReturn(List.of(buildAgencyModelDtos()));

        RequestResponse<JsonNode> jsonNodeRequestResponse = buildArchiveUnit(VITAM_UNIT_RESULTS_UNIT_WITH_OBJECT);
        ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);

        when(archiveSearchAgenciesInternalService.fillOriginatingAgencyName(any(), any()))
            .thenReturn(buildArchiveUnits(resultsDto));

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        when(objectMapper.treeToValue(any(), any()))
            .thenReturn(buildVitamUISearchResponseDto(VITAM_UNIT_ONE_RESULT_UNIT_WITH_OBJECT));
        // query
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue("ARCHIVE_UNIT_WITH_OBJECTS"), new CriteriaValue("ARCHIVE_UNIT_WITHOUT_OBJECTS")));
        criteriaList.add(searchCriteriaEltDto);

        SearchCriteriaDto query = new SearchCriteriaDto();
        query.setLanguage(Locale.FRENCH.getLanguage());
        query.setSize(20);
        query.setPageNumber(20);
        query.setCriteriaList(criteriaList);

        Resource GivenResourceCsv = new ByteArrayResource(ArchiveSearchInternalService.class.getClassLoader()
            .getResourceAsStream(ARCHIVE_UNITS_WITH_CONTENT_UNIT_WITH_OBJECT).readAllBytes());
        // When
        Resource responseCsv =
            archiveSearchInternalService.exportToCsvSearchArchiveUnitsByCriteria(query, new VitamContext(1));

        // Then
        Assertions.assertThat(responseCsv).isNotNull();
        InputStream inputStream = responseCsv.getInputStream();


        // The exact match of available bytes number
        Assertions.assertThat(GivenResourceCsv.getInputStream().available())
            .isEqualTo(responseCsv.getInputStream().available());
        Assertions.assertThat(inputStream.available()).isPositive();
        // Read the CSV InputStream
        List<String[]> results = readCSVFromInputStream(inputStream);
        List<String[]> resultsExpected = readCSVFromInputStream(GivenResourceCsv.getInputStream());

        // Two expecting entries here
        //1: The CSV Header
        Assertions.assertThat(results.get(0)[0]).isEqualTo(resultsExpected.get(0)[0]);
        //2: The first line content
        Assertions.assertThat(results.get(1)[0]).isEqualTo(resultsExpected.get(1)[0]);
        Assertions.assertThat(responseCsv).isNotNull();
    }

    @Test
    public void testExportCSVWithEnThenReturnTheExactExpectedFileAsUnitWithoutObject() throws Exception {
        // Given
        when(unitService.searchUnits(any(), any()))
            .thenReturn(buildUnitMetadataResponse(VITAM_UNIT_ONE_RESULT_UNIT_WITHOUT_OBJECT));
        when(agencyService.findAgencies(any(), any())).thenReturn(buildAgenciesResponse());

        when(archiveSearchAgenciesInternalService.findOriginAgenciesByCodes(any(), any()))
            .thenReturn(List.of(buildAgencyModelDtos()));

        RequestResponse<JsonNode> jsonNodeRequestResponse = buildArchiveUnit(VITAM_UNIT_RESULTS_UNIT_WITHOUT_OBJECT);
        ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);

        when(archiveSearchAgenciesInternalService.fillOriginatingAgencyName(any(), any()))
            .thenReturn(buildArchiveUnits(resultsDto));

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        when(objectMapper.treeToValue(any(), any()))
            .thenReturn(buildVitamUISearchResponseDto(VITAM_UNIT_ONE_RESULT_UNIT_WITHOUT_OBJECT));
        // query
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto searchCriteriaEltDto = new SearchCriteriaEltDto();
        searchCriteriaEltDto.setCriteria(ArchiveSearchConsts.ALL_ARCHIVE_UNIT_TYPES_CRITERIA);
        searchCriteriaEltDto.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        searchCriteriaEltDto.setValues(
            List.of(new CriteriaValue("ARCHIVE_UNIT_WITH_OBJECTS"), new CriteriaValue("ARCHIVE_UNIT_WITHOUT_OBJECTS")));
        criteriaList.add(searchCriteriaEltDto);

        SearchCriteriaDto query = new SearchCriteriaDto();
        query.setLanguage(Locale.FRENCH.getLanguage());
        query.setSize(20);
        query.setPageNumber(20);
        query.setCriteriaList(criteriaList);

        Resource GivenResourceCsv = new ByteArrayResource(ArchiveSearchInternalService.class.getClassLoader()
            .getResourceAsStream(ARCHIVE_UNITS_WITH_CONTENT_UNIT_WITHOUT_OBJECT).readAllBytes());
        // When
        Resource responseCsv =
            archiveSearchInternalService.exportToCsvSearchArchiveUnitsByCriteria(query, new VitamContext(1));

        // Then
        Assertions.assertThat(responseCsv).isNotNull();
        InputStream inputStream = responseCsv.getInputStream();


        // The exact match of available bytes number
        Assertions.assertThat(GivenResourceCsv.getInputStream().available())
            .isEqualTo(responseCsv.getInputStream().available());
        Assertions.assertThat(inputStream.available()).isPositive();
        // Read the CSV InputStream
        List<String[]> results = readCSVFromInputStream(inputStream);
        List<String[]> resultsExpected = readCSVFromInputStream(GivenResourceCsv.getInputStream());

        // Two expecting entries here
        //1: The CSV Header
        Assertions.assertThat(results.get(0)[0]).isEqualTo(resultsExpected.get(0)[0]);
        //2: The first line content
        Assertions.assertThat(results.get(1)[0]).isEqualTo(resultsExpected.get(1)[0]);
        Assertions.assertThat(responseCsv).isNotNull();
    }

    private void setUpData() throws Exception {
        when(unitService.searchUnits(any(), any()))
            .thenReturn(buildUnitMetadataResponse(VITAM_UNIT_RESULTS));
        when(agencyService.findAgencies(any(), any())).thenReturn(buildAgenciesResponse());

        when(archiveSearchAgenciesInternalService.findOriginAgenciesByCodes(any(), any()))
            .thenReturn(List.of(buildAgencyModelDtos()));

        RequestResponse<JsonNode> jsonNodeRequestResponse = buildArchiveUnit(VITAM_UNIT_ONE_RESULTS);
        ResultsDto resultsDto = buildResults(jsonNodeRequestResponse);

        when(archiveSearchAgenciesInternalService.fillOriginatingAgencyName(any(), any()))
            .thenReturn(buildArchiveUnits(resultsDto));

        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        when(objectMapper.treeToValue(any(), any())).thenReturn(buildVitamUISearchResponseDto(VITAM_UNIT_RESULTS));
    }

    @SneakyThrows
    private List<String[]> readCSVFromInputStream(InputStream inputStream) {
        List<String[]> results;
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream))) {
            results = reader.readAll();
        } catch (IOException | CsvException e) {
            throw e;
        }
        return results;
    }

    private ArchiveUnit buildArchiveUnits(ResultsDto resultsDto) {
        ArchiveUnit archiveUnit = new ArchiveUnit();
        BeanUtils.copyProperties(resultsDto, archiveUnit);
        return archiveUnit;
    }

    private RequestResponse<JsonNode> buildArchiveUnit(String filename)
        throws IOException, InvalidParseOperationException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream inputStream = ArchiveSearchInternalServiceTest.class.getClassLoader()
            .getResourceAsStream(filename);
        return RequestResponseOK
            .getFromJsonNode(objectMapper.readValue(ByteStreams.toByteArray(inputStream), JsonNode.class));
    }

    private ResultsDto buildResults(RequestResponse<JsonNode> jsonNodeRequestResponse) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String re = StringUtils.chop(jsonNodeRequestResponse.toJsonNode().get("$results").toString().substring(1));
        return objectMapper.readValue(re, ResultsDto.class);
    }

    private AgencyModelDto buildAgencyModelDtos() {
        AgencyModelDto agencyModelDto = new AgencyModelDto();
        agencyModelDto.setIdentifier("FRAN_NP_009915");
        agencyModelDto.setName("name");
        agencyModelDto.setTenant(1);
        return agencyModelDto;
    }

    AgenciesModel createAgencyModel(String identifier, String name, Integer tenant) {

        AgenciesModel agency = new AgenciesModel();
        agency.setIdentifier(identifier);
        agency.setName(name);
        agency.setTenant(tenant);
        return agency;
    }

    private VitamUISearchResponseDto buildVitamUISearchResponseDto(String filename) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream inputStream = ArchiveSearchInternalServiceTest.class.getClassLoader()
            .getResourceAsStream(filename);
        return objectMapper.readValue(ByteStreams.toByteArray(inputStream), VitamUISearchResponseDto.class);
    }

    private RequestResponse<AgenciesModel> buildAgenciesResponse() {
        JsonNode query = null;
        List<AgenciesModel> agenciesModelList = List.of(createAgencyModel("FRAN_NP_009915", "Service producteur1", 0));
        RequestResponseOK response =
            new RequestResponseOK<>(query, agenciesModelList, agenciesModelList.size()).setHttpCode(400);

        return response;
    }
}
