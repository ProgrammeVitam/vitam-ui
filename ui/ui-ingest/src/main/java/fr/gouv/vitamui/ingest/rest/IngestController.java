/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package fr.gouv.vitamui.ingest.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.common.security.SafeFileChecker;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.rest.AbstractUiRestController;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationDto;
import fr.gouv.vitamui.ingest.common.rest.RestApi;
import fr.gouv.vitamui.ingest.service.IngestService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Api(tags = "ingest")
@RestController
@RequestMapping("${ui-ingest.prefix}/ingest")
@Consumes("application/json")
@Produces("application/json")
public class IngestController extends AbstractUiRestController {

    private final IngestService ingestService;

    private final Map<String, AtomicLong> uploadMap = new ConcurrentHashMap<>();

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(IngestController.class);

    @Autowired
    public IngestController(final IngestService ingestService) {
        this.ingestService = ingestService;
    }

    @ApiOperation(value = "Get entities paginated")
    @GetMapping(params = {"page", "size"})
    @ResponseStatus(HttpStatus.OK)
    public PaginatedValuesDto<LogbookOperationDto> getAllPaginated(@RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam final Optional<String> criteria, @RequestParam final Optional<String> orderBy,
        @RequestParam final Optional<DirectionDto> direction) {
        LOGGER.debug("getAllPaginated page={}, size={}, criteria={}, orderBy={}, ascendant={}", page, size, criteria,
            orderBy, direction);
        return ingestService.getAllPaginated(page, size, criteria, orderBy, direction, buildUiHttpContext());
    }

    @ApiOperation(value = "Get one ingest operation details")
    @GetMapping(CommonConstants.PATH_ID)
    @ResponseStatus(HttpStatus.OK)
    public LogbookOperationDto getOne(final @PathVariable("id") String id) {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        LOGGER.error("Get Ingest={}", id);
        return ingestService.getOne(buildUiHttpContext(), id);
    }

    @ApiOperation(value = "download ODT Report for an ingest operation")
    @GetMapping(RestApi.INGEST_REPORT_ODT + CommonConstants.PATH_ID)
    public ResponseEntity<byte[]> generateODTReport(final @PathVariable("id") String id) {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        LOGGER.debug("download ODT report for the ingest with id :{}", id);
        byte[] bytes = ingestService.generateODTReport(buildUiHttpContext(), id).getBody();
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM).header("Content-Disposition", "attachment")
            .body(bytes);

    }

    @ApiOperation(value = "Upload an SIP", consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @Consumes(MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PostMapping(CommonConstants.INGEST_UPLOAD_V2)
    public ResponseEntity<Void> ingest(
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) final String tenantId,
        @RequestHeader(value = CommonConstants.X_ACTION) final String xAction,
        @RequestHeader(value = CommonConstants.X_CONTEXT_ID) final String contextId,
        @RequestHeader(value = "fileName") final String fileName,
        final InputStream inputStream) {
        ParameterChecker
            .checkParameter("The tenantId, xAction and contextId are mandatory parameters : ",
                tenantId, xAction, contextId);
        SafeFileChecker.checkSafeFilePath(fileName);
        LOGGER.info("Start uploading file ...{} ", fileName);
        ResponseEntity<Void> response =
            ingestService.streamingUpload(buildUiHttpContext(), fileName, inputStream, contextId, xAction);

        LOGGER.info("The response in ui Ingest is {} ", response.toString());
        return new ResponseEntity<>(HttpStatus.OK);
    }

}
