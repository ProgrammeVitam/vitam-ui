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

package fr.gouv.vitamui.archives.search.server.service;

import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitamui.iam.openapiclient.ExternalParametersApi;
import fr.gouv.vitamui.iam.security.service.SecurityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

@ExtendWith(SpringExtension.class)
public class ArchiveSearchExternalParametersServiceTest {

    public static final String SOME_ACCESS_CONTRACT = "SOME_ACCESS_CONTRACT";
    public static final int SOME_TENANT = 1;

    @MockitoBean(name = "externalParametersApi")
    private ExternalParametersApi externalParametersApi;

    @MockitoBean(name = "securityService")
    private SecurityService securityService;

    @InjectMocks
    private ArchiveSearchExternalParametersService archiveSearchExternalParametersService;

    @BeforeEach
    public void setUp() {
        archiveSearchExternalParametersService = new ArchiveSearchExternalParametersService(
            externalParametersApi,
            securityService
        );
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenNoAccessContract() {
        Map<String, String> myExternalParameter = Map.of("ANY_VALUE", "ANY_PARAM");
        Mockito.when(externalParametersApi.getMyExternalParameters()).thenReturn(myExternalParameter);

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            archiveSearchExternalParametersService.retrieveAccessContractFromExternalParam();
        });

        Assertions.assertEquals("No access contract defined", thrown.getMessage());
    }

    @Test
    void shouldThrowAnotherIllegalArgumentExceptionWhenNoAccessContract() {
        Map<String, String> myExternalParameter = Map.of();
        Mockito.when(externalParametersApi.getMyExternalParameters()).thenReturn(myExternalParameter);

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            archiveSearchExternalParametersService.retrieveAccessContractFromExternalParam();
        });

        Assertions.assertEquals("No external profile defined for access contract defined", thrown.getMessage());
    }

    @Test
    void shouldRetrieveTheRightAccessContractWhenDefiend() {
        Map<String, String> myExternalParameter = Map.of(
            ArchiveSearchExternalParametersService.PARAM_ACCESS_CONTRACT_NAME,
            SOME_ACCESS_CONTRACT
        );
        Mockito.when(externalParametersApi.getMyExternalParameters()).thenReturn(myExternalParameter);

        String accessContractFound = archiveSearchExternalParametersService.retrieveAccessContractFromExternalParam();
        Assertions.assertEquals(SOME_ACCESS_CONTRACT, accessContractFound);
    }

    @Test
    void shouldRetrieveTheRightAccessContractFromContextWhenDefiend() {
        Map<String, String> myExternalParameter = Map.of(
            ArchiveSearchExternalParametersService.PARAM_ACCESS_CONTRACT_NAME,
            SOME_ACCESS_CONTRACT
        );
        Mockito.when(externalParametersApi.getMyExternalParameters()).thenReturn(myExternalParameter);
        Mockito.when(securityService.getTenantIdentifier()).thenReturn(SOME_TENANT);
        VitamContext someContext = new VitamContext(SOME_TENANT).setAccessContract(SOME_ACCESS_CONTRACT);
        VitamContext context = archiveSearchExternalParametersService.buildVitamContextFromExternalParam();
        Assertions.assertEquals(someContext, context);
    }
}
