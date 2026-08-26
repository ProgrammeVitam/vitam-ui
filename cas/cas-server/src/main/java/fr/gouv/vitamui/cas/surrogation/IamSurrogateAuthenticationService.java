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
 */
package fr.gouv.vitamui.cas.surrogation;

import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.commons.api.exception.VitamUIException;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.authentication.surrogate.BaseSurrogateAuthenticationService;
import org.apereo.cas.services.ServicesManager;
import org.springframework.util.Assert;
import org.springframework.webflow.execution.RequestContextHolder;

import java.util.Collection;
import java.util.Optional;

/**
 * Specific surrogate service based on the IAM API.
 */
@Slf4j
public class IamSurrogateAuthenticationService extends BaseSurrogateAuthenticationService {

    private final CasApi casApi;

    public IamSurrogateAuthenticationService(final CasApi casApi, final ServicesManager servicesManager) {
        super(servicesManager);
        this.casApi = casApi;
    }

    @Override
    public boolean canImpersonateInternal(
        final String surrogate,
        final Principal principal,
        final Optional<Service> service
    ) {
        final var requestContext = RequestContextHolder.getRequestContext();
        final var flowScope = requestContext.getFlowScope();

        String surrogateEmail = (String) flowScope.get(Constants.FLOW_SURROGATE_EMAIL);
        String surrogateCustomerId = (String) flowScope.get(Constants.FLOW_SURROGATE_CUSTOMER_ID);
        String superUserEmail = (String) flowScope.get(Constants.FLOW_LOGIN_EMAIL);
        String superUserCustomerId = (String) flowScope.get(Constants.FLOW_LOGIN_CUSTOMER_ID);

        LOGGER.debug(
            "Checking impersonation of '{}' (customerId '{}') by super admin '{}' (customerId '{}')",
            surrogateEmail,
            surrogateCustomerId,
            superUserEmail,
            superUserCustomerId
        );

        Assert.isTrue(
            surrogateEmail.equals(surrogate),
            String.format("Invalid surrogate. Expected '%s', got: '%s'", surrogateEmail, surrogate)
        );

        final var id = principal.getId();
        boolean canAuthenticate = false;
        try {
            canAuthenticate = Boolean.TRUE.equals(
                casApi.canImpersonate(id, superUserEmail, superUserCustomerId, surrogateEmail, surrogateCustomerId)
            );
        } catch (final VitamUIException e) {
            LOGGER.error("Cannot check the impersonation permission for: {}", id, e);
        }
        LOGGER.debug("{} can surrogate: {}? -> {}", id, surrogate, canAuthenticate);
        return canAuthenticate;
    }

    @Override
    public boolean isWildcardedAccount(final String surrogate, final Principal principal) {
        return false;
    }

    @Override
    public Collection<String> getImpersonationAccounts(String username) {
        throw new UnsupportedOperationException("Not allowed to choose the surrogate");
    }
}
