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
import fr.gouv.vitam.common.database.builder.query.BooleanQuery;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.database.builder.request.single.Select;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.FileRulesModel;
import fr.gouv.vitamui.archives.search.common.common.ArchiveSearchConsts;
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnit;
import fr.gouv.vitamui.archives.search.common.dto.CriteriaValue;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaEltDto;
import fr.gouv.vitamui.commons.api.domain.AgencyModelDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.administration.RuleService;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import fr.gouv.vitamui.commons.vitam.api.dto.RuleNodeResponseDto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static fr.gouv.vitam.common.database.builder.query.QueryHelper.and;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.eq;
import static fr.gouv.vitam.common.database.builder.query.QueryHelper.or;

/**
 * Archive-Search rules service for archives unit .
 */
@Service
public class ArchiveSearchRulesInternalService {
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(ArchiveSearchRulesInternalService.class);


    private final ObjectMapper objectMapper;
    final private RuleService ruleService;

    @Autowired
    public ArchiveSearchRulesInternalService(final ObjectMapper objectMapper, final RuleService ruleService) {
        this.objectMapper = objectMapper;
        this.ruleService = ruleService;
    }

    public void mapAppraisalRulesTitlesToCodes(SearchCriteriaDto searchQuery,
        VitamContext vitamContext) throws VitamClientException {
        List<SearchCriteriaEltDto> appraisalMgtRulesCriteriaListProcessed;
        if (CollectionUtils.isEmpty(searchQuery.getCriteriaList())) {
            return;
        }
        List<SearchCriteriaEltDto> titleCriteriaElts =
            searchQuery.retrieveFieldsCriteriaInCategory(ArchiveSearchConsts.APPRAISAL_RULE_TITLE,
                ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);

        if (CollectionUtils.isEmpty(titleCriteriaElts)) {
            SearchCriteriaEltDto titleCriteriaElt = titleCriteriaElts.stream().findFirst().get();
            if (!CollectionUtils.isEmpty(titleCriteriaElt.getValues())) {
                List<String> mgtRulesIdsFound = findRulesByNames(vitamContext,
                    titleCriteriaElt.getValues().stream().map(value -> value.getValue()).collect(
                        Collectors.toList()),
                    ArchiveSearchConsts.APPRAISAL_RULE_TYPE).stream().map(rule -> rule.getRuleId())
                    .collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(mgtRulesIdsFound)) {
                    appraisalMgtRulesCriteriaListProcessed = searchQuery.getCriteriaList().stream()
                        .filter(criteriaElt -> !ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE
                            .equals(criteriaElt.getCategory()) ||
                            !criteriaElt.getCriteria().equals(ArchiveSearchConsts.APPRAISAL_RULE_TITLE)).collect(
                            Collectors.toList());
                    Map<String, SearchCriteriaEltDto> appraisalMgtRulesCriteriaMap =
                        appraisalMgtRulesCriteriaListProcessed.stream()
                            .collect(Collectors.toMap(SearchCriteriaEltDto::getCriteria, Function.identity()));
                    SearchCriteriaEltDto ruleIdCriteria;
                    if (appraisalMgtRulesCriteriaMap.containsKey(ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER)) {
                        ruleIdCriteria =
                            appraisalMgtRulesCriteriaMap.get(ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER);
                        if (!CollectionUtils.isEmpty(mgtRulesIdsFound)) {
                            mgtRulesIdsFound
                                .addAll(ruleIdCriteria.getValues().stream().map(value -> value.getValue()).collect(
                                    Collectors.toList()));
                        }
                    } else {
                        ruleIdCriteria = new SearchCriteriaEltDto();
                        ruleIdCriteria.setCriteria(ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER);
                        ruleIdCriteria.setOperator(ArchiveSearchConsts.CriteriaOperators.EQ.name());
                        ruleIdCriteria.setCategory(ArchiveSearchConsts.CriteriaCategory.APPRAISAL_RULE);
                    }
                    ruleIdCriteria
                        .setValues(mgtRulesIdsFound.stream().map(value -> new CriteriaValue(value)).collect(
                            Collectors.toList()));
                    appraisalMgtRulesCriteriaMap.put(ArchiveSearchConsts.APPRAISAL_RULE_IDENTIFIER, ruleIdCriteria);
                    searchQuery.setCriteriaList(appraisalMgtRulesCriteriaMap.values().stream().collect(
                        Collectors.toList()));
                }

            }
        }
    }


    /**
     * fill archive unit by adding originResponse
     *
     * @param originResponse
     * @param actualAgenciesMapById
     * @return
     */
    public ArchiveUnit fillOriginatingAgencyName(ResultsDto originResponse,
        Map<String, AgencyModelDto> actualAgenciesMapById) {
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


    public List<FileRulesModel> findRulesByCriteria(VitamContext vitamContext, String field,
        List<String> rulesIdentifiers, String ruleType) throws VitamClientException {
        List<FileRulesModel> rules = new ArrayList<>();
        if (rulesIdentifiers != null && !rulesIdentifiers.isEmpty()) {
            LOGGER.info("Finding management rules by field {}  values {} ", field, rulesIdentifiers);
            Map<String, Object> searchCriteriaMap = new HashMap<>();
            searchCriteriaMap.put(field, rulesIdentifiers.get(0));
            if (ruleType != null) {
                searchCriteriaMap.put(ArchiveSearchConsts.RULE_TYPE_FIELD, ruleType);
            }
            try {
                final Select select = new Select();
                final BooleanQuery query = and();
                BooleanQuery queryOr = or();
                for (String elt : rulesIdentifiers) {
                    queryOr.add(eq(field, elt));
                }
                query.add(queryOr);
                select.setLimitFilter(0, rulesIdentifiers.size());
                if (ruleType != null) {
                    query.add(eq(ArchiveSearchConsts.RULE_TYPE_FIELD, ruleType));
                }
                select.setQuery(query);
                JsonNode queryRules = select.getFinalSelect();

                RequestResponse<FileRulesModel> requestResponse =
                    ruleService.findRules(vitamContext, queryRules);
                rules =
                    objectMapper.treeToValue(requestResponse.toJsonNode(), RuleNodeResponseDto.class).getResults();
            } catch (InvalidCreateOperationException e) {
                throw new VitamClientException("Unable to find the rules ", e);
            } catch (JsonProcessingException e1) {
                throw new BadRequestException("Error parsing query ", e1);
            }
        }
        LOGGER.debug("management rules  found {} ", rules);
        return rules;
    }

    /**
     * Search origin agencies by theirs codes
     *
     * @param vitamContext
     * @param rulesIdentifiers
     * @return
     * @throws InvalidParseOperationException
     * @throws VitamClientException
     */
    public List<FileRulesModel> findRulesByIdentifiers(VitamContext vitamContext, Set<String> rulesIdentifiers,
        String ruleType)
        throws VitamClientException {
        List<String> rulesIdentifiersList = new ArrayList<>(rulesIdentifiers);
        return findRulesByCriteria(vitamContext, ArchiveSearchConsts.RULE_ID_FIELD, rulesIdentifiersList, ruleType);
    }

    /**
     * Search rules by their names
     *
     * @param vitamContext
     * @param rulesIdentifiers
     * @return
     * @throws InvalidParseOperationException
     * @throws VitamClientException
     */
    public List<FileRulesModel> findRulesByNames(VitamContext vitamContext, List<String> rulesIdentifiers,
        String ruleType)
        throws VitamClientException {
        List<String> rulesIdentifiersList = new ArrayList<>(rulesIdentifiers);
        return findRulesByCriteria(vitamContext, ArchiveSearchConsts.RULE_NAME_FIELD, rulesIdentifiersList, ruleType);
    }


}
