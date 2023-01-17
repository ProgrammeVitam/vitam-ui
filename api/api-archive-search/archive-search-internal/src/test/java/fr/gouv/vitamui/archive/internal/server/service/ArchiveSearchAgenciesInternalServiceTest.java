/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
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
package fr.gouv.vitamui.archive.internal.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitamui.commons.api.dtos.CriteriaValue;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
import fr.gouv.vitamui.archives.search.common.dto.AgencyResponseDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(SpringExtension.class)
@SuppressWarnings("unchecked")
public class ArchiveSearchAgenciesInternalServiceTest {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveSearchAgenciesInternalServiceTest.class);

    @MockBean(name = "objectMapper")
    private ObjectMapper objectMapper;

    @MockBean(name = "agencyService")
    private AgencyService agencyService;


    @InjectMocks
    private ArchiveSearchAgenciesInternalService archiveSearchAgenciesInternalService;


    @BeforeEach
    public void setUp() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        archiveSearchAgenciesInternalService = new ArchiveSearchAgenciesInternalService(objectMapper, agencyService);
    }

    @Test
    void testMapArgenciesCodesWhenSearchByAgenciesNamesWhenCodesIncluded()
        throws VitamClientException, JsonProcessingException {
        // Given

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
        criteriaList.add(agencyCodeCriteria);

        searchQuery.setCriteriaList(criteriaList);

        when(agencyService.findAgencies(any(), any())).thenReturn(buildAgenciesResponse());

        // Configure the mapper
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        when(objectMapper.treeToValue(any(), (Class<Object>) any()))
            .thenReturn(getResponseAgencies());

        // When
        archiveSearchAgenciesInternalService
            .mapAgenciesNameToCodes(searchQuery, new VitamContext(1));

        // Then
        Assertions.assertThat(searchQuery).isNotNull();
        Assertions.assertThat(searchQuery.getCriteriaList()).hasSize(1);
        List<SearchCriteriaEltDto> agencyIds = new ArrayList<>(searchQuery.getCriteriaList());
        Assertions.assertThat(agencyIds).isNotNull().hasSize(1);
        Assertions.assertThat(agencyIds.get(0).getValues()).hasSize(4);

    }

    @Test
    void testMapArgenciesCodesWhenSearchByAgenciesNamesWhenCodesNotIncluded()
        throws VitamClientException, JsonProcessingException {
        // Given

        SearchCriteriaDto searchQuery = new SearchCriteriaDto();
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>();
        SearchCriteriaEltDto agencyCodeCriteria = new SearchCriteriaEltDto();
        agencyCodeCriteria.setCriteria(ArchiveSearchConsts.ORIGINATING_AGENCY_LABEL_FIELD);
        agencyCodeCriteria.setValues(List.of(new CriteriaValue("ANY_LABEL")));
        agencyCodeCriteria.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
        criteriaList.add(agencyCodeCriteria);

        searchQuery.setCriteriaList(criteriaList);

        when(agencyService.findAgencies(any(), any())).thenReturn(buildAgenciesResponse());

        // Configure the mapper
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        when(objectMapper.treeToValue(any(), (Class<Object>) any()))
            .thenReturn(getResponseAgencies());

        // When
        archiveSearchAgenciesInternalService
            .mapAgenciesNameToCodes(searchQuery, new VitamContext(1));

        // Then
        Assertions.assertThat(searchQuery).isNotNull();
        Assertions.assertThat(searchQuery.getCriteriaList()).hasSize(1);
        List<SearchCriteriaEltDto> agencyIds = searchQuery.getCriteriaList().stream().filter(
            criteria -> criteria.getCriteria().equals(ArchiveSearchConsts.ORIGINATING_AGENCY_ID_FIELD))
            .collect(Collectors.toList());
        Assertions.assertThat(agencyIds).isNotNull().hasSize(1);
        Assertions.assertThat(agencyIds.get(0).getValues()).hasSize(3);
    }

    private AgencyResponseDto getResponseAgencies()
        throws JsonProcessingException {
        // Configure the mapper
        ObjectMapper objectMapper1 = new ObjectMapper();
        objectMapper1.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<AgenciesModel> agenciesModelList = List.of(createAgencyModel("producteur1", "Service producteur1", 0),
            createAgencyModel("producteur3", "Service producteur2", 0),
            createAgencyModel("ANY_CODE", "Service producteur3", 0));
        RequestResponseOK<AgenciesModel> response =
            new RequestResponseOK<>(null, agenciesModelList, agenciesModelList.size()).setHttpCode(400);


        return objectMapper1
            .treeToValue(response.toJsonNode(), AgencyResponseDto.class);
    }

    private RequestResponse<AgenciesModel> buildAgenciesResponse() {
        List<AgenciesModel> agenciesModelList = List.of(createAgencyModel("producteur1", "Service producteur1", 0),
            createAgencyModel("producteur3", "Service producteur2", 0));

        return new RequestResponseOK<>(null, agenciesModelList, agenciesModelList.size()).setHttpCode(400);
    }

    AgenciesModel createAgencyModel(String identifier, String name, Integer tenant) {

        AgenciesModel agency = new AgenciesModel();
        agency.setIdentifier(identifier);
        agency.setName(name);
        agency.setTenant(tenant);
        return agency;
    }


}
