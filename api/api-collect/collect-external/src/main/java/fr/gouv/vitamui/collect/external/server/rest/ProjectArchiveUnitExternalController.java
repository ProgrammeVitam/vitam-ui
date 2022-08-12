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
import fr.gouv.vitamui.archives.search.common.dto.ArchiveUnitsDto;
import fr.gouv.vitamui.archives.search.common.dto.SearchCriteriaDto;
import fr.gouv.vitamui.collect.external.server.service.ProjectArchiceUnitExternalService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Consumes;

import static fr.gouv.vitamui.collect.common.rest.RestApi.ARCHIVE_UNITS;
import static fr.gouv.vitamui.collect.common.rest.RestApi.COLLECT_PROJECT_ARCHIVE_UNITS_PATH;

/**
 * Project Archive units External controller
 */
@Api(tags = "Collect")
@RequestMapping(COLLECT_PROJECT_ARCHIVE_UNITS_PATH)
@RestController
@ResponseBody
public class ProjectArchiveUnitExternalController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProjectArchiveUnitExternalController.class);
    private final ProjectArchiceUnitExternalService projectArchiceUnitExternalService;

    @Autowired
    public ProjectArchiveUnitExternalController(
        ProjectArchiceUnitExternalService projectArchiceUnitExternalService) {
        this.projectArchiceUnitExternalService = projectArchiceUnitExternalService;
    }

    @ApiOperation(value = "find archive units by criteria")
    @Secured(ServicesData.ROLE_GET_PROJECTS)
    @PostMapping("/{projectId}" + ARCHIVE_UNITS)
    @Consumes(MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ArchiveUnitsDto searchArchiveUnits(final @PathVariable("projectId") String projectId,
        @RequestBody final SearchCriteriaDto searchQuery)
        throws InvalidParseOperationException, PreconditionFailedException {

        ParameterChecker.checkParameter("The Query and the projectId are mandatories parameters: ", projectId ,searchQuery);
        SanityChecker.sanitizeCriteria(searchQuery);
        SanityChecker.checkSecureParameter(projectId);
        LOGGER.debug("search archives Units by criteria = {}", searchQuery);

        return projectArchiceUnitExternalService.searchCollectProjectArchiveUnits(projectId, searchQuery);
    }

}
