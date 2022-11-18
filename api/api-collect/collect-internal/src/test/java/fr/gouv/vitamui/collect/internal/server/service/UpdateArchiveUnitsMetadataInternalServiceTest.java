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
 *
 *
 */

package fr.gouv.vitamui.collect.internal.server.service;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitamui.commons.api.exception.RequestTimeOutException;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.commons.vitam.api.collect.CollectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(SpringExtension.class)
class UpdateArchiveUnitsMetadataInternalServiceTest {

    @InjectMocks
    private TransactionInternalService transactionInternalService;

    @Mock
    private CollectService collectService;

    @BeforeEach
    public void beforeEach() {
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

    @Test
    void updateCollectArchiveUnits_should_pass_when_Vitam_Return_Ok() {

        // Given
        VitamContext vitamContext = new VitamContext(1);
        final String vitamResponse = "vitamJsonResponse";
        final String transactionId = "transactionId";
        InputStream csvFileInputStream = UpdateArchiveUnitsMetadataInternalServiceTest.class.getClassLoader()
            .getResourceAsStream("data/updateCollectArchiveUnits/collect_metadata.csv");

        //When
        Mockito.when(collectService.updateCollectArchiveUnits(eq(vitamContext), eq(transactionId), any())).thenReturn(vitamResponse);
        String response = transactionInternalService.updateArchiveUnitsFromFile(vitamContext, csvFileInputStream, transactionId);

        //Then
        assertThat(response).isEqualTo(vitamResponse);
    }

    @Test
    void updateCollectArchiveUnits_should_not_pass_when_Vitam_throw_exception() {

        // Given
        VitamContext vitamContext = new VitamContext(1);
        final String vitamResponse = "ERROR_400";
        final String transactionId = "transactionId";
        InputStream csvFileInputStream = UpdateArchiveUnitsMetadataInternalServiceTest.class.getClassLoader()
            .getResourceAsStream("data/updateCollectArchiveUnits/wrong_collect_metadata.csv");

        //When
        Mockito.when(collectService.updateCollectArchiveUnits(eq(vitamContext), eq(transactionId), any())).thenReturn(vitamResponse);

        //Then
        assertThatCode(() -> transactionInternalService.updateArchiveUnitsFromFile(vitamContext, csvFileInputStream, transactionId)).
            isInstanceOf(RequestTimeOutException.class);

    }

    private static String readFromInputStream(InputStream inputStream)
        throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader br
            = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        }
        return resultStringBuilder.toString();
    }

}
