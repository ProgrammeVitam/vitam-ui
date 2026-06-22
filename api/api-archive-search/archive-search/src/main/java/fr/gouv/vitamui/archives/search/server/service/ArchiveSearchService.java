/*
 *
 *  Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 *  and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 *  contact@programmevitam.fr
 *
 *  This software is a computer program whose purpose is to implement
 *  implement a digital archiving front-office system for the secure and
 *  efficient high volumetry VITAM solution.
 *
 *  This software is governed by the CeCILL-C license under French law and
 *  abiding by the rules of distribution of free software.  You can  use,
 *  modify and/ or redistribute the software under the terms of the CeCILL-C
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info".
 *
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability.
 *
 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or
 *  data to be ensured and,  more generally, to use and operate it in the
 *  same conditions as regards security.
 *
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL-C license and that you accept its terms.
 *
 */

package fr.gouv.vitamui.archives.search.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.query.Query;
import fr.gouv.vitam.common.database.builder.query.QueryHelper;
import fr.gouv.vitam.common.database.builder.request.configuration.BuilderToken;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.multiple.SelectMultiQuery;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.database.facet.model.FacetOrder;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitamui.archives.search.common.common.RulesUpdateCommonService;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnit;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.ReassignRequestDto;
import fr.gouv.vitamui.archives.search.common.dto.ReassignmentMode;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.VitamUIArchiveUnitResponseDto;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.api.dtos.CriteriaValue;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.dtos.TermsFacet;
import fr.gouv.vitamui.commons.api.dtos.VitamUiOntologyDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.api.exception.UnexpectedSettingsException;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
import fr.gouv.vitamui.commons.api.utils.OntologyServiceReader;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.access.PersistentIdentifierService;
import fr.gouv.vitamui.commons.vitam.api.access.UnitCommonService;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationDto;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsCommonResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.PersistentIdentifierResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.QualifiersDto;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VersionsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.commons.vitam.api.model.UnitTypeEnum;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import jakarta.ws.rs.core.Response;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.in;
import static fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper.unitType;
import static fr.gouv.vitamui.commons.api.utils.MetadataSearchCriteriaUtils.createDslQueryWithFacets;
import static fr.gouv.vitamui.commons.api.utils.MetadataSearchCriteriaUtils.mapRequestToDslQuery;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

/**
 * Archive-Search service communication with VITAM.
 */
