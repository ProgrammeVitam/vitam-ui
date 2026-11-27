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
package fr.gouv.vitamui.cas.webflow.mfa.actions;

import fr.gouv.vitamui.cas.webflow.mfa.MfaAttempts;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationConstants;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.mfa.simple.ticket.CasSimpleMultifactorAuthenticationTicket;
import org.apereo.cas.ticket.InvalidTicketException;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * Check the MFA token.
 *
 * <p>Every submitted code is counted in {@link MfaAttempts} before being checked, including the codes that pass this
 * check but fail the CAS authentication afterwards; the count is only reset once a code has completed the
 * authentication ({@link ResetMfaAttemptsAction}). Once too many codes were submitted, every code is refused with the
 * {@value #TRANSITION_ID_TOO_MANY_ATTEMPTS} event.
 */
@Slf4j
public class CheckMfaTokenAction extends AbstractAction {

    public static final String TRANSITION_ID_TOO_MANY_ATTEMPTS = "tooManyAttempts";

    private static final Duration TOKEN_VALIDITY = Duration.ofSeconds(60);

    private final TicketRegistry ticketRegistry;

    private final MfaAttempts mfaAttempts;

    public CheckMfaTokenAction(final TicketRegistry ticketRegistry, final MfaAttempts mfaAttempts) {
        this.ticketRegistry = ticketRegistry;
        this.mfaAttempts = mfaAttempts;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final var authentication = WebUtils.getAuthentication(requestContext);
        if (authentication == null) {
            LOGGER.warn("No primary authentication in the flow: the MFA token is refused");
            return error();
        }
        final var principalId = authentication.getPrincipal().getId();
        if (!mfaAttempts.tryAttempt(principalId)) {
            LOGGER.warn("Too many MFA tokens submitted for [{}]: the token is refused", principalId);
            return result(TRANSITION_ID_TOO_MANY_ATTEMPTS);
        }

        var credential = WebUtils.getCredential(requestContext);
        var tokenCredential = (CasSimpleMultifactorTokenCredential) credential;
        var token = CasSimpleMultifactorAuthenticationTicket.PREFIX + "-" + tokenCredential.getToken();
        LOGGER.debug("Checking token: {}", token);
        WebUtils.putCredential(requestContext, new CasSimpleMultifactorTokenCredential(token));

        // A token whose ticket is missing, not in the registry, older than the validity window or issued to another
        // principal is refused.
        try {
            var acct = this.ticketRegistry.getTicket(token, CasSimpleMultifactorAuthenticationTicket.class);
            if (acct == null || acct.getCreationTime().isBefore(ZonedDateTime.now().minus(TOKEN_VALIDITY))) {
                return error();
            }
            if (!isIssuedTo(acct, principalId)) {
                LOGGER.warn("MFA token submitted for [{}] was issued to another principal: it is refused", principalId);
                return error();
            }
        } catch (final InvalidTicketException e) {
            LOGGER.debug("MFA token {} is invalid: {}", token, e.getMessage());
            return error();
        }
        return success();
    }

    private static boolean isIssuedTo(final CasSimpleMultifactorAuthenticationTicket ticket, final String principalId) {
        final var properties = ticket.getProperties();
        return (
            properties != null &&
            properties.get(CasSimpleMultifactorAuthenticationConstants.PROPERTY_PRINCIPAL) instanceof
                final Principal ticketPrincipal &&
            principalId.equals(ticketPrincipal.getId())
        );
    }
}
