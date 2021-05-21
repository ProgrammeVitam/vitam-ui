/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2021)
 *
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

package fr.gouv.vitamui.referential.internal.server.logbookmanagement;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.ProcessQuery;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.administration.VitamOperationService;
import fr.gouv.vitamui.referential.common.dto.ProcessDetailDto;
import fr.gouv.vitamui.referential.common.dto.VitamUIProcessDetailResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class LogbookManagementOperationInternalService {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(LogbookManagementOperationInternalService.class);

    private ObjectMapper objectMapper;

    private VitamOperationService vitamOperationService;

    @Autowired
    public LogbookManagementOperationInternalService(ObjectMapper objectMapper, VitamOperationService vitamOperationService) {
        this.objectMapper = objectMapper;
        this.vitamOperationService = vitamOperationService;
    }

    public ProcessDetailDto listOperationsDetails(VitamContext vitamContext, ProcessQuery processQuery) throws VitamClientException, JsonProcessingException {
        LOGGER.info("Get Operations Details with processQuery = {}",processQuery);
        JsonNode response = vitamOperationService.listOperationsDetails(vitamContext,processQuery).toJsonNode();
        final VitamUIProcessDetailResponseDto processDetailResponse =
            objectMapper.treeToValue(response, VitamUIProcessDetailResponseDto.class);

        VitamUIProcessDetailResponseDto responseFilled = new VitamUIProcessDetailResponseDto();
        responseFilled.setContext(processDetailResponse.getContext());
        responseFilled.setFacetResults(processDetailResponse.getFacetResults());
        responseFilled.setResults(processDetailResponse.getResults());
        responseFilled.setHits(processDetailResponse.getHits());
        return new ProcessDetailDto(responseFilled);

    }
}
