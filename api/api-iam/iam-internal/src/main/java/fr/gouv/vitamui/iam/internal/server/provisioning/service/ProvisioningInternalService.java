/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2019-2020)
 * and the signatories of the "VITAM - Accord du Contributeur" agreement.
 *
 * contact@programmevitam.fr
 *
 * This software is a computer program whose purpose is to implement
 * implement a digital archiving front-office system for the secure and
 * efficient high volumetry VITAM solution.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
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
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */

package fr.gouv.vitamui.iam.internal.server.provisioning.service;

import fr.gouv.vitamui.commons.api.domain.AddressDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;

import fr.gouv.vitamui.commons.api.domain.AddressDto;
import fr.gouv.vitamui.commons.api.exception.NotFoundException;
import fr.gouv.vitamui.commons.rest.client.BaseWebClientFactory;
import fr.gouv.vitamui.iam.common.dto.ProvidedUserDto;
import fr.gouv.vitamui.iam.internal.server.provisioning.client.ProvisioningWebClient;
import fr.gouv.vitamui.iam.internal.server.provisioning.config.IdPProvisioningClientConfiguration;
import fr.gouv.vitamui.iam.internal.server.provisioning.config.ProvisioningClientConfiguration;
import fr.gouv.vitamui.iam.security.service.InternalSecurityService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.validation.constraints.NotNull;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import javax.validation.constraints.NotNull;

/**
 * Customer provisioning service.
 *
 *
 */
@Service
public class ProvisioningInternalService {

    private final WebClient.Builder webClientBuilder;

    private final ProvisioningClientConfiguration provisioningClientConfiguration;

    private final InternalSecurityService securityService;

    @Value("${address.max-street-length}")
    @NotNull
    private int maxStreetLength;

    public ProvisioningInternalService(final WebClient.Builder webClientBuilder, final ProvisioningClientConfiguration provisioningClientConfiguration,
        final InternalSecurityService securityService) {
        this.webClientBuilder = webClientBuilder;
        this.provisioningClientConfiguration = provisioningClientConfiguration;
        this.securityService = securityService;
    }

    public ProvidedUserDto getUserInformation(final String idp, final String email, final String groupId, final String unit, final String userIdentifier, final String customerId) {
        final IdPProvisioningClientConfiguration idpProvisioningClient = getProvisioningClientConfiguration(idp);

        final var webClient = buildWebClient(idpProvisioningClient);

        final ProvidedUserDto providedUser = webClient.getProvidedUser(securityService.getHttpContext(), email, groupId, unit, userIdentifier, customerId);

        if (Objects.isNull(providedUser)) {
            throw new NotFoundException(String.format("No user returned by provisioning with email %s, technicalUserId %s, idp %s", email, userIdentifier, idp));
        }

        final AddressDto address = providedUser.getAddress();
        if(address != null){
            final var shortStreetAddress = StringUtils.substring(address.getStreet(), 0, maxStreetLength);
            address.setStreet(shortStreetAddress);
            providedUser.setAddress(address);
        }
        return providedUser;
    }

    /**
     *
     * @param idp
     * @return
     */
    protected IdPProvisioningClientConfiguration getProvisioningClientConfiguration(final String idp) {
        return provisioningClientConfiguration.getIdentityProviders()
            .stream()
            .filter(provisioningClient -> provisioningClient.getIdpIdentifier().equalsIgnoreCase(idp))
            .findFirst().orElseThrow(() -> new NotFoundException(String.format("Provisioning client configuration not found for IdP : %S", idp)));
    }

    /**
     * Method for build webClient
     * @param idpProvisioningClient
     * @return
     */
    protected ProvisioningWebClient buildWebClient(final IdPProvisioningClientConfiguration idpProvisioningClient) {
        final BaseWebClientFactory clientFactory = new BaseWebClientFactory(idpProvisioningClient.getClient(), null , webClientBuilder,
            idpProvisioningClient.getUri());

        return new ProvisioningWebClient(clientFactory.getWebClient(), idpProvisioningClient.getUri());
    }
}
