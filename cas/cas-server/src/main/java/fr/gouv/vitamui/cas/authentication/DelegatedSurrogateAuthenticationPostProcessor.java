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
package fr.gouv.vitamui.cas.authentication;

import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import org.apereo.cas.audit.AuditableExecution;
import org.apereo.cas.authentication.*;
import org.apereo.cas.authentication.principal.ClientCredential;
import org.apereo.cas.authentication.surrogate.SurrogateAuthenticationService;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.pac4j.core.context.JEEContext;
import org.pac4j.core.context.session.SessionStore;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.webflow.execution.RequestContextHolder;

import lombok.val;

/**
 * Post-processor which also handles the surrogation in the authentication delegation.
 *
 *
 */
public class DelegatedSurrogateAuthenticationPostProcessor extends SurrogateAuthenticationPostProcessor {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(DelegatedSurrogateAuthenticationPostProcessor.class);

    private final SessionStore sessionStore;

    public DelegatedSurrogateAuthenticationPostProcessor(final SurrogateAuthenticationService surrogateAuthenticationService,
                                                         final ServicesManager servicesManager, final ApplicationEventPublisher applicationEventPublisher,
                                                         final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                                         final AuditableExecution surrogateEligibilityAuditableExecution,
                                                         final SessionStore sessionStore) {
        super(surrogateAuthenticationService, servicesManager, applicationEventPublisher, registeredServiceAccessStrategyEnforcer,
            surrogateEligibilityAuditableExecution);
        this.sessionStore = sessionStore;
    }

    @Override
    public void process(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) throws AuthenticationException {

        val credential = transaction.getPrimaryCredential().get();
        if (credential instanceof ClientCredential) {
            val clientCredential = (ClientCredential) credential;
            val requestContext = RequestContextHolder.getRequestContext();
            val request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            val response = WebUtils.getHttpServletResponseFromExternalWebflowContext(requestContext);
            val webContext = new JEEContext(request, response, sessionStore);
            val surrogateInSession = sessionStore.get(webContext, Constants.SURROGATE).orElse(null);
            if (surrogateInSession != null) {
                LOGGER.debug("surrogate: {} found after authentication delegation -> overriding credential", surrogateInSession);
                val newCredential = new SurrogateUsernamePasswordCredential();
                newCredential.setUsername(clientCredential.getUserProfile().getId());
                newCredential.setSurrogateUsername((String) surrogateInSession);
                WebUtils.putCredential(requestContext, newCredential);

                final AuthenticationTransaction newTransaction = DefaultAuthenticationTransaction.of(transaction.getService(), newCredential);
                super.process(builder, newTransaction);
            } else {
                return;
            }
        } else {
            super.process(builder, transaction);
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        final Class<? extends Credential> credentialClass = credential.getClass();
        return credentialClass.equals(SurrogateUsernamePasswordCredential.class) || credentialClass.equals(ClientCredential.class);
    }
}
