package fr.gouv.vitamui.cas.web;

import fr.gouv.vitamui.cas.util.Constants;
import fr.gouv.vitamui.commons.api.CommonConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
 * Propage les paramètres personnalisés depuis OIDC vers CAS.
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

        final var prompts = parameterResolver.resolveSupportedPromptValues(context);
        if (prompts.contains(OidcConstants.PROMPT_NONE)) {
            renew = false;
            gateway = true;
        } else if (
            prompts.contains(OidcConstants.PROMPT_LOGIN) ||
            oidcRequestSupport.isCasAuthenticationOldForMaxAgeAuthorizationRequest(context)
        ) {
            renew = true;
        }

        final var action = internalBuild(casClient, context, renew, gateway);
        LOGGER.debug("Final redirect action is [{}]", action);
        return action;
    }

    protected Optional<RedirectionAction> internalBuild(
        final CasClient casClient,
        final WebContext context,
        final boolean renew,
        final boolean gateway
    ) {
        final var username = context.getRequestParameter(Constants.LOGIN_USER_EMAIL_PARAM);
        final var superUserEmail = context.getRequestParameter(Constants.LOGIN_SUPER_USER_EMAIL_PARAM);
        final var superUserCustomerId = context.getRequestParameter(Constants.LOGIN_SUPER_USER_CUSTOMER_ID_PARAM);
        final var surrogateEmail = context.getRequestParameter(Constants.LOGIN_SURROGATE_EMAIL_PARAM);
        final var surrogateCustomerId = context.getRequestParameter(Constants.LOGIN_SURROGATE_CUSTOMER_ID_PARAM);

        boolean subrogationMode =
            superUserEmail.isPresent() &&
            superUserCustomerId.isPresent() &&
            surrogateEmail.isPresent() &&
            surrogateCustomerId.isPresent();

        final var idp = context.getRequestParameter(CommonConstants.IDP_PARAMETER);

        final var serviceUrl = casClient.computeFinalCallbackUrl(context);
        final var casServerLoginUrl = casClient.getConfiguration().getLoginUrl();
        // CUSTO : les indices de subrogation et de connexion sont transmis, encodés en URL (un e-mail peut contenir un '+').
        final var subrogationParams = subrogationMode
            ? param(Constants.LOGIN_SUPER_USER_EMAIL_PARAM, superUserEmail.get()) +
            param(Constants.LOGIN_SUPER_USER_CUSTOMER_ID_PARAM, superUserCustomerId.get()) +
            param(Constants.LOGIN_SURROGATE_EMAIL_PARAM, surrogateEmail.get()) +
            param(Constants.LOGIN_SURROGATE_CUSTOMER_ID_PARAM, surrogateCustomerId.get())
            : StringUtils.EMPTY;
        final var redirectionUrl =
            casServerLoginUrl +
            (casServerLoginUrl.contains("?") ? "&" : "?") +
            CasProtocolConstants.PARAMETER_SERVICE +
            '=' +
            EncodingUtils.urlEncode(serviceUrl) +
            (renew ? '&' + CasProtocolConstants.PARAMETER_RENEW + "=true" : StringUtils.EMPTY) +
            (gateway ? '&' + CasProtocolConstants.PARAMETER_GATEWAY + "=true" : StringUtils.EMPTY) +
            subrogationParams +
            username.map(value -> param(Constants.LOGIN_USER_EMAIL_PARAM, value)).orElse(StringUtils.EMPTY) +
            idp.map(value -> param(CommonConstants.IDP_PARAMETER, value)).orElse(StringUtils.EMPTY);

        LOGGER.debug("Final redirect url is [{}]", redirectionUrl);
        return Optional.of(new FoundAction(redirectionUrl));
    }

    private static String param(final String name, final String value) {
        return '&' + name + '=' + EncodingUtils.urlEncode(value);
    }
}
