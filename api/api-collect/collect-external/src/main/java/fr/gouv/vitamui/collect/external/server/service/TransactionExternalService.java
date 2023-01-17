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
package fr.gouv.vitamui.collect.external.server.service;

import fr.gouv.vitamui.collect.common.dto.CollectTransactionDto;
import fr.gouv.vitamui.collect.internal.client.CollectTransactionInternalRestClient;
import fr.gouv.vitamui.collect.internal.client.UpdateUnitsMetadataInternalRestClient;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import fr.gouv.vitamui.iam.security.client.AbstractResourceClientService;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * The service to manage transactions.
 */
@Getter
@Setter
@Service
public class TransactionExternalService extends AbstractResourceClientService<CollectTransactionDto, CollectTransactionDto> {

    private static final VitamUILogger LOGGER =
        VitamUILoggerFactory.getInstance(TransactionExternalService.class);

    private final CollectTransactionInternalRestClient collectTransactionInternalRestClient;

    private final UpdateUnitsMetadataInternalRestClient updateUnitsMetadataInternalRestClient;
    @Autowired
    public TransactionExternalService(ExternalSecurityService externalSecurityService,
        CollectTransactionInternalRestClient collectTransactionInternalRestClient,
        UpdateUnitsMetadataInternalRestClient updateUnitsMetadataInternalRestClient) {
        super(externalSecurityService);
        this.collectTransactionInternalRestClient = collectTransactionInternalRestClient;
        this.updateUnitsMetadataInternalRestClient = updateUnitsMetadataInternalRestClient;
    }



    public void sendTransaction(String projectId) {
        collectTransactionInternalRestClient.sendTransaction(getInternalHttpContext(), projectId);
    }

    public void validateTransaction(String projectId) {
        collectTransactionInternalRestClient.validateTransaction(getInternalHttpContext(), projectId);
    }

    public void reopenTransaction(String projectId) {
        collectTransactionInternalRestClient.reopenTransaction(getInternalHttpContext(), projectId);
    }

    public void abortTransaction(String projectId) {
        collectTransactionInternalRestClient.abortTransaction(getInternalHttpContext(), projectId);
    }

    @Override
    protected CollectTransactionInternalRestClient getClient() {
        return collectTransactionInternalRestClient;
    }


    public CollectTransactionDto getTransactionById(String transactionId) {
        return collectTransactionInternalRestClient.getTransactionById(getInternalHttpContext(), transactionId);
    }



    public CollectTransactionDto updateTransaction(CollectTransactionDto collectTransactionDto) {
        return collectTransactionInternalRestClient.updateTransaction(getInternalHttpContext(), collectTransactionDto);
    }

    /**
     * function to update archive Units Metadata with a CSV file
     *
     * @param transactionId the transaction id
     * @param fileName the file name
     * @param inputStream the inputstream file
     * @return String
     */
    public String updateArchiveUnitsFromFile(final String transactionId, InputStream inputStream, String fileName) {
        LOGGER.debug(" [External] start updating archive units from csv file for transactionId {}", transactionId);
        return updateUnitsMetadataInternalRestClient.updateArchiveUnitsMetadataFromFile(
            getInternalHttpContext(), fileName, transactionId, inputStream);
    }

}
