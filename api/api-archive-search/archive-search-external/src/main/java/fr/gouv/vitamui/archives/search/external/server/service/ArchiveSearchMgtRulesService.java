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

package fr.gouv.vitamui.archives.search.external.server.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitam.common.model.massupdate.MassUpdateUnitRuleRequest;
import fr.gouv.vitam.common.model.massupdate.RuleActions;
import fr.gouv.vitamui.archives.search.common.common.RulesUpdateCommonService;
import fr.gouv.vitamui.archives.search.common.dto.RuleSearchCriteriaDto;
import fr.gouv.vitamui.archives.search.external.server.converter.RuleOperationsConverter;
import fr.gouv.vitamui.commons.api.exception.ForbiddenException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.vitam.api.access.UnitService;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractService;
import fr.gouv.vitamui.commons.vitam.api.dto.AccessContractResponseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * Archive-Search Management rules Internal service.
 */
@Service
public class ArchiveSearchMgtRulesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveSearchMgtRulesService.class);
    private final ObjectMapper objectMapper;
    private final ArchiveSearchService archiveSearchService;
    private final RuleOperationsConverter ruleOperationsConverter;
    private final AccessContractService accessContractService;

    private final ArchiveSearchThresholdService archiveSearchThresholdService;
    private final ArchiveSearchExternalParametersService archiveSearchExternalParametersService;

    private final UnitService unitService;

    @Autowired
    public ArchiveSearchMgtRulesService(
        final @Lazy ArchiveSearchService archiveSearchService,
        final RuleOperationsConverter ruleOperationsConverter,
        final AccessContractService accessContractService,
        final UnitService unitService,
        final ObjectMapper objectMapper,
        ArchiveSearchThresholdService archiveSearchThresholdService,
        ArchiveSearchExternalParametersService archiveSearchExternalParametersService
    ) {
        this.archiveSearchService = archiveSearchService;
        this.objectMapper = objectMapper;
        this.ruleOperationsConverter = ruleOperationsConverter;
        this.accessContractService = accessContractService;
        this.unitService = unitService;
        this.archiveSearchThresholdService = archiveSearchThresholdService;
        this.archiveSearchExternalParametersService = archiveSearchExternalParametersService;
    }

    public String updateArchiveUnitsRules(final RuleSearchCriteriaDto ruleSearchCriteriaDto)
        throws VitamClientException {
        LOGGER.debug(
            "Add Rules to ArchiveUnits using query : {} and DSL actions : {}",
            ruleSearchCriteriaDto.getSearchCriteriaDto().toString(),
            ruleSearchCriteriaDto.getRuleActions()
        );
        final VitamContext vitamContext = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();
        Optional<Long> thresholdOpt = archiveSearchThresholdService.retrieveProfilThresholds();
        thresholdOpt.ifPresent(aLong -> ruleSearchCriteriaDto.getSearchCriteriaDto().setThreshold(aLong));
        boolean hasAccessContractWritePermission = checkAccessContractWritePermission(vitamContext);
        if (!hasAccessContractWritePermission) {
            LOGGER.error(
                "the access contract : {} ,using to update unit rules has no writing permission to update units",
                vitamContext.getAccessContract()
            );
            throw new ForbiddenException(
                "the access contract using to update unit rules has no writing permission to update units"
            );
        }
        RuleActions ruleActions = ruleOperationsConverter.convertToVitamRuleActions(
            ruleSearchCriteriaDto.getRuleActions()
        );
        MassUpdateUnitRuleRequest massUpdateUnitRuleRequest = new MassUpdateUnitRuleRequest();
        JsonNode dslQuery = archiveSearchService.mapRequestToDslQuery(ruleSearchCriteriaDto.getSearchCriteriaDto());
        ObjectNode dslRequest = (ObjectNode) dslQuery;
        RulesUpdateCommonService.deleteAttributesFromObjectNode(
            dslRequest,
            ArchiveSearchService.DSL_QUERY_PROJECTION,
            ArchiveSearchService.DSL_QUERY_FILTER,
            ArchiveSearchService.DSL_QUERY_FACETS
        );

        RulesUpdateCommonService.setMassUpdateUnitRuleRequest(massUpdateUnitRuleRequest, ruleActions, dslRequest);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JsonNode updateQuery = objectMapper.convertValue(massUpdateUnitRuleRequest, JsonNode.class);
        LOGGER.debug("Add Rules to UA final updateQuery : {}", updateQuery);

        return massUpdateUnitsRules(vitamContext, updateQuery);
    }

    public String massUpdateUnitsRules(final VitamContext vitamContext, final JsonNode updateQuery)
        throws VitamClientException {
        JsonNode response = unitService.massUpdateUnitsRules(vitamContext, updateQuery).toJsonNode();
        return response.findValue(ArchiveSearchService.OPERATION_IDENTIFIER).textValue();
    }

    private boolean checkAccessContractWritePermission(final VitamContext vitamContext) {
        LOGGER.debug("Check access contract writing permissions : {}", vitamContext.getAccessContract());
        AccessContractResponseDto accessContractResponseDto;
        try {
            RequestResponse<AccessContractModel> response =
                this.accessContractService.findAccessContractById(vitamContext, vitamContext.getAccessContract());
            accessContractResponseDto = objectMapper.treeToValue(
                response.toJsonNode(),
                AccessContractResponseDto.class
            );
        } catch (VitamClientException | JsonProcessingException e) {
            throw new InternalServerException("Error while parsing Vitam response", e);
        }
        if (
            Objects.nonNull(accessContractResponseDto) &&
            !CollectionUtils.isEmpty(accessContractResponseDto.getResults())
        ) {
            return accessContractResponseDto.getResults().get(0).getWritingPermission();
        } else {
            LOGGER.error(
                "the access contract {} using to update unit rules is not found in vitam",
                vitamContext.getAccessContract()
            );
            throw new ForbiddenException("the access contract is not found, update unit rules will fail.");
        }
    }
}
