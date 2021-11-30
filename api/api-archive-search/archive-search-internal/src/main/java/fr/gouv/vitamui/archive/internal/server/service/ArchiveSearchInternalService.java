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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import fr.gouv.vitam.common.LocalDateUtil;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.facet.FacetHelper;
import fr.gouv.vitam.common.database.builder.facet.RangeFacetValue;
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.query.Query;
import fr.gouv.vitam.common.database.builder.request.configuration.BuilderToken;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.multiple.SelectMultiQuery;
import fr.gouv.vitam.common.database.facet.model.FacetOrder;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.dip.DataObjectVersions;
import fr.gouv.vitam.common.model.elimination.EliminationRequestBody;
import fr.gouv.vitam.common.model.export.dip.DipExportType;
import fr.gouv.vitam.common.model.export.dip.DipRequest;
import fr.gouv.vitam.common.model.massupdate.MassUpdateUnitRuleRequest;
import fr.gouv.vitam.common.model.massupdate.RuleActions;
import fr.gouv.vitamui.archive.internal.server.rulesupdate.converter.RuleOperationsConverter;
import fr.gouv.vitamui.archive.internal.server.rulesupdate.service.RulesUpdateCommonService;
import fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnit;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitCsv;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.CriteriaValue;
import fr.gouv.vitamui.archives.search.common.dto.ExportDipCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.ExportSearchResultParam;
import fr.gouv.vitamui.archives.search.common.dto.RuleSearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaEltDto;
import fr.gouv.vitamui.archives.search.common.dto.VitamUIArchiveUnitResponseDto;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.exception.RequestEntityTooLargeException;
import fr.gouv.vitamui.commons.api.exception.UnexpectedDataException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.EliminationService;
import fr.gouv.vitamui.commons.vitam.api.access.ExportDipV2Service;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.dto.FacetBucketDto;
import fr.gouv.vitamui.commons.vitam.api.dto.FacetResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.commons.vitam.api.model.UnitTypeEnum;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.ws.rs.core.Response;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.in;
import static fr.gouv.vitam.common.database.builder.query.VitamFieldsHelper.unitType;

/**
 * Archive-Search Internal service communication with VITAM.
 */
