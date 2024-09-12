/*
 *
 *  * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *  *
 *  * contact.vitam@culture.gouv.fr
 *  *
 *  * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 *  * high volumetry securely and efficiently.
 *  *
 *  * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 *  * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 *  * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *  *
 *  * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 *  * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 *  * successive licensors have only limited liability.
 *  *
 *  * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 *  * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 *  * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 *  * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 *  * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 *  * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *  *
 *  * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 *  * accept its terms.
 *
 */

package fr.gouv.vitamui.collect.internal.server.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.ICSVWriter;
import fr.gouv.vitam.common.LocalDateUtil;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.facet.FacetHelper;
import fr.gouv.vitam.common.database.builder.facet.RangeFacetValue;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.multiple.SelectMultiQuery;
import fr.gouv.vitam.common.database.facet.model.FacetOrder;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AgenciesModel;
import fr.gouv.vitamui.archives.search.common.common.RulesUpdateCommonService;
import fr.gouv.vitamui.archives.search.common.dsl.VitamQueryHelper;
import fr.gouv.vitamui.archives.search.common.dto.AgencyResponseDto;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnit;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitCsv;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.VitamUIArchiveUnitResponseDto;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.api.dtos.CriteriaValue;
import fr.gouv.vitamui.commons.api.dtos.ExportSearchResultParam;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.dtos.VitamUiOntologyDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.InvalidTypeException;
import fr.gouv.vitamui.commons.api.exception.RequestEntityTooLargeException;
import fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts;
import fr.gouv.vitamui.commons.api.utils.OntologyServiceReader;
import fr.gouv.vitamui.commons.vitam.api.administration.AgencyService;
import fr.gouv.vitamui.commons.vitam.api.collect.CollectService;
import fr.gouv.vitamui.commons.vitam.api.dto.FacetBucketDto;
import fr.gouv.vitamui.commons.vitam.api.dto.FacetResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUISearchResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;

import javax.validation.constraints.NotNull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.ACCESS_RULE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.DISSEMINATION_RULE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.FIELDS;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.REUSE_RULE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaCategory.STORAGE_RULE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaDataType.STRING;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.CriteriaOperators.EQ;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FACETS_COUNT_WITHOUT_RULES;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FACETS_EXPIRED_RULES_COMPUTED;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FACETS_FINAL_ACTION_COMPUTED;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FACETS_RULES_COMPUTED_NUMBER;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FACETS_UNEXPIRED_RULES_COMPUTED;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FILING_UNIT_TYPE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FINAL_ACTION_CONFLICT_FIELD_VALUE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FINAL_ACTION_DESTROY_FIELD_VALUE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FINAL_ACTION_KEEP_FIELD_VALUE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FINAL_ACTION_TYPE_CONFLICT;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.FR_DATE_FORMAT_WITH_SLASH;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.HOLDING_UNIT_TYPE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.INGEST_ARCHIVE_TYPE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.ISO_FRENCH_FORMATER;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.RULES_COMPUTED;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.RULE_END_DATE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.RULE_FINAL_ACTION_TYPE;
import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.RULE_ORIGIN_CRITERIA;
import static fr.gouv.vitamui.commons.api.utils.MetadataSearchCriteriaUtils.COMPUTED_FIELDS;
import static fr.gouv.vitamui.commons.api.utils.MetadataSearchCriteriaUtils.FINAL_ACTION_FIELD;
import static fr.gouv.vitamui.commons.api.utils.MetadataSearchCriteriaUtils.MAX_END_DATE_FIELD;
import static fr.gouv.vitamui.commons.api.utils.MetadataSearchCriteriaUtils.RULES_RULE_ID_FIELD;
import static fr.gouv.vitamui.commons.api.utils.MetadataSearchCriteriaUtils.SOME_FUTUR_DATE;
import static fr.gouv.vitamui.commons.api.utils.MetadataSearchCriteriaUtils.SOME_OLD_DATE;
import static fr.gouv.vitamui.commons.api.utils.MetadataSearchCriteriaUtils.cleanString;
import static fr.gouv.vitamui.commons.api.utils.MetadataSearchCriteriaUtils.createDslQueryWithFacets;
import static fr.gouv.vitamui.commons.api.utils.MetadataSearchCriteriaUtils.createSelectMultiQuery;
import static fr.gouv.vitamui.commons.api.utils.MetadataSearchCriteriaUtils.getBasicQuery;
import static fr.gouv.vitamui.commons.api.utils.MetadataSearchCriteriaUtils.mapRequestToSelectMultiQuery;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

public class TransactionArchiveUnitInternalService {

    public static final String DSL_QUERY_FACETS = "$facets";
    public static final String TITLE_FIELD = "Title";
    private final CollectService collectService;

