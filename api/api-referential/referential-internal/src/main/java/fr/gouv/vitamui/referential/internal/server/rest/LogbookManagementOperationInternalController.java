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

package fr.gouv.vitamui.referential.internal.server.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.ProcessQuery;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.referential.common.dto.ProcessDetailDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.internal.server.logbookmanagement.LogbookManagementOperationInternalService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(RestApi.LOGBOOK_MANAGEMENT_OPERATION_PATH)
@Getter
@Setter
public class LogbookManagementOperationInternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        LogbookManagementOperationInternalController.class
    );

    @Autowired
    private LogbookManagementOperationInternalService logbookManagementOperationInternalService;

    @Autowired
    private InternalSecurityService securityService;

    @PostMapping(RestApi.OPERATIONS_PATH)
    public ProcessDetailDto listOperationsDetails(@RequestBody final ProcessQuery processQuery)
        throws VitamClientException, JsonProcessingException {
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        LOGGER.debug("Operations details by criteria={}", processQuery);
        LOGGER.debug("EvIdAppSession VitamContext={}", vitamContext.getApplicationSessionId());
        return logbookManagementOperationInternalService.searchOperationsDetails(vitamContext, processQuery);
    }

    @PostMapping(RestApi.CANCEL_OPERATION_PATH + CommonConstants.PATH_ID)
    public ProcessDetailDto cancelOperationProcessExecution(final @PathVariable("id") String operationId)
        throws VitamClientException, JsonProcessingException, InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The operation Id  is a mandatory paramater", operationId);
        SanityChecker.checkSecureParameter(operationId);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        LOGGER.info("EvIdAppSession=  {}", vitamContext.getApplicationSessionId());
        LOGGER.info("Cancel the operation Id=  {}", operationId);
        return logbookManagementOperationInternalService.cancelOperationProcessExecution(vitamContext, operationId);
    }

    @PostMapping(RestApi.UPDATE_OPERATION_PATH + CommonConstants.PATH_ID)
    public ProcessDetailDto updateOperationActionProcess(
        final @PathVariable("id") String operationId,
        @RequestBody final String actionId
    )
        throws VitamClientException, JsonProcessingException, InterruptedException, InvalidParseOperationException, PreconditionFailedException {
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        ParameterChecker.checkParameter("OperationId and actionId are mandatories : ", operationId, actionId);
        SanityChecker.checkSecureParameter(actionId, operationId);
        LOGGER.info("EvIdAppSession=  {}", vitamContext.getApplicationSessionId());
        LOGGER.info("Update the operation Id=  {}", operationId);

        return logbookManagementOperationInternalService.updateOperationActionProcess(
            vitamContext,
            actionId,
            operationId
        );
    }
}
