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
package fr.gouv.vitamui.cas.ticket;

import fr.gouv.vitamui.commons.api.CommonConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apereo.cas.authentication.Authentication;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.Service;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.ticket.ExpirationPolicyBuilder;
import org.apereo.cas.ticket.UniqueTicketIdGenerator;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.accesstoken.OAuth20DefaultAccessTokenFactory;
import org.apereo.cas.ticket.tracking.TicketTrackingPolicy;
import org.apereo.cas.token.JwtBuilder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Objects;

/**
 * Specific factory for access tokens using the auth token as identifier.
 *
 *
 */
@Getter
@Slf4j
public class CustomOAuth20DefaultAccessTokenFactory extends OAuth20DefaultAccessTokenFactory {

    public CustomOAuth20DefaultAccessTokenFactory(
        final UniqueTicketIdGenerator accessTokenIdGenerator,
        final ExpirationPolicyBuilder<OAuth20AccessToken> expirationPolicyBuilder,
        final JwtBuilder jwtBuilder,
        final ServicesManager servicesManager,
        final TicketTrackingPolicy descendantTicketsTrackingPolicy
    ) {
        super(
            accessTokenIdGenerator,
            expirationPolicyBuilder,
            jwtBuilder,
            servicesManager,
            descendantTicketsTrackingPolicy
        );
    }

    @Override
    protected String generateAccessTokenId(final Service service, final Authentication authentication)
        throws Throwable {
        final var request =
            ((ServletRequestAttributes) Objects.requireNonNull(
                    RequestContextHolder.getRequestAttributes(),
                    "No request context available — generateAccessTokenId must be called within an HTTP request scope"
                )).getRequest();

        final Principal principal = authentication.getPrincipal();
        final List<Object> values = principal.getAttributes().get(CommonConstants.AUTHTOKEN_ATTRIBUTE);
        if (values != null && !values.isEmpty()) {
            final var authToken = (String) values.getFirst();
            LOGGER.debug("authToken found in principal attributes for [{}].", principal.getId());
            request.setAttribute(CommonConstants.AUTHTOKEN_ATTRIBUTE, authToken);
            return authToken;
        }

        final var authTokenFromRequest = (String) request.getAttribute(CommonConstants.AUTHTOKEN_ATTRIBUTE);
        if (StringUtils.isNotBlank(authTokenFromRequest)) {
            LOGGER.debug("authToken found in request attributes for [{}].", principal.getId());
            return authTokenFromRequest;
        }

        LOGGER.debug("No authToken found for [{}], falling back to default token generation.", principal.getId());
        return super.generateAccessTokenId(service, authentication);
    }
}
