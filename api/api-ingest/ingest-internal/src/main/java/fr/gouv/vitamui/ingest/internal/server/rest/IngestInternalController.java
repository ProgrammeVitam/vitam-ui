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

import java.io.InputStream;
import java.util.Optional;

import javax.ws.rs.core.Response;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.ingest.external.api.exception.IngestExternalException;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.ingest.common.dto.LogbookOperationDto;
import fr.gouv.vitamui.ingest.common.rest.RestApi;
import fr.gouv.vitamui.ingest.internal.server.service.IngestInternalService;
import io.swagger.annotations.Api;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
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
import org.springframework.web.multipart.MultipartFile;

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
    public IngestInternalController(final IngestInternalService ingestInternalService, final InternalSecurityService securityService) {
        this.ingestInternalService = ingestInternalService;
        this.securityService = securityService;
    }

    @GetMapping(params = {"page", "size"})
    public PaginatedValuesDto<LogbookOperationDto> getAllPaginated(@RequestParam final Integer page, @RequestParam final Integer size,
            @RequestParam(required = false) final Optional<String> criteria, @RequestParam(required = false) final Optional<String> orderBy,
            @RequestParam(required = false) final Optional<DirectionDto> direction) {
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, criteria, orderBy, direction);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return ingestInternalService.getAllPaginated(page, size, orderBy, direction, vitamContext, criteria);
    }

    @GetMapping
    public String ingest() {
        return ingestInternalService.ingest();
    }

    @PostMapping(value = CommonConstants.INGEST_UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public RequestResponseOK upload(
        @RequestHeader(value = CommonConstants.X_ACTION) final String action,
        @RequestHeader(value = CommonConstants.X_CONTEXT_ID) final String contextId,
        @RequestParam(CommonConstants.MULTIPART_FILE_PARAM_NAME) final MultipartFile path)
        throws IngestExternalException {
        LOGGER.debug("[Internal] upload file : {}", path.getOriginalFilename());
        return ingestInternalService.upload(path, contextId, action);
    }

    @GetMapping("/manifest" + CommonConstants.PATH_ID)
    public ResponseEntity<Resource> exportManifest(
            final @PathVariable("id") String id /*,
            @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) String accessContractId */) {
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier()/*, accessContractId*/);
        LOGGER.debug("export manifest for operation with id :{}", id);
        Response response = ingestInternalService.exportManifest(vitamContext, id);
        Object entity = response.getEntity();
        if (entity instanceof InputStream) {
            Resource resource = new InputStreamResource((InputStream) entity);
            return new ResponseEntity<>(resource, HttpStatus.OK);
        }
        return null;
    }

    @GetMapping("/atr" + CommonConstants.PATH_ID)
    public ResponseEntity<Resource> exportATR(
            final @PathVariable("id") String id /*,
            @RequestHeader(value = CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) String accessContractId */) {
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier()/*, accessContractId*/);
        LOGGER.debug("export atr for operation with id :{}", id);
        Response response = ingestInternalService.exportATR(vitamContext, id);
        Object entity = response.getEntity();
        if (entity instanceof InputStream) {
            Resource resource = new InputStreamResource((InputStream) entity);
            return new ResponseEntity<>(resource, HttpStatus.OK);
        }
        return null;
    }

}
