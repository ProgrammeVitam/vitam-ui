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
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitamui.archive.internal.server.rulesupdate.converter.RuleOperationsConverter;
import fr.gouv.vitamui.archives.search.common.dto.RuleSearchCriteriaDto;
import fr.gouv.vitamui.commons.api.domain.AccessContractModelDto;
import fr.gouv.vitamui.commons.api.dtos.CriteriaValue;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractService;
import fr.gouv.vitamui.commons.vitam.api.dto.AccessContractResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.iam.common.dto.AccessContractsResponseDto;
import fr.gouv.vitamui.iam.common.dto.AccessContractsVitamDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
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
@SuppressWarnings("unchecked")
public class ArchiveSearchMgtRulesInternalServiceTest {

    @MockBean(name = "objectMapper")
    private ObjectMapper objectMapper;

    @MockBean(name = "archiveSearchInternalService")
    private ArchiveSearchInternalService archiveSearchInternalService;

    @MockBean(name = "ruleOperationsConverter")
    private RuleOperationsConverter ruleOperationsConverter;

    @MockBean(name = "unitService")
    private UnitService unitService;

    @InjectMocks
    private ArchiveSearchMgtRulesInternalService archiveSearchMgtRulesInternalService;

    @MockBean(name = "accessContractService")
    private AccessContractService accessContractService;

    public final String FILING_HOLDING_SCHEME_RESULTS = "data/vitam_filing_holding_units_response.json";
    public final String UPDATE_RULES_ASYNC_RESPONSE = "data/update_rules_async_response.json";

