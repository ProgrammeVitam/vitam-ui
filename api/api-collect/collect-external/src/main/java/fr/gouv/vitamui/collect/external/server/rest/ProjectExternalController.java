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
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.collect.common.rest.RestApi;
import fr.gouv.vitamui.collect.external.server.service.ProjectExternalService;
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
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import java.util.Map;
import java.util.Optional;

import static fr.gouv.vitamui.collect.common.rest.RestApi.TRANSACTIONS;
import static fr.gouv.vitamui.commons.api.CommonConstants.LAST_TRANSACTION_PATH;
import static fr.gouv.vitamui.commons.api.CommonConstants.PATH_ID;

/**
 * Project External controller
 */
@Api(tags = "Collect")
@RequestMapping(RestApi.COLLECT_PROJECT_PATH)
@RestController
@ResponseBody
public class ProjectExternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProjectExternalController.class);

    private final ProjectExternalService projectExternalService;

    private static final String MANDATORY_IDENTIFIER = "The Identifier is a mandatory parameter: ";

    @Autowired
    public ProjectExternalController(ProjectExternalService projectExternalService) {
        this.projectExternalService = projectExternalService;
    }

    @Secured(ServicesData.ROLE_GET_PROJECTS)
    @GetMapping(params = {"page", "size"})
    public PaginatedValuesDto<CollectProjectDto> getAllPaginated(@RequestParam final Integer page,
        @RequestParam final Integer size, @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction)
        throws PreconditionFailedException {
        direction.ifPresent(directionDto -> {
            SanityChecker.sanitizeCriteria(directionDto);
        });
        if (orderBy.isPresent()) {
            SanityChecker.checkSecureParameter(orderBy.get());
        }
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, orderBy,
            direction);
        return projectExternalService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @ApiOperation(value = "Get transactions by project paginated")
    @GetMapping(params = {"page", "size"}, value = "/{id}" + TRANSACTIONS)
    public PaginatedValuesDto<CollectTransactionDto> getTransactionsByProjectPaginated(@RequestParam final Integer page,
        @RequestParam final Integer size, @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction, @PathVariable("id") String projectId)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, projectId);
        SanityChecker.checkSecureParameter(projectId);
        SanityChecker.sanitizeCriteria(direction);
        SanityChecker.sanitizeCriteria(criteria);
        if (orderBy.isPresent()) {
            SanityChecker.checkSecureParameter(orderBy.get());
        }

        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, orderBy,
            direction);
        return projectExternalService.getTransactionsByProjectPaginated(page, size, criteria, orderBy, direction,
            projectId);
    }

    @Secured(ServicesData.ROLE_CREATE_PROJECTS)
    @PostMapping()
    public CollectProjectDto createProject(@RequestBody CollectProjectDto collectProjectDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        SanityChecker.sanitizeCriteria(collectProjectDto);
        LOGGER.debug("Project to create : {}", collectProjectDto);
        return projectExternalService.createProject(collectProjectDto);
    }

    @Secured(ServicesData.ROLE_CREATE_TRANSACTIONS)
    @PostMapping(value = PATH_ID + "/transactions")
    public CollectTransactionDto createTransactionForProject(final @PathVariable("id") String id, @RequestBody
    CollectTransactionDto collectTransactionDto)
        throws InvalidParseOperationException,
        PreconditionFailedException {
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(collectTransactionDto);
        LOGGER.debug("Transaction to create : {}", collectTransactionDto);
        return projectExternalService.createTransactionForProject(collectTransactionDto, id);
    }

    @Secured(ServicesData.ROLE_CREATE_PROJECTS)
    @ApiOperation(value = "Upload and stream collect zip file", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PostMapping(value = "/upload", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Void> streamingUpload(InputStream inputStream,
        @RequestHeader(value = CommonConstants.X_TRANSACTION_ID_HEADER) final String transactionId,
        @RequestHeader(value = CommonConstants.X_ORIGINAL_FILENAME_HEADER) final String originalFileName,
        @RequestHeader Map<String, String> headers
    ) throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The transaction ID is a mandatory parameter: ", transactionId);
        SanityChecker.checkSecureParameter(transactionId);
        SanityChecker.isValidFileName(originalFileName);
        SafeFileChecker.checkSafeFilePath(originalFileName);
        LOGGER.debug("[External] upload collect zip file : {}", originalFileName);
        return projectExternalService.streamingUpload(inputStream, transactionId, originalFileName);
    }

    @Secured(ServicesData.ROLE_UPDATE_PROJECTS)
    @PutMapping(PATH_ID)
    public CollectProjectDto updateProject(final @PathVariable("id") String id,
        @RequestBody CollectProjectDto collectProjectDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(collectProjectDto);
        LOGGER.debug("[External] Project to update : {}", collectProjectDto);
        return projectExternalService.updateProject(collectProjectDto);
    }

    @Secured(ServicesData.ROLE_GET_PROJECTS)
    @GetMapping(PATH_ID)
    public CollectProjectDto findProjectById(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("The project id {} ", id);
        return projectExternalService.findProjectById(id);
    }

    @Secured(ServicesData.ROLE_DELETE_PROJECTS)
    @DeleteMapping(PATH_ID)
    public void deleteProjectById(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("The project id {} ", id);
        projectExternalService.deleteProjectById(id);
    }

    @Secured(ServicesData.ROLE_GET_TRANSACTIONS)
    @GetMapping(PATH_ID + LAST_TRANSACTION_PATH)
    public CollectTransactionDto findLastTransactionByProjectId(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Find the transaction by project with ID {}", id);
        return projectExternalService.getLastTransactionForProjectId(id);
    }


}