@Service
public class ArchiveSearchInternalService {
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveSearchInternalService.class);
    private static final String INGEST_ARCHIVE_TYPE = "INGEST";
    private static final String ARCHIVE_UNIT_DETAILS = "$results";
    private static final Integer EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS = 10000;
    private static final Integer EXPORT_DIP_AND_ELIMINATION_MAX_ELEMENTS = 10000;
    private static final Integer ELIMINATION_ANALYSIS_THRESHOLD = 10000;
    public static final String SEMI_COLON = ";";
    public static final String COMMA = ",";
    public static final String DOUBLE_QUOTE = "\"";
    public static final String SINGLE_QUOTE = "'";
    public static final String NEW_LINE = "\n";
    public static final String NEW_TAB = "\t";
    public static final String NEW_LINE_1 = "\r\n";
    public static final String OPERATION_IDENTIFIER = "itemId";
    public static final String SPACE = " ";
    private static final String[] FILING_PLAN_PROJECTION =
        new String[] {"#id", "Title", "Title_", "DescriptionLevel", "#unitType", "#unitups", "#allunitups"};


    private final ObjectMapper objectMapper;
    private final UnitService unitService;
    private final EliminationService eliminationService;
    private final ArchiveSearchAgenciesInternalService archiveSearchAgenciesInternalService;
    private final ArchiveSearchRulesInternalService archiveSearchRulesInternalService;
    private final ArchivesSearchAppraisalQueryBuilderService archivesSearchAppraisalQueryBuilderService;
    private final ArchivesSearchFieldsQueryBuilderService archivesSearchFieldsQueryBuilderService;
    private final ExportDipV2Service exportDipV2Service;
    private final RuleOperationsConverter ruleOperationsConverter;
    private final RulesUpdateCommonService rulesUpdateCommonService;


    @Autowired
    public ArchiveSearchInternalService(final ObjectMapper objectMapper, final UnitService unitService,
        final ArchiveSearchAgenciesInternalService archiveSearchAgenciesInternalService,
        final ArchiveSearchRulesInternalService archiveSearchRulesInternalService,
        final ArchivesSearchFieldsQueryBuilderService archivesSearchFieldsQueryBuilderService,
        final ExportDipV2Service exportDipV2Service,
        final ArchivesSearchAppraisalQueryBuilderService archivesSearchAppraisalQueryBuilderService,
        final EliminationService eliminationService,
        final RuleOperationsConverter ruleOperationsConverter,
        final RulesUpdateCommonService rulesUpdateCommonService

    ) {
        this.unitService = unitService;
        this.objectMapper = objectMapper;
        this.archiveSearchAgenciesInternalService = archiveSearchAgenciesInternalService;
        this.archiveSearchRulesInternalService = archiveSearchRulesInternalService;
        this.archivesSearchFieldsQueryBuilderService = archivesSearchFieldsQueryBuilderService;
        this.archivesSearchAppraisalQueryBuilderService = archivesSearchAppraisalQueryBuilderService;
        this.exportDipV2Service = exportDipV2Service;
        this.eliminationService = eliminationService;
        this.ruleOperationsConverter = ruleOperationsConverter;
        this.rulesUpdateCommonService = rulesUpdateCommonService;
    }

    public ArchiveUnitsDto searchArchiveUnitsByCriteria(final SearchCriteriaDto searchQuery,
        final VitamContext vitamContext)
        throws VitamClientException, IOException {
        LOGGER.debug("calling find archive units by criteria {} ", searchQuery.toString());
        archiveSearchAgenciesInternalService.mapAgenciesNameToCodes(searchQuery, vitamContext);
        archiveSearchRulesInternalService.mapAppraisalRulesTitlesToCodes(searchQuery, vitamContext);
        JsonNode dslQuery = mapRequestToDslQuery(searchQuery);
        JsonNode vitamResponse = searchArchiveUnits(dslQuery, vitamContext);
        ArchiveUnitsDto archiveUnitsDto = decorateAndMapResponse(vitamResponse, vitamContext);
        if (archiveUnitsDto != null) {
            long nbAppraisalRulesCriteria = searchQuery.getCriteriaList().stream().filter(
                criteria -> (ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE.equals(criteria.getCategory())
                    && criteria.getCriteria()
                    .equals(ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_HAS_NO_ONE.name())))
                .count();
            if (nbAppraisalRulesCriteria != 0) {
                Integer withoutAppraisalRulesUnitsCount =
                    calculateWithoutAppraisalRulesUnitsCount(searchQuery, vitamContext);
                FacetResultsDto noRuleFacet = new FacetResultsDto();
                noRuleFacet.setName(ArchiveSearchConsts.FACETS_COUNT_WITHOUT_RULES);
                noRuleFacet.setBuckets(List.of(new FacetBucketDto(ArchiveSearchConsts.FACETS_COUNT_WITHOUT_RULES,
                    Long.valueOf(withoutAppraisalRulesUnitsCount))));
                archiveUnitsDto.getArchives().getFacetResults().add(noRuleFacet);
            }
        }
        return archiveUnitsDto;
    }

    private Integer calculateWithoutAppraisalRulesUnitsCount(SearchCriteriaDto initialSearchQuery,
        final VitamContext vitamContext) throws VitamClientException, JsonProcessingException {
        SearchCriteriaDto countSearchQuery = new SearchCriteriaDto();
        List<SearchCriteriaEltDto> mergedCriteria = initialSearchQuery.getCriteriaList().stream().filter(
            criteria -> (!(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE.equals(criteria.getCategory())
                && (criteria.getCriteria()
                .equals(
                    ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE.name())
                || criteria.getCriteria()
                .equals(ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE.name())
                || criteria.getCriteria()
                .equals(ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE.name()))
            )
            )
        ).collect(Collectors.toList());
        mergedCriteria.add(new SearchCriteriaEltDto(
            ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_HAS_NO_ONE.name(),
            ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE, ArchiveSearchConsts.CriteriaOperators.EQ.name(),
            List.of(new CriteriaValue(ArchiveSearchConsts.TRUE_CRITERIA_VALUE)),
            ArchiveSearchConsts.CriteriaDataType.STRING.name()));
        countSearchQuery.setCriteriaList(mergedCriteria);
        countSearchQuery.setSize(1);
        countSearchQuery.setPageNumber(0);
        countSearchQuery.setFieldsList(List.of("Title"));
        JsonNode dslQuery = mapRequestToDslQuery(countSearchQuery);
        JsonNode vitamResponse = searchArchiveUnits(dslQuery, vitamContext);
        VitamUISearchResponseDto archivesUnitsResults =
            objectMapper.treeToValue(vitamResponse, VitamUISearchResponseDto.class);
        return archivesUnitsResults.getHits().getTotal();
    }


    private Integer calculateWaitingToRecalculateInheritingRulesUnitsCount(SearchCriteriaDto initialSearchQuery,
        final VitamContext vitamContext) throws VitamClientException, JsonProcessingException {
        SearchCriteriaDto countSearchQuery = new SearchCriteriaDto();
        List<SearchCriteriaEltDto> mergedCriteria = initialSearchQuery.getCriteriaList().stream().filter(
            criteria -> (!(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE.equals(criteria.getCategory())
                && (criteria.getCriteria()
                .equals(
                    ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_INHERITE_AT_LEAST_ONE.name())
                || criteria.getCriteria()
                .equals(ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_HAS_AT_LEAST_ONE.name())
                || criteria.getCriteria()
                .equals(ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_HAS_NO_ONE.name()))
            )
            )
        ).collect(Collectors.toList());

        mergedCriteria.add(new SearchCriteriaEltDto(
            ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE.name(),
            ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE, ArchiveSearchConsts.CriteriaOperators.EQ.name(),
            List.of(new CriteriaValue(ArchiveSearchConsts.TRUE_CRITERIA_VALUE)),
            ArchiveSearchConsts.CriteriaDataType.STRING.name()));
        countSearchQuery.setCriteriaList(mergedCriteria);
        countSearchQuery.setSize(1);
        countSearchQuery.setPageNumber(0);
        countSearchQuery.setFieldsList(List.of("Title"));
        JsonNode dslQuery = mapRequestToDslQuery(countSearchQuery);
        JsonNode vitamResponse = searchArchiveUnits(dslQuery, vitamContext);
        VitamUISearchResponseDto archivesUnitsResults =
            objectMapper.treeToValue(vitamResponse, VitamUISearchResponseDto.class);
        return archivesUnitsResults.getHits().getTotal();
    }

    private ArchiveUnitsDto decorateAndMapResponse(JsonNode vitamResponse, VitamContext vitamContext)
        throws JsonProcessingException, VitamClientException {
        final VitamUISearchResponseDto archivesOriginResponse =
            objectMapper.treeToValue(vitamResponse, VitamUISearchResponseDto.class);
        Set<String> originatingAgenciesCodes = archivesOriginResponse.getResults().stream().map(
            archiveUnit -> archiveUnit.getOriginatingAgency()).collect(Collectors.toSet());
        List<AgencyModelDto> originAgenciesFound =
            archiveSearchAgenciesInternalService.findOriginAgenciesByCodes(vitamContext, originatingAgenciesCodes);
        Map<String, AgencyModelDto> agenciesMapByIdentifier =
            originAgenciesFound.stream().collect(Collectors.toMap(AgencyModelDto::getIdentifier, agency -> agency));

        List<ArchiveUnit> archivesFilled = new ArrayList<>();
        if (archivesOriginResponse != null) {
            archivesFilled = archivesOriginResponse.getResults().stream().map(
                archiveUnit -> archiveSearchAgenciesInternalService
                    .fillOriginatingAgencyName(archiveUnit, agenciesMapByIdentifier)
            ).collect(Collectors.toList());
        }
        VitamUIArchiveUnitResponseDto responseFilled = new VitamUIArchiveUnitResponseDto();
        responseFilled.setContext(archivesOriginResponse.getContext());
        responseFilled.setFacetResults(archivesOriginResponse.getFacetResults());
        responseFilled.setResults(archivesFilled);
        responseFilled.setHits(archivesOriginResponse.getHits());
        return new ArchiveUnitsDto(responseFilled);
    }

    /**
     * Map search query to DSl Query Json node
     *
     * @param searchQuery
     * @return
     */
    public JsonNode mapRequestToDslQuery(SearchCriteriaDto searchQuery)
        throws VitamClientException {
        if (searchQuery == null) {
            throw new BadRequestException("Can't parse null criteria");
        }
        Optional<String> orderBy = Optional.empty();
        Optional<DirectionDto> direction = Optional.empty();
        JsonNode query;
        try {
            if (searchQuery.getSortingCriteria() != null) {
                direction = Optional.of(searchQuery.getSortingCriteria().getSorting());
                orderBy = Optional.of(searchQuery.getSortingCriteria().getCriteria());
            }
            List<SearchCriteriaEltDto> appraisalMgtRulesCriteriaList = searchQuery.getCriteriaList().stream().filter(
                Objects::nonNull)
                .filter(searchCriteriaEltDto -> ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE
                    .equals(searchCriteriaEltDto.getCategory())).collect(
                    Collectors.toList());
            List<SearchCriteriaEltDto> simpleCriteriaList = searchQuery.getCriteriaList().stream().filter(
                Objects::nonNull).filter(searchCriteriaEltDto -> ArchiveSearchConsts.CriteriaCategory.FIELDS
                .equals(searchCriteriaEltDto.getCategory()))
                .collect(
                    Collectors.toList());
            List<String> nodesCriteriaList = searchQuery.getCriteriaList().stream().filter(
                Objects::nonNull).filter(searchCriteriaEltDto -> ArchiveSearchConsts.CriteriaCategory.NODES
                .equals(searchCriteriaEltDto.getCategory()))
                .flatMap(criteria -> criteria.getValues().stream()).map(valueCriteria -> valueCriteria.getValue())
                .collect(
                    Collectors.toList());
            List<String> archiveUnitsTypes = Arrays.asList(INGEST_ARCHIVE_TYPE);
            query = createQueryDSL(archiveUnitsTypes, nodesCriteriaList, simpleCriteriaList,
                appraisalMgtRulesCriteriaList,
                searchQuery.getPageNumber(),
                searchQuery.getSize(), orderBy, direction);
        } catch (InvalidCreateOperationException ioe) {
            throw new VitamClientException("Unable to find archive units with pagination", ioe);
        } catch (InvalidParseOperationException e) {
            throw new BadRequestException("Can't parse criteria as Vitam query" + e.getMessage());
        }

        return query;
    }


    public JsonNode searchArchiveUnits(final JsonNode dslQuery, final VitamContext vitamContext)
        throws VitamClientException {
        RequestResponse<JsonNode> response = unitService.searchUnits(dslQuery, vitamContext);
        return response.toJsonNode();
    }

    public JsonNode getFillingHoldingScheme(VitamContext vitamContext) throws VitamClientException {
        final JsonNode fillingHoldingQuery = createQueryForHoldingFillingUnit();
        return searchArchiveUnits(fillingHoldingQuery, vitamContext);
    }

    public ResultsDto findArchiveUnitById(String id, VitamContext vitamContext) throws VitamClientException {
        try {
            LOGGER.info("Archive Unit Id : {}", id);
            String re = StringUtils
                .chop(unitService.findUnitById(id, vitamContext).toJsonNode().get(ARCHIVE_UNIT_DETAILS).toString()
                    .substring(1));
            return objectMapper.readValue(re, ResultsDto.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Can not get the archive unit {} ", e);
            throw new VitamClientException("Unable to find the UA", e);
        }
    }

    public ResultsDto findObjectById(String id, VitamContext vitamContext) throws VitamClientException {
        try {
            LOGGER.info("Get Object Group");
            String re = StringUtils
                .chop(
                    unitService.findObjectMetadataById(id, vitamContext).toJsonNode()
                        .get(ARCHIVE_UNIT_DETAILS)
                        .toString()
                        .substring(1));
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
    public Response downloadObjectFromUnit(String id, String usage, Integer version, final VitamContext vitamContext)
        throws VitamClientException {
        LOGGER.info("Download Archive Unit Object with id {} , usage {} and version {}  ", id, usage, version);
        return unitService
            .getObjectStreamByUnitId(id, usage, version, vitamContext);
    }

    /**
     * Export archive unit by criteria into csv file
     *
     * @param searchQuery
     * @param vitamContext
     * @throws VitamClientException
     * @throws IOException
     */
    public Resource exportToCsvSearchArchiveUnitsByCriteria(final SearchCriteriaDto searchQuery,
        final VitamContext vitamContext) throws VitamClientException {
        LOGGER.info("Calling exportToCsvSearchArchiveUnitsByCriteria with query {} ", searchQuery);
        Locale locale = Locale.FRENCH;
        if (Locale.FRENCH.getLanguage().equals(searchQuery.getLanguage()) ||
            Locale.ENGLISH.getLanguage().equals(searchQuery.getLanguage())) {
            locale = Locale.forLanguageTag(searchQuery.getLanguage());
        }
        ExportSearchResultParam exportSearchResultParam = new ExportSearchResultParam(locale);
        return exportToCsvSearchArchiveUnitsByCriteriaAndParams(searchQuery, exportSearchResultParam, vitamContext);
    }


    /**
     * export ToCsv Search ArchiveUnits By Criteria And Params by language
     *
     * @param searchQuery
     * @param exportSearchResultParam
     * @param vitamContext
     * @return
     * @throws VitamClientException
     */
    public Resource exportToCsvSearchArchiveUnitsByCriteriaAndParams(final SearchCriteriaDto searchQuery, final
    ExportSearchResultParam exportSearchResultParam, final VitamContext vitamContext)
        throws VitamClientException {
        try {
            archiveSearchAgenciesInternalService.mapAgenciesNameToCodes(searchQuery, vitamContext);
            List<ArchiveUnitCsv> unitCsvList = exportArchiveUnitsByCriteriaToCsvFile(searchQuery, vitamContext);
            // create a write
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8.name());
            // header record
            String[] headerRecordFr =
                exportSearchResultParam.getHeaders().toArray(new String[exportSearchResultParam.getHeaders().size()]);
            SimpleDateFormat dateFormat = new SimpleDateFormat(exportSearchResultParam.getPatternDate());
            // create a csv writer
            ICSVWriter csvWriter = new CSVWriterBuilder(writer)
                .withSeparator(exportSearchResultParam.getSeparator())
                .withQuoteChar(CSVWriter.NO_QUOTE_CHARACTER)
                .withEscapeChar(CSVWriter.DEFAULT_ESCAPE_CHARACTER)
                .withLineEnd(CSVWriter.DEFAULT_LINE_END)
                .build();
            // write header record
            csvWriter.writeNext(headerRecordFr);

            // write data records
            unitCsvList.stream().forEach(archiveUnitCsv -> {
                String startDt = null;
                String endDt = null;
                if (archiveUnitCsv.getStartDate() != null) {
                    try {
                        startDt = dateFormat.format(LocalDateUtil.getDate(archiveUnitCsv.getStartDate()));
                    } catch (ParseException e) {
                        LOGGER.error("Error parsing starting date {} ", archiveUnitCsv.getStartDate());
                    }
                }
                if (archiveUnitCsv.getEndDate() != null) {
                    try {
                        endDt = dateFormat.format(LocalDateUtil.getDate(archiveUnitCsv.getEndDate()));
                    } catch (ParseException e) {
                        LOGGER.error("Error parsing end date {} ", archiveUnitCsv.getEndDate());
                    }
                }
                csvWriter.writeNext(new String[] {archiveUnitCsv.getId(), archiveUnitCsv.getOriginatingAgencyName(),
                    exportSearchResultParam.getDescriptionLevelMap().get(archiveUnitCsv.getDescriptionLevel()),
                    archiveUnitCsv.getTitle(),
                    startDt, endDt,
                    archiveUnitCsv.getDescription()});
            });
            // close writers
            csvWriter.close();
            writer.close();
            Resource generatedResult = new ByteArrayResource(outputStream.toByteArray());
            return generatedResult;
        } catch (IOException ex) {
            throw new BadRequestException("Unable to export csv file ", ex);
        }
    }


    private List<ArchiveUnitCsv> exportArchiveUnitsByCriteriaToCsvFile(final SearchCriteriaDto searchQuery,
        final VitamContext vitamContext) throws VitamClientException {
        try {
            LOGGER.info("Calling exporting  export ArchiveUnits to CSV with criteria {}", searchQuery);
            checkSizeLimit(vitamContext, searchQuery);
            searchQuery.setPageNumber(0);
            searchQuery.setSize(EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS);
            JsonNode archiveUnitsResult =
                searchArchiveUnits(mapRequestToDslQuery(searchQuery), vitamContext);
            final VitamUISearchResponseDto archivesResponse =
                objectMapper.treeToValue(archiveUnitsResult, VitamUISearchResponseDto.class);
            LOGGER.info("archivesResponse found {} ", archivesResponse.getResults().size());
            Set<String> originesAgenciesCodes = archivesResponse.getResults().stream().map(
                archiveUnit -> archiveUnit.getOriginatingAgency()).collect(Collectors.toSet());

            List<AgencyModelDto> originAgenciesFound =
                archiveSearchAgenciesInternalService.findOriginAgenciesByCodes(vitamContext, originesAgenciesCodes);
            Map<String, AgencyModelDto> agenciesMapByIdentifier =
                originAgenciesFound.stream().collect(Collectors.toMap(AgencyModelDto::getIdentifier, agency -> agency));
            List<ArchiveUnitCsv> archivesFilled = new ArrayList<>();
            if (archivesResponse != null) {
                archivesFilled = archivesResponse.getResults().stream().map(
                    archiveUnit -> archiveSearchAgenciesInternalService
                        .fillOriginatingAgencyName(archiveUnit, agenciesMapByIdentifier)
                ).map(archiveUnit -> cleanAndMapArchiveUnitResult(archiveUnit)).collect(Collectors.toList());
            }
            return archivesFilled;
        } catch (IOException e) {
            throw new BadRequestException("Can't parse criteria as Vitam query", e);
        }
    }

    private String getArchiveUnitTitle(ArchiveUnit archiveUnit) {
        String title = null;
        if (archiveUnit != null) {
            if (StringUtils.isEmpty(archiveUnit.getTitle()) || StringUtils.isBlank(archiveUnit.getTitle())) {
                if (archiveUnit.getTitle_() != null) {
                    if (!StringUtils.isEmpty(archiveUnit.getTitle_().getFr()) &&
                        !StringUtils.isBlank(archiveUnit.getTitle_().getFr())) {
                        title = archiveUnit.getTitle_().getFr();
                    } else {
                        title = archiveUnit.getTitle_().getEn();
                    }
                }
            } else {
                title = archiveUnit.getTitle();
            }
        }
        return title;
    }


    private ArchiveUnitCsv cleanAndMapArchiveUnitResult(ArchiveUnit archiveUnit) {
        if (archiveUnit == null) {
            return null;
        }
        ArchiveUnitCsv archiveUnitCsv = new ArchiveUnitCsv();
        BeanUtils.copyProperties(archiveUnit, archiveUnitCsv);
        archiveUnitCsv.setDescription(
            archiveUnit.getDescription() != null ? cleanString(archiveUnit.getDescription()) : null);
        archiveUnitCsv.setDescriptionLevel(
            archiveUnit.getDescriptionLevel() != null ? cleanString(archiveUnit.getDescriptionLevel()) : null);
        archiveUnitCsv.setTitle(cleanString(getArchiveUnitTitle(archiveUnit)));
        archiveUnitCsv.setOriginatingAgencyName(
            archiveUnit.getOriginatingAgencyName() != null ? cleanString(archiveUnit.getOriginatingAgencyName()) :
                null);
        return archiveUnitCsv;
    }

    private String cleanString(String initialValue) {
        if (initialValue == null)
            return null;
        return initialValue.replaceAll(SEMI_COLON, COMMA).replaceAll(DOUBLE_QUOTE, SINGLE_QUOTE)
            .replaceAll(NEW_LINE, SPACE)
            .replaceAll(NEW_LINE_1, SPACE)
            .replaceAll(NEW_TAB, SPACE);
    }

    /**
     * check limit of results limit
     *
     * @param vitamContext
     * @param searchQuery
     */
    private void checkSizeLimit(VitamContext vitamContext, SearchCriteriaDto searchQuery)
        throws VitamClientException, IOException {
        SearchCriteriaDto searchQueryCounting = new SearchCriteriaDto();
        searchQueryCounting.setCriteriaList(searchQuery.getCriteriaList());
        searchQueryCounting.setSize(1);
        searchQueryCounting.setPageNumber(0);
        JsonNode archiveUnitsResult =
            searchArchiveUnits(mapRequestToDslQuery(searchQueryCounting),
                vitamContext);
        final VitamUISearchResponseDto archivesOriginResponse =
            objectMapper.treeToValue(archiveUnitsResult, VitamUISearchResponseDto.class);
        Integer nbResults = archivesOriginResponse.getHits().getTotal();
        if (nbResults >= EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS) {
            LOGGER.error("The archives units result found is greater than allowed {} ",
                EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS);
            throw new RequestEntityTooLargeException(
                "The archives units result found is greater than allowed:  " + EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS);
        }
    }


    /**
     * create a valid VITAM DSL Query from a map of criteria
     *
     * @param unitTypes the input criteria. Should match pattern Map(FieldName, SearchValue)
     * @return The JsonNode required by VITAM external API for a DSL query
     * @throws InvalidParseOperationException
     */
    public JsonNode createQueryDSL(List<String> unitTypes, List<String> nodes,
        List<SearchCriteriaEltDto> simpleCriteriaList, List<SearchCriteriaEltDto> appraisalMgtRulesCriteriaList,
        final Integer pageNumber, final Integer size, final Optional<String> orderBy,
        final Optional<DirectionDto> direction)
        throws InvalidParseOperationException, InvalidCreateOperationException {
        final BooleanQuery query = and();
        final SelectMultiQuery select = new SelectMultiQuery();
        //Handle roots
        LOGGER.debug(
            "Call create Query DSL for unitTypes {} nodes {} simpleCriteriaList {} appraisalMgtRulesCriteriaList {} ",
            unitTypes, nodes, simpleCriteriaList, appraisalMgtRulesCriteriaList);
        if (CollectionUtils.isEmpty(unitTypes)) {
            LOGGER.error("Error on validation of criteria , units types is mandatory ");
            throw new InvalidParseOperationException("Error on validation of criteria,  units types is mandatory ");
        }
        if (!CollectionUtils.isEmpty(nodes)) {
            select.addRoots(nodes.toArray(new String[nodes.size()]));
            query.setDepthLimit(ArchiveSearchConsts.DEFAULT_DEPTH);
        }


        select.addFacets(FacetHelper.terms(ArchiveSearchConsts.FACETS_COUNT_BY_NODE, ArchiveSearchConsts.UNITS_UPS,
            (nodes.size() + 1) * ArchiveSearchConsts.FACET_SIZE_MILTIPLIER, FacetOrder.ASC));
        SearchCriteriaEltDto unitTypesCriteria = new SearchCriteriaEltDto();
        unitTypesCriteria.setCriteria(ArchiveSearchConsts.UNIT_TYPE);
        unitTypesCriteria.setOperator(ArchiveSearchConsts.CriteriaOperators.IN.name());
        unitTypesCriteria.setValues(unitTypes.stream().map(type -> new CriteriaValue(
            type)).collect(Collectors.toList()));
        simpleCriteriaList.add(unitTypesCriteria);

        fillAppraisalRulesFacets(appraisalMgtRulesCriteriaList, select);

        // Manage Filters
        if (orderBy.isPresent()) {
            if (direction.isPresent() && DirectionDto.DESC.equals(direction.get())) {
                select.addOrderByDescFilter(orderBy.get());
            } else {
                select.addOrderByAscFilter(orderBy.get());
            }
        }

        select.setLimitFilter(pageNumber * size, size);
        archivesSearchFieldsQueryBuilderService.fillQueryFromCriteriaList(query, simpleCriteriaList);
        archivesSearchAppraisalQueryBuilderService.fillQueryFromCriteriaList(query, appraisalMgtRulesCriteriaList);
        select.setQuery(query);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.info("Final query: {}", select.getFinalSelect().toPrettyString());
        }
        return select.getFinalSelect();
    }

    private void fillAppraisalRulesFacets(List<SearchCriteriaEltDto> appraisalMgtRulesCriteriaList,
        SelectMultiQuery select)
        throws InvalidCreateOperationException {
        if (!CollectionUtils.isEmpty(appraisalMgtRulesCriteriaList)) {

            select.addFacets(FacetHelper.terms(ArchiveSearchConsts.FACETS_FINAL_ACTION_COMPUTED,
                ArchiveSearchConsts.APPRAISAL_MGT_RULES_FINAL_ACTION_MAPPING.get(
                    ArchiveSearchConsts.APPRAISAL_RULE_FINAL_ACTION_INHERITE_FINAL_ACTION),
                3, FacetOrder.ASC));
            select.addFacets(FacetHelper.terms(ArchiveSearchConsts.FACETS_RULES_COMPUTED_NUMBER,
                ArchiveSearchConsts.INHERITED_APPRAISAL_MGT_RULES_SIMPLE_FIELDS_MAPPING.get(
                    ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER),
                1000, FacetOrder.ASC));
            select.addFacets(FacetHelper.terms(ArchiveSearchConsts.FACETS_WAITING_TO_RECALCULATE_NUMBER,
                ArchiveSearchConsts.APPRAISAL_MGT_RULES_FIELDS_MAPPING.get(
                    ArchiveSearchConsts.AppraisalRuleOriginValues.APPRAISAL_RULE_ORIGIN_WAITING_RECALCULATE.name()),
                2, FacetOrder.ASC));
            String strDateExpirationCriteria;
            Optional<SearchCriteriaEltDto> appraisalEndDateCriteria =
                appraisalMgtRulesCriteriaList.stream().filter(
                    searchCriteriaEltDto -> ArchiveSearchConsts.APPRAISAL_RULE_END_DATE
                        .equals(searchCriteriaEltDto.getCriteria())).findAny();
            if (appraisalEndDateCriteria.isPresent() &&
                !CollectionUtils.isEmpty(appraisalEndDateCriteria.get().getValues())) {

                String beginDtStr = appraisalEndDateCriteria.get().getValues().get(0).getBeginInterval();
                String endDtStr = appraisalEndDateCriteria.get().getValues().get(0).getEndInterval();
                LocalDateTime beginDt = null;
                if (!StringUtils.isEmpty(beginDtStr)) {
                    beginDt =
                        LocalDateTime.parse(beginDtStr, ArchiveSearchConsts.ISO_FRENCH_FORMATER);
                }
                LocalDateTime endDt = null;
                if (!StringUtils.isEmpty(endDtStr)) {
                    endDt = LocalDateTime.parse(endDtStr, ArchiveSearchConsts.ISO_FRENCH_FORMATER);
                }
                if (beginDt != null || endDt != null) {
                    if (beginDt == null || endDt.isAfter(beginDt)) {
                        strDateExpirationCriteria = ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATER.format(endDt);
                    } else {
                        strDateExpirationCriteria = ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATER.format(beginDt);
                    }
                } else {
                    strDateExpirationCriteria =
                        ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATER.format(LocalDateTime.now());
                }

            } else {
                strDateExpirationCriteria = ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATER.format(LocalDateTime.now());
            }

            select.addFacets(FacetHelper.dateRange(ArchiveSearchConsts.FACETS_EXPIRED_RULES_COMPUTED,
                ArchiveSearchConsts.INHERITED_APPRAISAL_MGT_RULES_SIMPLE_FIELDS_MAPPING.get(
                    ArchiveSearchConsts.APPRAISAL_RULE_END_DATE), ArchiveSearchConsts.ONLY_DATE_FORMAT,
                List.of(new RangeFacetValue("1000-01-01", strDateExpirationCriteria))));
        }
    }

    public String requestToExportDIP(final ExportDipCriteriaDto exportDipCriteriaDto,
        final VitamContext vitamContext)
        throws VitamClientException {

        LOGGER.debug("Export DIP by criteria {} ", exportDipCriteriaDto.toString());
        JsonNode dslQuery = prepareDslQuery(exportDipCriteriaDto.getExportDIPSearchCriteria(), vitamContext);
        LOGGER.debug("Export DIP final DSL query {} ", dslQuery);

        DataObjectVersions dataObjectVersionToExport = new DataObjectVersions();
        dataObjectVersionToExport.setDataObjectVersions(exportDipCriteriaDto.getDataObjectVersions());
        DipRequest dipRequest = prepareDipRequestBody(exportDipCriteriaDto, dslQuery);

        JsonNode response = exportDIP(vitamContext, dipRequest);
        return response.findValue(OPERATION_IDENTIFIER).textValue();
    }

    public JsonNode startEliminationAnalysis(final SearchCriteriaDto searchQuery, final VitamContext vitamContext)
        throws VitamClientException {

        LOGGER.debug("Elimination analysis by criteria {} ", searchQuery.toString());
        JsonNode dslQuery = prepareDslQuery(searchQuery, vitamContext);
        EliminationRequestBody eliminationRequestBody = null;
        try {
            eliminationRequestBody = getEliminationRequestBody(dslQuery);
        } catch (InvalidParseOperationException e) {
            throw new PreconditionFailedException("invalid request");
        }

        LOGGER.debug("Elimination analysis final query {} ",
            JsonHandler.prettyPrint(eliminationRequestBody.getDslRequest()));
        RequestResponse<JsonNode> jsonNodeRequestResponse =
            eliminationService.startEliminationAnalysis(vitamContext, eliminationRequestBody);


        return jsonNodeRequestResponse.toJsonNode();
    }

    public JsonNode startEliminationAction(final SearchCriteriaDto searchQuery, final VitamContext vitamContext)
        throws VitamClientException {

        LOGGER.debug("Elimination action by criteria {} ", searchQuery.toString());
        JsonNode dslQuery = prepareDslQuery(searchQuery, vitamContext);
        EliminationRequestBody eliminationRequestBody = null;
        try {
            eliminationRequestBody = getEliminationRequestBody(dslQuery);
        } catch (InvalidParseOperationException e) {
            throw new PreconditionFailedException("invalid request");
        }
        LOGGER.debug("Elimination action final query {} ",
            JsonHandler.prettyPrint(eliminationRequestBody.getDslRequest()));
        RequestResponse<JsonNode> jsonNodeRequestResponse =
            eliminationService.startEliminationAction(vitamContext, eliminationRequestBody);
        return jsonNodeRequestResponse.toJsonNode();
    }

    public EliminationRequestBody getEliminationRequestBody(JsonNode updateSet) throws InvalidParseOperationException {

        ObjectNode query = JsonHandler.createObjectNode();
        query.set(BuilderToken.GLOBAL.ROOTS.exactToken(), updateSet.get(BuilderToken.GLOBAL.ROOTS.exactToken()));
        query.set(BuilderToken.GLOBAL.QUERY.exactToken(), updateSet.get(BuilderToken.GLOBAL.QUERY.exactToken()));
        query.put(BuilderToken.GLOBAL.THRESOLD.exactToken(), ELIMINATION_ANALYSIS_THRESHOLD);

        EliminationRequestBody requestBody = new EliminationRequestBody();
        requestBody.setDate(new SimpleDateFormat(ArchiveSearchConsts.ONLY_DATE_FORMAT).format(new Date()));
        requestBody.setDslRequest(query);
        return requestBody;
    }

    private JsonNode prepareDslQuery(final SearchCriteriaDto searchQuery, final VitamContext vitamContext)
        throws VitamClientException {
        searchQuery.setPageNumber(0);
        searchQuery.setSize(EXPORT_DIP_AND_ELIMINATION_MAX_ELEMENTS);
        archiveSearchAgenciesInternalService.mapAgenciesNameToCodes(searchQuery, vitamContext);
        archiveSearchRulesInternalService.mapAppraisalRulesTitlesToCodes(searchQuery, vitamContext);
        JsonNode dslQuery = mapRequestToDslQuery(searchQuery);

        return dslQuery;
    }

    private DipRequest prepareDipRequestBody(final ExportDipCriteriaDto exportDipCriteriaDto, JsonNode dslQuery) {
        DipRequest dipRequest = new DipRequest();

        if (exportDipCriteriaDto != null) {
            DataObjectVersions dataObjectVersionToExport = new DataObjectVersions();
            dataObjectVersionToExport.setDataObjectVersions(exportDipCriteriaDto.getDataObjectVersions());
            dipRequest.setExportWithLogBookLFC(exportDipCriteriaDto.isLifeCycleLogs());
            dipRequest.setDslRequest(dslQuery);
            dipRequest.setDipExportType(DipExportType.FULL);
            dipRequest.setDataObjectVersionToExport(dataObjectVersionToExport);
            dipRequest.setDipRequestParameters(exportDipCriteriaDto.getDipRequestParameters());
        }
        return dipRequest;
    }

    public JsonNode createQueryForHoldingFillingUnit() {
        try {
            final SelectMultiQuery select = new SelectMultiQuery();
            final Query query =
                in(unitType(), UnitTypeEnum.HOLDING_UNIT.getValue(), UnitTypeEnum.FILING_UNIT.getValue());
            select.addQueries(query);
            ObjectNode orderFilter = JsonHandler.createObjectNode();
            orderFilter.put("Title", 1);
            ObjectNode filter = JsonHandler.createObjectNode();
            filter.set("$orderby", orderFilter);
            select.setFilter(filter);
            select.addUsedProjection(FILING_PLAN_PROJECTION);
            LOGGER.debug("query =", select.getFinalSelect().toPrettyString());
            return select.getFinalSelect();
        } catch (InvalidCreateOperationException | InvalidParseOperationException e) {
            throw new UnexpectedDataException(
                "Unexpected error occured while building holding dsl query : " + e.getMessage());
        }
    }

    public JsonNode exportDIP(final VitamContext vitamContext, DipRequest dipRequest)
        throws VitamClientException {
        RequestResponse<JsonNode> response = exportDipV2Service.exportDip(vitamContext, dipRequest);
        return response.toJsonNode();
    }

    public String massUpdateUnitsRules(final VitamContext vitamContext, final JsonNode updateQuery)
        throws VitamClientException {
        JsonNode response = unitService.massUpdateUnitsRules(vitamContext,updateQuery).toJsonNode();
        return response.findValue(OPERATION_IDENTIFIER).textValue();
    }

    public String updateArchiveUnitsRules(final VitamContext vitamContext, final RuleSearchCriteriaDto ruleSearchCriteriaDto)
        throws VitamClientException {

        LOGGER.info("Add Rules to Units VitamUI Rules : {}", ruleSearchCriteriaDto.getRuleActions());
        LOGGER.info("Add Rules to Units VitamUI search Criteria : {}", ruleSearchCriteriaDto.getSearchCriteriaDto().toString());
        RuleActions ruleActions = ruleOperationsConverter.convertToVitamRuleActions(ruleSearchCriteriaDto.getRuleActions());

        MassUpdateUnitRuleRequest massUpdateUnitRuleRequest = new MassUpdateUnitRuleRequest();
        JsonNode dslQuery = mapRequestToDslQuery(ruleSearchCriteriaDto.getSearchCriteriaDto());
        ObjectNode dslRequest = (ObjectNode) dslQuery;
        rulesUpdateCommonService.deleteAttributesFromObjectNode(dslRequest, "$projection" ,"$filter","$facets");

        rulesUpdateCommonService.setMassUpdateUnitRuleRequest(massUpdateUnitRuleRequest, ruleActions, dslRequest);

        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JsonNode updateQuery = objectMapper.convertValue(massUpdateUnitRuleRequest, JsonNode.class);
        LOGGER.debug("Add Rules to UA final updateQuery : {}", updateQuery);

        return massUpdateUnitsRules(vitamContext, updateQuery);
    }
}
