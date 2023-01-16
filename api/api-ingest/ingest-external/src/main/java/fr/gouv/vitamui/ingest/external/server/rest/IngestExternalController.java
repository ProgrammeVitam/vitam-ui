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
package fr.gouv.vitamui.ingest.external.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.common.security.SafeFileChecker;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationDto;
import fr.gouv.vitamui.ingest.common.rest.RestApi;
import fr.gouv.vitamui.ingest.external.server.service.IngestExternalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.Optional;

/**
 * UI Ingest External controller
 */
@Api(tags = "ingest")
@RequestMapping(RestApi.V1_INGEST)
@RestController
@ResponseBody
public class IngestExternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IngestExternalController.class);

    private final IngestExternalService ingestExternalService;

    @Autowired
    public IngestExternalController(final IngestExternalService ingestExternalService) {
        this.ingestExternalService = ingestExternalService;
    }

    @Secured(ServicesData.ROLE_GET_ALL_INGEST)
    @GetMapping(params = {"page", "size"})
    public PaginatedValuesDto<LogbookOperationDto> getAllPaginated(@RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction) throws PreconditionFailedException,  InvalidParseOperationException {
        if(direction.isPresent()) {
            SanityChecker.sanitizeCriteria(direction.get());
        }
        if(orderBy.isPresent()) {
            SanityChecker.checkSecureParameter(orderBy.get());
        }
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, orderBy,
            direction);
        return ingestExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Secured(ServicesData.ROLE_GET_INGEST)
    @GetMapping(CommonConstants.PATH_ID)
    public LogbookOperationDto getOne(@PathVariable("id") final String id) throws PreconditionFailedException, InvalidParseOperationException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get One Ingest id={}", id);
        return ingestExternalService.getOne(id);
    }

    @Secured(ServicesData.ROLE_LOGBOOKS)
    @GetMapping(RestApi.INGEST_REPORT_ODT + CommonConstants.PATH_ID)
    public ResponseEntity<byte[]> generateODTReport(final @PathVariable("id") String id)
        throws PreconditionFailedException, InvalidParseOperationException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter :", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("export ODT report for ingest with id :{}", id);
        return ingestExternalService.generateODTReport(id);
    }

    @Secured(ServicesData.ROLE_CREATE_INGEST)
    @ApiOperation(value = "Upload an streaming SIP", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PostMapping(value = CommonConstants.INGEST_UPLOAD_V2, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Void> streamingUpload(InputStream inputStream,
        @RequestHeader(value = CommonConstants.X_ACTION) final String action,
        @RequestHeader(value = CommonConstants.X_CONTEXT_ID) final String contextId,
        @RequestHeader(value = CommonConstants.X_ORIGINAL_FILENAME_HEADER) final String originalFileName
    ) throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The action and the context ID are mandatory parameters: ", action, contextId,
            originalFileName);
        SanityChecker.checkSecureParameter(action, contextId, originalFileName);
        SanityChecker.isValidFileName(originalFileName);
        SafeFileChecker.checkSafeFilePath(originalFileName);
        LOGGER.debug("[Internal] upload file v2: {}", originalFileName);
        return ingestExternalService.streamingUpload(inputStream, originalFileName, contextId, action);
    }
}
