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

package fr.gouv.vitamui.collect.service;

import fr.gouv.vitamui.collect.external.client.CollectExternalRestClient;
import fr.gouv.vitamui.collect.external.client.CollectExternalWebClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.ui.commons.service.CommonService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class TransactionServiceTest {

    private TransactionService transactionService;

    @Mock
    private CollectExternalRestClient collectExternalRestClient;

    @Mock
    private CollectExternalWebClient collectExternalWebClient;

    @Mock
    private CommonService commonService;

    @Before
    public void init() {
        transactionService = new TransactionService(collectExternalRestClient, collectExternalWebClient, commonService);
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    public void update_archive_units_metadata_should_call_appropriate_client() {
        // Given
        ExternalHttpContext context = new ExternalHttpContext(9, "", "", "");
        doNothing().when(collectExternalWebClient).updateArchiveUnitsMetadataFromFile(anyString(),
            any(InputStream.class), any(ExternalHttpContext.class));
        String transactionId = "1";
        InputStream anyInputStream = new ByteArrayInputStream("test data".getBytes());

        // When
        transactionService.updateArchiveUnitsMetadataFromFile(transactionId, anyInputStream, context);

        // Then
        verify(collectExternalWebClient, Mockito.times(1))
            .updateArchiveUnitsMetadataFromFile(ArgumentMatchers.any(), ArgumentMatchers.any(),
                any(ExternalHttpContext.class));
    }
}
