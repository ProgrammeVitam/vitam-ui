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

package fr.gouv.vitamui.collect.internal.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.collect.common.dto.TransactionDto;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.collect.internal.server.service.converters.TransactionConverter;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.exception.RequestTimeOutException;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.collect.CollectService;

import javax.ws.rs.core.Response;
import java.io.InputStream;

public class TransactionInternalService {


    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(TransactionInternalService.class);

    private final CollectService collectService;

    public static final String UNABLE_TO_UPDATE_TRANSACTION = "Unable to update transaction";

    public static final String UNABLE_TO_PROCESS_RESPONSE = "Unable to process response";
    public static final String UNABLE_TO_PROCESS_UNIT_UPDATE = "Unable to process units update operation";

    public static final String ERROR_400 = "ERROR_400";

    public static final String REQUEST_TIMEOUT_EXCEPTION_MESSAGE =
        "the server has decided to close the connection rather than continue waiting";

    public TransactionInternalService(CollectService collectService) {
        this.collectService = collectService;
    }


    public void validateTransaction(String idTransaction, VitamContext vitamContext) throws VitamClientException {
        try {
            RequestResponse requestResponse = collectService.validateTransaction(vitamContext, idTransaction);
            if (requestResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new VitamClientException("Error occurs when validating transaction!");
            }
        } catch (VitamClientException e) {
            throw new VitamClientException("Unable to validate transaction : ", e);
        }
    }

    public void sendTransaction(String idTransaction, VitamContext vitamContext) throws VitamClientException {
        try {
            RequestResponse requestResponse = collectService.sendTransaction(vitamContext, idTransaction);
            if (requestResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new VitamClientException("Error occurs when sending transaction!");
            }
        } catch (VitamClientException e) {
            throw new VitamClientException("Unable to send transaction : ", e);
        }
    }

    public void abortTransaction(String idTransaction, VitamContext vitamContext) throws VitamClientException {
        try {
            RequestResponse requestResponse = collectService.abortTransaction(vitamContext, idTransaction);
            if (requestResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new VitamClientException("Error occurs when aborting transaction!");
            }
        } catch (VitamClientException e) {
            throw new VitamClientException("Unable to abort transaction : ", e);
        }
    }

    public void reopenTransaction(String idTransaction, VitamContext vitamContext) throws VitamClientException {
        try {
            RequestResponse requestResponse = collectService.reopenTransaction(vitamContext, idTransaction);
            if (requestResponse.getStatus() != Response.Status.OK.getStatusCode()) {
                throw new VitamClientException("Error occurs when reopening transaction!");
            }
        } catch (VitamClientException e) {
            throw new VitamClientException("Unable to reopen transaction : ", e);
        }
    }

    public CollectTransactionDto getTransactionById(String transactionId, VitamContext vitamContext)
        throws VitamClientException {
        try {
            RequestResponse<JsonNode> requestResponse = collectService.getTransactionById(vitamContext, transactionId);
            if (!requestResponse.isOk()) {
                throw new VitamClientException("Error occurs when getting transaction!");
            }

            return TransactionConverter.toVitamUiDto(
                JsonHandler.getFromString(((RequestResponseOK) requestResponse).getFirstResult().toString(),
                    TransactionDto.class));
        } catch (VitamClientException | InvalidParseOperationException e) {
            throw new VitamClientException("Unable to find transaction : ", e);
        }
    }

    public CollectTransactionDto updateTransaction(CollectTransactionDto collectTransactionDto,
        VitamContext vitamContext) {
        LOGGER.debug("CollectTransactionDto: ", collectTransactionDto);
        try {
            TransactionDto transactionDto = TransactionConverter.toVitamDto(collectTransactionDto);
            RequestResponse<JsonNode> requestResponse = collectService.updateTransaction(vitamContext, transactionDto);
            if (!requestResponse.isOk()) {
                throw new VitamClientException("Error occurs when updating transaction!");
            }
            TransactionDto responseTransactionDto =
                JsonHandler.getFromString(((RequestResponseOK) requestResponse).getFirstResult().toString(),
                    TransactionDto.class);
            return TransactionConverter.toVitamUiDto(responseTransactionDto);
        } catch (VitamClientException e) {
            LOGGER.debug(UNABLE_TO_UPDATE_TRANSACTION + ": {}", e);
            throw new InternalServerException(UNABLE_TO_UPDATE_TRANSACTION, e);
        } catch (InvalidParseOperationException e) {
            LOGGER.debug(UNABLE_TO_PROCESS_RESPONSE + ": {}", e);
            throw new InternalServerException(UNABLE_TO_PROCESS_RESPONSE, e);
        }
    }

    public String updateArchiveUnitsFromFile(VitamContext vitamContext, InputStream inputStream, String transactionId)
        throws RequestTimeOutException {
        LOGGER.debug("[Internal] call update Archive Units From File for transaction Id {}  ", transactionId);
        final String result = collectService.updateCollectArchiveUnits(vitamContext, transactionId, inputStream);
        if (result.equals(ERROR_400)) {
            LOGGER.debug(UNABLE_TO_PROCESS_UNIT_UPDATE);
            throw new RequestTimeOutException(REQUEST_TIMEOUT_EXCEPTION_MESSAGE, REQUEST_TIMEOUT_EXCEPTION_MESSAGE);
        }
        return result;
    }
}
