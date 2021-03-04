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
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitamui.archives.search.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.archives.search.common.dto.AgencyResponseDto;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnit;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaEltDto;
import fr.gouv.vitamui.archives.search.common.dto.VitamUIArchiveUnitResponseDto;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Archive-Search Internal service communication with VITAM.
 */
@Service
public class ArchiveSearchInternalService {
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveSearchInternalService.class);
    public static final String INGEST_ARCHIVE_TYPE = "INGEST";
    public static final String ORIGINATING_AGENCY_LABEL_FIELD = "originating_agency_label";
    public static final String ORIGINATING_AGENCY_ID_FIELD = "#originating_agency";
    public static final String ARCHIVE_UNIT_DETAILS = "$results";
    public static final String ARCHIVE_UNIT_USAGE = "qualifier";
    public static final String ARCHIVE_UNIT_VERSION = "DataObjectVersion";


    private final ObjectMapper objectMapper;

    final private UnitService unitService;

    final private AgencyService agencyService;

    @Autowired
    public ArchiveSearchInternalService(final ObjectMapper objectMapper, final UnitService unitService,
        final AgencyService agencyService) {
        this.unitService = unitService;
        this.objectMapper = objectMapper;
        this.agencyService = agencyService;

    }

    public ArchiveUnitsDto searchArchiveUnitsByCriteria(final SearchCriteriaDto searchQuery,
        final VitamContext vitamContext)
        throws VitamClientException, IOException {
        RequestResponse<AgenciesModel> requestResponse =
            agencyService.findAgencies(vitamContext, new Select().getFinalSelect());
        final List<AgencyModelDto> actualAgencies = objectMapper
            .treeToValue(requestResponse.toJsonNode(), AgencyResponseDto.class).getResults();

        mapAgenciesLabelsToAgenciesCodesInCriteria(searchQuery, actualAgencies);
        Map<String, AgencyModelDto> actualAgenciesMapByIdentifier =
            actualAgencies.stream().collect(Collectors.toMap(AgencyModelDto::getIdentifier, agency -> agency));
        LOGGER.debug("calling find archive units by criteria {} ", searchQuery.toString());
        List<String> archiveUnits = Arrays.asList(INGEST_ARCHIVE_TYPE);
        JsonNode response = searchUnits(mapRequestToDslQuery(archiveUnits, searchQuery), vitamContext);
        final VitamUISearchResponseDto archivesOriginResponse =
            objectMapper.treeToValue(response, VitamUISearchResponseDto.class);
        List<ArchiveUnit> archivesFilled = new ArrayList<>();
        if (archivesOriginResponse != null) {
            archivesFilled = archivesOriginResponse.getResults().stream().map(
                archiveUnit -> fillOriginatingAgencyName(archiveUnit, actualAgenciesMapByIdentifier)
            ).collect(Collectors.toList());
        }

        VitamUIArchiveUnitResponseDto responseFilled = new VitamUIArchiveUnitResponseDto();
        responseFilled.setContext(archivesOriginResponse.getContext());
        responseFilled.setFacetResults(archivesOriginResponse.getFacetResults());
        responseFilled.setResults(archivesFilled);
        responseFilled.setHits(archivesOriginResponse.getHits());

        return new ArchiveUnitsDto(responseFilled);
    }

    private ArchiveUnit fillOriginatingAgencyName(ResultsDto originResponse,
        Map<String, AgencyModelDto> actualAgenciesMapById) {
        ArchiveUnit archiveUnit = new ArchiveUnit();
        BeanUtils.copyProperties(originResponse, archiveUnit);
        if (actualAgenciesMapById != null) {
            AgencyModelDto agencyModel = actualAgenciesMapById.get(originResponse.getOriginatingAgency());
            if (agencyModel != null) {
                archiveUnit.setOriginatingAgencyName(agencyModel.getName());
            }
        }
        return archiveUnit;
    }

    private void mapAgenciesLabelsToAgenciesCodesInCriteria(SearchCriteriaDto searchQuery,
        List<AgencyModelDto> actualAgencies) {
        if (searchQuery != null && searchQuery.getCriteriaList() != null && !searchQuery.getCriteriaList().isEmpty()) {
            List<String> originatingAgencyLabelList = searchQuery.getCriteriaList().stream()
                .filter(criteria -> ORIGINATING_AGENCY_LABEL_FIELD.equals(criteria.getCriteria()))
                .map(criteria -> criteria.getValues()).flatMap(List::stream)
                .collect(Collectors.toList());

            if (originatingAgencyLabelList != null && !originatingAgencyLabelList.isEmpty()) {
                List<String> filteredAgenciesId = actualAgencies.stream()
                    .filter(agency -> originatingAgencyLabelList.contains(agency.getName()))
                    .map(agency -> agency.getIdentifier())
                    .collect(Collectors.toList());
                AtomicInteger i = new AtomicInteger();
                int indexOpt = searchQuery.getCriteriaList().stream().peek(v -> i.incrementAndGet())
                    .anyMatch(criteria -> ORIGINATING_AGENCY_ID_FIELD.equals(criteria.getCriteria())) ?
                    i.get() - 1 : -1;
                SearchCriteriaEltDto agencyCodeCriteria;
                if (filteredAgenciesId != null && !filteredAgenciesId.isEmpty() && indexOpt != -1) {
                    agencyCodeCriteria = searchQuery.getCriteriaList().get(indexOpt);
                    filteredAgenciesId.addAll(agencyCodeCriteria.getValues());
                    agencyCodeCriteria.setValues(filteredAgenciesId);
                    searchQuery.getCriteriaList().set(indexOpt, agencyCodeCriteria);
                } else {
                    agencyCodeCriteria = new SearchCriteriaEltDto();
                    agencyCodeCriteria.setCriteria(ORIGINATING_AGENCY_ID_FIELD);
                    agencyCodeCriteria.setValues(filteredAgenciesId);
                    searchQuery.getCriteriaList().add(agencyCodeCriteria);
                }
                searchQuery.setCriteriaList(searchQuery.getCriteriaList().stream()
                    .filter(criteria -> !ORIGINATING_AGENCY_LABEL_FIELD.equals(criteria.getCriteria())).collect(
                        Collectors.toList()));
            }
        }
    }

    /**
     * Map search query to DSl Query Json node
     *
     * @param searchQuery
     * @return
     */
    private JsonNode mapRequestToDslQuery(List<String> archiveUnits, SearchCriteriaDto searchQuery) {

        Optional<String> orderBy = Optional.empty();
        Optional<DirectionDto> direction = Optional.empty();
        Map<String, List<String>> vitamCriteria = new HashMap<>();
        JsonNode query;
        try {
            if (searchQuery != null && searchQuery.getCriteriaList() != null &&
                !searchQuery.getCriteriaList().isEmpty()) {
                searchQuery.getCriteriaList().stream()
                    .filter(criteria -> criteria.getValues() != null && !criteria.getValues().isEmpty())
                    .forEach(criteria -> vitamCriteria.put(criteria.getCriteria(), criteria.getValues()));
            }
            if (searchQuery.getSortingCriteria() != null) {
                direction = Optional.of(searchQuery.getSortingCriteria().getSorting());
                orderBy = Optional.of(searchQuery.getSortingCriteria().getCriteria());
            }
            query = VitamQueryHelper
                .createQueryDSL(archiveUnits, searchQuery.getNodes(), vitamCriteria, searchQuery.getPageNumber(),
                    searchQuery.getSize(), orderBy,
                    direction);
        } catch (InvalidParseOperationException ioe) {
            LOGGER.error("Unable to find archive units with pagination " + ioe.getMessage());
            throw new BadRequestException("Unable to find archive units with pagination", ioe);
        } catch (InvalidCreateOperationException e) {
            LOGGER.error("Can't parse criteria as Vitam query" + e.getMessage());
            throw new BadRequestException("Can't parse criteria as Vitam query", e);
        }
        return query;
    }

    public JsonNode searchUnits(final JsonNode dslQuery, final VitamContext vitamContext) throws VitamClientException {
        RequestResponse<JsonNode> response = unitService.searchUnits(dslQuery, vitamContext);
        return response.toJsonNode();
    }

    public ResultsDto findUnitById(String id, VitamContext vitamContext) throws VitamClientException {

        try {
            LOGGER
                .info("Archive Unit Infos {}", unitService.findUnitById(id, vitamContext).toJsonNode().get(ARCHIVE_UNIT_DETAILS));
            String re = StringUtils
                .chop(unitService.findUnitById(id, vitamContext).toJsonNode().get(ARCHIVE_UNIT_DETAILS).toString().substring(1));
            return objectMapper.readValue(re, ResultsDto.class);
        } catch (VitamClientException | JsonProcessingException e) {
            LOGGER.error("Can not get the archive unit {} ", e);
            throw new VitamClientException("Unable to find the UA", e);
        }
    }

    private String getUsage(String id, VitamContext vitamContext) throws VitamClientException {
        return unitService.findObjectMetadataById(id, vitamContext).toJsonNode().findValue(ARCHIVE_UNIT_USAGE).textValue();
    }

    private int getVersion(String id, VitamContext vitamContext) throws VitamClientException {
        String result = unitService.findObjectMetadataById(id, vitamContext).toJsonNode()
            .findValue(ARCHIVE_UNIT_VERSION).textValue();

        return Integer.valueOf(result.split("_")[1]);

    }

    public Response downloadObjectFromUnit(String id, final VitamContext vitamContext)
        throws VitamClientException {

        LOGGER.info("Access Contract  {}", vitamContext.getAccessContract());
        LOGGER.info("Download Archive Unit Object with id {} ", id);
        try {
            return unitService
                .getObjectStreamByUnitId(id, getUsage(id, vitamContext), getVersion(id, vitamContext), vitamContext);
        } catch (VitamClientException e) {
            LOGGER.error("Unable to find the Archive Unit Object with id {} ", e);
            throw new VitamClientException("Unable to find the Archive Unit Object with id", e);
        }
    }
}
