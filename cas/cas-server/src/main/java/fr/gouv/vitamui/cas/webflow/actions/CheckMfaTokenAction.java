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
package fr.gouv.vitamui.cas.webflow.actions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorAuthenticationTicketFactory;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCredential;
import org.apereo.cas.ticket.TransientSessionTicket;
import org.apereo.cas.ticket.registry.TicketRegistry;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.action.AbstractAction;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Check the MFA token.
 */
@RequiredArgsConstructor
@Slf4j
public class CheckMfaTokenAction extends AbstractAction {

    private final TicketRegistry ticketRegistry;

    @Override
    protected Event doExecute(final RequestContext requestContext) {
        val credential = WebUtils.getCredential(requestContext);
        val tokenCredential = (CasSimpleMultifactorTokenCredential) credential;
        val token = CasSimpleMultifactorAuthenticationTicketFactory.PREFIX + "-" + tokenCredential.getToken();
        LOGGER.debug("Checking token: {}", token);
        WebUtils.putCredential(requestContext, new CasSimpleMultifactorTokenCredential(token));

        val acct = this.ticketRegistry.getTicket(token, TransientSessionTicket.class);
        if (acct != null) {
            val creationTime = acct.getCreationTime();
            val now_less_one_minute = ZonedDateTime.now().minus(60, ChronoUnit.SECONDS);
            // considered expired after 60 seconds
            if (creationTime.isBefore(now_less_one_minute)) {
                return error();
            }
        }
        return success();
    }
}
