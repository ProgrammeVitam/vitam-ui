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
import fr.gouv.vitamui.collect.external.server.service.ProjectObjectGroupExternalService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import static fr.gouv.vitamui.archives.search.common.rest.RestApi.DOWNLOAD_ARCHIVE_UNIT;
import static fr.gouv.vitamui.collect.common.rest.RestApi.COLLECT_PROJECT_OBJECT_GROUPS_PATH;

/**
 * Collect Archive search External controller
 */
@Api(tags = "Collect")
@RequestMapping(COLLECT_PROJECT_OBJECT_GROUPS_PATH)
@RestController
@ResponseBody
public class ProjectObjectGroupExternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProjectObjectGroupExternalController.class);
    private final ProjectObjectGroupExternalService projectObjectGroupExternalService;
    private static final String MANDATORY_IDENTIFIER = "The Identifier is a mandatory parameter: ";

    @Autowired
    public ProjectObjectGroupExternalController(
        ProjectObjectGroupExternalService projectObjectGroupExternalService) {
        this.projectObjectGroupExternalService = projectObjectGroupExternalService;
    }

    @GetMapping(value = DOWNLOAD_ARCHIVE_UNIT +
        CommonConstants.PATH_ID, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<Resource>> downloadObjectFromUnit(final @PathVariable("id") String id,
        final @RequestParam("objectId") String objectId,
        final @RequestParam(value = "usage", required = false) String usage,
        final @RequestParam(value = "version", required = false) Integer version)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, objectId);
        SanityChecker.checkSecureParameter(objectId);
        LOGGER.debug("Download the Archive Unit Object with id {} ", objectId);
        return projectObjectGroupExternalService.downloadObjectFromUnit(id, objectId, usage, version);
    }

    @GetMapping( CommonConstants.PATH_ID)
    public ResponseEntity<ResultsDto> findObjectById(final @PathVariable("id") String id)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Find an ObjectGroup by id {} ", id);
        return projectObjectGroupExternalService.findObjectById(id);
    }
}
