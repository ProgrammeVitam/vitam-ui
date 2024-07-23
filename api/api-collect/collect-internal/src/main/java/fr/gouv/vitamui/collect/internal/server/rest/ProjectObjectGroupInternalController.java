/*
 *
 *  * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *  *
 *  * contact.vitam@culture.gouv.fr
 *  *
 *  * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 *  * high volumetry securely and efficiently.
 *  *
 *  * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 *  * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 *  * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *  *
 *  * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 *  * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 *  * successive licensors have only limited liability.
 *  *
 *  * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 *  * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 *  * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 *  * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 *  * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 *  * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *  *
 *  * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 *  * accept its terms.
 *
 */

package fr.gouv.vitamui.collect.internal.server.rest;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.collect.common.rest.RestApi;
import fr.gouv.vitamui.collect.internal.server.service.ExternalParametersService;
import fr.gouv.vitamui.collect.internal.server.service.ProjectObjectGroupInternalService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.ws.rs.core.Response;
import java.io.InputStream;

import static fr.gouv.vitamui.archives.search.common.rest.RestApi.DOWNLOAD_ARCHIVE_UNIT;

@RestController
@RequestMapping(RestApi.COLLECT_PROJECT_OBJECT_GROUPS_PATH)
@Api(tags = "collect", value = "Groupe d'object d'un projet")
public class ProjectObjectGroupInternalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectObjectGroupInternalController.class);
    private final ProjectObjectGroupInternalService projectObjectGroupInternalService;
    private final ExternalParametersService externalParametersService;

    private static final String IDENTIFIER_MANDATORY = "The identifier is mandatory parameter: ";

    public ProjectObjectGroupInternalController(
        ProjectObjectGroupInternalService projectObjectGroupInternalService,
        final ExternalParametersService externalParametersService
    ) {
        this.projectObjectGroupInternalService = projectObjectGroupInternalService;
        this.externalParametersService = externalParametersService;
    }

    @GetMapping(
        value = DOWNLOAD_ARCHIVE_UNIT + CommonConstants.PATH_ID,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public Mono<ResponseEntity<Resource>> downloadObjectFromUnit(
        final @PathVariable("id") String id,
        final @RequestParam("usage") String usage,
        final @RequestParam("version") Integer version
    ) throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY, id);
        SanityChecker.checkSecureParameter(id, usage);
        LOGGER.debug("Download Archive Unit Object with id {}", id);

        VitamContext vitamContext = externalParametersService.buildVitamContextFromExternalParam();

        return Mono.<Resource>fromCallable(() -> {
            Response response = projectObjectGroupInternalService.downloadObjectFromUnit(
                id,
                usage,
                version,
                vitamContext
            );
            return new InputStreamResource((InputStream) response.getEntity());
        })
            .subscribeOn(Schedulers.boundedElastic())
            .flatMap(resource -> Mono.just(ResponseEntity.ok().cacheControl(CacheControl.noCache()).body(resource)));
    }

    @GetMapping(CommonConstants.PATH_ID)
    public ResultsDto findObjectById(final @PathVariable("id") String id)
        throws InvalidParseOperationException, VitamClientException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Get ObjectGroup By id : {}", id);
        return projectObjectGroupInternalService.findObjectById(
            id,
            externalParametersService.buildVitamContextFromExternalParam()
        );
    }
}
