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

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.gouv.vitam.common.client.VitamContext;
import fr.gouv.vitam.common.exception.VitamClientException;
import fr.gouv.vitam.common.model.RequestResponseOK;
import fr.gouv.vitam.common.model.administration.AccessContractModel;
import fr.gouv.vitamui.commons.api.domain.ExternalParametersDto;
import fr.gouv.vitamui.commons.api.domain.ParameterDto;
import fr.gouv.vitamui.commons.vitam.api.administration.AccessContractService;
import fr.gouv.vitamui.iam.internal.client.ExternalParametersInternalRestClient;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collections;
import java.util.List;

@ExtendWith(SpringExtension.class)
public class ExternalParametersServiceTest {

    public static final String SOME_ACCESS_CONTRACT = "SOME_ACCESS_CONTRACT";
    public static final int SOME_TENANT = 1;

    @MockBean
    private ExternalParametersInternalRestClient externalParametersInternalRestClient;

    @MockBean
    private InternalSecurityService securityService;

    @InjectMocks
    private ExternalParametersService externalParametersService;

    @MockBean
    private AccessContractService accessContractService;

    @BeforeEach
    public void setUp() {
        externalParametersService = new ExternalParametersService(
            externalParametersInternalRestClient,
            securityService,
            accessContractService
        );
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenNoAccessContract() {
        ExternalParametersDto myExternalParameter = new ExternalParametersDto();
        ParameterDto parameterDto = new ParameterDto();
        parameterDto.setValue("ANY_VALUE");
        parameterDto.setKey("ANY_PARAM");
        myExternalParameter.setParameters(List.of(parameterDto));
        Mockito.when(
            externalParametersInternalRestClient.getMyExternalParameters(securityService.getHttpContext())
        ).thenReturn(myExternalParameter);

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            externalParametersService.retrieveAccessContract();
        });

        Assertions.assertEquals("No access contract defined", thrown.getMessage());
    }

    @Test
    void shouldThrowAnotherIllegalArgumentExceptionWhenNoAccessContract() {
        ExternalParametersDto myExternalParameter = new ExternalParametersDto();
        myExternalParameter.setParameters(Collections.emptyList());
        Mockito.when(
            externalParametersInternalRestClient.getMyExternalParameters(securityService.getHttpContext())
        ).thenReturn(myExternalParameter);

        IllegalArgumentException thrown = Assertions.assertThrows(IllegalArgumentException.class, () -> {
            externalParametersService.retrieveAccessContract();
        });

        Assertions.assertEquals("No external profile defined for access contract defined", thrown.getMessage());
    }

    @Test
    void shouldRetrieveTheRightAccessContractWhenDefined() throws VitamClientException, JsonProcessingException {
        ExternalParametersDto myExternalParameter = new ExternalParametersDto();
        ParameterDto parameterDto = new ParameterDto();
        parameterDto.setValue(SOME_ACCESS_CONTRACT);
        parameterDto.setKey(ExternalParametersService.PARAM_ACCESS_CONTRACT_NAME);
        myExternalParameter.setParameters(List.of(parameterDto));
        Mockito.when(
            externalParametersInternalRestClient.getMyExternalParameters(securityService.getHttpContext())
        ).thenReturn(myExternalParameter);

        final RequestResponseOK<AccessContractModel> response = new RequestResponseOK<>();
        response.setHttpCode(200);
        response.addResult(
            (AccessContractModel) new AccessContractModel()
                .setWritingPermission(true)
                .setIdentifier("contratTNR")
                .setName("contrat d acces")
                .setTenant(0)
        );
        Mockito.when(
            accessContractService.findAccessContractById(
                ArgumentMatchers.any(VitamContext.class),
                ArgumentMatchers.eq(SOME_ACCESS_CONTRACT)
            )
        ).thenReturn(response);

        AccessContractModel accessContractFound = externalParametersService.retrieveAccessContract();
        Assertions.assertEquals("contratTNR", accessContractFound.getIdentifier());
    }

    @Test
    void shouldRetrieveTheRightAccessContractFromContextWhenDefiend() {
        ExternalParametersDto myExternalParameter = new ExternalParametersDto();
        ParameterDto parameterDto = new ParameterDto();
        parameterDto.setValue(SOME_ACCESS_CONTRACT);
        parameterDto.setKey(ExternalParametersService.PARAM_ACCESS_CONTRACT_NAME);
        myExternalParameter.setParameters(List.of(parameterDto));

        Mockito.when(
            externalParametersInternalRestClient.getMyExternalParameters(securityService.getHttpContext())
        ).thenReturn(myExternalParameter);
        Mockito.when(securityService.getTenantIdentifier()).thenReturn(SOME_TENANT);
        VitamContext someContext = new VitamContext(SOME_TENANT).setAccessContract(SOME_ACCESS_CONTRACT);
        VitamContext context = externalParametersService.buildVitamContextFromExternalParam();
        Assertions.assertEquals(someContext, context);
    }
}
