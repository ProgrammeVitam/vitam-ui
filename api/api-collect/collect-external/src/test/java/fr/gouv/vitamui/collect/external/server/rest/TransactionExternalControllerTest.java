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

package fr.gouv.vitamui.collect.external.server.rest;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitamui.collect.external.server.service.TransactionExternalService;
import fr.gouv.vitamui.commons.api.domain.IdDto;
import fr.gouv.vitamui.commons.api.domain.ServicesData;
import fr.gouv.vitamui.commons.api.exception.PreconditionFailedException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static fr.gouv.vitamui.collect.common.rest.RestApi.COLLECT_TRANSACTION_PATH;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@WebMvcTest(controllers = { TransactionExternalController.class })
class TransactionExternalControllerTest extends ApiCollectExternalControllerTest<IdDto> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionExternalControllerTest.class);

    @MockBean
    private TransactionExternalService transactionExternalService;

    private TransactionExternalController transactionExternalController;

    @BeforeEach
    public void setUp() {
        transactionExternalController = new TransactionExternalController(transactionExternalService);
    }

    @Override
    protected String[] getServices() {
        return new String[] { ServicesData.TRANSACTIONS };
    }

    @Override
    protected Class<IdDto> getDtoClass() {
        return IdDto.class;
    }

    @Override
    protected IdDto buildDto() {
        return null;
    }

    @Override
    protected Logger getLog() {
        return LOGGER;
    }

    @Override
    protected void preparedServices() {}

    @Override
    protected String getRessourcePrefix() {
        return COLLECT_TRANSACTION_PATH;
    }

    @Test
    void when_abortTransaction_ok() throws InvalidParseOperationException {
        Mockito.doNothing().when(transactionExternalService).abortTransaction("transactionId");
        this.transactionExternalController.abortTransaction("transactionId");
        verify(transactionExternalService, times(1)).abortTransaction("transactionId");
    }

    @Test
    void when_searchCollectUnitsByCriteria_Service_ko_should_return_ko() throws InvalidParseOperationException {
        Mockito.doNothing().when(transactionExternalService).reopenTransaction("transactionId");
        this.transactionExternalController.reopenTransaction("transactionId");
        verify(transactionExternalService, times(1)).reopenTransaction("transactionId");
    }

    @Test
    void testUpdateUnitsMetadataThenReturnVitamOperationDetails()
        throws InvalidParseOperationException, PreconditionFailedException {
        // Given
        String fileName = "FileName";
        String transactionId = "transactionId";
        String expectedResponse = "operationStatus";
        String initialString = "csv file to update collect units";
        InputStream csvFile = new ByteArrayInputStream(initialString.getBytes());

        // When
        Mockito.when(
            transactionExternalService.updateArchiveUnitsFromFile(transactionId, csvFile, fileName)
        ).thenReturn(expectedResponse);
        String response = transactionExternalController.updateArchiveUnitsMetadataFromFile(
            transactionId,
            csvFile,
            fileName
        );

        // Then
        Assertions.assertEquals(response, expectedResponse);
    }
}