    private AgencyService agencyService;
    private final ObjectMapper objectMapper;
    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionArchiveUnitInternalService.class);

    private static final String RESULTS = "$results";

    @Value("${ontologies_file_path}")
    private String ontologiesFilePath;

    public TransactionArchiveUnitInternalService(
        CollectService collectService,
        AgencyService agencyService,
        ObjectMapper objectMapper
    ) {
        this.collectService = collectService;
        this.agencyService = agencyService;
        this.objectMapper = objectMapper;
    }

    public ArchiveUnitsDto searchArchiveUnitsByCriteria(
        String transactionId,
        SearchCriteriaDto searchQuery,
        VitamContext vitamContext
    )
        throws VitamClientException, JsonProcessingException, InvalidParseOperationException, InvalidCreateOperationException {
        LOGGER.debug("get units by query {}", searchQuery);
        SelectMultiQuery searchQuerySelectMultiQuery = isEmpty(searchQuery.getCriteriaList())
            ? getBasicQuery(searchQuery)
            : createDslQueryWithFacets(searchQuery);
        JsonNode searchQueryToDSL = searchQuerySelectMultiQuery.getFinalSelect();
        final RequestResponse<JsonNode> result = collectService.searchUnitsByTransactionId(
            transactionId,
            searchQueryToDSL,
            vitamContext
        );
        VitamRestUtils.checkResponse(result);
        final VitamUISearchResponseDto archivesOriginResponse = objectMapper.treeToValue(
            result.toJsonNode(),
            VitamUISearchResponseDto.class
        );

        VitamUIArchiveUnitResponseDto responseFilled = new VitamUIArchiveUnitResponseDto();
        responseFilled.setContext(archivesOriginResponse.getContext());
        responseFilled.setFacetResults(archivesOriginResponse.getFacetResults());
        responseFilled.setResults(
            JsonHandler.getFromJsonNodeList(((RequestResponseOK) result).getResults(), ArchiveUnit.class)
        );
        responseFilled.setHits(archivesOriginResponse.getHits());
        ArchiveUnitsDto resultedArchiveUnits = new ArchiveUnitsDto(responseFilled);

        fillFacets(transactionId, searchQuery, resultedArchiveUnits, vitamContext);

        return resultedArchiveUnits;
    }

    public Resource exportToCsvSearchArchiveUnitsByCriteria(
        String transactionId,
        final SearchCriteriaDto searchQuery,
        final VitamContext vitamContext
    ) throws VitamClientException {
        LOGGER.info("Calling exportToCsvSearchArchiveUnitsByCriteria with query {} ", searchQuery);
        Locale locale = Locale.FRENCH;
        if (
            Locale.FRENCH.getLanguage().equals(searchQuery.getLanguage()) ||
            Locale.ENGLISH.getLanguage().equals(searchQuery.getLanguage())
        ) {
            locale = Locale.forLanguageTag(searchQuery.getLanguage());
        }
        ExportSearchResultParam exportSearchResultParam = new ExportSearchResultParam(locale);
        return exportToCsvSearchArchiveUnitsByCriteriaAndParams(
            transactionId,
            searchQuery,
            exportSearchResultParam,
            vitamContext
        );
    }

    public ResultsDto findArchiveUnitById(String id, VitamContext vitamContext) throws VitamClientException {
        try {
            LOGGER.debug("Archive Unit Id : {}", id);
            String re = StringUtils.chop(
                collectService.findUnitById(id, vitamContext).toJsonNode().get(RESULTS).toString().substring(1)
            );
            return objectMapper.readValue(re, ResultsDto.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Can not get the archive unit {} ", e);
            throw new VitamClientException("Unable to find the UA", e);
        }
    }

    private String getArchiveUnitTitle(ArchiveUnit archiveUnit) {
        return getArchiveUnitI18nAttribute(archiveUnit, ResultsDto::getTitle, ResultsDto::getTitle_);
    }

    private String getArchiveUnitDescription(ArchiveUnit archiveUnit) {
        return getArchiveUnitI18nAttribute(archiveUnit, ResultsDto::getDescription, ResultsDto::getDescription_);
    }

    private String getArchiveUnitI18nAttribute(
        ArchiveUnit archiveUnit,
        Function<ArchiveUnit, String> attributeExtractor,
        Function<ArchiveUnit, Map<String, String>> i18nAttributeExtractor
    ) {
        if (archiveUnit == null) {
            return null;
        }
        final String attribute = attributeExtractor.apply(archiveUnit);
        if (StringUtils.isNotBlank(attribute)) {
            return attribute;
        }
        final Map<String, String> attribute_ = i18nAttributeExtractor.apply(archiveUnit);
        if (attribute_ == null) {
            return null;
        }
        return Stream.of("fr", "en")
            .map(lang -> attribute_.entrySet().stream().filter(e -> lang.equalsIgnoreCase(e.getKey())).findFirst())
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .map(Map.Entry::getValue)
            .or(() -> attribute_.values().stream().findFirst())
            .orElse(null);
    }

    private String getArchiveUnitType(ArchiveUnit archiveUnit, String language) {
        String archiveUnitType = null;
        if (archiveUnit != null && !org.apache.commons.lang3.StringUtils.isEmpty(archiveUnit.getUnitType())) {
            switch (archiveUnit.getUnitType()) {
                case FILING_UNIT_TYPE:
                    archiveUnitType = language.equals(Locale.FRENCH.getLanguage())
                        ? ExportSearchResultParam.FR_AU_FILING_SCHEME
                        : ExportSearchResultParam.EN_AU_FILING_SCHEME;
                    break;
                case HOLDING_UNIT_TYPE:
                    archiveUnitType = language.equals(Locale.FRENCH.getLanguage())
                        ? ExportSearchResultParam.FR_AU_HOLDING_SCHEME
                        : ExportSearchResultParam.EN_AU_HOLDING_SCHEME;
                    break;
                case INGEST_ARCHIVE_TYPE:
                    if (org.apache.commons.lang3.StringUtils.isEmpty(archiveUnit.getUnitObject())) {
                        archiveUnitType = language.equals(Locale.FRENCH.getLanguage())
                            ? ExportSearchResultParam.FR_AU_WITHOUT_OBJECT
                            : ExportSearchResultParam.EN_AU_WITHOUT_OBJECT;
                    } else {
                        archiveUnitType = language.equals(Locale.FRENCH.getLanguage())
                            ? ExportSearchResultParam.FR_AU_WITH_OBJECT
                            : ExportSearchResultParam.EN_AU_WITH_OBJECT;
                    }
                    break;
                default:
                    throw new InvalidTypeException("Description Level Type is Unknown !");
            }
        }
        return archiveUnitType;
    }

    private void fillFacets(
        String transactionId,
        SearchCriteriaDto searchQuery,
        ArchiveUnitsDto archiveUnitsDto,
        VitamContext vitamContext
    ) throws InvalidCreateOperationException, VitamClientException, JsonProcessingException {
        if (searchQuery.isComputeFacets()) {
            List<FacetResultsDto> facetResults = archiveUnitsDto.getArchives().getFacetResults();
            if (CollectionUtils.isEmpty(facetResults)) {
                facetResults = new ArrayList<>();
            }
            facetResults.addAll(
                computeFacetsForIndexedRulesCriteria(transactionId, searchQuery.getCriteriaList(), vitamContext)
            );
            archiveUnitsDto.getArchives().setFacetResults(facetResults);
        }
    }

    private List<FacetResultsDto> computeFacetsForIndexedRulesCriteria(
        String transactionId,
        List<SearchCriteriaEltDto> initialArchiveUnitsCriteriaList,
        final VitamContext vitamContext
    ) throws InvalidCreateOperationException, VitamClientException, JsonProcessingException {
        LOGGER.debug("Start finding facets for computed rules  ");

        List<FacetResultsDto> globalRulesFacets = new ArrayList<>();
        try {
            List<ArchiveSearchConsts.CriteriaCategory> categories = List.of(
                APPRAISAL_RULE,
                ACCESS_RULE,
                STORAGE_RULE,
                REUSE_RULE,
                DISSEMINATION_RULE
            );
            List<SearchCriteriaEltDto> indexedArchiveUnitsCriteriaList = new ArrayList<>(
                initialArchiveUnitsCriteriaList
            );
            indexedArchiveUnitsCriteriaList.add(
                new SearchCriteriaEltDto(
                    RULES_COMPUTED,
                    FIELDS,
                    EQ.name(),
                    List.of(new CriteriaValue("true")),
                    STRING.name()
                )
            );
            SelectMultiQuery selectMultiQuery = createSelectMultiQuery(indexedArchiveUnitsCriteriaList);
            selectMultiQuery.addUsedProjection("#id");
            selectMultiQuery.setLimitFilter(0, 1);
            RequestResponse<JsonNode> result = collectService.searchUnitsByTransactionId(
                transactionId,
                selectMultiQuery.getFinalSelect(),
                vitamContext
            );
            VitamRestUtils.checkResponse(result);
            final VitamUISearchResponseDto archivesUnitsResults = objectMapper.treeToValue(
                result.toJsonNode(),
                VitamUISearchResponseDto.class
            );
            List<FacetResultsDto> indexedAuFacets = archivesUnitsResults.getFacetResults();
            globalRulesFacets.addAll(indexedAuFacets);

            for (ArchiveSearchConsts.CriteriaCategory category : categories) {
                FacetResultsDto withoutRulesByCategoryFacet = computeNoRulesFacets(
                    transactionId,
                    indexedArchiveUnitsCriteriaList,
                    category,
                    vitamContext
                );
                globalRulesFacets.add(withoutRulesByCategoryFacet);
                List<FacetResultsDto> facetsForAuHavingRules = computeFacetsForAuHavingRules(
                    transactionId,
                    indexedArchiveUnitsCriteriaList,
                    category,
                    vitamContext
                );
                globalRulesFacets.addAll(facetsForAuHavingRules);
            }
        } catch (InvalidParseOperationException e) {
            throw new BadRequestException("Can't parse criteria as Vitam query" + e.getMessage());
        }
        return globalRulesFacets;
    }

    private FacetResultsDto computeNoRulesFacets(
        String transactionId,
        List<SearchCriteriaEltDto> indexedCriteriaList,
        ArchiveSearchConsts.CriteriaCategory category,
        VitamContext vitamContext
    )
        throws VitamClientException, JsonProcessingException, InvalidCreateOperationException, InvalidParseOperationException {
        List<SearchCriteriaEltDto> criteriaListFacet = new ArrayList<>(indexedCriteriaList);

        criteriaListFacet.add(
            new SearchCriteriaEltDto(
                RULE_ORIGIN_CRITERIA,
                category,
                EQ.name(),
                List.of(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_HAS_NO_ONE.name())),
                STRING.name()
            )
        );

        FacetResultsDto noRuleFacet = new FacetResultsDto();
        noRuleFacet.setName(FACETS_COUNT_WITHOUT_RULES + "_" + category.name());
        noRuleFacet.setBuckets(
            List.of(
                new FacetBucketDto(
                    FACETS_COUNT_WITHOUT_RULES,
                    Long.valueOf(countArchiveUnitByCriteriaList(transactionId, criteriaListFacet, vitamContext))
                )
            )
        );
        return noRuleFacet;
    }

    private Integer countArchiveUnitByCriteriaList(
        String transactionId,
        List<SearchCriteriaEltDto> criteriaList,
        VitamContext vitamContext
    )
        throws VitamClientException, JsonProcessingException, InvalidCreateOperationException, InvalidParseOperationException {
        SearchCriteriaDto facetSearchQuery = new SearchCriteriaDto();
        facetSearchQuery.setCriteriaList(criteriaList);
        facetSearchQuery.setFieldsList(List.of(TITLE_FIELD));
        ArchiveUnitsDto vitamResponse = searchArchiveUnitsByCriteria(transactionId, facetSearchQuery, vitamContext);
        return vitamResponse.getArchives().getHits().getTotal();
    }

    private List<FacetResultsDto> computeFacetsForAuHavingRules(
        String transactionId,
        List<SearchCriteriaEltDto> indexedArchiveUnitsCriteriaList,
        ArchiveSearchConsts.CriteriaCategory category,
        VitamContext vitamContext
    )
        throws InvalidParseOperationException, InvalidCreateOperationException, VitamClientException, JsonProcessingException {
        List<SearchCriteriaEltDto> criteriaList = new ArrayList<>(indexedArchiveUnitsCriteriaList);

        criteriaList.add(
            new SearchCriteriaEltDto(
                RULE_ORIGIN_CRITERIA,
                category,
                EQ.name(),
                List.of(new CriteriaValue(ArchiveSearchConsts.RuleOriginValues.ORIGIN_LOCAL_OR_INHERIT_RULES.name())),
                STRING.name()
            )
        );

        SelectMultiQuery selectMultiQuery = createSelectMultiQuery(criteriaList);

        selectMultiQuery.setLimitFilter(0, 1);
        selectMultiQuery.trackTotalHits(false);

        try {
            List<SearchCriteriaEltDto> rulesCriteriaList = indexedArchiveUnitsCriteriaList
                .stream()
                .filter(Objects::nonNull)
                .filter(searchCriteriaEltDto -> (category.equals(searchCriteriaEltDto.getCategory())))
                .collect(Collectors.toList());
            String computedRulesIdentifierMapping =
                COMPUTED_FIELDS +
                ArchiveSearchConsts.CriteriaMgtRulesCategory.valueOf(category.name()).getFieldMapping() +
                RULES_RULE_ID_FIELD;
            if (APPRAISAL_RULE.equals(category) || STORAGE_RULE.equals(category)) {
                String computedRulesFinalActionMapping =
                    COMPUTED_FIELDS +
                    ArchiveSearchConsts.CriteriaMgtRulesCategory.valueOf(category.name()).getFieldMapping() +
                    FINAL_ACTION_FIELD;
                selectMultiQuery.addFacets(
                    FacetHelper.terms(
                        FACETS_FINAL_ACTION_COMPUTED + "_" + category.name(),
                        computedRulesFinalActionMapping,
                        3,
                        FacetOrder.ASC
                    )
                );
            }
            selectMultiQuery.addFacets(
                FacetHelper.terms(
                    FACETS_RULES_COMPUTED_NUMBER + "_" + category.name(),
                    computedRulesIdentifierMapping,
                    100,
                    FacetOrder.ASC
                )
            );
            addExpirationRulesFacet(rulesCriteriaList, category, selectMultiQuery);
        } catch (DateTimeParseException e) {
            throw new InvalidCreateOperationException(e);
        }

        RequestResponse<JsonNode> result = collectService.searchUnitsByTransactionId(
            transactionId,
            selectMultiQuery.getFinalSelect(),
            vitamContext
        );
        VitamRestUtils.checkResponse(result);
        final VitamUISearchResponseDto archivesUnitsResults = objectMapper.treeToValue(
            result.toJsonNode(),
            VitamUISearchResponseDto.class
        );
        List<FacetResultsDto> auWithRulesFacets = archivesUnitsResults.getFacetResults();

        if (APPRAISAL_RULE.equals(category) || STORAGE_RULE.equals(category)) {
            FacetResultsDto finalActionIndexedFacet = buildComputedAuFinalActionFacet(
                transactionId,
                indexedArchiveUnitsCriteriaList,
                category,
                auWithRulesFacets,
                vitamContext
            );
            auWithRulesFacets = auWithRulesFacets
                .stream()
                .filter(facet -> !(FACETS_FINAL_ACTION_COMPUTED + "_" + category.name()).equals(facet.getName()))
                .collect(Collectors.toList());
            auWithRulesFacets.add(finalActionIndexedFacet);
        }
        return auWithRulesFacets;
    }

    private void addExpirationRulesFacet(
        List<SearchCriteriaEltDto> mgtRulesCriteriaList,
        ArchiveSearchConsts.CriteriaCategory category,
        SelectMultiQuery select
    ) throws InvalidCreateOperationException {
        String strDateExpirationCriteria = extractRuleExpirationDateFromCriteria(mgtRulesCriteriaList, category);
        String managementRuleEndDateMapping =
            COMPUTED_FIELDS +
            ArchiveSearchConsts.CriteriaMgtRulesCategory.valueOf(category.name()).getFieldMapping() +
            MAX_END_DATE_FIELD;
        select.addFacets(
            FacetHelper.dateRange(
                FACETS_EXPIRED_RULES_COMPUTED + "_" + category.name(),
                managementRuleEndDateMapping,
                FR_DATE_FORMAT_WITH_SLASH,
                List.of(new RangeFacetValue(SOME_OLD_DATE, strDateExpirationCriteria))
            )
        );

        select.addFacets(
            FacetHelper.dateRange(
                FACETS_UNEXPIRED_RULES_COMPUTED + "_" + category.name(),
                managementRuleEndDateMapping,
                FR_DATE_FORMAT_WITH_SLASH,
                List.of(new RangeFacetValue(strDateExpirationCriteria, SOME_FUTUR_DATE))
            )
        );
    }

    private FacetResultsDto buildComputedAuFinalActionFacet(
        String transactionId,
        List<SearchCriteriaEltDto> indexedArchiveUnitsCriteriaList,
        ArchiveSearchConsts.CriteriaCategory category,
        List<FacetResultsDto> indexedRulesFacets,
        VitamContext vitamContext
    )
        throws VitamClientException, JsonProcessingException, InvalidCreateOperationException, InvalidParseOperationException {
        FacetResultsDto finalActionIndexedFacet = new FacetResultsDto();
        if (APPRAISAL_RULE.equals(category)) {
            List<FacetBucketDto> finalActionBuckets = computeFinalActionFacetsForComputedAppraisalRules(
                transactionId,
                indexedArchiveUnitsCriteriaList,
                indexedRulesFacets,
                category,
                vitamContext
            );
            finalActionIndexedFacet.setName(FACETS_FINAL_ACTION_COMPUTED + "_" + category.name());
            finalActionIndexedFacet.setBuckets(finalActionBuckets);
        } else if (STORAGE_RULE.equals(category)) {
            Optional<FacetResultsDto> finalActionIndexedFacetOpt = indexedRulesFacets
                .stream()
                .filter(facet -> (FACETS_FINAL_ACTION_COMPUTED + "_" + category.name()).equals(facet.getName()))
                .findAny();
            if (finalActionIndexedFacetOpt.isPresent()) {
                finalActionIndexedFacet = finalActionIndexedFacetOpt.get();
            } else {
                finalActionIndexedFacet = new FacetResultsDto();
            }
        }
        return finalActionIndexedFacet;
    }

    @NotNull
    private List<FacetBucketDto> computeFinalActionFacetsForComputedAppraisalRules(
        String transactionId,
        List<SearchCriteriaEltDto> indexedArchiveUnitsCriteriaList,
        List<FacetResultsDto> indexedRulesFacets,
        ArchiveSearchConsts.CriteriaCategory category,
        VitamContext vitamContext
    )
        throws VitamClientException, JsonProcessingException, InvalidCreateOperationException, InvalidParseOperationException {
        Map<String, Long> finalActionCountMap = new HashMap<>();
        finalActionCountMap.put(FINAL_ACTION_KEEP_FIELD_VALUE, 0l);
        finalActionCountMap.put(FINAL_ACTION_DESTROY_FIELD_VALUE, 0l);
        finalActionCountMap.put(FINAL_ACTION_CONFLICT_FIELD_VALUE, 0l);
        Optional<FacetResultsDto> facetFinalActionValue = indexedRulesFacets
            .stream()
            .filter(facet -> (FACETS_FINAL_ACTION_COMPUTED + "_" + category.name()).equals(facet.getName()))
            .findAny();
        facetFinalActionValue.ifPresent(
            facetResultsDto ->
                facetResultsDto
                    .getBuckets()
                    .stream()
                    .forEach(bucket -> finalActionCountMap.put(bucket.getValue(), bucket.getCount()))
        );
        Integer withConflictFinalActionUnitsCount = computeFinalActionCountByValue(
            transactionId,
            indexedArchiveUnitsCriteriaList,
            FINAL_ACTION_TYPE_CONFLICT,
            category,
            vitamContext
        );
        finalActionCountMap.put(FINAL_ACTION_CONFLICT_FIELD_VALUE, Long.valueOf(withConflictFinalActionUnitsCount));
        if (withConflictFinalActionUnitsCount != 0) {
            Long withKeepFinalActionAppraisalRulesUnitsCount = finalActionCountMap.get(FINAL_ACTION_KEEP_FIELD_VALUE);
            Long withDestroyFinalActionAppraisalRulesUnitsCount = finalActionCountMap.get(
                FINAL_ACTION_DESTROY_FIELD_VALUE
            );
            if (withKeepFinalActionAppraisalRulesUnitsCount > 0) {
                finalActionCountMap.put(
                    FINAL_ACTION_KEEP_FIELD_VALUE,
                    withKeepFinalActionAppraisalRulesUnitsCount - withConflictFinalActionUnitsCount
                );
            }
            if (withDestroyFinalActionAppraisalRulesUnitsCount > 0) {
                finalActionCountMap.put(
                    FINAL_ACTION_DESTROY_FIELD_VALUE,
                    withDestroyFinalActionAppraisalRulesUnitsCount - withConflictFinalActionUnitsCount
                );
            }
        }
        List<FacetBucketDto> finalActionBuckets = new ArrayList<>();
        for (Map.Entry<String, Long> entry : finalActionCountMap.entrySet()) {
            finalActionBuckets.add(new FacetBucketDto(entry.getKey(), entry.getValue()));
        }
        return finalActionBuckets;
    }

    private Integer computeFinalActionCountByValue(
        String transactionId,
        List<SearchCriteriaEltDto> initialCriteriaList,
        String value,
        ArchiveSearchConsts.CriteriaCategory category,
        VitamContext vitamContext
    )
        throws VitamClientException, JsonProcessingException, InvalidCreateOperationException, InvalidParseOperationException {
        List<SearchCriteriaEltDto> criteriaListFacet = new ArrayList<>();
        criteriaListFacet.addAll(initialCriteriaList);
        SearchCriteriaDto countSearchQuery = new SearchCriteriaDto();
        criteriaListFacet.add(
            new SearchCriteriaEltDto(
                RULE_FINAL_ACTION_TYPE,
                category,
                EQ.name(),
                List.of(new CriteriaValue(value)),
                STRING.name()
            )
        );

        criteriaListFacet.add(
            new SearchCriteriaEltDto(
                RULES_COMPUTED,
                FIELDS,
                EQ.name(),
                List.of(new CriteriaValue("true")),
                STRING.name()
            )
        );

        countSearchQuery.setCriteriaList(criteriaListFacet);
        countSearchQuery.setFieldsList(List.of(TITLE_FIELD));
        ArchiveUnitsDto archiveUnitsDto = searchArchiveUnitsByCriteria(transactionId, countSearchQuery, vitamContext);
        return archiveUnitsDto.getArchives().getHits().getTotal();
    }

    @NotNull
    private String extractRuleExpirationDateFromCriteria(
        List<SearchCriteriaEltDto> mgtRulesCriteriaList,
        ArchiveSearchConsts.CriteriaCategory category
    ) {
        String strDateExpirationCriteria;
        Optional<SearchCriteriaEltDto> endDateCriteria = mgtRulesCriteriaList
            .stream()
            .filter(
                searchCriteriaEltDto ->
                    (category.equals(searchCriteriaEltDto.getCategory()) &&
                        RULE_END_DATE.equals(searchCriteriaEltDto.getCriteria()))
            )
            .findAny();
        if (endDateCriteria.isPresent() && !CollectionUtils.isEmpty(endDateCriteria.get().getValues())) {
            String beginDtStr = endDateCriteria.get().getValues().get(0).getBeginInterval();
            String endDtStr = endDateCriteria.get().getValues().get(0).getEndInterval();
            LocalDateTime beginDt = null;
            if (!StringUtils.isEmpty(beginDtStr)) {
                beginDt = LocalDateTime.parse(beginDtStr, ISO_FRENCH_FORMATER);
            }
            LocalDateTime endDt = null;
            if (!StringUtils.isEmpty(endDtStr)) {
                endDt = LocalDateTime.parse(endDtStr, ISO_FRENCH_FORMATER);
            }
            if (beginDt != null && endDt != null) {
                strDateExpirationCriteria = endDt.isAfter(beginDt)
                    ? ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(endDt)
                    : ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(beginDt);
            } else if (beginDt != null) {
                strDateExpirationCriteria = ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(beginDt);
            } else if (endDt != null) {
                strDateExpirationCriteria = ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(endDt);
            } else {
                strDateExpirationCriteria = ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(LocalDateTime.now());
            }
        } else {
            strDateExpirationCriteria = ONLY_DATE_FRENCH_FORMATTER_WITH_SLASH.format(LocalDateTime.now());
        }
        return strDateExpirationCriteria;
    }

    private Resource exportToCsvSearchArchiveUnitsByCriteriaAndParams(
        String transactionId,
        final SearchCriteriaDto searchQuery,
        final ExportSearchResultParam exportSearchResultParam,
        final VitamContext vitamContext
    ) throws VitamClientException {
        try {
            mapAgenciesNameToCodes(searchQuery, vitamContext);
            List<ArchiveUnitCsv> unitCsvList = exportArchiveUnitsByCriteriaToCsvFile(
                transactionId,
                searchQuery,
                vitamContext
            );
            // create a write
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8.name());
            // header record
            String[] headerRecordFr = exportSearchResultParam
                .getHeaders()
                .toArray(new String[exportSearchResultParam.getHeaders().size()]);
            SimpleDateFormat dateFormat = new SimpleDateFormat(exportSearchResultParam.getPatternDate());
            // create a csv writer
            ICSVWriter csvWriter = new CSVWriterBuilder(writer)
                .withSeparator(exportSearchResultParam.getSeparator())
                .withQuoteChar(ICSVWriter.NO_QUOTE_CHARACTER)
                .withEscapeChar(ICSVWriter.DEFAULT_ESCAPE_CHARACTER)
                .withLineEnd(ICSVWriter.DEFAULT_LINE_END)
                .build();
            // write header record
            csvWriter.writeNext(headerRecordFr);

            // write data records
            unitCsvList
                .stream()
                .forEach(archiveUnitCsv -> {
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
                    csvWriter.writeNext(
                        new String[] {
                            archiveUnitCsv.getId(),
                            archiveUnitCsv.getArchiveUnitType(),
                            archiveUnitCsv.getOriginatingAgencyName(),
                            exportSearchResultParam.getDescriptionLevelMap().get(archiveUnitCsv.getDescriptionLevel()),
                            archiveUnitCsv.getTitle(),
                            startDt,
                            endDt,
                            archiveUnitCsv.getDescription(),
                        }
                    );
                });
            // close writers
            csvWriter.close();
            writer.close();
            return new ByteArrayResource(outputStream.toByteArray());
        } catch (IOException ex) {
            throw new BadRequestException("Unable to export csv file ", ex);
        }
    }

    public ResultsDto findObjectGroupById(String objectId, VitamContext vitamContext) throws VitamClientException {
        try {
            LOGGER.debug("[INTERNAL] : Get Object Group by Id");
            String resultStringValue = StringUtils.chop(
                collectService.getObjectById(vitamContext, objectId).toJsonNode().get(RESULTS).toString().substring(1)
            );
            return objectMapper.readValue(resultStringValue, ResultsDto.class);
        } catch (JsonProcessingException e) {
            LOGGER.error("Can not get the object group {} ", e);
            throw new InternalServerException("Unable to find the ObjectGroup", e);
        }
    }

    private List<ArchiveUnitCsv> exportArchiveUnitsByCriteriaToCsvFile(
        String transactionId,
        final SearchCriteriaDto searchQuery,
        final VitamContext vitamContext
    ) throws VitamClientException {
        try {
            LOGGER.info("Calling exporting  export ArchiveUnits to CSV with criteria {}", searchQuery);
            checkSizeLimit(transactionId, vitamContext, searchQuery);
            searchQuery.setPageNumber(0);
            searchQuery.setSize(EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS);
            ArchiveUnitsDto archiveUnitsResult = searchArchiveUnitsByCriteria(transactionId, searchQuery, vitamContext);
            LOGGER.info("archivesResponse found {} ", archiveUnitsResult.getArchives().getResults().size());
            Set<String> originesAgenciesCodes = archiveUnitsResult
                .getArchives()
                .getResults()
                .stream()
                .map(ResultsDto::getOriginatingAgency)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

            List<AgencyModelDto> originAgenciesFound = findOriginAgenciesByCriteria(
                vitamContext,
                "Name",
                new ArrayList<>(originesAgenciesCodes)
            );
            Map<String, AgencyModelDto> agenciesMapByIdentifier = originAgenciesFound
                .stream()
                .collect(Collectors.toMap(AgencyModelDto::getIdentifier, agency -> agency));
            return archiveUnitsResult
                .getArchives()
                .getResults()
                .stream()
                .map(archiveUnit -> fillOriginatingAgencyName(archiveUnit, agenciesMapByIdentifier))
                .map(archiveUnit -> cleanAndMapArchiveUnitResult(archiveUnit, searchQuery.getLanguage()))
                .collect(Collectors.toList());
        } catch (IOException | InvalidParseOperationException | InvalidCreateOperationException e) {
            throw new BadRequestException("Can't parse criteria as Vitam query", e);
        }
    }

    public ArchiveUnit fillOriginatingAgencyName(
        ResultsDto originResponse,
        Map<String, AgencyModelDto> actualAgenciesMapById
    ) {
        ArchiveUnit archiveUnit = new ArchiveUnit();
        BeanUtils.copyProperties(originResponse, archiveUnit);
        if (actualAgenciesMapById != null && !actualAgenciesMapById.isEmpty()) {
            AgencyModelDto agencyModel = actualAgenciesMapById.get(originResponse.getOriginatingAgency());
            if (agencyModel != null) {
                archiveUnit.setOriginatingAgencyName(agencyModel.getName());
            }
        }
        return archiveUnit;
    }

    private void checkSizeLimit(String transactionId, VitamContext vitamContext, SearchCriteriaDto searchQuery)
        throws VitamClientException, IOException, InvalidCreateOperationException, InvalidParseOperationException {
        SearchCriteriaDto searchQueryCounting = new SearchCriteriaDto();
        searchQueryCounting.setCriteriaList(searchQuery.getCriteriaList());
        ArchiveUnitsDto archiveUnitsResult = searchArchiveUnitsByCriteria(
            transactionId,
            searchQueryCounting,
            vitamContext
        );
        Integer nbResults = archiveUnitsResult.getArchives().getHits().getTotal();
        if (nbResults >= EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS) {
            LOGGER.error(
                "The archives units result found is greater than allowed {} ",
                EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS
            );
            throw new RequestEntityTooLargeException(
                "The archives units result found is greater than allowed:  " + EXPORT_ARCHIVE_UNITS_MAX_ELEMENTS
            );
        }
    }

    public void mapAgenciesNameToCodes(SearchCriteriaDto searchQuery, VitamContext vitamContext)
        throws VitamClientException {
        LOGGER.debug("calling mapAgenciesNameToCodes  {} ", searchQuery.toString());
        Set<String> agencyOriginNamesCriteria = new HashSet<>();
        searchQuery
            .getCriteriaList()
            .stream()
            .filter(criteriaElt -> criteriaElt.getCriteria().equals(ArchiveSearchConsts.ORIGINATING_AGENCY_LABEL_FIELD))
            .forEach(
                criteriaElt ->
                    agencyOriginNamesCriteria.addAll(
                        criteriaElt.getValues().stream().map(CriteriaValue::getValue).collect(Collectors.toList())
                    )
            );
        List<AgencyModelDto> agenciesOrigins;
        if (!agencyOriginNamesCriteria.isEmpty()) {
            LOGGER.debug(" trying to mapping agencies labels {} ", agencyOriginNamesCriteria.toString());
            agenciesOrigins = findOriginAgenciesByCriteria(
                vitamContext,
                "Name",
                new ArrayList<>(agencyOriginNamesCriteria)
            );
            if (!CollectionUtils.isEmpty(agenciesOrigins)) {
                mapAgenciesNamesToAgenciesCodesInCriteria(searchQuery, agenciesOrigins);
            }
        }
    }

    public List<AgencyModelDto> findOriginAgenciesByCriteria(
        VitamContext vitamContext,
        String field,
        List<String> originAgenciesCodes
    ) throws VitamClientException {
        List<AgencyModelDto> agencies = new ArrayList<>();
        if (originAgenciesCodes != null && !originAgenciesCodes.isEmpty()) {
            LOGGER.debug("Finding originating agencies by field {}  values {} ", field, originAgenciesCodes);
            Map<String, Object> searchCriteriaMap = new HashMap<>();
            searchCriteriaMap.put(field, originAgenciesCodes);
            try {
                JsonNode queryOriginAgencies = VitamQueryHelper.createQueryDSL(
                    searchCriteriaMap,
                    Optional.empty(),
                    Optional.empty()
                );
                RequestResponse<AgenciesModel> requestResponse = agencyService.findAgencies(
                    vitamContext,
                    queryOriginAgencies
                );
                agencies = objectMapper.treeToValue(requestResponse.toJsonNode(), AgencyResponseDto.class).getResults();
            } catch (InvalidCreateOperationException e) {
                throw new VitamClientException("Unable to find the agencies ", e);
            } catch (InvalidParseOperationException | JsonProcessingException e1) {
                throw new BadRequestException("Error parsing query ", e1);
            }
        }
        LOGGER.debug("origin agencies  found {} ", agencies);
        return agencies;
    }

    private void mapAgenciesNamesToAgenciesCodesInCriteria(
        SearchCriteriaDto searchQuery,
        List<AgencyModelDto> actualAgencies
    ) {
        if (searchQuery != null && searchQuery.getCriteriaList() != null && !searchQuery.getCriteriaList().isEmpty()) {
            List<SearchCriteriaEltDto> mergedCriteriaList = searchQuery
                .getCriteriaList()
                .stream()
                .filter(
                    criteria ->
                        (!ArchiveSearchConsts.ORIGINATING_AGENCY_ID_FIELD.equals(criteria.getCriteria()) &&
                            !ArchiveSearchConsts.ORIGINATING_AGENCY_LABEL_FIELD.equals(criteria.getCriteria()))
                )
                .collect(Collectors.toList());

            List<String> filteredAgenciesId = actualAgencies
                .stream()
                .map(AgencyModelDto::getIdentifier)
                .collect(Collectors.toList());

            List<SearchCriteriaEltDto> idCriteriaList = searchQuery
                .getCriteriaList()
                .stream()
                .filter(criteria -> ArchiveSearchConsts.ORIGINATING_AGENCY_ID_FIELD.equals(criteria.getCriteria()))
                .collect(Collectors.toList());
            SearchCriteriaEltDto idCriteria;
            if (CollectionUtils.isEmpty(idCriteriaList)) {
                idCriteria = new SearchCriteriaEltDto();
                idCriteria.setCriteria(ArchiveSearchConsts.ORIGINATING_AGENCY_ID_FIELD);
                idCriteria.setValues(filteredAgenciesId.stream().map(CriteriaValue::new).collect(Collectors.toList()));
                idCriteria.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
                idCriteria.setCategory(ArchiveSearchConsts.CriteriaCategory.FIELDS);
                mergedCriteriaList.add(idCriteria);
            } else {
                idCriteriaList.forEach(criteria -> {
                    if (!CollectionUtils.isEmpty(criteria.getValues())) {
                        filteredAgenciesId.addAll(
                            criteria.getValues().stream().map(CriteriaValue::getValue).collect(Collectors.toList())
                        );
                    }
                    criteria.setValues(
                        filteredAgenciesId.stream().map(CriteriaValue::new).collect(Collectors.toList())
                    );
                    mergedCriteriaList.add(criteria);
                });
            }

            searchQuery.setCriteriaList(mergedCriteriaList);
        }
    }

    private ArchiveUnitCsv cleanAndMapArchiveUnitResult(ArchiveUnit archiveUnit, String language) {
        if (archiveUnit == null) {
            return null;
        }
        ArchiveUnitCsv archiveUnitCsv = new ArchiveUnitCsv();
        BeanUtils.copyProperties(archiveUnit, archiveUnitCsv);
        archiveUnitCsv.setDescription(cleanString(getArchiveUnitDescription(archiveUnit)));
        archiveUnitCsv.setDescriptionLevel(
            archiveUnit.getDescriptionLevel() != null ? cleanString(archiveUnit.getDescriptionLevel()) : null
        );
        archiveUnitCsv.setArchiveUnitType(getArchiveUnitType(archiveUnit, language));
        archiveUnitCsv.setTitle(cleanString(getArchiveUnitTitle(archiveUnit)));
        archiveUnitCsv.setOriginatingAgencyName(
            archiveUnit.getOriginatingAgencyName() != null ? cleanString(archiveUnit.getOriginatingAgencyName()) : null
        );
        return archiveUnitCsv;
    }

    /**
     * Read ontologies list from a file
     *
     * @param tenantId : tenant identifier
     * @throws IOException : throw an exception while parsing ontologies file
     */
    public List<VitamUiOntologyDto> readExternalOntologiesFromFile(Integer tenantId) throws IOException {
        LOGGER.debug("get ontologies file from path : {} ", ontologiesFilePath);
        return OntologyServiceReader.readExternalOntologiesFromFile(tenantId, ontologiesFilePath);
    }

    /**
     * select archive Unit With Inherited Rules
     *
     * @param searchQuery the search Query
     * @param vitamContext vitam context
     * @return the unit
     * @throws VitamClientException
     * @throws IOException
     */
    public ResultsDto selectUnitWithInheritedRules(
        final SearchCriteriaDto searchQuery,
        String transactionId,
        final VitamContext vitamContext
    ) throws VitamClientException, IOException {
        ResultsDto resultsDto = new ResultsDto();
        LOGGER.debug("calling select Units With Inherited Rules by criteria {} ", searchQuery.toString());
        mapAgenciesNameToCodes(searchQuery, vitamContext);

        SelectMultiQuery selectMultiQuery = mapRequestToSelectMultiQuery(searchQuery);
        JsonNode dslQuery = selectMultiQuery.getFinalSelect();
        RulesUpdateCommonService.deleteAttributesFromObjectNode((ObjectNode) dslQuery, DSL_QUERY_FACETS);
        JsonNode vitamResponse = collectService.selectUnitWithInheritedRules(dslQuery, transactionId, vitamContext);

        final VitamUISearchResponseDto archivesOriginResponse = objectMapper.treeToValue(
            vitamResponse,
            VitamUISearchResponseDto.class
        );

        VitamUIArchiveUnitResponseDto responseFilled = new VitamUIArchiveUnitResponseDto();
        responseFilled.setContext(archivesOriginResponse.getContext());
        responseFilled.setFacetResults(archivesOriginResponse.getFacetResults());
        responseFilled.setResults(
            archivesOriginResponse
                .getResults()
                .stream()
                .map(archiveUnit -> RulesUpdateCommonService.fillOriginatingAgencyName(archiveUnit, null))
                .collect(Collectors.toList())
        );
        responseFilled.setHits(archivesOriginResponse.getHits());

        ArchiveUnitsDto archiveUnitsFound = new ArchiveUnitsDto(responseFilled);

        if (
            Objects.nonNull(archiveUnitsFound.getArchives()) &&
            !CollectionUtils.isEmpty(archiveUnitsFound.getArchives().getResults())
        ) {
            resultsDto = archiveUnitsFound.getArchives().getResults().get(0);
        }
        return resultsDto;
    }
}
