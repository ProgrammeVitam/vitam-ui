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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import javax.servlet.http.HttpServletRequest;

/**
 * Post-processor which also handles the surrogation in the authentication delegation.
 *
 *
 */
public class DelegatedSurrogateAuthenticationPostProcessor extends SurrogateAuthenticationPostProcessor {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(DelegatedSurrogateAuthenticationPostProcessor.class);

    public DelegatedSurrogateAuthenticationPostProcessor(final SurrogateAuthenticationService surrogateAuthenticationService,
                                                         final ServicesManager servicesManager, final ApplicationEventPublisher applicationEventPublisher,
                                                         final AuditableExecution registeredServiceAccessStrategyEnforcer,
                                                         final AuditableExecution surrogateEligibilityAuditableExecution) {
        super(surrogateAuthenticationService, servicesManager, applicationEventPublisher, registeredServiceAccessStrategyEnforcer,
            surrogateEligibilityAuditableExecution, null);
    }

    @Override
    public void process(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) throws AuthenticationException {

        final Credential credential = transaction.getPrimaryCredential().get();
        if (credential instanceof ClientCredential) {
            final ClientCredential clientCredential = (ClientCredential) credential;
            final RequestContext requestContext = RequestContextHolder.getRequestContext();
            final HttpServletRequest request = WebUtils.getHttpServletRequestFromExternalWebflowContext(requestContext);
            final String surrogate = (String) request.getAttribute(Constants.SURROGATE);
            if (surrogate != null) {
                LOGGER.debug("surrogate: {} found after authentication delegation -> overriding credential", surrogate);
                final SurrogateUsernamePasswordCredential newCredential = new SurrogateUsernamePasswordCredential();
                newCredential.setUsername(clientCredential.getUserProfile().getId());
                newCredential.setSurrogateUsername(surrogate);
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
