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

package fr.gouv.vitamui.collect.internal.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.collect.common.rest.RestApi;
import fr.gouv.vitamui.collect.internal.server.service.ExternalParametersService;
import fr.gouv.vitamui.collect.internal.server.service.TransactionInternalService;
import fr.gouv.vitamui.common.security.SafeFileChecker;
import fr.gouv.vitamui.common.security.SanityChecker;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.ParameterChecker;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import fr.gouv.vitamui.commons.api.exception.RequestTimeOutException;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

import static fr.gouv.vitamui.collect.common.rest.RestApi.ABORT_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.REOPEN_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.SEND_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.UPDATE_UNITS_METADATA_PATH;
import static fr.gouv.vitamui.collect.common.rest.RestApi.VALIDATE_PATH;

@RestController
@RequestMapping(RestApi.COLLECT_TRANSACTION_PATH)
@Api(tags = "collect", value = "Préparation de versements")
public class TransactionInternalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionInternalController.class);
    private final TransactionInternalService transactionInternalService;

    private final ExternalParametersService externalParametersService;
    private static final String IDENTIFIER_MANDATORY_MESSAGE = "The Identifier is a mandatory parameter: ";

    @Autowired
    public TransactionInternalController(
        final TransactionInternalService transactionInternalService,
        final ExternalParametersService externalParametersService
    ) {
        this.transactionInternalService = transactionInternalService;
        this.externalParametersService = externalParametersService;
    }

    @PutMapping(CommonConstants.PATH_ID + SEND_PATH)
    public void sendTransaction(final @PathVariable("id") String id)
        throws VitamClientException, InvalidParseOperationException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_MESSAGE, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Transaction to send  {}", id);
        transactionInternalService.sendTransaction(id, externalParametersService.buildVitamContextFromExternalParam());
    }

    @PutMapping(CommonConstants.PATH_ID + VALIDATE_PATH)
    public void validateTransaction(final @PathVariable("id") String id)
        throws VitamClientException, InvalidParseOperationException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_MESSAGE, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Transaction to close  {}", id);
        transactionInternalService.validateTransaction(
            id,
            externalParametersService.buildVitamContextFromExternalParam()
        );
    }

    @PutMapping(CommonConstants.PATH_ID + REOPEN_PATH)
    public void reopenTransaction(final @PathVariable("id") String id)
        throws VitamClientException, InvalidParseOperationException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_MESSAGE, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Transaction to reopen  {}", id);
        transactionInternalService.reopenTransaction(
            id,
            externalParametersService.buildVitamContextFromExternalParam()
        );
    }

    @PutMapping(CommonConstants.PATH_ID + ABORT_PATH)
    public void abortTransaction(final @PathVariable("id") String id)
        throws VitamClientException, InvalidParseOperationException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_MESSAGE, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Transaction to abort  {}", id);
        transactionInternalService.abortTransaction(id, externalParametersService.buildVitamContextFromExternalParam());
    }

    @GetMapping(CommonConstants.PATH_ID)
    public CollectTransactionDto getTransactionById(final @PathVariable("id") String id)
        throws VitamClientException, InvalidParseOperationException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_MESSAGE, id);
        SanityChecker.checkSecureParameter(id);
        LOGGER.debug("Project Id  {}", id);
        return transactionInternalService.getTransactionById(
            id,
            externalParametersService.buildVitamContextFromExternalParam()
        );
    }

    @PutMapping(
        value = CommonConstants.TRANSACTION_PATH_ID + UPDATE_UNITS_METADATA_PATH,
        consumes = MediaType.APPLICATION_OCTET_STREAM_VALUE
    )
    public String updateArchiveUnitsMetadataFromFile(
        final @PathVariable("transactionId") String transactionId,
        InputStream inputStream,
        @RequestHeader(value = CommonConstants.X_ORIGINAL_FILENAME_HEADER) final String originalFileName
    ) throws InvalidParseOperationException, PreconditionFailedException, RequestTimeOutException {
        ParameterChecker.checkParameter("The transaction Id is a mandatory parameter: ", transactionId);
        SanityChecker.checkSecureParameter(transactionId);
        LOGGER.debug("[Internal] update archiveUnits metadata from file for transaction  {}", transactionId);
        SanityChecker.isValidFileName(originalFileName);
        SafeFileChecker.checkSafeFilePath(originalFileName);
        LOGGER.debug("[Internal] csv FileName  {}", originalFileName);

        return transactionInternalService.updateArchiveUnitsFromFile(
            inputStream,
            transactionId,
            externalParametersService.buildVitamContextFromExternalParam()
        );
    }

    @PutMapping
    public CollectTransactionDto updateTransaction(@RequestBody CollectTransactionDto transactionDto)
        throws InvalidParseOperationException, PreconditionFailedException {
        ParameterChecker.checkParameter(IDENTIFIER_MANDATORY_MESSAGE, transactionDto);
        SanityChecker.sanitizeCriteria(transactionDto);
        LOGGER.debug("[Internal] Transaction to update : {}", transactionDto);
        return transactionInternalService.updateTransaction(
            transactionDto,
            externalParametersService.buildVitamContextFromExternalParam()
        );
    }
}
