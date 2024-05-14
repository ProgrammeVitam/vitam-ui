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

import fr.gouv.vitamui.cas.util.Utils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apereo.cas.bucket4j.consumer.BucketConsumer;
import org.apereo.cas.configuration.model.support.mfa.simple.CasSimpleMultifactorAuthenticationProperties;
import org.apereo.cas.mfa.simple.CasSimpleMultifactorTokenCommunicationStrategy;
import org.apereo.cas.mfa.simple.validation.CasSimpleMultifactorAuthenticationService;
import org.apereo.cas.mfa.simple.web.flow.CasSimpleMultifactorSendTokenAction;
import org.apereo.cas.notifications.CommunicationsManager;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.webflow.execution.Event;
import org.springframework.webflow.execution.RequestContext;

import static org.apache.commons.text.StringEscapeUtils.escapeHtml4;

/**
 * The custom action to send SMS for the MFA simple token.
 */
@Slf4j
public class CustomSendTokenAction extends CasSimpleMultifactorSendTokenAction {

    private static final String MESSAGE_MFA_TOKEN_SENT = "cas.mfa.simple.label.tokensent";

    private final Utils utils;

    public CustomSendTokenAction(
        final CommunicationsManager communicationsManager,
        final CasSimpleMultifactorAuthenticationService multifactorAuthenticationService,
        final CasSimpleMultifactorAuthenticationProperties properties,
        final CasSimpleMultifactorTokenCommunicationStrategy tokenCommunicationStrategy,
        final BucketConsumer bucketConsumer,
        final Utils utils
    ) {
        super(
            communicationsManager,
            multifactorAuthenticationService,
            properties,
            tokenCommunicationStrategy,
            bucketConsumer
        );
        this.utils = utils;
    }

    @Override
    protected Event doExecute(final RequestContext requestContext) throws Exception {
        val authentication = WebUtils.getInProgressAuthentication();
        val principal = resolvePrincipal(authentication.getPrincipal());

        // check for a principal attribute and redirect to a custom page when missing
        val principalAttributes = principal.getAttributes();
        val mobile = (String) utils.getAttributeValue(principalAttributes, "mobile");
        if (mobile == null) {
            requestContext.getFlowScope().put("firstname", utils.getAttributeValue(principalAttributes, "firstname"));
            return getEventFactorySupport().event(this, "missingPhone");
        }

        // remove token
        WebUtils.removeSimpleMultifactorAuthenticationToken(requestContext);

        val event = super.doExecute(requestContext);

        // add the obfuscated phone to the webflow in case of success
        if (CasWebflowConstants.TRANSITION_ID_SUCCESS.equals(event.getId())) {
            requestContext.getFlowScope().put("mobile", escapeHtml4(obfuscateMobile(mobile)));
        }

        return event;
    }

    private String obfuscateMobile(final String mobile) {
        String m = mobile.replaceFirst("\\+33", "0");
        m = m.substring(0, 2) + " XX XX XX " + m.substring(m.length() - 2);
        return m;
    }
}
