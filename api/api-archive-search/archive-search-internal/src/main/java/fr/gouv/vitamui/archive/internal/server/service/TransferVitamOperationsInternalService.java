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
package fr.gouv.vitamui.archive.internal.server.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponse;
import fr.gouv.vitam.common.model.dip.DataObjectVersions;
import fr.gouv.vitam.common.model.export.transfer.TransferRequest;
import fr.gouv.vitamui.archives.search.common.dto.TransferRequestDto;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.commons.vitam.api.access.TransferAcknowledgmentService;
import fr.gouv.vitamui.commons.vitam.api.access.TransferRequestService;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class TransferVitamOperationsInternalService {
    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(TransferVitamOperationsInternalService.class);
    public static final String OPERATION_IDENTIFIER = "itemId";
    private final TransferAcknowledgmentService transferAcknowledgmentService;
    private final TransferRequestService transferRequestService;
    private final ArchiveSearchInternalService archiveSearchInternalService;


    public TransferVitamOperationsInternalService(final TransferAcknowledgmentService transferAcknowledgmentService,
        final @Lazy ArchiveSearchInternalService archiveSearchInternalService,
        final TransferRequestService transferRequestService
    ) {
        this.transferAcknowledgmentService = transferAcknowledgmentService;
        this.archiveSearchInternalService = archiveSearchInternalService;
        this.transferRequestService = transferRequestService;
    }

    private JsonNode sendTransferRequest(final VitamContext vitamContext, TransferRequest transferRequest)
        throws VitamClientException {
        RequestResponse<JsonNode> response = transferRequestService.transferRequest(vitamContext, transferRequest);
        return response.toJsonNode();
    }

    private TransferRequest prepareTransferRequestBody(final TransferRequestDto transferRequestDto,
        JsonNode dslQuery) {
        final TransferRequest transferRequest = new TransferRequest();
        if (transferRequestDto != null) {
            final DataObjectVersions dataObjectVersions = new DataObjectVersions();
            dataObjectVersions.setDataObjectVersionsPatterns(transferRequestDto.getDataObjectVersionsPatterns());

            transferRequest.setTransferWithLogBookLFC(transferRequestDto.isLifeCycleLogs());
            transferRequest.setDslRequest(dslQuery);
            transferRequest.setDataObjectVersionToExport(dataObjectVersions);
            transferRequest.setTransferRequestParameters(transferRequestDto.getTransferRequestParameters());
            transferRequest.setSedaVersion(transferRequestDto.getSedaVersion());
        }
        return transferRequest;
    }

    public String transferRequest(final TransferRequestDto transferRequestDto,
        final VitamContext vitamContext)
        throws VitamClientException {

        LOGGER.debug("Transfer request: {} ", transferRequestDto.toString());
        JsonNode dslQuery =
            archiveSearchInternalService.prepareDslQuery(transferRequestDto.getSearchCriteria(), vitamContext);
        LOGGER.debug("Transfer request final DSL query: {} ", dslQuery);

        TransferRequest transferRequest = prepareTransferRequestBody(transferRequestDto, dslQuery);

        JsonNode response = sendTransferRequest(vitamContext, transferRequest);
        return response.findValue(OPERATION_IDENTIFIER).textValue();
    }

    public String transferAcknowledgmentService(InputStream atrInputStream, VitamContext vitamContext)
        throws VitamClientException {

        LOGGER.debug("Transfer Acknowledgment Operation");
        JsonNode transferAcknowledgmentResponse =
            transferAcknowledgmentService.transferAcknowledgment(vitamContext, atrInputStream).toJsonNode();
        return transferAcknowledgmentResponse.findValue(OPERATION_IDENTIFIER).textValue();
    }
}
