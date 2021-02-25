/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.iam.internal.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookLifeCycleResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response;
import java.io.IOException;

/**
 * Controller for logbooks.
 */
@RestController
@RequestMapping(CommonConstants.API_VERSION_1)
@Api(tags = "logbooks", value = "Logbooks Management", description = "logbooks Management")
public class LogbookInternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(LogbookInternalController.class);

    protected final InternalSecurityService securityService;

    private final LogbookService logbookService;

    @Autowired
    public LogbookInternalController(final LogbookService logbookService, final InternalSecurityService securityService) {
        this.logbookService = logbookService;
        this.securityService = securityService;
    }

    @ApiOperation(value = "Get log book operation by json select")
    @Secured({ServicesData.ROLE_LOGBOOKS})
    @PostMapping(value = CommonConstants.LOGBOOK_OPERATIONS_PATH)
    public LogbookOperationsResponseDto findOperations(@RequestHeader(required = true, value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
            @RequestBody final JsonNode select) throws VitamClientException {
        SanityChecker.sanitizeJson(select);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId);
        return VitamRestUtils.responseMapping(logbookService.selectOperations(select, vitamContext).toJsonNode(), LogbookOperationsResponseDto.class);
    }

    @ApiOperation(value = "Get log book operation by id")
    @Secured({ ServicesData.ROLE_LOGBOOKS })
    @GetMapping(value = CommonConstants.LOGBOOK_OPERATION_BY_ID_PATH)
    public LogbookOperationsResponseDto findOperationById(@RequestHeader(required = true, value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
            @RequestHeader(required = true, value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId, @PathVariable final String id)
            throws VitamClientException {
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        return VitamRestUtils.responseMapping(logbookService.selectOperationbyId(id, vitamContext).toJsonNode(), LogbookOperationsResponseDto.class);
    }

    @ApiOperation(value = "Get unit lifecycle by id")
    @Secured(ServicesData.ROLE_LOGBOOKS)
    @GetMapping(value = CommonConstants.LOGBOOK_UNIT_LYFECYCLES_PATH)
    public LogbookLifeCycleResponseDto findUnitLifeCyclesByUnitId(
            @RequestHeader(required = true, value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
            @RequestHeader(required = true, value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId, @PathVariable final String id)
            throws VitamClientException {
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        return VitamRestUtils.responseMapping(logbookService.findUnitLifeCyclesByUnitId(id, vitamContext).toJsonNode(), LogbookLifeCycleResponseDto.class);
    }

    @ApiOperation(value = "Get object lifecycle by id")
    @Secured(ServicesData.ROLE_LOGBOOKS)
    @GetMapping(value = CommonConstants.LOGBOOK_OBJECT_LYFECYCLES_PATH)
    public LogbookLifeCycleResponseDto findObjectGroupLifeCyclesByUnitId(
            @RequestHeader(required = true, value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
            @RequestHeader(required = true, value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId, @PathVariable final String id)
            throws VitamClientException {
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        return VitamRestUtils
                .responseMapping(logbookService.findObjectGroupLifeCyclesByUnitId(id, vitamContext).toJsonNode(), LogbookLifeCycleResponseDto.class);
    }

    @ApiOperation(value = "Download the manifest for a given operation")
    @Secured(ServicesData.ROLE_LOGBOOKS)
    @GetMapping(value = CommonConstants.LOGBOOK_DOWNLOAD_MANIFEST_PATH)
    @ResponseStatus(HttpStatus.OK)
    public void downloadManifest(@PathVariable final String id, final HttpServletResponse response,
            @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER, required = true) final Integer tenantIdentifier) throws IOException {
        LOGGER.debug("Download the manifest for the following Vitam operation : {}", id);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantIdentifier);
        final Response vitamResponse = logbookService.downloadManifest(id, vitamContext);
        VitamRestUtils.writeFileResponse(vitamResponse, response);
    }

    @ApiOperation(value = "Download the ATR for a given operation")
    @Secured(ServicesData.ROLE_LOGBOOKS)
    @GetMapping(value = CommonConstants.LOGBOOK_DOWNLOAD_ATR_PATH)
    @ResponseStatus(HttpStatus.OK)
    public void downloadAtr(@PathVariable final String id, final HttpServletResponse response,
            @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER, required = true) final Integer tenantIdentifier) throws IOException {
        LOGGER.debug("Download the ATR file for the following Vitam operation : {}", id);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantIdentifier);
        final Response vitamResponse = logbookService.downloadAtr(id, vitamContext);
        VitamRestUtils.writeFileResponse(vitamResponse, response);
    }

    @ApiOperation(value = "Download the report file for a given operation")
    @GetMapping(value = CommonConstants.LOGBOOK_DOWNLOAD_REPORT_PATH)
    @Secured(ServicesData.ROLE_LOGBOOKS)
    @ResponseStatus(HttpStatus.OK)
    public void downloadReport(
            @RequestHeader(required = true, value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
            @RequestHeader(required = true, value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
            @PathVariable final String id,
            @PathVariable final String downloadType,
            final HttpServletResponse response) throws VitamClientException, IOException {
        LOGGER.debug("Download the report file for the Vitam operation : {} with download type : {}", id, downloadType);
        final VitamContext vitamContext;
        if(accessContractId != null) {
            vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        } else {
            vitamContext = securityService.buildVitamContext(tenantId);
        }
        final Response vitamResponse = logbookService.downloadReport(id, downloadType, vitamContext);
        VitamRestUtils.writeFileResponse(vitamResponse, response);
    }

}
