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

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import fr.gouv.vitamui.commons.api.domain.ProvidedUserDto;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.rest.client.BaseWebClientFactory;
import fr.gouv.vitamui.commons.rest.client.configuration.IdPProvisioningClientConfiguration;
import fr.gouv.vitamui.commons.rest.client.configuration.ProvisioningClientConfiguration;

/**
 * Customer provisioning service.
 *
 *
 */
@Service
public class ProvisioningInternalService {

    private final WebClient.Builder webClientBuilder;

    private final ProvisioningClientConfiguration provisioningClientConfiguration;

    public ProvisioningInternalService(final WebClient.Builder webClientBuilder, final ProvisioningClientConfiguration provisioningClientConfiguration) {
        this.webClientBuilder = webClientBuilder;
        this.provisioningClientConfiguration = provisioningClientConfiguration;
    }

    public ProvidedUserDto getUserInformation(final String email, final String idp, Optional<String> groupId, Optional<String> unit, final Optional<String> technicalUserId) {
        final IdPProvisioningClientConfiguration idpProvisioningClient =
                provisioningClientConfiguration.getIdentityProviders().stream().filter(provisioningClient -> provisioningClient.getIdpIdentifier().equals(idp))
                        .findFirst().orElseThrow(() -> new NotFoundException(String.format("Provisioning client configuration not found for IdP : {}", idp)));
        final BaseWebClientFactory clientFactory = new BaseWebClientFactory(idpProvisioningClient.getClient(), webClientBuilder);

        return clientFactory.getWebClient().get()
                .uri(getUri(email, groupId, unit, technicalUserId, idpProvisioningClient))
                .retrieve().bodyToMono(ProvidedUserDto.class).block();
    }

    @NotNull
    private String getUri(final String email, final Optional<String> groupId, final Optional<String> unit, final Optional<String> technicalUserId,
            final IdPProvisioningClientConfiguration idpProvisioningClient) {
        final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(idpProvisioningClient.getUri());
        uriBuilder.queryParam("email", email);
        if (groupId.isPresent()) {
            uriBuilder.queryParam("groupId", groupId);
        }
        if (unit.isPresent()) {
            uriBuilder.queryParam("unit", unit.get());
        }
        if (technicalUserId.isPresent()) {
            uriBuilder.queryParam("technicalUserId", technicalUserId.get());
        }

        return uriBuilder.toUriString();
    }

}
