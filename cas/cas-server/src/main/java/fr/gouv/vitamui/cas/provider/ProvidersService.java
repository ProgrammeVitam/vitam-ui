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
package fr.gouv.vitamui.cas.provider;

import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.common.ProviderEmbeddedOptions;
import fr.gouv.vitamui.iam.common.utils.Pac4jClientBuilder;
import fr.gouv.vitamui.iam.external.client.IdentityProviderExternalRestClient;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.IndirectClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Retrieve all the identity providers from the IAM API.
 *
 *
 */
@Getter
@RequiredArgsConstructor
public class ProvidersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProvidersService.class);

    private List<IdentityProviderDto> providers = new ArrayList<>();

    private final Clients clients;

    private final IdentityProviderExternalRestClient identityProviderExternalRestClient;

    private final Pac4jClientBuilder pac4jClientBuilder;

    private final Utils utils;

    @PostConstruct
    public void afterPropertiesSet() {
        loadData();
        Assert.notNull(providers, "No provider found");
    }

    // every minute, reload the data
    @Scheduled(initialDelay = 60 * 1000, fixedRate = 60 * 1000)
    public void reloadData() {
        try {
            loadData();
        } catch (final RuntimeException e) {
            LOGGER.warn("Reloading failed", e);
        }
    }

    protected void loadData() {
        final List<IdentityProviderDto> temporaryProviders = identityProviderExternalRestClient.getAll(
            utils.buildContext(null),
            Optional.empty(),
            Optional.of(ProviderEmbeddedOptions.KEYSTORE + "," + ProviderEmbeddedOptions.IDPMETADATA)
        );
        // sort by identifier. This is needed in order to take the internal provider first.
        temporaryProviders.sort(Comparator.comparing(IdentityProviderDto::getIdentifier));
        LOGGER.debug(
            "Reloaded {} providers: {}",
            temporaryProviders.size(),
            StringUtils.join(
                temporaryProviders.stream().map(IdentityProviderDto::getId).collect(Collectors.toList()),
                ", "
            )
        );

        final List<Client> newClients = new ArrayList<>();
        final List<IdentityProviderDto> newProviders = new ArrayList<>();
        temporaryProviders.forEach(p -> {
            final IndirectClient client = pac4jClientBuilder.buildClient(p).orElse(null);
            if (client != null) {
                newClients.add(client);
            }
            newProviders.add(new Pac4jClientIdentityProviderDto(p, client));
        });
        clients.setClients(newClients);
        providers = newProviders;
    }
}
