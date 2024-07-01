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
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.configuration.BuilderToken;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationAction;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationQueryActionType;
import fr.gouv.vitamui.archives.search.common.dto.UnitDescriptiveMetadataDto;
import fr.gouv.vitamui.commons.api.dtos.CriteriaValue;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ArchiveSearchInternalServiceTest {

    @MockBean(name = "objectMapper")
    private ObjectMapper objectMapper;

    @MockBean(name = "unitService")
    private UnitService unitService;

    @MockBean(name = "archiveSearchAgenciesInternalService")
    private ArchiveSearchAgenciesInternalService archiveSearchAgenciesInternalService;

    @MockBean(name = "archiveSearchRulesInternalService")
    private ArchiveSearchRulesInternalService archiveSearchRulesInternalService;

    @InjectMocks
    private ArchiveSearchInternalService archiveSearchInternalService;

    @MockBean(name = "archiveSearchInternalService")
    private ArchiveSearchFacetsInternalService archiveSearchFacetsInternalService;

    public final String FILING_HOLDING_SCHEME_RESULTS = "data/vitam_filing_holding_units_response.json";
    public final String UPDATE_RULES_ASYNC_RESPONSE = "data/update_rules_async_response.json";
    public final String UPDATE_UNIT_DESCRIPTIVE_METADATA_RESPONSE =
        "data/update_unit_descriptive_metadata_response.json";
    public final String FILLING_HOLDING_SCHEME_EXPECTED_QUERY = "data/fillingholding/expected_query.json";

    public final String FILLING_HOLDING_SCHEME_QUERY = "data/fillingholding/expected_unitType_query.json";

    @BeforeEach
    public void setUp() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        archiveSearchInternalService = new ArchiveSearchInternalService(
            objectMapper,
            unitService,
            archiveSearchAgenciesInternalService,
            archiveSearchRulesInternalService,
            archiveSearchFacetsInternalService
        );
    }

    @Test
    void testSearchFilingHoldingSchemeResultsThanReturnVitamUISearchResponseDto()
        throws VitamClientException, IOException, InvalidParseOperationException {
        // Given
        when(unitService.searchUnits(any(), any())).thenReturn(
            buildUnitMetadataResponse(FILING_HOLDING_SCHEME_RESULTS)
        );
        // When
        JsonNode jsonNode = archiveSearchInternalService.searchArchiveUnits(any(), any());

        // Configure the mapper
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        VitamUISearchResponseDto vitamUISearchResponseDto = objectMapper.treeToValue(
            jsonNode,
            VitamUISearchResponseDto.class
        );

        // Then
        Assertions.assertThat(vitamUISearchResponseDto).isNotNull();
        Assertions.assertThat(vitamUISearchResponseDto.getResults()).hasSize(20);
    }

    private RequestResponse<JsonNode> buildUnitMetadataResponse(String filename)
        throws IOException, InvalidParseOperationException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream inputStream = ArchiveSearchInternalServiceTest.class.getClassLoader().getResourceAsStream(filename);
        Assertions.assertThat(inputStream).isNotNull();
        return RequestResponseOK.getFromJsonNode(
            objectMapper.readValue(ByteStreams.toByteArray(inputStream), JsonNode.class)
        );
    }

    @Test
    public void getFinalFillingHoldingSchemeQuery() throws Exception {
        // Given
        JsonNode expectedQuery = JsonHandler.getFromFile(
            PropertiesUtils.findFile(FILLING_HOLDING_SCHEME_EXPECTED_QUERY)
        );

        // When
        JsonNode givenQuery = archiveSearchInternalService.createQueryForHoldingFillingUnit();

        // Then
        Assertions.assertThat(expectedQuery.toString()).hasToString(String.valueOf(givenQuery));
        Assertions.assertThat(
            givenQuery
                .get(BuilderToken.GLOBAL.FILTER.exactToken())
                .get(BuilderToken.SELECTFILTER.ORDERBY.exactToken())
                .has("Title")
        ).isTrue();
    }

    @Test
    public void testReclassificationThenOK() throws Exception {
        // Given
        when(unitService.reclassification(any(), any())).thenReturn(
            buildUnitMetadataResponse(UPDATE_RULES_ASYNC_RESPONSE)
        );

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
            Arrays.asList(new CriteriaValue("node1"), new CriteriaValue("node2"), new CriteriaValue("node3"))
        );
        criteriaList.add(agencyCodeCriteria);
        criteriaList.add(searchCriteriaElementsNodes);
        searchQuery.setSize(20);
        searchQuery.setPageNumber(20);
        searchQuery.setCriteriaList(criteriaList);

        ReclassificationCriteriaDto reclassificationCriteriaDto = buildReclassificationCriteriaDto();
        reclassificationCriteriaDto.setSearchCriteriaDto(searchQuery);

        //When //Then
        String expectingGuid = archiveSearchInternalService.reclassification(
            reclassificationCriteriaDto,
            new VitamContext(1)
        );
        assertThatCode(() -> {
            archiveSearchInternalService.reclassification(reclassificationCriteriaDto, new VitamContext(1));
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
        when(unitService.updateUnitById(any(), any(), any())).thenReturn(
            buildUnitMetadataResponse(UPDATE_UNIT_DESCRIPTIVE_METADATA_RESPONSE)
        );

        UnitDescriptiveMetadataDto unitDescriptiveMetadataDto = buildUnitDescriptiveMetadataDto(
            null,
            null,
            null,
            null,
            "01/01/2023",
            Arrays.asList("StartDate")
        );

        //When //Then
        ObjectNode expectingQuery = archiveSearchInternalService.createUpdateQuery(unitDescriptiveMetadataDto);
        JsonNode fromFile = JsonHandler.getFromFile(PropertiesUtils.findFile("data/queries/updateUnits/query_1.json"));
        Assertions.assertThat(expectingQuery.toPrettyString()).isEqualTo(fromFile.toPrettyString());
    }

    @Test
    void testUpdateUnitDescriptiveMetadataWithUnsetFieldsTest2() throws Exception {
        // Given
        when(unitService.updateUnitById(any(), any(), any())).thenReturn(
            buildUnitMetadataResponse(UPDATE_UNIT_DESCRIPTIVE_METADATA_RESPONSE)
        );

        UnitDescriptiveMetadataDto unitDescriptiveMetadataDto = buildUnitDescriptiveMetadataDto(
            "some title",
            null,
            null,
            null,
            "01/01/2023",
            Arrays.asList("StartDate", "Description")
        );

        //When //Then
        ObjectNode expectingQuery = archiveSearchInternalService.createUpdateQuery(unitDescriptiveMetadataDto);

        JsonNode fromFile = JsonHandler.getFromFile(PropertiesUtils.findFile("data/queries/updateUnits/query_2.json"));
        Assertions.assertThat(expectingQuery.toPrettyString()).isEqualTo(fromFile.toPrettyString());
    }

    @Test
    public void testUpdateUnitDescriptiveMetadataWithUnsetFieldsTest3() throws Exception {
        // Given
        when(unitService.updateUnitById(any(), any(), any())).thenReturn(
            buildUnitMetadataResponse(UPDATE_UNIT_DESCRIPTIVE_METADATA_RESPONSE)
        );

        UnitDescriptiveMetadataDto unitDescriptiveMetadataDto = buildUnitDescriptiveMetadataDto(
            "some title",
            "some description",
            "Item",
            null,
            null,
            Arrays.asList("StartDate", "EndDate")
        );

        //When //Then
        ObjectNode expectingQuery = archiveSearchInternalService.createUpdateQuery(unitDescriptiveMetadataDto);

        JsonNode fromFile = JsonHandler.getFromFile(PropertiesUtils.findFile("data/queries/updateUnits/query_3.json"));
        Assertions.assertThat(expectingQuery.toPrettyString()).isEqualTo(fromFile.toPrettyString());
    }

    @Test
    public void testUpdateUnitDescriptiveMetadataWithUnsetFieldsTest4() throws Exception {
        // Given
        when(unitService.updateUnitById(any(), any(), any())).thenReturn(
            buildUnitMetadataResponse(UPDATE_UNIT_DESCRIPTIVE_METADATA_RESPONSE)
        );

        UnitDescriptiveMetadataDto unitDescriptiveMetadataDto = buildUnitDescriptiveMetadataDto(
            null,
            null,
            null,
            null,
            null,
            Arrays.asList("StartDate", "EndDate", "Description")
        );

        //When //Then
        ObjectNode expectingQuery = archiveSearchInternalService.createUpdateQuery(unitDescriptiveMetadataDto);

        JsonNode fromFile = JsonHandler.getFromFile(PropertiesUtils.findFile("data/queries/updateUnits/query_4.json"));
        Assertions.assertThat(expectingQuery.toPrettyString()).isEqualTo(fromFile.toPrettyString());
    }

    @Test
    public void testUpdateUnitDescriptiveMetadataWithUnsetFieldsTest5() throws Exception {
        // Given
        when(unitService.updateUnitById(any(), any(), any())).thenReturn(
            buildUnitMetadataResponse(UPDATE_UNIT_DESCRIPTIVE_METADATA_RESPONSE)
        );

        UnitDescriptiveMetadataDto unitDescriptiveMetadataDto = buildUnitDescriptiveMetadataDto(
            "Title",
            null,
            "Item",
            null,
            null,
            Arrays.asList("StartDate", "EndDate", "Description")
        );

        //When //Then
        ObjectNode expectingQuery = archiveSearchInternalService.createUpdateQuery(unitDescriptiveMetadataDto);

        JsonNode fromFile = JsonHandler.getFromFile(PropertiesUtils.findFile("data/queries/updateUnits/query_5.json"));
        Assertions.assertThat(expectingQuery.toPrettyString()).isEqualTo(fromFile.toPrettyString());
    }

    @Test
    public void testUpdateUnitDescriptiveMetadataWithUnsetFieldsTest6() throws Exception {
        // Given
        when(unitService.updateUnitById(any(), any(), any())).thenReturn(
            buildUnitMetadataResponse(UPDATE_UNIT_DESCRIPTIVE_METADATA_RESPONSE)
        );

        UnitDescriptiveMetadataDto unitDescriptiveMetadataDto = buildFullUnitDescriptiveMetadataDto(
            "french title",
            "english title",
            "french description",
            "english description",
            "Item",
            null,
            null,
            Arrays.asList("StartDate", "EndDate")
        );

        //When //Then
        ObjectNode expectingQuery = archiveSearchInternalService.createUpdateQuery(unitDescriptiveMetadataDto);

        JsonNode fromFile = JsonHandler.getFromFile(PropertiesUtils.findFile("data/queries/updateUnits/query_6.json"));

        Assertions.assertThat(expectingQuery.toPrettyString()).isEqualTo(fromFile.toPrettyString());
    }

    @Test
    public void testUpdateUnitDescriptiveMetadataWithUnsetFieldsTest7() throws Exception {
        // Given
        when(unitService.updateUnitById(any(), any(), any())).thenReturn(
            buildUnitMetadataResponse(UPDATE_UNIT_DESCRIPTIVE_METADATA_RESPONSE)
        );

        UnitDescriptiveMetadataDto unitDescriptiveMetadataDto = buildFullUnitDescriptiveMetadataDto(
            null,
            "english title",
            "french description",
            null,
            "Item",
            null,
            null,
            Arrays.asList("StartDate", "EndDate", "Title_.fr", "Description_.en")
        );

        //When //Then
        ObjectNode expectingQuery = archiveSearchInternalService.createUpdateQuery(unitDescriptiveMetadataDto);

        JsonNode fromFile = JsonHandler.getFromFile(PropertiesUtils.findFile("data/queries/updateUnits/query_7.json"));

        Assertions.assertThat(expectingQuery.toPrettyString()).isEqualTo(fromFile.toPrettyString());
    }

    @Test
    void getFinalFillingHoldingSchemeQueryWithProjection() throws Exception {
        // Given
        JsonNode expectedQuery = JsonHandler.getFromFile(PropertiesUtils.findFile(FILLING_HOLDING_SCHEME_QUERY));

        // When
        JsonNode givenQuery = archiveSearchInternalService.createQueryForHoldingFillingUnit();

        // Then
        Assertions.assertThat(expectedQuery.toString()).hasToString(String.valueOf(givenQuery));
        Assertions.assertThat(
            givenQuery
                .get(BuilderToken.GLOBAL.FILTER.exactToken())
                .get(BuilderToken.SELECTFILTER.ORDERBY.exactToken())
                .has("Title")
        ).isTrue();
    }

    @Test
    void getFinalFillingHoldingSchemeQueryWithAllProjectionFields() throws Exception {
        // Given
        JsonNode expectedQuery = JsonHandler.getFromFile(PropertiesUtils.findFile(FILLING_HOLDING_SCHEME_QUERY));

        // When
        JsonNode givenQuery = archiveSearchInternalService.createQueryForHoldingFillingUnit();

        // Then
        Assertions.assertThat(expectedQuery.toString()).hasToString(String.valueOf(givenQuery));
        Assertions.assertThat(
            givenQuery
                .get(BuilderToken.GLOBAL.PROJECTION.exactToken())
                .get(BuilderToken.PROJECTION.FIELDS.exactToken())
                .has("#object")
        ).isTrue();
        Assertions.assertThat(
            givenQuery
                .get(BuilderToken.GLOBAL.PROJECTION.exactToken())
                .get(BuilderToken.PROJECTION.FIELDS.exactToken())
                .has("#unitType")
        ).isTrue();
        Assertions.assertThat(
            givenQuery
                .get(BuilderToken.GLOBAL.PROJECTION.exactToken())
                .get(BuilderToken.PROJECTION.FIELDS.exactToken())
                .has("#id")
        ).isTrue();
        Assertions.assertThat(
            givenQuery
                .get(BuilderToken.GLOBAL.PROJECTION.exactToken())
                .get(BuilderToken.PROJECTION.FIELDS.exactToken())
                .has("DescriptionLevel")
        ).isTrue();
    }

    private UnitDescriptiveMetadataDto buildUnitDescriptiveMetadataDto(
        String title,
        String description,
        String descriptionLevel,
        String startDate,
        String endDate,
        List<String> unsetAction
    ) {
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

    private UnitDescriptiveMetadataDto buildFullUnitDescriptiveMetadataDto(
        String title_fr,
        String title_en,
        String description_fr,
        String description_en,
        String descriptionLevel,
        String startDate,
        String endDate,
        List<String> unsetAction
    ) {
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
}
