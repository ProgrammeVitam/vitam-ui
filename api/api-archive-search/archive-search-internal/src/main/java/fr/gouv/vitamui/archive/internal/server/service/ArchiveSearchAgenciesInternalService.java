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

package fr.gouv.vitamui.archive.internal.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitamui.archives.search.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.archives.search.common.dto.AgencyResponseDto;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.api.dtos.CriteriaValue;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Archive-Search agencies service for archives unit .
 */
@Service
public class ArchiveSearchAgenciesInternalService {
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveSearchAgenciesInternalService.class);

    private final ObjectMapper objectMapper;
    private final AgencyService agencyService;

    @Autowired
    public ArchiveSearchAgenciesInternalService(final ObjectMapper objectMapper, final AgencyService agencyService) {
        this.objectMapper = objectMapper;
        this.agencyService = agencyService;
    }

    public void mapAgenciesNameToCodes(SearchCriteriaDto searchQuery, VitamContext vitamContext)
        throws VitamClientException {
        LOGGER.debug("calling mapAgenciesNameToCodes  {} ", searchQuery.toString());
        Set<String> agencyOriginNamesCriteria = new HashSet<>();
        searchQuery.getCriteriaList().stream()
            .filter(criteriaElt -> criteriaElt.getCriteria().equals(ArchiveSearchConsts.ORIGINATING_AGENCY_LABEL_FIELD))
            .forEach(
                criteriaElt -> agencyOriginNamesCriteria
                    .addAll(criteriaElt.getValues().stream().map(CriteriaValue::getValue).collect(
                        Collectors.toList())));
        List<AgencyModelDto> agenciesOrigins;
        if (!agencyOriginNamesCriteria.isEmpty()) {
            LOGGER.debug(" trying to mapping agencies labels {} ", agencyOriginNamesCriteria.toString());
            agenciesOrigins = findOriginAgenciesByNames(vitamContext, agencyOriginNamesCriteria);
            if (!CollectionUtils.isEmpty(agenciesOrigins)) {
                mapAgenciesNamesToAgenciesCodesInCriteria(searchQuery, agenciesOrigins);
            }

        }
    }

    private void mapAgenciesNamesToAgenciesCodesInCriteria(SearchCriteriaDto searchQuery,
        List<AgencyModelDto> actualAgencies) {

        if (searchQuery != null && searchQuery.getCriteriaList() != null && !searchQuery.getCriteriaList().isEmpty()) {
            List<SearchCriteriaEltDto> mergedCriteriaList = searchQuery.getCriteriaList().stream().filter(
                    criteria -> (!ArchiveSearchConsts.ORIGINATING_AGENCY_ID_FIELD.equals(criteria.getCriteria())
                        && !ArchiveSearchConsts.ORIGINATING_AGENCY_LABEL_FIELD.equals(criteria.getCriteria())))
                .collect(Collectors.toList());

            List<String> filteredAgenciesId = actualAgencies.stream()
                .map(AgencyModelDto::getIdentifier)
                .collect(Collectors.toList());

            List<SearchCriteriaEltDto> idCriteriaList = searchQuery.getCriteriaList().stream()
                .filter(criteria -> ArchiveSearchConsts.ORIGINATING_AGENCY_ID_FIELD.equals(criteria.getCriteria()))
                .collect(Collectors.toList());
            SearchCriteriaEltDto idCriteria;
            if (CollectionUtils.isEmpty(idCriteriaList)) {
                idCriteria = new SearchCriteriaEltDto();
                idCriteria.setCriteria(ArchiveSearchConsts.ORIGINATING_AGENCY_ID_FIELD);
                idCriteria.setValues(
                    filteredAgenciesId.stream().map(CriteriaValue::new).collect(Collectors.toList()));
                idCriteria.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
                idCriteria.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
                mergedCriteriaList.add(idCriteria);
            } else {
                idCriteriaList.forEach(criteria -> {
                        if (!CollectionUtils.isEmpty(criteria.getValues())) {
                            filteredAgenciesId.addAll(criteria.getValues().stream().map(CriteriaValue::getValue).collect(
                                Collectors.toList()));
                        }
                        criteria.setValues(
                            filteredAgenciesId.stream().map(CriteriaValue::new).collect(Collectors.toList()));
                        mergedCriteriaList.add(criteria);
                    }
                );
            }

            searchQuery.setCriteriaList(mergedCriteriaList);
        }
    }

    public List<AgencyModelDto> findOriginAgenciesByCriteria(VitamContext vitamContext, String field,
        List<String> originAgenciesCodes) throws VitamClientException {
        List<AgencyModelDto> agencies = new ArrayList<>();
        if (originAgenciesCodes != null && !originAgenciesCodes.isEmpty()) {
            LOGGER.debug("Finding originating agencies by field {}  values {} ", field, originAgenciesCodes);
            Map<String, Object> searchCriteriaMap = new HashMap<>();
            searchCriteriaMap.put(field, originAgenciesCodes);
            try {
                JsonNode queryOriginAgencies = VitamQueryHelper
                    .createQueryDSL(searchCriteriaMap, Optional.empty(),
                        Optional.empty());
                RequestResponse<AgenciesModel> requestResponse =
                    agencyService.findAgencies(vitamContext, queryOriginAgencies);
                agencies = objectMapper
                    .treeToValue(requestResponse.toJsonNode(), AgencyResponseDto.class).getResults();
            } catch (InvalidCreateOperationException e) {
                throw new VitamClientException("Unable to find the agencies ", e);
            } catch (InvalidParseOperationException | JsonProcessingException e1) {
                throw new BadRequestException("Error parsing query ", e1);
            }
        }
        LOGGER.debug("origin agencies  found {} ", agencies);
        return agencies;
    }

    /**
     * Search origin agencies by theirs codes
     *
     * @param vitamContext
     * @param originAgenciesCodes
     * @return
     * @throws InvalidParseOperationException
     * @throws VitamClientException
     */
    public List<AgencyModelDto> findOriginAgenciesByCodes(VitamContext vitamContext,
        Set<String> originAgenciesCodes) throws VitamClientException {
        List<String> originAgenciesCodesList = new ArrayList<>(originAgenciesCodes);
        return findOriginAgenciesByCriteria(vitamContext, "Identifier", originAgenciesCodesList);
    }

    /**
     * Search origin agencies by theirs names
     *
     * @param vitamContext
     * @param originAgenciesCodes
     * @return
     * @throws InvalidParseOperationException
     * @throws VitamClientException
     */
    public List<AgencyModelDto> findOriginAgenciesByNames(VitamContext vitamContext,
        Set<String> originAgenciesCodes) throws VitamClientException {
        List<String> originAgenciesCodesList = new ArrayList<>(originAgenciesCodes);
        return findOriginAgenciesByCriteria(vitamContext, "Name", originAgenciesCodesList);
    }
}
