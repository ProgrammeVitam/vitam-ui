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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
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
 * <p>Wrong codes are counted per principal: once {@code maxFailedAttempts} is reached, every code is refused until no
 * wrong code has been submitted for {@code failedAttemptsRetention}. The count lives on the server, so resubmitting a
 * previous flow state or asking for a new code does not reset it; only a valid code does.
 */
@Slf4j
public class CheckMfaTokenAction extends AbstractAction {

    private static final Duration TOKEN_VALIDITY = Duration.ofSeconds(60);

    private final TicketRegistry ticketRegistry;

    private final int maxFailedAttempts;

    private final Cache<String, Integer> failedAttempts;

    public CheckMfaTokenAction(
        final TicketRegistry ticketRegistry,
        final int maxFailedAttempts,
        final Duration failedAttemptsRetention
    ) {
        this.ticketRegistry = ticketRegistry;
        this.maxFailedAttempts = maxFailedAttempts;
        this.failedAttempts = Caffeine.newBuilder().expireAfterWrite(failedAttemptsRetention).build();
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        final var authentication = WebUtils.getAuthentication(requestContext);
        if (authentication == null) {
            LOGGER.warn("No primary authentication in the flow: the MFA token is refused");
            return error();
        }
        final var principalId = authentication.getPrincipal().getId();
        if (failedAttempts.asMap().getOrDefault(principalId, 0) >= maxFailedAttempts) {
            LOGGER.warn("Too many wrong MFA tokens for [{}]: the token is refused", principalId);
            return failure(principalId);
        }

        var credential = WebUtils.getCredential(requestContext);
        var tokenCredential = (CasSimpleMultifactorTokenCredential) credential;
        var token = CasSimpleMultifactorAuthenticationTicket.PREFIX + "-" + tokenCredential.getToken();
        LOGGER.debug("Checking token: {}", token);
        WebUtils.putCredential(requestContext, new CasSimpleMultifactorTokenCredential(token));

        // A token whose ticket is missing, not in the registry, or older than the validity window is refused.
        try {
            var acct = this.ticketRegistry.getTicket(token, CasSimpleMultifactorAuthenticationTicket.class);
            if (acct == null || acct.getCreationTime().isBefore(ZonedDateTime.now().minus(TOKEN_VALIDITY))) {
                return failure(principalId);
            }
        } catch (final InvalidTicketException e) {
            LOGGER.debug("MFA token {} is invalid: {}", token, e.getMessage());
            return failure(principalId);
        }
        failedAttempts.invalidate(principalId);
        return success();
    }

    private Event failure(final String principalId) {
        failedAttempts.asMap().merge(principalId, 1, Integer::sum);
        return error();
    }
}
