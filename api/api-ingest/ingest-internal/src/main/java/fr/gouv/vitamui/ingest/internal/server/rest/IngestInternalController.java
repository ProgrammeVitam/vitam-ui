/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2020)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
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
package fr.gouv.vitamui.ingest.internal.server.rest;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.ingest.external.api.exception.IngestExternalException;
import fr.gouv.vitamui.common.security.SafeFileChecker;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.IngestFileGenerationException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationDto;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.ingest.common.rest.RestApi;
import fr.gouv.vitamui.ingest.internal.server.service.IngestInternalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.V1_INGEST)
@Getter
@Setter
@Api(tags = "ingest", value = "Ingest an SIP", description = "Ingest an SIP")
public class IngestInternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IngestInternalController.class);

    private IngestInternalService ingestInternalService;

    private InternalSecurityService securityService;

    @Autowired
    public IngestInternalController(final IngestInternalService ingestInternalService,
        final InternalSecurityService securityService) {
        this.ingestInternalService = ingestInternalService;
        this.securityService = securityService;
    }

    @GetMapping(params = {"page", "size"})
    public PaginatedValuesDto<LogbookOperationDto> getAllPaginated(@RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction)
        throws PreconditionFailedException, InvalidParseOperationException, IOException {

        if(orderBy.isPresent()) {
            SanityChecker.checkSecureParameter(orderBy.get());
        }
        if(direction.isPresent()) {
            SanityChecker.sanitizeCriteria(direction.get());
        }
        SanityChecker.sanitizeCriteria(criteria);
        if(criteria.isPresent()) {
            SanityChecker.sanitizeCriteria(VitamUIUtils
                .convertObjectFromJson(criteria.get(), Object.class));
        }
        LOGGER
            .debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, criteria,
                orderBy, direction);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return ingestInternalService.getAllPaginated(page, size, orderBy, direction, vitamContext, criteria);
    }

    @GetMapping(CommonConstants.PATH_ID)
    public LogbookOperationDto getOne(@PathVariable("id") String id) throws PreconditionFailedException , InvalidParseOperationException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get Ingest Entities for id={} ", id);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return ingestInternalService.getOne(vitamContext, id);
    }

    @GetMapping(RestApi.INGEST_REPORT_ODT + CommonConstants.PATH_ID)
    public ResponseEntity<byte[]> generateODTReport(final @PathVariable("id") String id)
        throws IngestFileGenerationException, PreconditionFailedException {
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        try {
            ParameterChecker.checkParameter("Identifier is mandatory : ", id);
            SanityChecker.checkSecureParameter(id);
            LOGGER.debug("export ODT report for operation with id :{}", id);
            byte[] response = this.ingestInternalService.generateODTReport(vitamContext, id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException | URISyntaxException | IngestFileGenerationException e) {
            LOGGER.error("Error with generating Report : {} ", e.getMessage());
            throw new IngestFileGenerationException("Unable to generate the ingest report " + e);
        } catch (PreconditionFailedException exception ) {
            LOGGER.error("The id parameter is not valid" , exception.getMessage());
            throw new PreconditionFailedException("The id parameter is not valid" ,exception.getMessage());
        }
    }

    @ApiOperation(value = "Upload an SIP", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PostMapping(value = CommonConstants.INGEST_UPLOAD_V2, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Void> streamingUpload(
        InputStream inputStream,
        @RequestHeader(value = CommonConstants.X_ACTION) final String action,
        @RequestHeader(value = CommonConstants.X_CONTEXT_ID) final String contextId,
        @RequestHeader(value = CommonConstants.X_ORIGINAL_FILENAME_HEADER) final String originalFileName
    )
        throws IngestExternalException, PreconditionFailedException, InvalidParseOperationException {
        ParameterChecker.checkParameter("The action and the context ID are mandatory parameters: ", action, contextId,
            originalFileName);
        SanityChecker.isValidFileName(originalFileName);
        SafeFileChecker.checkSafeFilePath(originalFileName);
        SanityChecker.checkSecureParameter(action, contextId, originalFileName);
        LOGGER.debug("[Internal] upload file v2: {}", originalFileName);
        final String operationId = ingestInternalService.streamingUpload(inputStream, contextId, action);
        if (operationId != null) {
            return ResponseEntity.ok().header(CommonConstants.X_OPERATION_ID_HEADER, operationId).build();
        } else {
            LOGGER.error("Cannot retrieve operation id");
            throw new IngestExternalException("Cannot retrieve operation id");
        }
    }
}