    @BeforeEach
    public void setUp() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        archiveSearchMgtRulesInternalService = new ArchiveSearchMgtRulesInternalService(
            archiveSearchInternalService,
            ruleOperationsConverter,
            accessContractService,
            unitService,
            objectMapper
        );
    }

    @Test
    public void testSearchFilingHoldingSchemeResultsThanReturnVitamUISearchResponseDto()
        throws VitamClientException, IOException, InvalidParseOperationException {
        // Given

        when(archiveSearchInternalService.searchArchiveUnits(any(), any())).thenReturn(
            buildUnitMetadataResponse(FILING_HOLDING_SCHEME_RESULTS).toJsonNode()
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
        InputStream inputStream =
            ArchiveSearchMgtRulesInternalServiceTest.class.getClassLoader().getResourceAsStream(filename);
        Assertions.assertThat(inputStream).isNotNull();
        return RequestResponseOK.getFromJsonNode(
            objectMapper.readValue(ByteStreams.toByteArray(inputStream), JsonNode.class)
        );
    }

    @Test
    public void testUpdateArchiveUnitsRulesWithCorrectAccessContractThenReturnSuccess() throws Exception {
        // Given
        when(unitService.massUpdateUnitsRules(any(), any())).thenReturn(
            buildUnitMetadataResponse(UPDATE_RULES_ASYNC_RESPONSE)
        );

        RequestResponseOK<AccessContractModel> response1 = new RequestResponseOK<>();
        response1.setHttpCode(200);
        response1.setHits(1, 1, 1, 1);
        response1.addResult(createAccessContractModel("contratTNR", "contrat d acces", 0, true));

        when(accessContractService.findAccessContractById(any(), any())).thenReturn(response1);

        RequestResponse<AccessContractModel> requestResponse = Mockito.mock(RequestResponse.class);
        Mockito.when(
            accessContractService.findAccessContracts(ArgumentMatchers.any(), ArgumentMatchers.any())
        ).thenReturn(requestResponse);
        List<AccessContractModelDto> results = List.of(
            createAccessContractModelDto("contratTNR", "contrat d acces", 0, true)
        );
        JsonHandler.toJsonNode(results);
        AccessContractResponseDto response = new AccessContractResponseDto();
        response.setResults(results);
        Mockito.when(
            objectMapper.treeToValue(requestResponse.toJsonNode(), AccessContractResponseDto.class)
        ).thenReturn(response);

        // Configure the mapper
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        when(objectMapper.treeToValue(any(), (Class<Object>) any())).thenReturn(
            createAccessContractsResponseDto("contratTNR", "contrat d acces", 0, true)
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
        RuleSearchCriteriaDto ruleSearchCriteriaDto = new RuleSearchCriteriaDto();
        ruleSearchCriteriaDto.setSearchCriteriaDto(searchQuery);

        //When //Then
        String expectingGuid = archiveSearchMgtRulesInternalService.updateArchiveUnitsRules(
            ruleSearchCriteriaDto,
            new VitamContext(1)
        );
        assertThatCode(() -> {
            archiveSearchMgtRulesInternalService.updateArchiveUnitsRules(ruleSearchCriteriaDto, new VitamContext(1));
        }).doesNotThrowAnyException();

        Assertions.assertThat(expectingGuid).isEqualTo("aeeaaaaaagh23tjvabz5gal6qlt6iaaaaaaq");
    }

    @Test
    public void testUpdateArchiveUnitsRulesWithInCorrectAccessContractThenReturBadRequest() throws Exception {
        // Given
        when(unitService.massUpdateUnitsRules(any(), any())).thenReturn(
            buildUnitMetadataResponse(UPDATE_RULES_ASYNC_RESPONSE)
        );

        RequestResponseOK<AccessContractModel> response1 = new RequestResponseOK<>();
        response1.setHttpCode(200);
        response1.setHits(1, 1, 1, 1);
        response1.addResult(createAccessContractModel("contratTNR", "contrat d acces", 0, false));

        when(accessContractService.findAccessContractById(any(), any())).thenReturn(response1);

        RequestResponse<AccessContractModel> requestResponse = Mockito.mock(RequestResponse.class);
        Mockito.when(
            accessContractService.findAccessContracts(ArgumentMatchers.any(), ArgumentMatchers.any())
        ).thenReturn(requestResponse);
        List<AccessContractModelDto> results = List.of(
            createAccessContractModelDto("contratTNR", "contrat d acces", 0, false)
        );
        JsonHandler.toJsonNode(results);
        AccessContractResponseDto response = new AccessContractResponseDto();
        response.setResults(results);
        Mockito.when(
            objectMapper.treeToValue(requestResponse.toJsonNode(), AccessContractResponseDto.class)
        ).thenReturn(response);

        // Configure the mapper
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        when(objectMapper.treeToValue(any(), (Class<Object>) any())).thenReturn(
            createAccessContractsResponseDto("contratTNR", "contrat d acces", 0, false)
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
        RuleSearchCriteriaDto ruleSearchCriteriaDto = new RuleSearchCriteriaDto();
        ruleSearchCriteriaDto.setSearchCriteriaDto(searchQuery);

        //When //Then
        assertThatCode(() -> {
            archiveSearchMgtRulesInternalService.updateArchiveUnitsRules(ruleSearchCriteriaDto, new VitamContext(1));
        }).hasMessage("the access contract using to update unit rules has no writing permission to update units");
    }

    AccessContractModel createAccessContractModel(
        String identifier,
        String name,
        Integer tenant,
        Boolean writingPermission
    ) {
        AccessContractModel accessContractModel = new AccessContractModel();
        accessContractModel.setIdentifier(identifier);
        accessContractModel.setName(name);
        accessContractModel.setTenant(tenant);
        accessContractModel.setWritingPermission(writingPermission);
        return accessContractModel;
    }

    AccessContractsResponseDto createAccessContractsResponseDto(
        String identifier,
        String name,
        Integer tenant,
        Boolean writingPermission
    ) {
        AccessContractsResponseDto accessContractModel = new AccessContractsResponseDto();
        accessContractModel.setResults(
            List.of(createAccessContractsVitamDto(identifier, name, tenant, writingPermission))
        );
        return accessContractModel;
    }

    AccessContractsVitamDto createAccessContractsVitamDto(
        String identifier,
        String name,
        Integer tenant,
        Boolean writingPermission
    ) {
        AccessContractsVitamDto accessContractModel = new AccessContractsVitamDto();
        accessContractModel.setIdentifier(identifier);
        accessContractModel.setName(name);
        accessContractModel.setTenant(tenant);
        accessContractModel.setWritingPermission(writingPermission);
        return accessContractModel;
    }

    AccessContractModelDto createAccessContractModelDto(
        String identifier,
        String name,
        Integer tenant,
        Boolean writingPermission
    ) {
        AccessContractModelDto accessContractModel = new AccessContractModelDto();
        accessContractModel.setIdentifier(identifier);
        accessContractModel.setName(name);
        accessContractModel.setTenant(tenant);
        accessContractModel.setWritingPermission(writingPermission);
        return accessContractModel;
    }
}
