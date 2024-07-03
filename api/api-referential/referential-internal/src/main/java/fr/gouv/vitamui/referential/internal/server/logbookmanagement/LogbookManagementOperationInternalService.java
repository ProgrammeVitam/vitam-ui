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
import fr.gouv.vitamui.commons.api.enums.OperationActionStatus;
import fr.gouv.vitamui.commons.vitam.api.administration.VitamOperationService;
import fr.gouv.vitamui.commons.vitam.api.dto.ProcessDetailDto;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUIProcessDetailResponseDto;
import org.apache.commons.lang3.EnumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogbookManagementOperationInternalService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogbookManagementOperationInternalService.class);
    private static final String START_MAX_DATE = "30/12/2999";
    private static final String START_MIN_DATE = "01/01/1900";

    private ObjectMapper objectMapper;

    private VitamOperationService vitamOperationService;

    @Autowired
    public LogbookManagementOperationInternalService(
        ObjectMapper objectMapper,
        VitamOperationService vitamOperationService
    ) {
        this.objectMapper = objectMapper;
        this.vitamOperationService = vitamOperationService;
    }

    public ProcessDetailDto searchOperationsDetails(VitamContext vitamContext, ProcessQuery processQuery)
        throws VitamClientException, JsonProcessingException {
        LOGGER.info("Get Operations Details with processQuery = {}", processQuery);
        if (processQuery.getStartDateMax() == null) {
            processQuery.setStartDateMax(START_MAX_DATE);
        }
        if (processQuery.getStartDateMin() == null) {
            processQuery.setStartDateMin(START_MIN_DATE);
        }

        JsonNode response = vitamOperationService.listOperationsDetails(vitamContext, processQuery).toJsonNode();
        final VitamUIProcessDetailResponseDto processDetailResponse = objectMapper.treeToValue(
            response,
            VitamUIProcessDetailResponseDto.class
        );

        VitamUIProcessDetailResponseDto responseFilled = new VitamUIProcessDetailResponseDto();
        responseFilled.setContext(processDetailResponse.getContext());
        responseFilled.setFacetResults(processDetailResponse.getFacetResults());
        responseFilled.setResults(processDetailResponse.getResults());
        responseFilled.setHits(processDetailResponse.getHits());
        return new ProcessDetailDto(responseFilled);
    }

    public ProcessDetailDto updateOperationActionProcess(
        VitamContext vitamContext,
        String actionId,
        String operationId
    ) throws VitamClientException, JsonProcessingException, InterruptedException {
        ProcessDetailDto operation;
        LOGGER.info("Update operation Id= {} with the action Id= {}", operationId, actionId);
        if (!EnumUtils.isValidEnum(OperationActionStatus.class, actionId)) {
            LOGGER.error("Cannot update  the operation, because the actionId= {} given is not correct ", actionId);
            throw new VitamClientException("Cannot update  the operation, because the actionId given is not correct");
        } else {
            vitamOperationService.updateOperationActionProcess(vitamContext, actionId, operationId);
            ProcessQuery processQuery = new ProcessQuery();
            processQuery.setId(operationId);
            operation = searchOperationsDetails(vitamContext, processQuery);
        }
        return operation;
    }

    public ProcessDetailDto cancelOperationProcessExecution(VitamContext vitamContext, String operationId)
        throws VitamClientException, JsonProcessingException {
        ProcessDetailDto operation;
        LOGGER.info("Cancel the operation Id=  {}", operationId);
        vitamOperationService.cancelOperationProcessExecution(vitamContext, operationId);
        ProcessQuery processQuery = new ProcessQuery();
        processQuery.setId(operationId);
        operation = searchOperationsDetails(vitamContext, processQuery);
        return operation;
    }
}
