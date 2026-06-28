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
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.AccessionRegisterSearchDto;
import fr.gouv.vitamui.commons.api.domain.DirectionDto;
import fr.gouv.vitamui.commons.api.domain.PaginatedValuesDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.download.DownloadClaims;
import fr.gouv.vitamui.commons.api.download.SignedDownloadTokenService;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.rest.util.RestUtils;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterDetailDto;
import fr.gouv.vitamui.referential.common.dto.AccessionRegisterSummaryDto;
import fr.gouv.vitamui.referential.common.rest.RestApi;
import fr.gouv.vitamui.referential.server.service.accessionregister.AccessionRegisterService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping(RestApi.ACCESSION_REGISTER_URL)
public class AccessionRegisterController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessionRegisterController.class);
    private static final String ACCESSION_REGISTER_EXPORT_RESOURCE = "accession-register-export";
    private static final String SIGNED_DOWNLOAD_EXPORT_CSV_ENDPOINT = "/signed-download/export-csv";
    private static final String SIGNED_DOWNLOAD_ACCESSION_REGISTER_EXPORT_PATH =
        "/accession-register" + RestApi.DETAILS + SIGNED_DOWNLOAD_EXPORT_CSV_ENDPOINT;
    private static final String QUERY_PARAMETER = "query";
    private static final String EXPORT_ACCESSION_REGISTERS_FILE_NAME = "export-accession-registers.csv";

    private final AccessionRegisterService accessionRegisterService;
    private final ObjectMapper objectMapper;
    private final SecurityService securityService;
    private final SignedDownloadTokenService signedDownloadTokenService;

    public AccessionRegisterController(
        AccessionRegisterService accessionRegisterService,
        ObjectMapper objectMapper,
        SecurityService securityService,
        SignedDownloadTokenService signedDownloadTokenService
    ) {
        this.accessionRegisterService = accessionRegisterService;
        this.objectMapper = objectMapper;
        this.securityService = securityService;
        this.signedDownloadTokenService = signedDownloadTokenService;
    }

    @GetMapping("/summary")
    @Secured(ServicesData.ROLE_GET_OPERATIONS)
    public Collection<AccessionRegisterSummaryDto> getAccessionRegisterSummaries(
        @RequestParam final Optional<String> criteria
    ) {
        SanityChecker.sanitizeCriteria(criteria);
        LOGGER.debug("get all accessionRegister criteria={}", criteria);
        return accessionRegisterService.getAll();
    }

    @GetMapping(value = RestApi.DETAILS + "/paginated", params = { "page", "size" })
    @Secured(ServicesData.ROLE_GET_ACCESSION_REGISTER_DETAIL)
    public PaginatedValuesDto<AccessionRegisterDetailDto> getAccessionRegisterDetails(
        @RequestParam final Integer page,
        @RequestParam final Integer size,
        @RequestParam(required = false) final Optional<String> criteria,
        @RequestParam(required = false) final Optional<String> orderBy,
        @RequestParam(required = false) final Optional<DirectionDto> direction
    ) {
        SanityChecker.sanitizeCriteria(criteria);
        orderBy.ifPresent(SanityChecker::checkSecureParameter);
        LOGGER.debug(
            "getPaginateEntities page={}, size={}, criteria={}, orderBy={}, ascendant={}",
            page,
            size,
            criteria,
            orderBy,
            direction
        );
        return accessionRegisterService.getAllPaginated(page, size, criteria, orderBy, direction);
    }

    @PostMapping(RestApi.DETAILS_EXPORT_CSV)
    @Secured(ServicesData.ROLE_GET_ACCESSION_REGISTER_DETAIL)
    public Resource exportCsvArchiveUnitsByCriteria(final @RequestBody AccessionRegisterSearchDto query)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter("The query is a mandatory parameter: ", query);
        SanityChecker.sanitizeCriteria(query);
        LOGGER.info("Calling export to csv search archive Units By Criteria {} ", query);
        return accessionRegisterService.exportCsvArchiveUnitsByCriteria(query);
    }

    @PostMapping(RestApi.DETAILS_EXPORT_CSV + "/signed-url")
    @Secured(ServicesData.ROLE_GET_ACCESSION_REGISTER_DETAIL)
    public String prepareSignedExportCsvArchiveUnitsByCriteria(
        @RequestHeader(CommonConstants.X_TENANT_ID_HEADER) final Integer tenantId,
        @RequestHeader(CommonConstants.X_ACCESS_CONTRACT_ID_HEADER) final String accessContractId,
        final @RequestBody AccessionRegisterSearchDto query
    ) throws PreconditionFailedException {
        ParameterChecker.checkParameter(
            "The query and access contract are mandatory parameters: ",
            query,
            accessContractId
        );
        SanityChecker.sanitizeCriteria(query);
        SanityChecker.checkSecureParameter(accessContractId);
        LOGGER.info("Prepare signed export to csv search archive Units By Criteria {} ", query);

        DownloadClaims claims = new DownloadClaims();
        claims.setResource(ACCESSION_REGISTER_EXPORT_RESOURCE);
        claims.setTenantId(tenantId);
        claims.setAccessContractId(accessContractId);
        claims.setParameters(Map.of(QUERY_PARAMETER, serializeQuery(query)));

        return signedDownloadTokenService.generateSignedUrl(claims, SIGNED_DOWNLOAD_ACCESSION_REGISTER_EXPORT_PATH);
    }

    @GetMapping(
        value = RestApi.DETAILS + SIGNED_DOWNLOAD_EXPORT_CSV_ENDPOINT,
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public void signedExportCsvArchiveUnitsByCriteria(
        @RequestParam final String token,
        final HttpServletResponse response
    ) throws IOException, PreconditionFailedException {
        ParameterChecker.checkParameter("The token is a mandatory parameter: ", token);
        DownloadClaims claims = signedDownloadTokenService.validate(token, ACCESSION_REGISTER_EXPORT_RESOURCE);
        String serializedQuery = claims.getParameters().get(QUERY_PARAMETER);
        if (Objects.isNull(serializedQuery)) {
            throw new BadRequestException("Invalid signed download URL");
        }

        AccessionRegisterSearchDto query = deserializeQuery(serializedQuery);
        SanityChecker.sanitizeCriteria(query);

        VitamContext vitamContext = new VitamContext(claims.getTenantId())
            .setAccessContract(claims.getAccessContractId())
            .setApplicationSessionId(claims.getApplicationSessionId());
        Resource resource = accessionRegisterService.exportToCsvAccessionRegister(query, vitamContext);
        response.setHeader(
            HttpHeaders.CONTENT_DISPOSITION,
            ContentDisposition.attachment()
                .filename(EXPORT_ACCESSION_REGISTERS_FILE_NAME, StandardCharsets.UTF_8)
                .build()
                .toString()
        );
        response.setHeader(RestUtils.REFERRER_POLICY, "no-referrer");
        response.getOutputStream().write(resource.getContentAsByteArray());
    }

    private String serializeQuery(AccessionRegisterSearchDto query) {
        try {
            return objectMapper.writeValueAsString(query);
        } catch (JacksonException e) {
            throw new BadRequestException("Unable to serialize accession register export query", e);
        }
    }

    private AccessionRegisterSearchDto deserializeQuery(String query) {
        try {
            return objectMapper.readValue(query, AccessionRegisterSearchDto.class);
        } catch (JacksonException e) {
            throw new BadRequestException("Unable to deserialize accession register export query", e);
        }
    }
}