@Getter
@Service
public class ArchiveSearchService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveSearchService.class);
    private static final String ARCHIVE_UNIT_DETAILS = "$results";
    private static final String HISTORY = "$history";
    public static final String DSL_QUERY_PROJECTION = "$projection";
    public static final String DSL_QUERY_FILTER = "$filter";
    public static final String DSL_QUERY_THRESHOLD = "$threshold";
    public static final String DSL_QUERY_FACETS = "$facets";
    public static final String OPERATION_IDENTIFIER = "itemId";

    public static final String TITLE_FIELD = "Title";

    private static final Integer SEARCH_UNIT_MAX_RESULTS = 10000;

    private static final String[] FILING_PLAN_PROJECTION = new String[] {
        "#id",
        TITLE_FIELD,
        "Title_",
        "DescriptionLevel",
        "#unitType",
        "#unitups",
        "#allunitups",
        "#object",
    };
    public static final String FALSE = "false";
    public static final String TRUE = "true";

    private static final String SET = "$set";
    private static final String UNSET = "$unset";
    private static final String ACTION = "$action";

    @Value("${ontologies_file_path}")
    private String ontologiesFilePath;

    private final ObjectMapper objectMapper;
    private final UnitCommonService unitCommonService;
    private final ArchiveSearchAgenciesService archiveSearchAgenciesService;
    private final ArchiveSearchRulesService archiveSearchRulesService;
    private final ArchiveSearchFacetsService archiveSearchFacetsService;
    private final PersistentIdentifierService persistentIdentifierService;
    private final ArchiveSearchThresholdService archiveSearchThresholdService;
    private final ArchiveSearchExternalParametersService archiveSearchExternalParametersService;
    private final SecurityService securityService;
    private final LogbookService logbookService;

    @Autowired
    public ArchiveSearchService(
        final ObjectMapper objectMapper,
        final UnitCommonService unitCommonService,
        final ArchiveSearchAgenciesService archiveSearchAgenciesService,
        final ArchiveSearchRulesService archiveSearchRulesService,
        final ArchiveSearchFacetsService archiveSearchFacetsService,
        final PersistentIdentifierService persistentIdentifierService,
        final ArchiveSearchThresholdService archiveSearchThresholdService,
        ArchiveSearchExternalParametersService archiveSearchExternalParametersService,
        SecurityService securityService,
        LogbookService logbookService
    ) {
        this.unitCommonService = unitCommonService;
        this.objectMapper = objectMapper;
        this.archiveSearchAgenciesService = archiveSearchAgenciesService;
        this.archiveSearchRulesService = archiveSearchRulesService;
        this.archiveSearchFacetsService = archiveSearchFacetsService;
        this.persistentIdentifierService = persistentIdentifierService;
        this.archiveSearchThresholdService = archiveSearchThresholdService;
        this.archiveSearchExternalParametersService = archiveSearchExternalParametersService;
        this.securityService = securityService;
        this.logbookService = logbookService;
    }

    public VitamUIArchiveUnitResponseDto searchArchiveUnitsByCriteria(final SearchCriteriaDto searchQuery)
        throws VitamClientException, IOException {
        try {
            Optional<Long> thresholdOpt = archiveSearchThresholdService.retrieveProfilThresholds();
            thresholdOpt.ifPresent(searchQuery::setThreshold);
            VitamContext vitamContext = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();

            LOGGER.debug("calling find archive units by criteria {} ", searchQuery.toString());
            archiveSearchAgenciesService.mapAgenciesNameToCodes(searchQuery, vitamContext);
            archiveSearchRulesService.mapManagementRulesTitlesToCodes(searchQuery, vitamContext);

            JsonNode dslQuery = createDslQueryWithFacets(searchQuery).getFinalSelect();
            JsonNode vitamResponse = searchArchiveUnits(dslQuery, vitamContext);
            ArchiveUnitsDto archiveUnitsDto = decorateAndMapResponse(vitamResponse, vitamContext);
            Integer totalResults = archiveUnitsDto.getArchives().getHits().getTotal();
            boolean trackTotalHits = totalResults >= SEARCH_UNIT_MAX_RESULTS;
            fillManagementRulesFacets(searchQuery, archiveUnitsDto, trackTotalHits, vitamContext);

            return archiveUnitsDto.getArchives();
        } catch (InvalidCreateOperationException ioe) {
            throw new VitamClientException("Unable to find archive units with pagination", ioe);
        }
    }

    private void fillManagementRulesFacets(
        SearchCriteriaDto searchQuery,
        ArchiveUnitsDto archiveUnitsDto,
        boolean trackTotalHits,
        VitamContext vitamContext
    ) throws InvalidCreateOperationException, VitamClientException, JsonProcessingException {
        try {
            archiveUnitsDto
                .getArchives()
                .getFacetResults()
                .addAll(
                    archiveSearchFacetsService.fillManagementRulesFacets(searchQuery, trackTotalHits, vitamContext)
                );
        } catch (UnexpectedSettingsException e) {
            if (!searchQuery.isComputeMgtRulesFacets()) {
                LOGGER.error(
                    "Could not compute facets,the setting track total hits is not allowed in vitam, we return units without facets"
                );
            } else {
                LOGGER.error("Could not compute facets,the setting track total hits is not allowed in vitam, ");
                throw new UnexpectedSettingsException(e.getMessage());
            }
        }
    }

    private ArchiveUnitsDto decorateAndMapResponse(JsonNode vitamResponse, VitamContext vitamContext)
        throws JsonProcessingException, VitamClientException {
        final VitamUISearchResponseDto archivesOriginResponse = objectMapper.treeToValue(
            vitamResponse,
            VitamUISearchResponseDto.class
        );
        Set<String> originatingAgenciesCodes = archivesOriginResponse
            .getResults()
            .stream()
            .map(ResultsDto::getOriginatingAgency)
            .filter(Objects::nonNull)
            .collect(Collectors.toSet());
        List<AgencyModelDto> originAgenciesFound = archiveSearchAgenciesService.findOriginAgenciesByCodes(
            vitamContext,
            originatingAgenciesCodes
        );
        Map<String, AgencyModelDto> agenciesMapByIdentifier = originAgenciesFound
            .stream()
            .collect(Collectors.toMap(AgencyModelDto::getIdentifier, agency -> agency));

        List<ArchiveUnit> archivesFilled = archivesOriginResponse
            .getResults()
            .stream()
            .map(
                archiveUnit -> RulesUpdateCommonService.fillOriginatingAgencyName(archiveUnit, agenciesMapByIdentifier)
            )
            .collect(Collectors.toList());
        VitamUIArchiveUnitResponseDto responseFilled = new VitamUIArchiveUnitResponseDto();
        responseFilled.setContext(archivesOriginResponse.getContext());
        responseFilled.setFacetResults(archivesOriginResponse.getFacetResults());
        responseFilled.setResults(archivesFilled);
        responseFilled.setHits(archivesOriginResponse.getHits());
        return new ArchiveUnitsDto(responseFilled);
    }

    public JsonNode searchArchiveUnits(final JsonNode dslQuery, final VitamContext vitamContext)
        throws VitamClientException {
        RequestResponse<JsonNode> response = unitCommonService.searchUnits(dslQuery, vitamContext);
        return response.toJsonNode();
    }

    public VitamUISearchResponseDto getFillingHoldingScheme() throws VitamClientException {
        try {
            final JsonNode fillingHoldingQuery = createQueryForHoldingFillingUnit();
            JsonNode archiveUnits = searchArchiveUnits(
                fillingHoldingQuery,
                archiveSearchExternalParametersService.buildVitamContextFromExternalParam()
            );
            return objectMapper.treeToValue(archiveUnits, VitamUISearchResponseDto.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Error parsing query ", e);
        }
    }

    public ResultsDto findArchiveUnitById(String id) throws VitamClientException {
        try {
            VitamContext vitamContext = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();
            LOGGER.debug("Archive Unit Id : {}", id);
            String re = StringUtils.chop(
                unitCommonService
                    .findUnitById(id, vitamContext)
                    .toJsonNode()
                    .get(ARCHIVE_UNIT_DETAILS)
                    .toString()
                    .substring(1)
            );
            return objectMapper.readValue(re, ResultsDto.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Can not get the archive unit {} ", e);
            throw new VitamClientException("Unable to find the UA", e);
        }
    }

    public ResultsDto findObjectById(String id) throws VitamClientException {
        VitamContext vitamContext = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();
        return findObjectById(id, vitamContext);
    }

    public ResultsDto findObjectById(String id, VitamContext vitamContext) throws VitamClientException {
        try {
            LOGGER.debug("Get Object Group");
            String re = StringUtils.chop(
                unitCommonService
                    .findObjectMetadataById(id, vitamContext)
                    .toJsonNode()
                    .get(ARCHIVE_UNIT_DETAILS)
                    .toString()
                    .substring(1)
            );
            LOGGER.debug("Object received: {}" + re);
            return objectMapper.readValue(re, ResultsDto.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Can not get the object group {} ", e);
            throw new InternalServerException("Unable to find the ObjectGroup", e);
        }
    }

    /**
     * Download the Unit Binary Object
     *
     * @param id
     * @param usage
     * @param version
     * @param vitamContext
     * @throws VitamClientException
     */
    private Response downloadObjectStreamFromUnit(
        String id,
        String usage,
        Integer version,
        final VitamContext vitamContext
    ) throws VitamClientException {
        LOGGER.debug("Download Archive Unit Object with id {} , usage {} and version {}  ", id, usage, version);
        return unitCommonService.getObjectStreamByUnitId(id, usage, version, vitamContext);
    }

    public JsonNode prepareDslQuery(final SearchCriteriaDto searchQuery, final VitamContext vitamContext)
        throws VitamClientException {
        searchQuery.setPageNumber(0);
        archiveSearchAgenciesService.mapAgenciesNameToCodes(searchQuery, vitamContext);
        archiveSearchRulesService.mapManagementRulesTitlesToCodes(searchQuery, vitamContext);
        return mapRequestToDslQuery(searchQuery);
    }

    public JsonNode createQueryForHoldingFillingUnit() {
        try {
            final SelectMultiQuery select = new SelectMultiQuery();
            final Query query = in(
                unitType(),
                UnitTypeEnum.HOLDING_UNIT.getValue(),
                UnitTypeEnum.FILING_UNIT.getValue()
            );
            select.addQueries(query);
            ObjectNode orderFilter = JsonHandler.createObjectNode();
            orderFilter.put(TITLE_FIELD, 1);
            ObjectNode filter = JsonHandler.createObjectNode();
            filter.set("$orderby", orderFilter);
            select.setFilter(filter);
            select.addUsedProjection(FILING_PLAN_PROJECTION);
            LOGGER.debug("query =", select.getFinalSelect().toPrettyString());
            return select.getFinalSelect();
        } catch (InvalidCreateOperationException | InvalidParseOperationException e) {
            throw new UnexpectedDataException(
                "Unexpected error occured while building holding dsl query : " + e.getMessage()
            );
        }
    }

    public String computedInheritedRules(final SearchCriteriaDto searchCriteriaDto) throws VitamClientException {
        LOGGER.debug("Computed Inherited Rules by criteria {} ", searchCriteriaDto.toString());
        VitamContext vitamContext = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();
        JsonNode jsonNode = mapRequestToDslQuery(searchCriteriaDto);
        ObjectNode dslRequest = (ObjectNode) jsonNode;
        RulesUpdateCommonService.deleteAttributesFromObjectNode(
            dslRequest,
            DSL_QUERY_PROJECTION,
            DSL_QUERY_FILTER,
            DSL_QUERY_FACETS
        );
        LOGGER.debug("Computed Inherited Rules final dslQuery : {}", dslRequest);
        JsonNode response = computedInheritedRules(vitamContext, dslRequest);
        return response.findValue(OPERATION_IDENTIFIER).textValue();
    }

    private JsonNode computedInheritedRules(final VitamContext vitamContext, final JsonNode dslQuery)
        throws VitamClientException {
        RequestResponse<JsonNode> response = unitCommonService.computedInheritedRules(vitamContext, dslQuery);
        return response.toJsonNode();
    }

    public JsonNode selectUnitWithInheritedRules(final JsonNode dslQuery, final VitamContext vitamContext)
        throws VitamClientException {
        RequestResponse<JsonNode> response = unitCommonService.selectUnitsWithInheritedRules(vitamContext, dslQuery);
        return response.toJsonNode();
    }

    public ResultsDto selectUnitWithInheritedRules(final SearchCriteriaDto searchQuery)
        throws VitamClientException, IOException {
        ResultsDto response = new ResultsDto();
        LOGGER.debug("calling select Units With Inherited Rules by criteria {} ", searchQuery.toString());
        Optional<Long> thresholdOpt = archiveSearchThresholdService.retrieveProfilThresholds();
        thresholdOpt.ifPresent(searchQuery::setThreshold);
        final VitamContext vitamContext = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();
        archiveSearchAgenciesService.mapAgenciesNameToCodes(searchQuery, vitamContext);
        JsonNode dslQuery = mapRequestToDslQuery(searchQuery);
        RulesUpdateCommonService.deleteAttributesFromObjectNode((ObjectNode) dslQuery, DSL_QUERY_FACETS);
        JsonNode vitamResponse = selectUnitWithInheritedRules(dslQuery, vitamContext);
        ArchiveUnitsDto archiveUnitsDto = decorateAndMapResponse(vitamResponse, vitamContext);
        if (
            Objects.nonNull(archiveUnitsDto.getArchives()) &&
            !CollectionUtils.isEmpty(archiveUnitsDto.getArchives().getResults())
        ) {
            response = archiveUnitsDto.getArchives().getResults().get(0);
        }
        return response;
    }

    public String reclassification(final ReclassificationCriteriaDto reclassificationCriteriaDto)
        throws VitamClientException {
        if (reclassificationCriteriaDto == null) {
            throw new BadRequestException("Error reclassification criteria");
        }
        LOGGER.debug("Reclassification Object : {}", reclassificationCriteriaDto.toString());

        Optional<Long> thresholdOpt = archiveSearchThresholdService.retrieveProfilThresholds();
        thresholdOpt.ifPresent(aLong -> reclassificationCriteriaDto.getSearchCriteriaDto().setThreshold(aLong));
        final VitamContext vitamContext = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();
        JsonNode dslQuery = mapRequestToDslQuery(reclassificationCriteriaDto.getSearchCriteriaDto());
        ArrayNode array = JsonHandler.createArrayNode();
        ((ObjectNode) dslQuery).putPOJO(ACTION, reclassificationCriteriaDto.getAction());
        Arrays.stream(
            new String[] { DSL_QUERY_PROJECTION, DSL_QUERY_FILTER, DSL_QUERY_FACETS, DSL_QUERY_THRESHOLD }
        ).forEach(((ObjectNode) dslQuery)::remove);
        array.add(dslQuery);
        LOGGER.debug("Reclassification query : {}", array);
        RequestResponse<JsonNode> jsonNodeRequestResponse = unitCommonService.reclassification(vitamContext, array);
        return jsonNodeRequestResponse.toJsonNode().findValue(OPERATION_IDENTIFIER).textValue();
    }

    /**
     * Read external ontology fields list from a file
     *
     * @throws IOException : throw an exception while parsing ontologies file
     */
    public List<VitamUiOntologyDto> readExternalOntologiesFromFile() throws IOException {
        LOGGER.debug("get ontologies file from path : {} ", ontologiesFilePath);
        final Integer tenantId = securityService.getTenantIdentifier();
        return OntologyServiceReader.readExternalOntologiesFromFile(tenantId, ontologiesFilePath);
    }

    public PersistentIdentifierResponseDto findUnitsByPersistentIdentifier(String identifier)
        throws VitamClientException {
        LOGGER.debug("Persistent identifier : {}", identifier);
        VitamContext vitamContext = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();
        RequestResponse<JsonNode> response = persistentIdentifierService.findUnitsByPersistentIdentifier(
            identifier,
            vitamContext
        );
        try {
            return objectMapper.readValue(response.toString(), PersistentIdentifierResponseDto.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Response not in good format {}", e);
            throw new VitamClientException("Unable to find the UA", e);
        }
    }

    public PersistentIdentifierResponseDto findObjectsByPersistentIdentifier(String identifier)
        throws VitamClientException {
        LOGGER.debug("Persistent identifier : {}", identifier);

        VitamContext vitamContext = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();
        RequestResponse<JsonNode> response = persistentIdentifierService.findObjectsByPersistentIdentifier(
            identifier,
            vitamContext
        );
        try {
            return objectMapper.readValue(response.toString(), PersistentIdentifierResponseDto.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Response not in good format {}", e);
            throw new VitamClientException("Unable to find the GOT", e);
        }
    }

    public Mono<ResponseEntity<Resource>> downloadObjectFromUnit(String id, String usage, Integer version)
        throws VitamClientException {
        return downloadObjectFromUnit(
            id,
            usage,
            version,
            archiveSearchExternalParametersService.buildVitamContextFromExternalParam()
        );
    }

    public Mono<ResponseEntity<Resource>> downloadObjectFromUnit(
        String id,
        String usage,
        Integer version,
        VitamContext vitamContext
    ) throws VitamClientException {
        // Logic moved here from ui-archive-search
        String fileName = null;
        ResultsDto got = findObjectById(id, vitamContext);
        if (nonNull(got)) {
            QualifiersDto qualifier;
            if (isEmpty(usage)) {
                // find the best qualifier for download
                qualifier = ArchiveSearchUtils.getLastObjectQualifier(got);
            } else {
                String finalUsage = usage;
                qualifier = got
                    .getQualifiers()
                    .stream()
                    .filter(q -> finalUsage.equals(q.getQualifier()))
                    .findFirst()
                    .orElse(null);
            }
            if (nonNull(qualifier)) {
                usage = qualifier.getQualifier();
                VersionsDto versionsDto;
                if (isNull(version)) {
                    // find the latest version for the qualifier
                    versionsDto = ArchiveSearchUtils.getLastVersion(qualifier);
                } else {
                    Integer finalVersion = version;
                    versionsDto = qualifier
                        .getVersions()
                        .stream()
                        .filter(v -> finalVersion.equals(ArchiveSearchUtils.extractVersion(v)))
                        .findFirst()
                        .orElse(null);
                }
                if (nonNull(versionsDto)) {
                    version = ArchiveSearchUtils.extractVersion(versionsDto);
                    fileName = ArchiveSearchUtils.getFilename(versionsDto);
                }
            }
        }

        return ArchiveSearchUtils.convertResponseToMono(
            this.downloadObjectStreamFromUnit(id, usage, version, vitamContext),
            fileName
        );
    }

    public String reassignOriginatingAgency(ReassignRequestDto reassignRequestDto) throws VitamClientException {
        // Validate single originating agency
        validateSingleOriginatingAgency(reassignRequestDto.getSearchCriteria());

        if (ReassignmentMode.BY_ID.equals(reassignRequestDto.getReassignMode())) {
            validateNoHoldingUnits(reassignRequestDto.getSearchCriteria()); // validate no holding units
        } else if (ReassignmentMode.BY_OPI.equals(reassignRequestDto.getReassignMode())) {
            validateEntryOperationReassignment(reassignRequestDto.getSearchCriteria()); // validate that operations have INGEST type
        }

        Optional<Long> thresholdOpt = archiveSearchThresholdService.retrieveProfilThresholds();
        thresholdOpt.ifPresent(reassignRequestDto.getSearchCriteria()::setThreshold);
        VitamContext vitamContext = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();
        JsonNode dslQuery = prepareDslQuery(reassignRequestDto.getSearchCriteria(), vitamContext);
        JsonNode reassignmentDsl = buildReassignmentDsl(
            dslQuery,
            reassignRequestDto.getSearchCriteria().getThreshold()
        );
        RequestResponse<JsonNode> jsonNodeRequestResponse = unitCommonService.reassignment(
            vitamContext,
            reassignmentDsl,
            reassignRequestDto.getSourceOriginatingAgency(),
            reassignRequestDto.getTargetOriginatingAgency(),
            reassignRequestDto.isPropagateToObjectGroups()
        );
        return jsonNodeRequestResponse.toJsonNode().findValue(OPERATION_IDENTIFIER).textValue();
    }

    /**
     * Check if operation IDs exist in the logbook.
     *
     * @param operationIds List of operation IDs to check
     * @return Map of operation IDs to their existence status (true if exists, false otherwise)
     * @throws VitamClientException if there's an error communicating with Vitam
     */
    public Map<String, Boolean> checkOperationIdsExistence(List<String> operationIds) throws VitamClientException {
        if (operationIds == null || operationIds.isEmpty()) {
            return Map.of();
        }

        List<LogbookOperationDto> operations = retrieveOperationsInformation(operationIds);
        Set<String> existingOperationIds = operations
            .stream()
            .map(LogbookOperationDto::getEvId)
            .collect(Collectors.toSet());

        return operationIds.stream().collect(Collectors.toMap(id -> id, existingOperationIds::contains));
    }

    private JsonNode buildReassignmentDsl(JsonNode searchDsl, Long threshold) {
        ObjectNode reassignmentDsl = JsonHandler.createObjectNode();
        reassignmentDsl.set(
            BuilderToken.GLOBAL.ROOTS.exactToken(),
            searchDsl.get(BuilderToken.GLOBAL.ROOTS.exactToken())
        );
        reassignmentDsl.set(
            BuilderToken.GLOBAL.QUERY.exactToken(),
            searchDsl.get(BuilderToken.GLOBAL.QUERY.exactToken())
        );
        if (threshold != null) {
            reassignmentDsl.set(
                BuilderToken.GLOBAL.THRESOLD.exactToken(),
                objectMapper.convertValue(threshold, JsonNode.class)
            );
        }
        return reassignmentDsl;
    }

    private void validateEntryOperationReassignment(SearchCriteriaDto searchCriteria) throws VitamClientException {
        var operationIds = extractOperationIds(searchCriteria);
        if (operationIds.isEmpty()) {
            return;
        }

        var operations = retrieveOperationsInformation(operationIds);
        if (operations.isEmpty()) {
            throw new VitamClientException("NO_OPERATIONS_FOUND");
        }
        boolean hasNonIngestOperation = operations.stream().anyMatch(op -> !"INGEST".equals(op.getEvTypeProc()));
        if (hasNonIngestOperation) {
            throw new BadRequestException("INVALID_ARCHIVAL_UNIT_TYPE");
        }
    }

    private void validateNoHoldingUnits(SearchCriteriaDto searchCriteria) throws VitamClientException {
        try {
            SearchCriteriaDto searchCriteriaCopy = new SearchCriteriaDto();
            searchCriteriaCopy.setCriteriaList(new ArrayList<>(searchCriteria.getCriteriaList()));
            searchCriteriaCopy.setFieldsList(new ArrayList<>(searchCriteria.getFieldsList()));
            searchCriteriaCopy.setPageNumber(1);
            searchCriteriaCopy.setSize(1);

            // Add criteria to search for holding units
            SearchCriteriaEltDto holdingUnitCriteria = new SearchCriteriaEltDto();
            holdingUnitCriteria.setCriteria("ALL_ARCHIVE_UNIT_TYPES");
            holdingUnitCriteria.setValues(List.of(new CriteriaValue("ARCHIVE_UNIT_HOLDING_UNIT")));
            holdingUnitCriteria.setOperator("EQ");
            holdingUnitCriteria.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
            holdingUnitCriteria.setDataType("STRING");
            searchCriteriaCopy.getCriteriaList().add(holdingUnitCriteria);

            // Build and execute query
            VitamContext vitamContext = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();
            JsonNode dslQuery = mapRequestToDslQuery(searchCriteriaCopy);
            JsonNode response = searchArchiveUnits(dslQuery, vitamContext);

            // Check if any holding units were found
            VitamUISearchResponseDto searchResponse = objectMapper.treeToValue(
                response,
                VitamUISearchResponseDto.class
            );

            if (searchResponse.getHits() != null && searchResponse.getHits().getTotal() > 0) {
                throw new BadRequestException("INVALID_ARCHIVAL_UNIT_TYPE");
            }
        } catch (JsonProcessingException e) {
            throw new VitamClientException("Error while validating holding units", e);
        }
    }

    private List<String> extractOperationIds(SearchCriteriaDto searchCriteria) {
        List<String> operationIds = new ArrayList<>();

        if (searchCriteria.getCriteriaList() != null) {
            searchCriteria
                .getCriteriaList()
                .stream()
                .filter(criteria -> "GUID_OPI".equals(criteria.getCriteria()))
                .flatMap(criteria -> criteria.getValues().stream())
                .forEach(value -> operationIds.add(value.getValue()));
        }

        return operationIds;
    }

    private List<LogbookOperationDto> retrieveOperationsInformation(List<String> operationIds)
        throws VitamClientException {
        try {
            VitamContext vitamContext = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();

            // Convert to Set to remove duplicates and optimize query
            Set<String> uniqueOperationIds = new HashSet<>(operationIds);

            // Build query to retrieve operations by evIdProc using OR conditions
            final Select select = new Select();
            final BooleanQuery query = QueryHelper.or();

            // Add an eq condition for each unique operation ID
            for (String operationId : uniqueOperationIds) {
                query.add(QueryHelper.eq("evIdProc", operationId));
            }

            select.setQuery(query);

            return VitamRestUtils.responseMapping(
                logbookService.selectOperations(select.getFinalSelect(), vitamContext).toJsonNode(),
                LogbookOperationsCommonResponseDto.class
            ).getResults();
        } catch (InvalidCreateOperationException e) {
            throw new VitamClientException("Error building query for operations retrieval", e);
        }
    }

    private void validateSingleOriginatingAgency(SearchCriteriaDto searchCriteria) throws VitamClientException {
        try {
            VitamContext vitamContext = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();
            // Add facet for originating agency
            List<TermsFacet> facets = new ArrayList<>();
            TermsFacet originatingAgencyFacet = new TermsFacet(
                "originating_agency_facet",
                "#originating_agency",
                2, // We only need to know if there's more than 1
                FacetOrder.ASC
            );
            facets.add(originatingAgencyFacet);
            searchCriteria.setFacets(facets);

            // Build DSL query with facets
            JsonNode dslQuery = mapRequestToDslQuery(searchCriteria);

            // Execute search
            JsonNode response = searchArchiveUnits(dslQuery, vitamContext);
            VitamUISearchResponseDto searchResponse = objectMapper.treeToValue(
                response,
                VitamUISearchResponseDto.class
            );

            // Check facet results
            if (searchResponse.getFacetResults() != null) {
                searchResponse
                    .getFacetResults()
                    .stream()
                    .filter(facet -> "originating_agency_facet".equals(facet.getName()))
                    .findFirst()
                    .ifPresent(facet -> {
                        if (facet.getBuckets() != null && facet.getBuckets().size() > 1) {
                            throw new BadRequestException("ERROR_MULTIPLE_SP");
                        }
                    });
            }
        } catch (JsonProcessingException e) {
            throw new VitamClientException("Error while validating originating agencies", e);
        }
    }
}
