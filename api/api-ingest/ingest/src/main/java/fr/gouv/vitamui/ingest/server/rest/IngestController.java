/*
 *
 *  Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2022)
 *  and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 *  contact@programmevitam.fr
 *
 *  This software is a computer program whose purpose is to implement
 *  implement a digital archiving front-office system for the secure and
 *  efficient high volumetry VITAM solution.
 *
 *  This software is governed by the CeCILL-C license under French law and
 *  abiding by the rules of distribution of free software.  You can  use,
 *  modify and/ or redistribute the software under the terms of the CeCILL-C
 *  license as circulated by CEA, CNRS and INRIA at the following URL
 *  "http://www.cecill.info".
 *
 *  As a counterpart to the access to the source code and  rights to copy,
 *  modify and redistribute granted by the license, users are provided only
 *  with a limited warranty  and the software's author,  the holder of the
 *  economic rights,  and the successive licensors  have only  limited
 *  liability.
 *
 *  In this respect, the user's attention is drawn to the risks associated
 *  with loading,  using,  modifying and/or developing or reproducing the
 *  software by the user in light of its specific status of free software,
 *  that may mean  that it is complicated to manipulate,  and  that  also
 *  therefore means  that it is reserved for developers  and  experienced
 *  professionals having in-depth computer knowledge. Users are therefore
 *  encouraged to load and test the software's suitability as regards their
 *  requirements in conditions enabling the security of their systems and/or
 *  data to be ensured and,  more generally, to use and operate it in the
 *  same conditions as regards security.
 *
 *  The fact that you are presently reading this means that you have had
 *  knowledge of the CeCILL-C license and that you accept its terms.
 *
 */
package fr.gouv.vitamui.ingest.server.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.ingest.external.api.exception.IngestExternalException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.download.DownloadClaims;
import fr.gouv.vitamui.commons.api.download.SignedDownloadTokenService;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.utils.VitamUIUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.LogbookOperationDto;
import fr.gouv.vitamui.iam.common.dto.CustomerDto;
import fr.gouv.vitamui.ingest.common.rest.RestApi;
import fr.gouv.vitamui.ingest.server.service.IngestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * UI Ingest External controller
 */
