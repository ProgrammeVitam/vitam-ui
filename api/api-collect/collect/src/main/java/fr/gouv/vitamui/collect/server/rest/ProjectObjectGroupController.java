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
package fr.gouv.vitamui.collect.server.rest;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.collect.server.service.ExternalParametersService;
import fr.gouv.vitamui.collect.server.service.ProjectObjectGroupService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.download.DownloadClaims;
import fr.gouv.vitamui.commons.api.download.SignedDownloadTokenService;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.vitam.api.dto.ResultsDto;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Objects;

import static fr.gouv.vitamui.archives.search.common.rest.RestApi.DOWNLOAD_ARCHIVE_UNIT;
import static fr.gouv.vitamui.collect.common.rest.RestApi.COLLECT_PROJECT_OBJECT_GROUPS_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.OBJECT_GROUPS;
import static fr.gouv.vitamui.collect.common.rest.RestApi.PROJECTS;

/**
 * Collect Archive search External controller
 */
@RequestMapping(COLLECT_PROJECT_OBJECT_GROUPS_PATH)
@RestController
@Tag(name = "Collect")
@ResponseBody
@RequiredArgsConstructor
public class ProjectObjectGroupController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectObjectGroupController.class);

    private static final String MANDATORY_IDENTIFIER = "The Identifier is a mandatory parameter: ";
    private static final String COLLECT_OBJECT_DOWNLOAD_RESOURCE = "collect-object-download";
    private static final String SIGNED_DOWNLOAD_OBJECT_ENDPOINT = "/signed-download/object";
    private static final String SIGNED_DOWNLOAD_COLLECT_OBJECT_PATH =
        PROJECTS + OBJECT_GROUPS + SIGNED_DOWNLOAD_OBJECT_ENDPOINT;
    private static final String ID_PARAMETER = "id";
    private static final String OBJECT_ID_PARAMETER = "objectId";
    private static final String USAGE_PARAMETER = "usage";
    private static final String VERSION_PARAMETER = "version";

    private final ProjectObjectGroupService projectObjectGroupService;
    private final ExternalParametersService externalParametersService;
    private final SignedDownloadTokenService signedDownloadTokenService;

    @GetMapping(
        value = DOWNLOAD_ARCHIVE_UNIT + CommonConstants.PATH_ID,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @Secured(ServicesData.COLLECT_ROLE_GET_ARCHIVE_BINARY)
    public Mono<ResponseEntity<Resource>> downloadObjectFromUnit(
        final @PathVariable("id") String id,
        final @RequestParam("objectId") String objectId,
        final @RequestParam(value = "usage", required = false) String usage,
        final @RequestParam(value = "version", required = false) Integer version
    ) throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, objectId);
        SanityChecker.checkSecureParameter(objectId);
        LOGGER.debug("Download the Archive Unit Object with id {} ", objectId);
        return projectObjectGroupService.downloadObjectFromUnit(
            id,
            objectId,
            usage,
            version,
            externalParametersService.buildVitamContextFromExternalParam()
        );
    }

    @PostMapping(DOWNLOAD_ARCHIVE_UNIT + CommonConstants.PATH_ID + "/signed-url")
    @Secured(ServicesData.COLLECT_ROLE_GET_ARCHIVE_BINARY)
    public String prepareSignedDownloadObjectFromUnit(
        final @PathVariable("id") String id,
        final @RequestParam("objectId") String objectId,
        final @RequestParam(value = "usage", required = false) String usage,
        final @RequestParam(value = "version", required = false) Integer version
    ) throws PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, objectId);
        SanityChecker.checkSecureParameter(objectId);
        LOGGER.debug("Prepare signed download Collect Archive Unit Object with id {} ", objectId);

        DownloadClaims claims = new DownloadClaims();
        claims.setResource(COLLECT_OBJECT_DOWNLOAD_RESOURCE);
        claims.setParameters(
            Map.of(
                ID_PARAMETER,
                id,
                OBJECT_ID_PARAMETER,
                objectId,
                USAGE_PARAMETER,
                Objects.toString(usage, ""),
                VERSION_PARAMETER,
                Objects.toString(version, "")
            )
        );

        return signedDownloadTokenService.generateSignedUrl(claims, SIGNED_DOWNLOAD_COLLECT_OBJECT_PATH);
    }

    @GetMapping(value = SIGNED_DOWNLOAD_OBJECT_ENDPOINT, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<Resource>> signedDownloadObjectFromUnit(@RequestParam final String token)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter("The token is a mandatory parameter: ", token);
        DownloadClaims claims = signedDownloadTokenService.validate(token, COLLECT_OBJECT_DOWNLOAD_RESOURCE);
        String id = claims.getParameters().get(ID_PARAMETER);
        String objectId = claims.getParameters().get(OBJECT_ID_PARAMETER);
        if (Objects.isNull(id) || Objects.isNull(objectId)) {
            throw new BadRequestException("Invalid signed download URL");
        }

        SanityChecker.checkSecureParameter(id);
        SanityChecker.checkSecureParameter(objectId);
        String usage = emptyToNull(claims.getParameters().get(USAGE_PARAMETER));
        Integer version = parseVersion(emptyToNull(claims.getParameters().get(VERSION_PARAMETER)));

        VitamContext vitamContext = new VitamContext(claims.getTenantId())
            .setAccessContract(claims.getAccessContractId())
            .setApplicationSessionId(claims.getApplicationSessionId());
        return projectObjectGroupService.downloadObjectFromUnit(id, objectId, usage, version, vitamContext);
    }

    @GetMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.COLLECT_ROLE_GET_ARCHIVE_BINARY)
    public ResultsDto findObjectById(final @PathVariable("id") String id)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Find an ObjectGroup by id {} ", id);
        return projectObjectGroupService.findObjectById(
            id,
            externalParametersService.buildVitamContextFromExternalParam()
        );
    }

    private static String emptyToNull(String value) {
        return value == null || value.isEmpty() ? null : value;
    }

    private static Integer parseVersion(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new BadRequestException("Invalid signed download URL", e);
        }
    }
}
