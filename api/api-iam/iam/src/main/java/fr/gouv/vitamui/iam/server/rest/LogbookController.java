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

package fr.gouv.vitamui.iam.server.rest;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.domain.TenantDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.vitam.api.access.LogbookService;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookLifeCycleResponseDto;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationsCommonResponseDto;
import fr.gouv.vitamui.commons.vitam.api.util.VitamRestUtils;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.iam.server.tenant.service.TenantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Objects;

/**
 * UI logbook controller.
 */
@RequestMapping(CommonConstants.API_VERSION_1)
@RestController
@ResponseBody
@Tag(name = "Logbooks", description = "Logbooks Management")
public class LogbookController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogbookController.class);

    protected final SecurityService securityService;

    private final LogbookService logbookService;

    private static final String MANDATORY_IDENTIFIER = "The Identifier is a mandatory parameter: ";

    private static final String DOWNLOAD_TYPE_DIP = "dip";
    private static final String DOWNLOAD_TYPE_TRANSFER_SIP = "transfersip";
    private static final String DOWNLOAD_TYPE_BATCH_REPORT = "batchreport";
    private static final String DOWNLOAD_TYPE_OBJECT = "object";

    private final TenantService tenantService;

    public LogbookController(
        final LogbookService logbookService,
        final SecurityService securityService,
        final TenantService tenantService
    ) {
        this.logbookService = logbookService;
        this.securityService = securityService;
        this.tenantService = tenantService;
    }

    @Operation(operationId = "logbooks_findOperations", summary = "Get log book operation by json select")
    @Secured({ ServicesData.ROLE_LOGBOOKS })
    @PostMapping(value = CommonConstants.LOGBOOK_OPERATIONS_PATH)
    public LogbookOperationsCommonResponseDto findOperations(
        @RequestHeader(CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestBody final JsonNode select,
        @RequestParam(required = false) final Integer vitamTenantIdentifier
    ) throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeJson(select);
        SanityChecker.sanitizeCriteria(select);
        final VitamContext vitamContext;
        if (Objects.nonNull(vitamTenantIdentifier)) {
            TenantDto requestedTenant = tenantService.findByIdentifier(vitamTenantIdentifier);
            if (Objects.isNull(requestedTenant)) {
                throw new BadRequestException(
                    "Tenant not found with identifier : '%d'".formatted(vitamTenantIdentifier)
                );
            }
            vitamContext = securityService.buildVitamContext(vitamTenantIdentifier);
        } else {
            vitamContext = securityService.buildVitamContext(tenantId);
        }
        return VitamRestUtils.responseMapping(
            logbookService.selectOperations(select, vitamContext).toJsonNode(),
            LogbookOperationsCommonResponseDto.class
        );
    }

    @Operation(operationId = "logbooks_findOperationByUnitId", summary = "Get operation by id")
    @GetMapping(CommonConstants.LOGBOOK_OPERATION_BY_ID_PATH)
    @Secured({ ServicesData.ROLE_LOGBOOKS })
    @ResponseStatus(HttpStatus.OK)
    public LogbookOperationsCommonResponseDto findOperationByUnitId(
        @RequestHeader(CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        @PathVariable final String id
    ) throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id, accessContractId);
        LOGGER.debug("Get log book operation by id : {} ", id);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        return VitamRestUtils.responseMapping(
            logbookService.selectOperationbyId(id, vitamContext).toJsonNode(),
            LogbookOperationsCommonResponseDto.class
        );
    }

    @Operation(operationId = "logbooks_findUnitLifeCyclesByUnitId", summary = "Get unit lifecycle by id")
    @Secured(ServicesData.ROLE_LOGBOOKS)
    @GetMapping(value = CommonConstants.LOGBOOK_UNIT_LYFECYCLES_PATH)
    public LogbookLifeCycleResponseDto findUnitLifeCyclesByUnitId(
        @RequestHeader(CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        @PathVariable final String id
    ) throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id, accessContractId);
        LOGGER.debug("Get unit lifecycle by id : {} ", id);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        return VitamRestUtils.responseMapping(
            logbookService.findUnitLifeCyclesByUnitId(id, vitamContext).toJsonNode(),
            LogbookLifeCycleResponseDto.class
        );
    }

    @Operation(operationId = "logbooks_findObjectGroupLifeCyclesByUnitId", summary = "Get object lifecycle by id")
    @Secured(ServicesData.ROLE_LOGBOOKS)
    @GetMapping(value = CommonConstants.LOGBOOK_OBJECT_LYFECYCLES_PATH)
    public LogbookLifeCycleResponseDto findObjectGroupLifeCyclesByUnitId(
        @RequestHeader(required = true, value = CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(
            required = true,
            value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER
        ) final String accessContractId,
        @PathVariable final String id
    ) throws VitamClientException, InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id, accessContractId);
        LOGGER.debug("Get object lifecycle by id : {} ", id);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        return VitamRestUtils.responseMapping(
            logbookService.findObjectGroupLifeCyclesByUnitId(id, vitamContext).toJsonNode(),
            LogbookLifeCycleResponseDto.class
        );
    }

    @Operation(operationId = "logbooks_downloadManifest", summary = "Download the manifest for a given operation")
    @Secured(ServicesData.ROLE_LOGBOOKS)
    @GetMapping(value = CommonConstants.LOGBOOK_DOWNLOAD_MANIFEST_PATH)
    @ResponseStatus(HttpStatus.OK)
    public void downloadManifest(
        @RequestHeader(CommonConstants.X_TENANT_ID_HEADER) final Integer tenantIdentifier,
        @PathVariable final String id,
        final HttpServletResponse response
    ) throws IOException, InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Download the manifest for the following Vitam operation : {}", id);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantIdentifier);
        try (final Response vitamResponse = logbookService.downloadManifest(id, vitamContext)) {
            VitamRestUtils.writeFileResponse(vitamResponse, response);
        }
    }

    @Operation(operationId = "logbooks_downloadAtr", summary = "Download the ATR file for a given operation")
    @Secured(ServicesData.ROLE_LOGBOOKS)
    @GetMapping(value = CommonConstants.LOGBOOK_DOWNLOAD_ATR_PATH)
    @ResponseStatus(HttpStatus.OK)
    public void downloadAtr(
        @RequestHeader(CommonConstants.X_TENANT_ID_HEADER) final Integer tenantIdentifier,
        @PathVariable final String id,
        final HttpServletResponse response
    ) throws IOException, InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Download the ATR file for the following Vitam operation : {}", id);
        final VitamContext vitamContext = securityService.buildVitamContext(tenantIdentifier);
        try (final Response vitamResponse = logbookService.downloadAtr(id, vitamContext)) {
            VitamRestUtils.writeFileResponse(vitamResponse, response);
        }
    }

    @Operation(operationId = "logbooks_downloadReport", summary = "Download the report file for a given operation")
    @GetMapping(
        value = CommonConstants.LOGBOOK_DOWNLOAD_REPORT_PATH,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @Secured(ServicesData.ROLE_LOGBOOKS)
    @ResponseStatus(HttpStatus.OK)
    public void downloadReport(
        @RequestHeader(CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        @PathVariable final String id,
        @PathVariable final String downloadType,
        final HttpServletResponse response
    ) throws VitamClientException, IOException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id, downloadType, accessContractId);
        LOGGER.debug("Download the report file for the Vitam operation : {} with download type : {}", id, downloadType);
        LOGGER.debug("Access Contract {} ", accessContractId);
        ParameterChecker.checkParameter(
            "The identifier, the accessContract, and Id are mandatory parameters: ",
            id,
            accessContractId
        );
        final VitamContext vitamContext = securityService.buildVitamContext(tenantId, accessContractId);
        try (Response vitamResponse = logbookService.downloadReport(id, downloadType, vitamContext)) {
            String fileName = getDownloadReportFileName(id, downloadType);
            response.setHeader(RestUtils.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");
            VitamRestUtils.writeFileResponse(vitamResponse, response);
        }
    }

    private static String getDownloadReportFileName(String id, String downloadType) {
        String fileName = id;
        switch (downloadType) {
            case DOWNLOAD_TYPE_OBJECT:
                fileName += ".xml";
                break;
            case DOWNLOAD_TYPE_BATCH_REPORT:
                fileName += ".jsonl";
                break;
            case DOWNLOAD_TYPE_DIP:
            case DOWNLOAD_TYPE_TRANSFER_SIP:
                fileName += ".zip";
                break;
            default:
                fileName += ".json";
                break;
        }
        return fileName;
    }
}