@RequestMapping(RestApi.V1_INGEST)
@RestController
@Tag(name = "ingest")
@ResponseBody
public class IngestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestController.class);
    private static final String INGEST_ODT_REPORT_RESOURCE = "ingest-odt-report";
    private static final String SIGNED_DOWNLOAD_ODT_REPORT_ENDPOINT = "/signed-download/odtreport";
    private static final String SIGNED_DOWNLOAD_INGEST_ODT_REPORT_PATH =
        "/ingest" + SIGNED_DOWNLOAD_ODT_REPORT_ENDPOINT;
    private static final String ID_PARAMETER = "id";
    private static final String CUSTOMER_PARAMETER = "customer";

    private final IngestService ingestService;
    private final ObjectMapper objectMapper;
    private final SignedDownloadTokenService signedDownloadTokenService;

    public IngestController(
        IngestService ingestService,
        ObjectMapper objectMapper,
        SignedDownloadTokenService signedDownloadTokenService
    ) {
        this.ingestService = ingestService;
        this.objectMapper = objectMapper;
        this.signedDownloadTokenService = signedDownloadTokenService;
    }

    @Secured(ServicesData.ROLE_GET_ALL_INGEST)
    @GetMapping(value = "/paginated", params = { "page", "size" })
    public PaginatedValuesDto<LogbookOperationDto> getAllPaginated(
        @RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction
    ) throws PreconditionFailedException, IOException {
        direction.ifPresent(SanityChecker::sanitizeCriteria);
        orderBy.ifPresent(SanityChecker::checkSecureParameter);
        SanityChecker.sanitizeCriteria(criteria);
        if (criteria.isPresent()) {
            SanityChecker.sanitizeCriteria(VitamUIUtils.convertObjectFromJson(criteria.get(), Object.class));
        }
        LOGGER.debug(
            "getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}",
            page,
            size,
            criteria,
            orderBy,
            direction
        );
        return ingestService.getAllPaginated(page, size, orderBy, direction, criteria);
    }

    @Secured(ServicesData.ROLE_GET_INGEST)
    @GetMapping(CommonConstants.PATH_ID)
    public LogbookOperationDto getOne(@PathVariable final String id)
        throws PreconditionFailedException, InvalidParseOperationException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get One Ingest id={}", id);
        return ingestService.getOne(id);
    }

    @Secured(ServicesData.ROLE_LOGBOOKS)
    @GetMapping(RestApi.INGEST_REPORT_ODT + CommonConstants.PATH_ID)
    public byte[] generateODTReport(final @PathVariable("id") String id)
        throws PreconditionFailedException, InvalidParseOperationException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter :", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("export ODT report for ingest with id :{}", id);
        return ingestService.generateODTReport(id);
    }

    @Secured(ServicesData.ROLE_LOGBOOKS)
    @PostMapping(RestApi.INGEST_REPORT_ODT + CommonConstants.PATH_ID + "/signed-url")
    public String prepareSignedGenerateODTReport(final @PathVariable("id") String id)
        throws PreconditionFailedException {
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter :", id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Prepare signed ODT report for ingest with id :{}", id);

        DownloadClaims claims = new DownloadClaims();
        claims.setResource(INGEST_ODT_REPORT_RESOURCE);
        claims.setParameters(
            Map.of(ID_PARAMETER, id, CUSTOMER_PARAMETER, serializeCustomer(ingestService.getMyCustomer()))
        );

        return signedDownloadTokenService.generateSignedUrl(claims, SIGNED_DOWNLOAD_INGEST_ODT_REPORT_PATH);
    }

    @GetMapping(value = SIGNED_DOWNLOAD_ODT_REPORT_ENDPOINT, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void signedGenerateODTReport(@RequestParam final String token, final HttpServletResponse response)
        throws IOException, PreconditionFailedException {
        ParameterChecker.checkParameter("The token is a mandatory parameter: ", token);
        DownloadClaims claims = signedDownloadTokenService.validate(token, INGEST_ODT_REPORT_RESOURCE);
        String id = claims.getParameters().get(ID_PARAMETER);
        String serializedCustomer = claims.getParameters().get(CUSTOMER_PARAMETER);
        if (Objects.isNull(id) || Objects.isNull(serializedCustomer)) {
            throw new BadRequestException("Invalid signed download URL");
        }

        SanityChecker.checkSecureParameter(id);
        VitamContext vitamContext = new VitamContext(claims.getTenantId())
            .setAccessContract(claims.getAccessContractId())
            .setApplicationSessionId(claims.getApplicationSessionId());
        byte[] odtReport = ingestService.generateODTReport(vitamContext, id, deserializeCustomer(serializedCustomer));
        response.setHeader(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename("Bordereau-" + id + ".odt", StandardCharsets.UTF_8)
                .build()
                .toString()
        );
        response.setHeader(RestUtils.REFERRER_POLICY, "no-referrer");
        response.getOutputStream().write(odtReport);
    }

    @Secured(ServicesData.ROLE_CREATE_INGEST)
    @Operation(summary = "Upload an streaming SIP")
    @PostMapping(value = CommonConstants.INGEST_UPLOAD, consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Void> streamingUpload(
        InputStream inputStream,
        @RequestHeader(value = CommonConstants.X_ACTION) final String action,
        @RequestHeader(value = CommonConstants.X_CONTEXT_ID) final String contextId
    ) throws PreconditionFailedException, IngestExternalException {
        ParameterChecker.checkParameter("The action and the context ID are mandatory parameters: ", action, contextId);
        LOGGER.debug("[Ingest] upload file ");
        return ingestService.streamingUpload(inputStream, contextId, action);
    }

    private String serializeCustomer(CustomerDto customer) {
        try {
            return objectMapper.writeValueAsString(customer);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Unable to serialize customer", e);
        }
    }

    private CustomerDto deserializeCustomer(String customer) {
        try {
            return objectMapper.readValue(customer, CustomerDto.class);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Unable to deserialize customer", e);
        }
    }
}
