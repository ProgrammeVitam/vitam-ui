/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 * <p>
 * contact@programmevitam.fr
 * <p>
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 * <p>
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * <p>
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * <p>
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */

package fr.gouv.vitamui.iam.internal.server.provisioning.service;


import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.rest.client.configuration.RestClientConfiguration;
import fr.gouv.vitamui.iam.common.dto.ProvidedUserDto;
import fr.gouv.vitamui.iam.internal.server.provisioning.client.ProvisioningWebClient;
import fr.gouv.vitamui.iam.internal.server.provisioning.config.IdPProvisioningClientConfiguration;
import fr.gouv.vitamui.iam.internal.server.provisioning.config.ProvisioningClientConfiguration;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;

import fr.gouv.vitamui.commons.api.domain.AddressDto;
import org.junit.Assert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ProvisioningInternalServiceTest {

    @InjectMocks
    private ProvisioningInternalService service;

    @Mock
    private ProvisioningClientConfiguration provisioningClientConfigurationMock;

    @Mock
    private WebClient.Builder webClientMock;

    @Mock
    private InternalSecurityService securityServiceMock;


    @Nested
    class getProvisioningClientConfiguration {

        @Test
        void whenIdpIsUnknown_NotFoundExceptionIsThrown() {
            assertThrows(NotFoundException.class, () -> {
                service.getProvisioningClientConfiguration("unknowIdp");
            });
        }

        @Test
        void whenIdpIsKnown_thenIdpIsReturned() {
            // Prepare
            var idpConfiguration = new IdPProvisioningClientConfiguration();
            idpConfiguration.setIdpIdentifier("idp");
            Mockito.when(provisioningClientConfigurationMock.getIdentityProviders())
                .thenReturn(Arrays.asList(idpConfiguration));

            // Do
            IdPProvisioningClientConfiguration idpFound = service.getProvisioningClientConfiguration("idp");

            // Verify
            Assert.assertEquals(idpConfiguration, idpFound);
        }

    }

    @Test
    void buildWebClient_whenCall_thenOk() {
        // Prepare
        var idpConfiguration = new IdPProvisioningClientConfiguration();
        idpConfiguration.setClient(new RestClientConfiguration());
        Mockito.when(webClientMock.exchangeStrategies(ArgumentMatchers.any(ExchangeStrategies.class)))
            .thenReturn(webClientMock);

        // Do
        ProvisioningWebClient webClient = service.buildWebClient(idpConfiguration);

        // Verify
        Assert.assertNotNull(webClient);
    }

    @Test
    void getUserInformation_whenCall_thenUserIsReturned() {
        // Prepare
        var provisioningInternalServiceSpy = Mockito.spy(service);
        var provisioningWebClient = Mockito.mock(ProvisioningWebClient.class);
        var providedUserDtoStub = new ProvidedUserDto();
        providedUserDtoStub.setFirstname("youyou");

        Mockito.doReturn(new IdPProvisioningClientConfiguration()).when(provisioningInternalServiceSpy)
            .getProvisioningClientConfiguration(ArgumentMatchers.any());
        Mockito.doReturn(provisioningWebClient).when(provisioningInternalServiceSpy)
            .buildWebClient(ArgumentMatchers.any());
        Mockito.when(provisioningWebClient.getProvidedUser(ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
            .thenReturn(providedUserDtoStub);

        // Do
        ProvidedUserDto providedUserDto =
            provisioningInternalServiceSpy
                .getUserInformation("idp", "email@toto.com", null, null, null, null);

        // Verify
        Mockito.verify(provisioningWebClient, Mockito.times(1)).getProvidedUser(ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any());
        assertEquals(providedUserDtoStub, providedUserDto);
    }

    @Test
    void getUserInformation_whenAddressReturned_isTooLong() {
        var provisioningInternalServiceSpy = Mockito.spy(service);
        var provisioningWebClient = Mockito.mock(ProvisioningWebClient.class);
        var providedUserDtoStub = new ProvidedUserDto();
        providedUserDtoStub.setFirstname("youyou");
        AddressDto addressDto =  new AddressDto();
        addressDto.setStreet("57 Avenue de Grandes Armées");
        addressDto.setCity("Paris");
        providedUserDtoStub.setAddress(addressDto);
        ReflectionTestUtils.setField(provisioningInternalServiceSpy, "maxStreetLength", 20);

        Mockito.doReturn(new IdPProvisioningClientConfiguration()).when(provisioningInternalServiceSpy).getProvisioningClientConfiguration(ArgumentMatchers.any());
        Mockito.doReturn(provisioningWebClient).when(provisioningInternalServiceSpy).buildWebClient(ArgumentMatchers.any());
        Mockito.when(provisioningWebClient.getProvidedUser(ArgumentMatchers.any(),
            ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(providedUserDtoStub);

        ProvidedUserDto providedUserDto =
            provisioningInternalServiceSpy
                .getUserInformation("idp", "email@toto.com", null, null, null, null);


        assertEquals(providedUserDto.getAddress().getStreet(), "57 Avenue de Grandes");
    }
}
