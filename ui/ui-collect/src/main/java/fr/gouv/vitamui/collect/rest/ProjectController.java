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

package fr.gouv.vitamui.collect.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.collect.common.dto.CollectProjectDto;
import fr.gouv.vitamui.collect.service.ProjectService;
import fr.gouv.vitamui.common.security.SafeFileChecker;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.io.InputStream;
import java.util.Optional;

@Api(tags = "Collect")
@RestController
@RequestMapping("${ui-collect.prefix}/project")
@Consumes("application/json")
@Produces("application/json")
public class ProjectController extends AbstractUiRestController {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProjectController.class);

    private final ProjectService projectService;

    @Autowired
    public ProjectController(final ProjectService service) {
        this.projectService = service;
    }

    @ApiOperation(value = "Get projects paginated")
    @GetMapping(params = {"page", "size"})
    @ResponseStatus(HttpStatus.OK)
    public PaginatedValuesDto<CollectProjectDto> getAllProjectsPaginated(@RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam final Optional<String> criteria, @RequestParam final Optional<String> orderBy,
        @RequestParam final Optional<DirectionDto> direction) throws InvalidParseOperationException {
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug("getAllProjectsPaginated page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, criteria,
            orderBy, direction);
        return projectService.getAllProjectsPaginated(buildUiHttpContext(), page, size, criteria, orderBy, direction);
    }

    @ApiOperation(value = "Create new collect project")
    @PostMapping
    public CollectProjectDto createProject(@RequestBody CollectProjectDto collectProjectDto) throws InvalidParseOperationException {
        SanityChecker.sanitizeCriteria(collectProjectDto);
        return projectService.createProject(buildUiHttpContext(), collectProjectDto);
    }

    @ApiOperation(value = "Upload collect zip file", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PostMapping("/upload")
    public ResponseEntity<Void> upload(
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final String tenantId,
        @RequestHeader(value = CommonConstants.X_PROJECT_ID_HEADER) final String projectId,
        @RequestHeader(value = CommonConstants.X_ORIGINAL_FILENAME_HEADER) final String filename,
        final InputStream inputStream) throws InvalidParseOperationException {
        ParameterChecker
            .checkParameter("The tenantId and projectId are mandatory parameters : ",
                tenantId, projectId);
        SanityChecker.checkSecureParameter(tenantId);
        SanityChecker.checkSecureParameter(projectId);
        SafeFileChecker.checkSafeFilePath(filename);
        LOGGER.debug("Start uploading file ...{} ", filename);
        ResponseEntity<Void> response =
            projectService.streamingUpload(buildUiHttpContext(), filename, projectId, inputStream);

        LOGGER.debug("The response in ui Ingest is {} ", response.toString());
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping(CommonConstants.PATH_ID)
    public CollectProjectDto updateProject(final @PathVariable("id") String id, @RequestBody CollectProjectDto collectProjectDto)
        throws InvalidParseOperationException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(collectProjectDto);
        LOGGER.debug("[Internal] Project to update : {}", collectProjectDto);
        return projectService.update(buildUiHttpContext(), collectProjectDto);
    }

    @ApiOperation(value = "Get project Details")
    @GetMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public CollectProjectDto findProjectById(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Find the Project with ID {}", id);
        return projectService.getOne(buildUiHttpContext(), id);
    }

}
