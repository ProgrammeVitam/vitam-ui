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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.configuration.BuilderToken;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.elimination.EliminationRequestBody;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.vitam.api.access.EliminationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;

import static fr.gouv.vitamui.commons.api.utils.ArchiveSearchConsts.ONLY_DATE_FORMAT;

/**
 * Archive-Search destroy and destroy analysis rules service.
 */
@Service
public class ArchiveSearchEliminationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveSearchEliminationService.class);

    private final EliminationService eliminationService;
    private final ArchiveSearchService archiveSearchService;

    private final ArchiveSearchThresholdService archiveSearchThresholdService;
    private final ArchiveSearchExternalParametersService archiveSearchExternalParametersService;
    private final ObjectMapper objectMapper;

    public ArchiveSearchEliminationService(
        final @Lazy ArchiveSearchService archiveSearchService,
        final EliminationService eliminationService,
        ArchiveSearchThresholdService archiveSearchThresholdService,
        ArchiveSearchExternalParametersService archiveSearchExternalParametersService,
        final ObjectMapper objectMapper
    ) {
        this.eliminationService = eliminationService;
        this.archiveSearchService = archiveSearchService;
        this.archiveSearchThresholdService = archiveSearchThresholdService;
        this.archiveSearchExternalParametersService = archiveSearchExternalParametersService;
        this.objectMapper = objectMapper;
    }

    public JsonNode startEliminationAction(final SearchCriteriaDto searchQuery) throws VitamClientException {
        LOGGER.debug("Elimination action by criteria {} ", searchQuery.toString());
        VitamContext vitamContext = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();
        JsonNode dslQuery = archiveSearchService.prepareDslQuery(searchQuery, vitamContext);
        EliminationRequestBody eliminationRequestBody = null;
        eliminationRequestBody = getEliminationRequestBody(dslQuery, searchQuery.getThreshold());
        LOGGER.debug(
            "Elimination action final query {} ",
            JsonHandler.prettyPrint(eliminationRequestBody.getDslRequest())
        );
        RequestResponse<JsonNode> jsonNodeRequestResponse = eliminationService.startEliminationAction(
            vitamContext,
            eliminationRequestBody
        );
        return jsonNodeRequestResponse.toJsonNode();
    }

    public EliminationRequestBody getEliminationRequestBody(JsonNode updateSet, Long threshold) {
        ObjectNode query = JsonHandler.createObjectNode();
        query.set(BuilderToken.GLOBAL.ROOTS.exactToken(), updateSet.get(BuilderToken.GLOBAL.ROOTS.exactToken()));
        query.set(BuilderToken.GLOBAL.QUERY.exactToken(), updateSet.get(BuilderToken.GLOBAL.QUERY.exactToken()));
        if (threshold != null) {
            query.set(BuilderToken.GLOBAL.THRESOLD.exactToken(), objectMapper.convertValue(threshold, JsonNode.class));
        }
        EliminationRequestBody requestBody = new EliminationRequestBody();
        requestBody.setDate(new SimpleDateFormat(ONLY_DATE_FORMAT).format(new Date()));
        requestBody.setDslRequest(query);
        return requestBody;
    }

    public JsonNode startEliminationAnalysis(final SearchCriteriaDto searchQuery) throws VitamClientException {
        LOGGER.debug("Elimination analysis by criteria {} ", searchQuery.toString());

        Optional<Long> thresholdOpt = archiveSearchThresholdService.retrieveProfilThresholds();
        thresholdOpt.ifPresent(searchQuery::setThreshold);
        VitamContext vitamContext = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();
        JsonNode dslQuery = archiveSearchService.prepareDslQuery(searchQuery, vitamContext);
        EliminationRequestBody eliminationRequestBody = getEliminationRequestBody(dslQuery, searchQuery.getThreshold());

        LOGGER.debug(
            "Elimination analysis final query {} ",
            JsonHandler.prettyPrint(eliminationRequestBody.getDslRequest())
        );
        RequestResponse<JsonNode> jsonNodeRequestResponse = eliminationService.startEliminationAnalysis(
            vitamContext,
            eliminationRequestBody
        );

        return jsonNodeRequestResponse.toJsonNode();
    }
}
