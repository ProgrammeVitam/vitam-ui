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

package fr.gouv.vitamui.collect.internal.server.rest;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.collect.common.dto.CollectProjectDto;
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.collect.common.rest.RestApi;
import fr.gouv.vitamui.collect.internal.server.service.ProjectInternalService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.Optional;

import static fr.gouv.vitamui.collect.common.rest.RestApi.TRANSACTIONS;
import static fr.gouv.vitamui.commons.api.CommonConstants.IDENTIFIER_MANDATORY_PARAMETER;
import static fr.gouv.vitamui.commons.api.CommonConstants.LAST_TRANSACTION_PATH;
import static fr.gouv.vitamui.commons.api.CommonConstants.PATH_ID;

@RestController
@RequestMapping(RestApi.COLLECT_PROJECT_PATH)
@Api(tags = "collect", value = "Préparation de versements")
public class ProjectInternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProjectInternalController.class);
    private final InternalSecurityService securityService;
    private final ProjectInternalService projectInternalService;

    @Autowired
    public ProjectInternalController(final ProjectInternalService projectInternalService,
        final InternalSecurityService securityService) {
        this.securityService = securityService;
        this.projectInternalService = projectInternalService;
    }

    @GetMapping(params = {"page", "size"})
    public PaginatedValuesDto<CollectProjectDto> getAllProjectsPaginated(@RequestParam final Integer page,
        @RequestParam final Integer size, @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction) {
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size,
            criteria, orderBy, direction);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return projectInternalService.getAllProjectsPaginated(vitamContext, page, size, orderBy, direction, criteria);
    }

    @PostMapping()
    public CollectProjectDto createProject(@RequestBody CollectProjectDto collectProjectDto)
        throws InvalidParseOperationException {
        SanityChecker.sanitizeCriteria(collectProjectDto);
        LOGGER.debug("Project to create {}", collectProjectDto);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return projectInternalService.createProject(vitamContext, collectProjectDto);
    }

    @PostMapping(value = CommonConstants.PATH_ID+ "/transactions")
    public CollectTransactionDto createTransactionForProject(final @PathVariable("id") String id, @RequestBody CollectTransactionDto collectTransactionDto)
        throws InvalidParseOperationException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        SanityChecker.sanitizeCriteria(collectTransactionDto);
        LOGGER.debug("Transaction to create {}", collectTransactionDto);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return projectInternalService.createTransactionForProject(vitamContext, collectTransactionDto, id);
    }

    @ApiOperation(value = "Upload and stream collect zip file", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PostMapping(value = "/upload", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void streamingUpload(
        InputStream inputStream,
        @RequestHeader(value = CommonConstants.X_TRANSACTION_ID_HEADER) final String transactionId,
        @RequestHeader(value = CommonConstants.X_ORIGINAL_FILENAME_HEADER) final String originalFileName
    ) throws InvalidParseOperationException {
        ParameterChecker.checkParameter("The transaction ID is a mandatory parameter: ", transactionId);
        SanityChecker.isValidFileName(originalFileName);
        SanityChecker.checkSecureParameter(transactionId);
        LOGGER.debug("[Internal] upload collect zip file : {}", originalFileName);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        projectInternalService.streamingUpload(vitamContext, inputStream, transactionId, originalFileName);
    }

    @PutMapping(PATH_ID)
    public CollectProjectDto updateProject(final @PathVariable("id") String id,
        @RequestBody CollectProjectDto collectProjectDto) throws InvalidParseOperationException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_PARAMETER, id);
        SanityChecker.sanitizeCriteria(collectProjectDto);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("[Internal] Project to update : {}", collectProjectDto);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return projectInternalService.update(vitamContext, id, collectProjectDto);
    }

    @GetMapping(PATH_ID)
    public CollectProjectDto findProjectById(final @PathVariable("id") String id) throws VitamClientException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_PARAMETER, id);
        LOGGER.debug("Project to get  {}", id);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return projectInternalService.getProjectById(id, vitamContext);
    }

    @DeleteMapping(PATH_ID)
    public void deleteProjectById(final @PathVariable("id") String id)
        throws VitamClientException, InvalidParseOperationException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_PARAMETER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Project to delete  {}", id);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        projectInternalService.deleteProjectById(id, vitamContext);
    }

    @GetMapping(PATH_ID  + LAST_TRANSACTION_PATH)
    public CollectTransactionDto findLastTransactionByProjectId(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Find the transaction by project with ID {}", id);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return projectInternalService.getLastTransactionForProjectId(id, vitamContext);
    }

    @ApiOperation(value = "Get transactions by project paginated")
    @GetMapping(params = {"page", "size"}, value = PATH_ID + TRANSACTIONS)
    public PaginatedValuesDto<CollectTransactionDto> getTransactionsByProjectPaginated(@RequestParam final Integer page,
        @RequestParam final Integer size, @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction, @PathVariable("id") String projectId)
        throws VitamClientException, InvalidParseOperationException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_PARAMETER, projectId);
        SanityChecker.checkSecureParameter(projectId);
        LOGGER.debug("getPaginateEntities page={}, size={}, orderBy={}, ascendant={}", page, size, orderBy, direction);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return projectInternalService.getTransactionsByProjectPaginated(projectId, page, size, orderBy, direction,
            vitamContext);
    }



}
