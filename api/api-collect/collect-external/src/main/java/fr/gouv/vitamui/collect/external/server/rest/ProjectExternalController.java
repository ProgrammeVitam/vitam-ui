/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
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
package fr.gouv.vitamui.collect.external.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.collect.common.dto.CollectProjectDto;
import fr.gouv.vitamui.collect.common.rest.RestApi;
import fr.gouv.vitamui.collect.external.server.service.CollectExternalService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.Optional;

/**
 * Project External controller
 */
@Api(tags = "Collect")
@RequestMapping(RestApi.COLLECT_PROJECT_PATH)
@RestController
@ResponseBody
public class ProjectExternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProjectExternalController.class);

    private final CollectExternalService collectExternalService;

    @Autowired
    public ProjectExternalController(CollectExternalService collectExternalService) {
        this.collectExternalService = collectExternalService;
    }

    @Secured(ServicesData.ROLE_GET_PROJECTS)
    @GetMapping(params = {"page", "size"})
    public PaginatedValuesDto<CollectProjectDto> getAllPaginated(@RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction) throws InvalidParseOperationException,
        PreconditionFailedException{
        direction.ifPresent(directionDto ->
            {
                try {
                    SanityChecker.sanitizeCriteria(directionDto);
                } catch (InvalidParseOperationException exception) {
                    LOGGER.error("Exception error : {}", exception.getMessage());
                    throw new PreconditionFailedException("Exception error",exception);
                }
            }
        );
        if(orderBy.isPresent()) {
            SanityChecker.checkSecureParameter(orderBy.get());
        }
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, orderBy,
            direction);
        return collectExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Secured(ServicesData.ROLE_CREATE_PROJECTS)
    @PostMapping()
    public CollectProjectDto createProject(@RequestBody CollectProjectDto collectProjectDto) throws InvalidParseOperationException,
        PreconditionFailedException {
        SanityChecker.sanitizeCriteria(collectProjectDto);
        LOGGER.debug("Project to create : {}", collectProjectDto);
        return collectExternalService.createProject(collectProjectDto);
    }

    @Secured(ServicesData.ROLE_CREATE_PROJECTS)
    @ApiOperation(value = "Upload and stream collect zip file", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PostMapping(value = "/upload", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Void> streamingUpload(InputStream inputStream,
        @RequestHeader(value = CommonConstants.X_PROJECT_ID_HEADER) final String projectId,
        @RequestHeader(value = CommonConstants.X_ORIGINAL_FILENAME_HEADER) final String originalFileName
    ) throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The project ID is a mandatory parameter: ", projectId);
        SanityChecker.checkSecureParameter(projectId);
        SanityChecker.isValidFileName(originalFileName);
        LOGGER.debug("[External] upload collect zip file : {}", originalFileName);
        return collectExternalService.streamingUpload(inputStream, projectId, originalFileName);
    }

    @Secured(ServicesData.ROLE_UPDATE_PROJECTS)
    @PutMapping(CommonConstants.PATH_ID)
    public CollectProjectDto updateProject(final @PathVariable("id") String id, @RequestBody CollectProjectDto collectProjectDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(collectProjectDto);
        LOGGER.debug("[External] Project to update : {}", collectProjectDto);
        return collectExternalService.updateProject(collectProjectDto);
    }
}
