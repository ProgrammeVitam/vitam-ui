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
package fr.gouv.vitamui.ingest.internal.server.rest;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import fr.gouv.vitamui.ingest.common.dto.ProjectDto;
import fr.gouv.vitamui.ingest.common.rest.RestApi;
import fr.gouv.vitamui.ingest.internal.server.service.collect.CollectInternalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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

@RestController
@RequestMapping(RestApi.V1_COLLECT)
@Api(tags = "collect", value = "Préparation de versements")
public class CollectInternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CollectInternalController.class);
    private final InternalSecurityService securityService;
    private final CollectInternalService collectInternalService;

    @Autowired
    public CollectInternalController(final CollectInternalService collectInternalService,
        final InternalSecurityService securityService) {
        this.securityService = securityService;
        this.collectInternalService = collectInternalService;
    }

    @GetMapping(params = {"page", "size"})
    public PaginatedValuesDto<ProjectDto> getAllProjectsPaginated(@RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction) {
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER
            .debug("getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, criteria,
                orderBy, direction);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return collectInternalService.getAllProjectsPaginated(vitamContext, page, size, orderBy, direction, criteria);
    }

    @PostMapping()
    public ProjectDto createProject(@RequestBody ProjectDto projectDto) {
        SanityChecker.sanitizeCriteria(projectDto);
        LOGGER.debug("Project to create {}", projectDto);
        final VitamContext vitamContext = securityService.buildVitamContext(securityService.getTenantIdentifier());
        return collectInternalService.createProject(vitamContext, projectDto);
    }

    @ApiOperation(value = "Upload and stream collect zip file", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PostMapping(value = CommonConstants.UPLOAD, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void streamingUpload(
        InputStream inputStream,
        @RequestHeader(value = CommonConstants.X_CONTEXT_ID) final String contextId,
        @RequestHeader(value = CommonConstants.X_ORIGINAL_FILENAME_HEADER) final String originalFileName
    ) {
        ParameterChecker.checkParameter("The context ID are mandatory parameters: ", contextId);
        SanityChecker.isValidFileName(originalFileName);
        LOGGER.debug("[Internal] upload collect zip file : {}", originalFileName);
        collectInternalService.streamingUpload(inputStream, contextId, originalFileName);
    }

    @PutMapping(CommonConstants.PATH_ID)
    public ProjectDto updateProject(final @PathVariable("id") String id, @RequestBody ProjectDto projectDto) {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.sanitizeCriteria(projectDto);
        LOGGER.debug("[Internal] Project to update : {}", projectDto);
        return collectInternalService.update(id, projectDto);
    }

}
