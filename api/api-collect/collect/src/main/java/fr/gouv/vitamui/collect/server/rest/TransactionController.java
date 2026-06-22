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

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.collect.common.enums.TransactionValidationMode;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitamui.archives.search.common.dto.ReclassificationCriteriaDto;
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.collect.common.rest.RestApi;
import fr.gouv.vitamui.collect.server.service.ExternalParametersService;
import fr.gouv.vitamui.collect.server.service.TransactionService;
import fr.gouv.vitamui.common.security.SafeFileChecker;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.download.DownloadClaims;
import fr.gouv.vitamui.commons.api.download.SignedDownloadTokenService;
import fr.gouv.vitamui.commons.api.exception.BadRequestException;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

import static fr.gouv.vitamui.collect.common.rest.RestApi.ABORT_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.DOWNLOAD_SIP_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.REOPEN_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.SEND_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.UPDATE_UNITS_METADATA_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.VALIDATE_PATH;

/**
 * Transaction External controller
 */
@RequestMapping(RestApi.COLLECT_TRANSACTION_PATH)
@RestController
@Tag(name = "Collect")
@ResponseBody
@RequiredArgsConstructor
public class TransactionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionController.class);

    private static final String MANDATORY_IDENTIFIER = "The Identifier is a mandatory parameter: ";
    private static final String MANDATORY_QUERY = "The query is a mandatory parameter: ";
    private static final String TRANSACTION_ID = "The transaction id {} ";
    private static final String COLLECT_TRANSACTION_SIP_DOWNLOAD_RESOURCE = "collect-transaction-sip-download";
    private static final String SIGNED_DOWNLOAD_SIP_ENDPOINT = "/signed-download/sip";
    private static final String SIGNED_DOWNLOAD_TRANSACTION_SIP_PATH =
        RestApi.TRANSACTIONS + SIGNED_DOWNLOAD_SIP_ENDPOINT;
    private static final String ID_PARAMETER = "id";

    private final TransactionService transactionService;
    private final ExternalParametersService externalParametersService;
    private final SignedDownloadTokenService signedDownloadTokenService;

    @Secured(ServicesData.ROLE_SEND_TRANSACTIONS)
    @PutMapping(CommonConstants.PATH_ID + SEND_PATH)
    public void sendTransaction(final @PathVariable("id") String id)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug(TRANSACTION_ID, id);
        transactionService.sendTransaction(id, externalParametersService.buildVitamContextFromExternalParam());
    }

    @Secured(ServicesData.ROLE_REOPEN_TRANSACTIONS)
    @PutMapping(CommonConstants.PATH_ID + REOPEN_PATH)
    public void reopenTransaction(final @PathVariable("id") String id)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug(TRANSACTION_ID, id);
        transactionService.reopenTransaction(id, externalParametersService.buildVitamContextFromExternalParam());
    }

    @Secured(ServicesData.ROLE_ABORT_TRANSACTIONS)
    @PutMapping(CommonConstants.PATH_ID + ABORT_PATH)
    public void abortTransaction(final @PathVariable("id") String id)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug(TRANSACTION_ID, id);
        transactionService.abortTransaction(id, externalParametersService.buildVitamContextFromExternalParam());
    }

    @Secured(ServicesData.ROLE_CLOSE_TRANSACTIONS)
    @PutMapping(CommonConstants.PATH_ID + VALIDATE_PATH)
    public void validateTransaction(
        final @PathVariable("id") String id,
        @RequestParam("validationMode") final TransactionValidationMode validationMode
    ) throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug(TRANSACTION_ID, id);
        transactionService.validateTransaction(
            id,
            externalParametersService.buildVitamContextFromExternalParam(),
            validationMode
        );
    }

    @Operation(summary = "Get transaction by id")
    @Secured(ServicesData.ROLE_GET_TRANSACTIONS)
    @GetMapping(CommonConstants.PATH_ID)
    public CollectTransactionDto getTransactionById(final @PathVariable("id") String id)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Find the Transactions with project Id {}", id);
        return transactionService.getTransactionById(
            id,
            externalParametersService.buildVitamContextFromExternalParam()
        );
    }

    @Secured(ServicesData.ROLE_UPDATE_TRANSACTIONS)
    @PutMapping
    public CollectTransactionDto updateTransaction(@RequestBody CollectTransactionDto transactionDto)
        throws PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, transactionDto.getId());
        SanityChecker.checkSecureParameter(transactionDto.getId());
        SanityChecker.sanitizeCriteria(transactionDto);
        LOGGER.debug("[External] Transaction to update : {}", transactionDto);
        return transactionService.updateTransaction(
            transactionDto,
            externalParametersService.buildVitamContextFromExternalParam()
        );
    }

    @Secured(ServicesData.COLLECT_UPDATE_BULK_ARCHIVE_UNIT_ROLE)
    @Operation(summary = "Upload on streaming metadata file and update archive units by CSV file")
    @PutMapping(
        value = CommonConstants.TRANSACTION_PATH_ID + UPDATE_UNITS_METADATA_PATH,
        consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public ResponseEntity<JsonNode> updateArchiveUnitsMetadataFromCsvFile(
        final @PathVariable("transactionId") String transactionId,
        InputStream inputStream,
        @RequestHeader(value = CommonConstants.X_ORIGINAL_FILENAME_HEADER) final String originalFileName
    ) throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(" [External] The transactionId is a mandatory parameter: ", transactionId);
        SanityChecker.checkSecureParameter(transactionId);
        SanityChecker.isValidFileName(originalFileName);
        SafeFileChecker.checkSafeFilePath(originalFileName);
        LOGGER.debug("[External] Calling update archive units metadata for transaction Id  {} ", transactionId);
        RequestResponse<JsonNode> response = transactionService.updateArchiveUnitsFromCsvFile(
            inputStream,
            transactionId,
            externalParametersService.buildVitamContextFromExternalParam()
        );
        return new ResponseEntity<>(response.toJsonNode(), HttpStatusCode.valueOf(response.getHttpCode()));
    }

    @Secured(ServicesData.ROLE_COLLECT_RECLASSIFICATION)
    @PostMapping(CommonConstants.TRANSACTION_PATH_ID + "/reclassification")
    public String reclassification(
        final @PathVariable("transactionId") String transactionId,
        @RequestBody final ReclassificationCriteriaDto reclassificationCriteriaDto
    ) throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_QUERY, reclassificationCriteriaDto);
        SanityChecker.sanitizeCriteria(reclassificationCriteriaDto);
        LOGGER.debug("Reclassification query {}", reclassificationCriteriaDto);
        return transactionService.reclassification(
            transactionId,
            reclassificationCriteriaDto,
            externalParametersService.buildVitamContextFromExternalParam()
        );
    }

    @Operation(summary = "Download SIP transaction as a zip file")
    @Secured(ServicesData.ROLE_DOWNLOAD_SIP_TRANSACTIONS)
    @GetMapping(CommonConstants.PATH_ID + DOWNLOAD_SIP_PATH)
    public Mono<ResponseEntity<Resource>> downloadSipTransaction(final @PathVariable("id") String id)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Download SIP transaction with id: {}", id);
        return transactionService.downloadSipTransaction(
            id,
            externalParametersService.buildVitamContextFromExternalParam()
        );
    }

    @Operation(summary = "Prepare signed URL to download SIP transaction as a zip file")
    @Secured(ServicesData.ROLE_DOWNLOAD_SIP_TRANSACTIONS)
    @PostMapping(CommonConstants.PATH_ID + DOWNLOAD_SIP_PATH + "/signed-url")
    public String prepareSignedDownloadSipTransaction(final @PathVariable("id") String id)
        throws PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Prepare signed download SIP transaction with id: {}", id);

        DownloadClaims claims = new DownloadClaims();
        claims.setResource(COLLECT_TRANSACTION_SIP_DOWNLOAD_RESOURCE);
        claims.setParameters(Map.of(ID_PARAMETER, id));

        return signedDownloadTokenService.generateSignedUrl(claims, SIGNED_DOWNLOAD_TRANSACTION_SIP_PATH);
    }

    @Operation(summary = "Download SIP transaction as a zip file from a signed URL")
    @GetMapping(value = SIGNED_DOWNLOAD_SIP_ENDPOINT, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<Resource>> signedDownloadSipTransaction(@RequestParam final String token)
        throws PreconditionFailedException, VitamClientException {
        ParameterChecker.checkParameter("The token is a mandatory parameter: ", token);
        DownloadClaims claims = signedDownloadTokenService.validate(token, COLLECT_TRANSACTION_SIP_DOWNLOAD_RESOURCE);
        String id = claims.getParameters().get(ID_PARAMETER);
        if (Objects.isNull(id)) {
            throw new BadRequestException("Invalid signed download URL");
        }

        SanityChecker.checkSecureParameter(id);
        VitamContext vitamContext = new VitamContext(claims.getTenantId())
            .setAccessContract(claims.getAccessContractId())
            .setApplicationSessionId(claims.getApplicationSessionId());
        return transactionService.downloadSipTransaction(id, vitamContext);
    }
}
