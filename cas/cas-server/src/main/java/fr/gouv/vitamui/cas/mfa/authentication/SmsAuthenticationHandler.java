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
package fr.gouv.vitamui.cas.mfa.authentication;

import java.security.GeneralSecurityException;
import java.util.ArrayList;

import javax.security.auth.login.FailedLoginException;
import javax.servlet.http.HttpServletRequest;

import org.apereo.cas.authentication.AuthenticationHandlerExecutionResult;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.authentication.OneTimeTokenCredential;
import org.apereo.cas.authentication.handler.support.AbstractPreAndPostProcessingAuthenticationHandler;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalFactory;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.web.support.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.cas.util.VitamStatusCode;
import fr.gouv.vitamui.commons.api.CommonConstants;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;

/**
 * The authentication handler to validate SMS code.
 *
 *
 */
public class SmsAuthenticationHandler extends AbstractPreAndPostProcessingAuthenticationHandler {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(SmsAuthenticationHandler.class);

    @Autowired
    private Utils utils;

    @Value("${ip.header}")
    private String ipHeaderName;

    public SmsAuthenticationHandler(final String name, final ServicesManager servicesManager,
            final PrincipalFactory principalFactory, final Integer order) {
        super(name, servicesManager, principalFactory, order);
    }

    @Override
    protected AuthenticationHandlerExecutionResult doAuthentication(final Credential credential)
            throws GeneralSecurityException {
        final OneTimeTokenCredential smsCodeCredential = (OneTimeTokenCredential) credential;
        final String credentialCode = smsCodeCredential.getToken();

        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        final String generatedCode = (String) requestContext.getFlowScope().get(Constants.GENERATED_CODE);
        LOGGER.debug("credential code: {} vs generated code: {}", credentialCode, generatedCode);

        final Principal principal = WebUtils.getAuthentication(requestContext).getPrincipal();
        if (generatedCode.equals(credentialCode)) {
            log(VitamStatusCode.OK, principal, null);
            return createHandlerResult(smsCodeCredential, principal, new ArrayList<>());
        } else {
            final String errorMessage = "the provided SMS code does not match the generated one";
            log(VitamStatusCode.KO, principal, errorMessage);
            throw new FailedLoginException(errorMessage);
        }
    }

    protected void log(final VitamStatusCode status, final Principal principal, final String errorMessage) {

        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        String ip = null;
        if (requestContext != null) {
            final ExternalContext externalContext = requestContext.getExternalContext();
            if (externalContext != null) {
                ip = ((HttpServletRequest) externalContext.getNativeRequest()).getHeader(ipHeaderName);
            }
        }

        LOGGER.info(
                "OTP authentication / status: {} / (super)UserIdentifier: {} / email: {} / IP: {} / errorMessage: {}",
                status, utils.getAttributeValue(principal, CommonConstants.IDENTIFIER_ATTRIBUTE),
                utils.getAttributeValue(principal, CommonConstants.EMAIL_ATTRIBUTE), ip, errorMessage);
    }

    @Override
    public boolean supports(final Credential credential) {
        return OneTimeTokenCredential.class.isAssignableFrom(credential.getClass());
    }
}
