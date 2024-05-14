package fr.gouv.vitamui.cas.web;

import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.commons.api.CommonConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.CasProtocolConstants;
import org.apereo.cas.oidc.OidcConstants;
import org.apereo.cas.oidc.util.OidcRequestSupport;
import org.apereo.cas.support.oauth.web.OAuth20RequestParameterResolver;
import org.apereo.cas.support.oauth.web.response.OAuth20DefaultCasClientRedirectActionBuilder;
import org.apereo.cas.util.EncodingUtils;
import org.pac4j.cas.client.CasClient;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.exception.http.FoundAction;
import org.pac4j.core.exception.http.RedirectionAction;

import java.util.Optional;

/**
 * Propagates custom parameters from OIDC to CAS.
 */
@Slf4j
@RequiredArgsConstructor
public class CustomOidcCasClientRedirectActionBuilder extends OAuth20DefaultCasClientRedirectActionBuilder {

    private final OidcRequestSupport oidcRequestSupport;

    private final OAuth20RequestParameterResolver parameterResolver;

    @Override
    public Optional<RedirectionAction> build(final CasClient casClient, final WebContext context) {
        var renew = casClient.getConfiguration().isRenew();
        var gateway = casClient.getConfiguration().isGateway();

        val prompts = parameterResolver.resolveSupportedPromptValues(context);
        if (prompts.contains(OidcConstants.PROMPT_NONE)) {
            renew = false;
            gateway = true;
        } else if (
            prompts.contains(OidcConstants.PROMPT_LOGIN) ||
            oidcRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(context)
        ) {
            renew = true;
        }

        val action = internalBuild(casClient, context, renew, gateway);
        LOGGER.debug("Final redirect action is [{}]", action);
        return action;
    }

    protected Optional<RedirectionAction> internalBuild(
        final CasClient casClient,
        final WebContext context,
        final boolean renew,
        final boolean gateway
    ) {
        val username = context.getRequestParameter(Constants.LOGIN_USER_EMAIL_PARAM);
        val superUserEmail = context.getRequestParameter(Constants.LOGIN_SUPER_USER_EMAIL_PARAM);
        val superUserCustomerId = context.getRequestParameter(Constants.LOGIN_SUPER_USER_CUSTOMER_ID_PARAM);
        val surrogateEmail = context.getRequestParameter(Constants.LOGIN_SURROGATE_EMAIL_PARAM);
        val surrogateCustomerId = context.getRequestParameter(Constants.LOGIN_SURROGATE_CUSTOMER_ID_PARAM);

        boolean subrogationMode =
            superUserEmail.isPresent() &&
            superUserCustomerId.isPresent() &&
            surrogateEmail.isPresent() &&
            surrogateCustomerId.isPresent();

        val idp = context.getRequestParameter(CommonConstants.IDP_PARAMETER);

        val serviceUrl = casClient.computeFinalCallbackUrl(context);
        val casServerLoginUrl = casClient.getConfiguration().getLoginUrl();
        val redirectionUrl =
            casServerLoginUrl +
            (casServerLoginUrl.contains("?") ? "&" : "?") +
            CasProtocolConstants.PARAMETER_SERVICE +
            '=' +
            EncodingUtils.urlEncode(serviceUrl) +
            (renew ? '&' + CasProtocolConstants.PARAMETER_RENEW + "=true" : StringUtils.EMPTY) +
            (gateway ? '&' + CasProtocolConstants.PARAMETER_GATEWAY + "=true" : StringUtils.EMPTY) +
            // CUSTO:
            (subrogationMode
                    ? '&' +
                    Constants.LOGIN_SUPER_USER_EMAIL_PARAM +
                    '=' +
                    superUserEmail.get() +
                    '&' +
                    Constants.LOGIN_SUPER_USER_CUSTOMER_ID_PARAM +
                    '=' +
                    superUserCustomerId.get() +
                    '&' +
                    Constants.LOGIN_SURROGATE_EMAIL_PARAM +
                    '=' +
                    surrogateEmail.get() +
                    '&' +
                    Constants.LOGIN_SURROGATE_CUSTOMER_ID_PARAM +
                    '=' +
                    surrogateCustomerId.get()
                    : StringUtils.EMPTY) +
            (username.isPresent() ? '&' + Constants.LOGIN_USER_EMAIL_PARAM + '=' + username.get() : StringUtils.EMPTY) +
            (idp.isPresent() ? '&' + CommonConstants.IDP_PARAMETER + '=' + idp.get() : StringUtils.EMPTY);

        LOGGER.debug("Final redirect url is [{}]", redirectionUrl);
        return Optional.of(new FoundAction(redirectionUrl));
    }
}
