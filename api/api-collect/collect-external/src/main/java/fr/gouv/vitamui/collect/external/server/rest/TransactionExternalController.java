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

import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.collect.common.rest.RestApi;
import fr.gouv.vitamui.collect.external.server.service.TransactionExternalService;
import fr.gouv.vitamui.common.security.SafeFileChecker;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

import static fr.gouv.vitamui.collect.common.rest.RestApi.ABORT_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.REOPEN_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.SEND_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.UPDATE_UNITS_METADATA_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.VALIDATE_PATH;

/**
 * Transaction External controller
 */
@Api(tags = "Collect")
@RequestMapping(RestApi.COLLECT_TRANSACTION_PATH)
@RestController
@ResponseBody
public class TransactionExternalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionExternalController.class);

    private final TransactionExternalService transactionExternalService;

    private static final String MANDATORY_IDENTIFIER = "The Identifier is a mandatory parameter: ";
    private static final String TRANSACTION_ID = "The transaction id {} ";

    @Autowired
    public TransactionExternalController(TransactionExternalService transactionExternalService) {
        this.transactionExternalService = transactionExternalService;
    }

    @Secured(ServicesData.ROLE_SEND_TRANSACTIONS)
    @PutMapping(CommonConstants.PATH_ID + SEND_PATH)
    public void sendTransaction(final @PathVariable("id") String id) throws PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug(TRANSACTION_ID, id);
        transactionExternalService.sendTransaction(id);
    }

    @Secured(ServicesData.ROLE_REOPEN_TRANSACTIONS)
    @PutMapping(CommonConstants.PATH_ID + REOPEN_PATH)
    public void reopenTransaction(final @PathVariable("id") String id) throws PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug(TRANSACTION_ID, id);
        transactionExternalService.reopenTransaction(id);
    }

    @Secured(ServicesData.ROLE_ABORT_TRANSACTIONS)
    @PutMapping(CommonConstants.PATH_ID + ABORT_PATH)
    public void abortTransaction(final @PathVariable("id") String id) throws PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug(TRANSACTION_ID, id);
        transactionExternalService.abortTransaction(id);
    }

    @Secured(ServicesData.ROLE_CLOSE_TRANSACTIONS)
    @PutMapping(CommonConstants.PATH_ID + VALIDATE_PATH)
    public void validateTransaction(final @PathVariable("id") String id) throws PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug(TRANSACTION_ID, id);
        transactionExternalService.validateTransaction(id);
    }

    @ApiOperation(value = "Get transaction by id")
    @Secured(ServicesData.ROLE_GET_TRANSACTIONS)
    @GetMapping(CommonConstants.PATH_ID)
    public CollectTransactionDto getTransactionById(final @PathVariable("id") String id)
        throws PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Find the Transactions with project Id {}", id);
        return transactionExternalService.getTransactionById(id);
    }

    @Secured(ServicesData.ROLE_UPDATE_TRANSACTIONS)
    @PutMapping
    public CollectTransactionDto updateTransaction(@RequestBody CollectTransactionDto transactionDto)
        throws PreconditionFailedException {
        ParameterChecker.checkParameter(MANDATORY_IDENTIFIER, transactionDto.getId());
        SanityChecker.checkSecureParameter(transactionDto.getId());
        SanityChecker.sanitizeCriteria(transactionDto);
        LOGGER.debug("[External] Transaction to update : {}", transactionDto);
        return transactionExternalService.updateTransaction(transactionDto);
    }

    @Secured(ServicesData.COLLECT_UPDATE_BULK_ARCHIVE_UNIT_ROLE)
    @ApiOperation(
        value = "Upload on streaming metadata file and update archive units",
        consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    @PutMapping(
        value = CommonConstants.TRANSACTION_PATH_ID + UPDATE_UNITS_METADATA_PATH,
        consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public String updateArchiveUnitsMetadataFromFile(
        final @PathVariable("transactionId") String transactionId,
        InputStream inputStream,
        @RequestHeader(value = CommonConstants.X_ORIGINAL_FILENAME_HEADER) final String originalFileName
    ) throws PreconditionFailedException {
        ParameterChecker.checkParameter(" [External] The transactionId is a mandatory parameter: ", transactionId);
        SanityChecker.checkSecureParameter(transactionId);
        SanityChecker.isValidFileName(originalFileName);
        SafeFileChecker.checkSafeFilePath(originalFileName);
        LOGGER.debug("[External] Calling update archive units metadata for transaction Id  {} ", transactionId);
        return transactionExternalService.updateArchiveUnitsFromFile(transactionId, inputStream, originalFileName);
    }
}
