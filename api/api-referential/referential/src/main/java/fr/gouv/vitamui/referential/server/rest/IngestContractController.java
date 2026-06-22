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
package fr.gouv.vitamui.referential.server.rest;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.download.DownloadClaims;
import fr.gouv.vitamui.commons.api.download.SignedDownloadTokenService;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.commons.vitam.api.dto.HistoryEventDto;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.common.dto.IngestContractDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.server.service.ingestcontract.IngestContractService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.INGEST_CONTRACTS_URL)
@Getter
@Setter
public class IngestContractController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IngestContractController.class);

    private static final String INGEST_CONTRACT_EXPORT_RESOURCE = "ingest-contract-export";
    private static final String SIGNED_DOWNLOAD_EXPORT_CSV_ENDPOINT = "/signed-download/export-csv";
    private static final String SIGNED_DOWNLOAD_INGEST_CONTRACT_EXPORT_PATH =
        "/ingestcontract" + SIGNED_DOWNLOAD_EXPORT_CSV_ENDPOINT;
    private static final String EXPORT_INGEST_CONTRACTS_FILE_NAME = "Exported_ingest_contracts.csv";

    private IngestContractService ingestContractService;
    private SecurityService securityService;
    private SignedDownloadTokenService signedDownloadTokenService;

    @Autowired
    public IngestContractController(
        final IngestContractService ingestContractService,
        final SecurityService securityService,
        final SignedDownloadTokenService signedDownloadTokenService
    ) {
        this.ingestContractService = ingestContractService;
        this.securityService = securityService;
        this.signedDownloadTokenService = signedDownloadTokenService;
    }

    @GetMapping
    @Secured(ServicesData.ROLE_GET_INGEST_CONTRACTS)
    public Collection<IngestContractDto> getAll(final Optional<String> criteria) {
        LOGGER.debug("get all customer criteria={}", criteria);
        SanityChecker.sanitizeCriteria(criteria);
        return ingestContractService.getAll();
    }

    @Secured(ServicesData.ROLE_GET_INGEST_CONTRACTS)
    @GetMapping(value = "/paginated", params = { "page", "size" })
    public PaginatedValuesDto<IngestContractDto> getAllPaginated(
        @RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction
    ) {
        orderBy.ifPresent(SanityChecker::checkSecureParameter);
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug(
            "getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}",
            page,
            size,
            orderBy,
            direction
        );
        return ingestContractService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @Secured(ServicesData.ROLE_GET_INGEST_CONTRACTS)
    @GetMapping(path = RestApi.PATH_REFERENTIAL_ID)
    public IngestContractDto getOne(final @PathVariable("identifier") String identifier)
        throws UnsupportedEncodingException {
        SanityChecker.checkSecureParameter(identifier);
        LOGGER.debug("get ingestcontract  identifier={}", identifier);
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", identifier);
        return ingestContractService.getOne(identifier);
    }

    @Secured({ ServicesData.ROLE_GET_INGEST_CONTRACTS })
    @PostMapping(CommonConstants.PATH_CHECK)
    public ResponseEntity<Void> check(
        @RequestBody IngestContractDto ingestContractDto,
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant
    ) {
        SanityChecker.sanitizeCriteria(ingestContractDto);
        LOGGER.debug("check exist ingestContract={}", ingestContractDto);
        ingestContractDto.setTenant(tenant);
        final boolean exist = ingestContractService.check(ingestContractDto);
        return RestUtils.buildBooleanResponse(exist);
    }

    @Secured(ServicesData.ROLE_CREATE_INGEST_CONTRACTS)
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public IngestContractDto create(
        final @Valid @RequestBody IngestContractDto ingestContractDto,
        @RequestHeader(value = CommonConstants.X_TENANT_ID_HEADER) Integer tenant
    ) {
        SanityChecker.sanitizeCriteria(ingestContractDto);
        LOGGER.debug("Create {}", ingestContractDto);
        ingestContractDto.setTenant(tenant);
        return ingestContractService.create(ingestContractDto);
    }

    @PatchMapping(CommonConstants.PATH_ID)
    @Secured(ServicesData.ROLE_UPDATE_INGEST_CONTRACTS)
    public IngestContractDto patch(
        final @PathVariable("id") String id,
        @RequestBody final Map<String, Object> partialDto
    ) {
        SanityChecker.sanitizeCriteria(partialDto);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Patch {} with {}", id, partialDto);
        ParameterChecker.checkParameter("The Identifier is a mandatory parameter: ", id);
        Assert.isTrue(
            StringUtils.equals(id, (String) partialDto.get("id")),
            "The DTO identifier must match the path identifier for update."
        );
        return ingestContractService.patch(partialDto);
    }

    @Secured(ServicesData.ROLE_GET_INGEST_CONTRACTS)
    @GetMapping(CommonConstants.PATH_LOGBOOK)
    public List<HistoryEventDto> findHistoryById(final @PathVariable("id") String id) throws VitamClientException {
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("get logbook for ingestContract with id :{}", id);
        ParameterChecker.checkParameter("Identifier is mandatory : ", id);
        return ingestContractService.findHistoryById(id);
    }

    /***
     * Import ingest contracts from a csv file
     * @param file the access contracts csv file to import
     * @return the vitam response
     */
    @Secured(ServicesData.ROLE_CREATE_INGEST_CONTRACTS)
    @PostMapping(CommonConstants.PATH_IMPORT)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<Void> importIngestContracts(@RequestParam MultipartFile file) {
        ParameterChecker.checkParameter("The fileName is mandatory parameter : ", file.getOriginalFilename());
        SanityChecker.isValidFileName(file.getOriginalFilename());
        LOGGER.debug("Import ingest contracts file {}", file.getOriginalFilename());

        return ingestContractService.importIngestContracts(file);
    }

    @Operation(summary = "Export ingest contracts to a csv file")
    @GetMapping(path = RestApi.EXPORT_CSV)
    @Secured(ServicesData.ROLE_GET_INGEST_CONTRACTS)
    public ResponseEntity<Resource> exportIngestContracts() {
        LOGGER.debug("export all ingest contracts to csv file");
        return ingestContractService.exportIngestContracts();
    }

    @Operation(summary = "Prepare signed ingest contracts export URL")
    @PostMapping(path = RestApi.EXPORT_CSV + "/signed-url")
    @Secured(ServicesData.ROLE_GET_INGEST_CONTRACTS)
    public String prepareSignedExportIngestContracts(
        @RequestHeader(CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId
    ) {
        LOGGER.debug("Prepare signed ingest contracts export URL");

        DownloadClaims claims = new DownloadClaims();
        claims.setResource(INGEST_CONTRACT_EXPORT_RESOURCE);
        claims.setTenantId(tenantId);

        return signedDownloadTokenService.generateSignedUrl(claims, SIGNED_DOWNLOAD_INGEST_CONTRACT_EXPORT_PATH);
    }

    @GetMapping(value = SIGNED_DOWNLOAD_EXPORT_CSV_ENDPOINT, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public void signedExportIngestContracts(@RequestParam final String token, final HttpServletResponse response)
        throws IOException {
        ParameterChecker.checkParameter("The token is a mandatory parameter: ", token);
        DownloadClaims claims = signedDownloadTokenService.validate(token, INGEST_CONTRACT_EXPORT_RESOURCE);
        VitamContext vitamContext = new VitamContext(claims.getTenantId()).setApplicationSessionId(
            claims.getApplicationSessionId()
        );

        Resource resource = ingestContractService.exportIngestContracts(vitamContext);
        response.setHeader(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename(EXPORT_INGEST_CONTRACTS_FILE_NAME, StandardCharsets.UTF_8)
                .build()
                .toString()
        );
        response.setHeader(RestUtils.REFERRER_POLICY, "no-referrer");
        response.getOutputStream().write(resource.getContentAsByteArray());
    }
}
