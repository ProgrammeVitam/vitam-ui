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

package fr.gouv.vitamui.iam.internal.server.provisioning.client;

import fr.gouv.vitamui.commons.rest.client.InternalHttpContext;
import fr.gouv.vitamui.iam.common.dto.ProvidedUserDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProvisioningWebClientTest {

    @InjectMocks
    private ProvisioningWebClient provisioningWebClient;

    @Mock
    private WebClient webClientMock;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpecMock;

    @Mock
    private WebClient.ResponseSpec responseSpecMock;

    @Mock
    private InternalHttpContext httpContextMock;

    @Test
    void getProvidedUser_whenCalled_thenProvidedUserIsReturned() {
        // Prepare
        final var email = "email@toto.com";
        ProvidedUserDto providedUserDtoStub = new ProvidedUserDto();
        providedUserDtoStub.setEmail(email);

        mockWebClientResponse(providedUserDtoStub);
        ArgumentCaptor<URI> URICaptor = ArgumentCaptor.forClass(URI.class);

        // Do
        provisioningWebClient.getProvidedUser(httpContextMock, email, null, null, null, null);

        // Verify
        verify(requestHeadersUriSpecMock, times(1)).uri(URICaptor.capture());
        URI uri = URICaptor.getValue();
        assertThat(uri.getQuery()).contains(email);
    }

    private void mockWebClientResponse(final ProvidedUserDto providedUserDtoStub) {
        Mono monoMock = Mockito.mock(Mono.class);

        when(webClientMock.get()).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.uri(ArgumentMatchers.any(URI.class))).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.headers(ArgumentMatchers.any())).thenReturn(requestHeadersUriSpecMock);
        when(requestHeadersUriSpecMock.retrieve()).thenReturn(responseSpecMock);
        when(responseSpecMock.onStatus(ArgumentMatchers.any(), ArgumentMatchers.any())).thenReturn(responseSpecMock);
        when(responseSpecMock.bodyToMono(ProvidedUserDto.class)).thenReturn(monoMock);
        when(monoMock.block()).thenReturn(providedUserDtoStub);
    }
}
