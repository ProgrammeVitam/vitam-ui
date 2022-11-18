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

package fr.gouv.vitamui.collect.external.server.service;

import fr.gouv.vitamui.collect.internal.client.CollectTransactionInternalRestClient;
import fr.gouv.vitamui.collect.internal.client.UpdateUnitsMetadataInternalRestClient;
import fr.gouv.vitamui.commons.rest.client.ExternalHttpContext;
import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.commons.security.client.dto.AuthUserDto;
import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;
import fr.gouv.vitamui.iam.security.service.ExternalSecurityService;
import org.apache.commons.collections4.CollectionUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class TransactionExternalServiceTest {

    @Mock
    private ExternalSecurityService externalSecurityService;
    @Mock
    private CollectTransactionInternalRestClient collectTransactionInternalRestClient;
    @Mock
    private UpdateUnitsMetadataInternalRestClient updateUnitsMetadataInternalRestClient;

    private TransactionExternalService transactionExternalService;

    @BeforeEach
    public void setup() {
        final String userCustomerId = "customerIdAllowed";
        mockSecurityContext(externalSecurityService, userCustomerId, 10);
        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
        transactionExternalService = new TransactionExternalService(externalSecurityService, collectTransactionInternalRestClient,
             updateUnitsMetadataInternalRestClient);
    }

    @Test
    void update_units_should_return_response_when_vitamUpdateUnits_return_response() {

        // Given
        String initialString = "csv file to update collect units";
        final String transactionId = "transactionId";
        final String fileName = "fileName";
        final String vitamResponse = "vitamResponseDetails";
        InputStream csvFile = new ByteArrayInputStream(initialString.getBytes());

        // When
        when(updateUnitsMetadataInternalRestClient.updateArchiveUnitsMetadataFromFile(any(InternalHttpContext.class), eq(fileName),
           eq(transactionId), eq(csvFile)))
            .thenReturn(vitamResponse);

        // Then
        assertThatCode(() -> {
            transactionExternalService.updateArchiveUnitsFromFile(transactionId, csvFile, fileName);
        }).doesNotThrowAnyException();
    }

    private void mockSecurityContext(ExternalSecurityService externalSecurityService, final String userCustomerId, final Integer tenantIdentifier,
        final String... userRoles) {
        final AuthUserDto user = new AuthUserDto();
        user.setLevel("");
        user.setCustomerId(userCustomerId);
        user.setProofTenantIdentifier(tenantIdentifier);
        final List<String> roles = Arrays.asList(userRoles);
        if (CollectionUtils.isNotEmpty(roles)) {
            roles.forEach(r -> Mockito.when(externalSecurityService.hasRole(r)).thenReturn(true));
        }

        Mockito.when(externalSecurityService.getUser()).thenReturn(user);
        Mockito.when(externalSecurityService.getHttpContext())
            .thenReturn(new ExternalHttpContext(10, "userToken", "applicationId", "id"));
    }
}
