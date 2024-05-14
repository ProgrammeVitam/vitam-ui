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
import fr.gouv.vitamui.archives.search.common.dto.ObjectData;
import fr.gouv.vitamui.collect.service.ProjectObjectGroupService;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static fr.gouv.vitamui.archives.search.common.rest.RestApi.DOWNLOAD_ARCHIVE_UNIT;
import static fr.gouv.vitamui.commons.api.CommonConstants.PATH_ID;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import static org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE;

@Api(tags = "Collect")
@RequestMapping("${ui-collect.prefix}/projects/object-groups")
@RestController
public class ProjectObjectGroupController extends AbstractUiRestController {

    static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(ProjectObjectGroupController.class);
    public static final String CONTENT_DISPOSITION = "Content-Disposition";

    private final ProjectObjectGroupService projectObjectGroupService;

    @Autowired
    public ProjectObjectGroupController(final ProjectObjectGroupService service) {
        this.projectObjectGroupService = service;
    }

    @ApiOperation(value = "Download Archive Unit Object")
    @GetMapping(
        value = DOWNLOAD_ARCHIVE_UNIT + PATH_ID,
        produces = APPLICATION_OCTET_STREAM_VALUE,
        params = { "objectId", "tenantId" }
    )
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Resource> downloadObjectFromUnit(
        final @PathVariable("id") String unitId,
        @RequestParam(name = "objectId") String objectId,
        @RequestParam(name = "tenantId") Integer tenantId,
        @RequestParam(name = "qualifier", required = false) String qualifier,
        @RequestParam(name = "version", required = false) Integer version
    ) throws PreconditionFailedException, InvalidParseOperationException {
        ParameterChecker.checkParameter(
            "The Identifier, and The tenantId are mandatory parameters: ",
            unitId,
            objectId,
            String.valueOf(tenantId)
        );
        SanityChecker.checkSecureParameter(unitId, objectId);
        LOGGER.debug("Download the Archive Unit Object with Unit ID {}", unitId);
        final ObjectData objectData = new ObjectData();
        ResponseEntity<Resource> responseResource = projectObjectGroupService
            .downloadObjectFromUnit(unitId, objectId, qualifier, version, objectData, buildUiHttpContext(tenantId))
            .block();
        List<String> headersValuesContentDispo = responseResource.getHeaders().get(CONTENT_DISPOSITION);
        LOGGER.info("Content-Disposition value is {} ", headersValuesContentDispo);
        String fileNameHeader = isNotEmpty(objectData.getFilename())
            ? "attachment;filename=" + objectData.getFilename()
            : "attachment";
        MediaType contentType = isNotEmpty(objectData.getMimeType())
            ? new MediaType(MimeType.valueOf(objectData.getMimeType()))
            : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
            .contentType(contentType)
            .header(CONTENT_DISPOSITION, fileNameHeader)
            .body(responseResource.getBody());
    }
}
