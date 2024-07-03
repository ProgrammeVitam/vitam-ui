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
import com.google.common.io.ByteStreams;
import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.configuration.BuilderToken;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationAction;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationQueryActionType;
import fr.gouv.vitamui.commons.api.dtos.CriteriaValue;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
import fr.gouv.vitamui.commons.vitam.api.access.PersistentIdentifierService;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.dto.PersistentIdentifierResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
public class ArchiveSearchInternalServiceTest {

    VitamContext defaultVitamContext = new VitamContext(1);
    private ObjectMapper simpleObjectMapper = new ObjectMapper();

    @MockBean(name = "unitService")
    private UnitService unitService;

    @MockBean(name = "persistentIdentifierService")
    private PersistentIdentifierService persistentIdentifierService;

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
        archiveSearchInternalService = new ArchiveSearchInternalService(
            simpleObjectMapper,
            unitService,
            archiveSearchAgenciesInternalService,
            archiveSearchRulesInternalService,
            archiveSearchFacetsInternalService,
            persistentIdentifierService
        );
    }

    @Test
    void testSearchFilingHoldingSchemeResultsThanReturnVitamUISearchResponseDto() throws Exception {
        // Given
        when(unitService.searchUnits(any(), any())).thenReturn(responseFromFile(FILING_HOLDING_SCHEME_RESULTS));
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
        assertThat(vitamUISearchResponseDto).isNotNull();
        assertThat(vitamUISearchResponseDto.getResults()).hasSize(20);
    }

    private RequestResponse<JsonNode> responseFromFile(String filename) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream inputStream = ArchiveSearchInternalServiceTest.class.getClassLoader().getResourceAsStream(filename);
        assertThat(inputStream).isNotNull();
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
        assertThat(expectedQuery.toString()).hasToString(String.valueOf(givenQuery));
        assertThat(
            givenQuery
                .get(BuilderToken.GLOBAL.FILTER.exactToken())
                .get(BuilderToken.SELECTFILTER.ORDERBY.exactToken())
                .has("Title")
        ).isTrue();
    }

    @Test
    public void testReclassificationThenOK() throws Exception {
        // Given
        when(unitService.reclassification(any(), any())).thenReturn(responseFromFile(UPDATE_RULES_ASYNC_RESPONSE));

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
            defaultVitamContext
        );
        assertThatCode(() -> {
            archiveSearchInternalService.reclassification(reclassificationCriteriaDto, defaultVitamContext);
        }).doesNotThrowAnyException();

        assertThat(expectingGuid).isEqualTo("aeeaaaaaagh23tjvabz5gal6qlt6iaaaaaaq");
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
    void getFinalFillingHoldingSchemeQueryWithProjection() throws Exception {
        // Given
        JsonNode expectedQuery = JsonHandler.getFromFile(PropertiesUtils.findFile(FILLING_HOLDING_SCHEME_QUERY));

        // When
        JsonNode givenQuery = archiveSearchInternalService.createQueryForHoldingFillingUnit();

        // Then
        assertThat(expectedQuery.toString()).hasToString(String.valueOf(givenQuery));
        assertThat(
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
        assertThat(expectedQuery.toString()).hasToString(String.valueOf(givenQuery));
        assertThat(
            givenQuery
                .get(BuilderToken.GLOBAL.PROJECTION.exactToken())
                .get(BuilderToken.PROJECTION.FIELDS.exactToken())
                .has("#object")
        ).isTrue();
        assertThat(
            givenQuery
                .get(BuilderToken.GLOBAL.PROJECTION.exactToken())
                .get(BuilderToken.PROJECTION.FIELDS.exactToken())
                .has("#unitType")
        ).isTrue();
        assertThat(
            givenQuery
                .get(BuilderToken.GLOBAL.PROJECTION.exactToken())
                .get(BuilderToken.PROJECTION.FIELDS.exactToken())
                .has("#id")
        ).isTrue();
        assertThat(
            givenQuery
                .get(BuilderToken.GLOBAL.PROJECTION.exactToken())
                .get(BuilderToken.PROJECTION.FIELDS.exactToken())
                .has("DescriptionLevel")
        ).isTrue();
    }

    @Test
    void findUnitsByPersistentIdentifier_return_units() throws Exception {
        // Given
        String arkId = "ark:/22567/001a957db5eadaac";
        when(
            persistentIdentifierService.findUnitsByPersistentIdentifier(eq(arkId), any(VitamContext.class))
        ).thenReturn(responseFromFile("data/ark/unit_with_ark_id.json"));
        // When
        PersistentIdentifierResponseDto persistentIdentifierResponseDto =
            archiveSearchInternalService.findUnitsByPersistentIdentifier(arkId, defaultVitamContext);
        // Then
        verify(persistentIdentifierService).findUnitsByPersistentIdentifier(eq(arkId), eq(defaultVitamContext));
        assertThat(persistentIdentifierResponseDto.getResults().size()).isEqualTo(4);
        assertThat(persistentIdentifierResponseDto.getResults().get(0).getId()).isEqualTo(
            "aeaqaaaaaaecg2hnabhluammhk7supyaaabq"
        );
        assertThat(persistentIdentifierResponseDto.getResults().get(0).getPersistentIdentifier()).isNotEmpty();
        assertThat(
            persistentIdentifierResponseDto
                .getResults()
                .get(0)
                .getPersistentIdentifier()
                .get(0)
                .getPersistentIdentifierContent()
        ).isEqualTo(arkId);
    }

    @Test
    void findUnitsByPersistentIdentifier_return_history() throws Exception {
        // Given
        String arkId = "ark:/2778447/1234567xyz";
        when(
            persistentIdentifierService.findUnitsByPersistentIdentifier(eq(arkId), any(VitamContext.class))
        ).thenReturn(responseFromFile("data/ark/ark_id_purged_units.json"));
        // When
        PersistentIdentifierResponseDto persistentIdentifierResponseDto =
            archiveSearchInternalService.findUnitsByPersistentIdentifier(arkId, defaultVitamContext);
        // Then
        verify(persistentIdentifierService).findUnitsByPersistentIdentifier(eq(arkId), eq(defaultVitamContext));
        assertThat(persistentIdentifierResponseDto.getResults()).isEmpty();
        assertThat(persistentIdentifierResponseDto.getHistory().get(0).getId()).isEqualTo(
            "aeaqaaaaaeechwidabqcqamm7fpntviaaaba"
        );
        assertThat(persistentIdentifierResponseDto.getHistory().get(0).getPersistentIdentifier()).isNotEmpty();
        assertThat(
            persistentIdentifierResponseDto
                .getHistory()
                .get(0)
                .getPersistentIdentifier()
                .get(0)
                .getPersistentIdentifierContent()
        ).isEqualTo(arkId);
    }

    @Test
    void findUnitsByPersistentIdentifier_with_no_results_should_return_empty_list() throws Exception {
        // Given
        String arkId = "ark:/22567/001a957db5eadaac";
        when(
            persistentIdentifierService.findUnitsByPersistentIdentifier(eq(arkId), any(VitamContext.class))
        ).thenReturn(responseFromFile("data/ark/bad_ark_id.json"));
        // When Then
        PersistentIdentifierResponseDto persistentIdentifierResponseDto =
            archiveSearchInternalService.findUnitsByPersistentIdentifier(arkId, defaultVitamContext);
        // Then
        verify(persistentIdentifierService).findUnitsByPersistentIdentifier(eq(arkId), eq(defaultVitamContext));
        assertThat(persistentIdentifierResponseDto.getResults().size()).isEqualTo(0);
    }

    @Test
    void findUnitsByPersistentIdentifier_whith_client_error_should_throw() throws Exception {
        // Given
        String arkId = "ark:/22567/001a957db5eadaac";
        when(persistentIdentifierService.findUnitsByPersistentIdentifier(eq(arkId), any(VitamContext.class))).thenThrow(
            new VitamClientException("exception thrown by client")
        );
        // When Then
        assertThatThrownBy(
            () -> archiveSearchInternalService.findUnitsByPersistentIdentifier(arkId, defaultVitamContext)
        )
            .isInstanceOf(VitamClientException.class)
            .hasMessage("exception thrown by client");
        verify(persistentIdentifierService).findUnitsByPersistentIdentifier(eq(arkId), eq(defaultVitamContext));
    }

    @Test
    void findObjectsByPersistentIdentifier_return_Objects() throws Exception {
        // Given
        final String arkId = "ark:/23567/001a9d7db5eadabac_binary_master";
        when(
            persistentIdentifierService.findObjectsByPersistentIdentifier(eq(arkId), any(VitamContext.class))
        ).thenReturn(responseFromFile("data/ark/object_with_ark_id.json"));
        // When
        final PersistentIdentifierResponseDto persistentIdentifierResponseDto =
            archiveSearchInternalService.findObjectsByPersistentIdentifier(arkId, defaultVitamContext);
        // Then
        verify(persistentIdentifierService).findObjectsByPersistentIdentifier(eq(arkId), eq(defaultVitamContext));
        assertThat(persistentIdentifierResponseDto.getResults().size()).isEqualTo(1);
        final ResultsDto resultsDto = persistentIdentifierResponseDto.getResults().get(0);
        assertThat(resultsDto.getId()).isEqualTo("aebaaaaaaeehf3vdbncaiamnryv7fryaaaaq");
        assertThat(
            resultsDto
                .getQualifiers()
                .get(0)
                .getVersions()
                .get(0)
                .getPersistentIdentifier()
                .get(0)
                .getPersistentIdentifierContent()
        ).isEqualTo(arkId);
    }

    @Test
    void findObjectsByPersistentIdentifier_return_history() throws Exception {
        // Given
        final String arkId = "ark:/23567/42421a9d7db5eadabac_binary_master";
        when(
            persistentIdentifierService.findObjectsByPersistentIdentifier(eq(arkId), any(VitamContext.class))
        ).thenReturn(responseFromFile("data/ark/ark_id_purged_object.json"));
        // When
        final PersistentIdentifierResponseDto persistentIdentifierResponseDto =
            archiveSearchInternalService.findObjectsByPersistentIdentifier(arkId, defaultVitamContext);
        // Then
        verify(persistentIdentifierService).findObjectsByPersistentIdentifier(eq(arkId), eq(defaultVitamContext));
        assertThat(persistentIdentifierResponseDto.getResults()).isEmpty();
        assertThat(persistentIdentifierResponseDto.getHistory().get(0).getId()).isEqualTo(
            "aebqaaaaacehf3vdbepceamnqqxrocaaaaba"
        );
        assertThat(
            persistentIdentifierResponseDto
                .getHistory()
                .get(0)
                .getPersistentIdentifier()
                .get(0)
                .getPersistentIdentifierContent()
        ).isEqualTo(arkId);
    }

    @Test
    void findObjectsByPersistentIdentifier_with_no_results_should_return_empty_list() throws Exception {
        // Given
        final String arkId = "ark:/22567/001a957db5eadaac_binary_master";
        when(
            persistentIdentifierService.findObjectsByPersistentIdentifier(eq(arkId), any(VitamContext.class))
        ).thenReturn(responseFromFile("data/ark/bad_ark_id.json"));
        // When Then
        final PersistentIdentifierResponseDto persistentIdentifierResponseDto =
            archiveSearchInternalService.findObjectsByPersistentIdentifier(arkId, defaultVitamContext);
        // Then
        verify(persistentIdentifierService).findObjectsByPersistentIdentifier(eq(arkId), eq(defaultVitamContext));
        assertThat(persistentIdentifierResponseDto.getResults().size()).isEqualTo(0);
    }

    @Test
    void findObjectsByPersistentIdentifier_whith_client_error_should_throw() throws Exception {
        // Given
        final String arkId = "ark:/22567/001a957db5eadaac_binary_master";
        when(
            persistentIdentifierService.findObjectsByPersistentIdentifier(eq(arkId), any(VitamContext.class))
        ).thenThrow(new VitamClientException("exception thrown by client"));
        // When Then
        assertThatThrownBy(
            () -> archiveSearchInternalService.findObjectsByPersistentIdentifier(arkId, defaultVitamContext)
        )
            .isInstanceOf(VitamClientException.class)
            .hasMessage("exception thrown by client");
        verify(persistentIdentifierService).findObjectsByPersistentIdentifier(eq(arkId), eq(defaultVitamContext));
    }
}
