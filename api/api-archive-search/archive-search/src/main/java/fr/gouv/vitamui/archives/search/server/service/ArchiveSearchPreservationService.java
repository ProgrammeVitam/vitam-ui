/*
 *
 *  Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
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
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.database.builder.request.configuration.BuilderToken;
import fr.gouv.vitam.common.database.builder.request.exception.InvalidCreateOperationException;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.PreservationRequest;
import fr.gouv.vitam.common.model.PreservationVersion;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitamui.archives.search.common.dto.PreservationRequestDto;
import fr.gouv.vitamui.commons.api.dtos.SearchCriteriaDto;
import fr.gouv.vitamui.commons.vitam.api.access.PreservationService;
import fr.gouv.vitamui.iam.security.service.ExternalParametersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Archive-Search preservation operations service.
 */
@Service
public class ArchiveSearchPreservationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveSearchPreservationService.class);

    private static final String OPERATION_IDENTIFIER = "itemId";

    private final PreservationService preservationService;
    private final ArchiveSearchService archiveSearchService;
    private final ArchiveSearchFacetsService archiveSearchFacetsService;
    private final ArchiveSearchThresholdService archiveSearchThresholdService;
    private final ExternalParametersService externalParametersService;
    private final ObjectMapper objectMapper;

    public ArchiveSearchPreservationService(
        final @Lazy ArchiveSearchService archiveSearchService,
        final ArchiveSearchFacetsService archiveSearchFacetsService,
        final PreservationService preservationService,
        final ArchiveSearchThresholdService archiveSearchThresholdService,
        final ExternalParametersService externalParametersService,
        final ObjectMapper objectMapper
    ) {
        this.archiveSearchService = archiveSearchService;
        this.archiveSearchFacetsService = archiveSearchFacetsService;
        this.preservationService = preservationService;
        this.archiveSearchThresholdService = archiveSearchThresholdService;
        this.externalParametersService = externalParametersService;
        this.objectMapper = objectMapper;
    }

    /**
     * Starts the preservation workflow over the units matching the given search criteria.
     * @param preservationRequestDto the scenario parameters and the units selection
     * @return the identifier of the created operation
     * @throws VitamClientException
     */
    public String launchPreservation(final PreservationRequestDto preservationRequestDto) throws VitamClientException {
        LOGGER.debug("Preservation by criteria {} ", preservationRequestDto);

        final SearchCriteriaDto searchCriteria = preservationRequestDto.getSearchCriteria();
        Optional<Long> thresholdOpt = archiveSearchThresholdService.retrieveProfilThresholds();
        thresholdOpt.ifPresent(searchCriteria::setThreshold);

        VitamContext vitamContext = externalParametersService.buildVitamContextFromExternalParam();
        JsonNode dslQuery = archiveSearchService.prepareDslQuery(searchCriteria, vitamContext);
        JsonNode preservationDsl = buildPreservationDsl(dslQuery, searchCriteria.getThreshold());

        PreservationRequest preservationRequest = new PreservationRequest(
            preservationDsl,
            preservationRequestDto.getScenarioIdentifier(),
            preservationRequestDto.getTargetUsage(),
            PreservationVersion.valueOf(preservationRequestDto.getVersion().getValue()),
            preservationRequestDto.getSourceUsage()
        );

        LOGGER.debug("Preservation final query {} ", JsonHandler.prettyPrint(preservationDsl));
        RequestResponse<JsonNode> jsonNodeRequestResponse = preservationService.launchPreservation(
            vitamContext,
            preservationRequest
        );
        return jsonNodeRequestResponse.toJsonNode().findValue(OPERATION_IDENTIFIER).textValue();
    }

    /**
     * Counts the distinct object groups referenced by the units matching the given search criteria.
     * @param searchCriteriaDto the units selection criteria
     * @return the number of distinct object groups, or {@code null} if it could not be computed
     */
    public Long countObjectGroups(final SearchCriteriaDto searchCriteriaDto)
        throws InvalidCreateOperationException, InvalidParseOperationException, VitamClientException, JsonProcessingException {
        VitamContext vitamContext = externalParametersService.buildVitamContextFromExternalParam();
        archiveSearchService.prepareDslQuery(searchCriteriaDto, vitamContext);
        return archiveSearchFacetsService.countObjectGroups(searchCriteriaDto, vitamContext);
    }

    private JsonNode buildPreservationDsl(JsonNode searchDsl, Long threshold) {
        ObjectNode preservationDsl = JsonHandler.createObjectNode();
        preservationDsl.set(
            BuilderToken.GLOBAL.ROOTS.exactToken(),
            searchDsl.get(BuilderToken.GLOBAL.ROOTS.exactToken())
        );
        preservationDsl.set(
            BuilderToken.GLOBAL.QUERY.exactToken(),
            searchDsl.get(BuilderToken.GLOBAL.QUERY.exactToken())
        );
        if (threshold != null) {
            preservationDsl.set(
                BuilderToken.GLOBAL.THRESOLD.exactToken(),
                objectMapper.convertValue(threshold, JsonNode.class)
            );
        }
        return preservationDsl;
    }
}
