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

package fr.gouv.vitamui.referential.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.model.ProcessQuery;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.commons.vitam.api.dto.VitamUIProcessDetailResponseDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.service.LogbookManagementOperationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;

@Api(tags = "logbook-management-operation")
@RestController
@RequestMapping("${ui-referential.prefix}/logbook-management-operation")
@Consumes("application/json")
@Produces("application/json")
public class LogbookManagementOperationController extends AbstractUiRestController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(
        LogbookManagementOperationController.class
    );

    protected final LogbookManagementOperationService logbookManagementOperationService;

    @Autowired
    public LogbookManagementOperationController(
        final LogbookManagementOperationService logbookManagementOperationService
    ) {
        this.logbookManagementOperationService = logbookManagementOperationService;
    }

    @ApiOperation(value = "Get All Operations Details")
    @PostMapping(RestApi.OPERATIONS_PATH)
    @ResponseStatus(HttpStatus.OK)
    public VitamUIProcessDetailResponseDto listOperationsDetails(@RequestBody final ProcessQuery processQuery)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(processQuery);
        LOGGER.debug("Get the operations details with criteria = {}", processQuery);
        VitamUIProcessDetailResponseDto operationResponseDto = new VitamUIProcessDetailResponseDto();
        ResponseEntity<VitamUIProcessDetailResponseDto> processDetailResponse =
            logbookManagementOperationService.searchOperationsDetails(buildUiHttpContext(), processQuery);
        if (processDetailResponse != null) {
            operationResponseDto = processDetailResponse.getBody();
        }
        return operationResponseDto;
    }

    @ApiOperation(value = "Cancel the operation")
    @PostMapping(RestApi.OPERATIONS_PATH + "/cancel" + CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public VitamUIProcessDetailResponseDto cancelOperationProcessExecution(
        final @PathVariable("id") String operationId
    ) throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("operationId is mandatory : ", operationId);
        SanityChecker.checkSecureParameter(operationId);
        LOGGER.debug("Cancel the operation id= {}", operationId);
        return (
                logbookManagementOperationService.cancelOperationProcessExecution(buildUiHttpContext(), operationId) !=
                null
            )
            ? logbookManagementOperationService
                .cancelOperationProcessExecution(buildUiHttpContext(), operationId)
                .getBody()
            : null;
    }

    @ApiOperation(value = "Update the operation status")
    @PostMapping(RestApi.OPERATIONS_PATH + "/update" + CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public VitamUIProcessDetailResponseDto updateOperationActionProcess(
        final @PathVariable("id") String operationId,
        @RequestBody final String actionId
    ) throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("operationId and actionId are mandatories : ", operationId, actionId);
        SanityChecker.checkSecureParameter(operationId, actionId);
        LOGGER.debug("Update Operation Id={} with ActionId = {}", operationId, actionId);
        return (
                logbookManagementOperationService.updateOperationActionProcess(
                    buildUiHttpContext(),
                    actionId,
                    operationId
                ) !=
                null
            )
            ? logbookManagementOperationService
                .updateOperationActionProcess(buildUiHttpContext(), actionId, operationId)
                .getBody()
            : null;
    }
}
