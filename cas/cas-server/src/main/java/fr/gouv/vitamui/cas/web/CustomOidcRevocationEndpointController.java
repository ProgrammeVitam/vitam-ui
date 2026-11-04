package fr.gouv.vitamui.cas.web;

import fr.gouv.vitamui.cas.util.Utils;
import fr.gouv.vitamui.iam.openapiclient.CasApi;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.web.controllers.token.OidcRevocationEndpointController;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.util.function.FunctionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import static fr.gouv.vitamui.commons.api.CommonConstants.AUTHTOKEN_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_ATTRIBUTE;
import static fr.gouv.vitamui.commons.api.CommonConstants.SUPER_USER_CUSTOMER_ID_ATTRIBUTE;

/**
 * Custom : Revoke token for all services without checking clientId : Global
 * Logout
 */
@Slf4j
public class CustomOidcRevocationEndpointController extends OidcRevocationEndpointController {

    private final CasApi casApi;

    private final Utils utils;

    public CustomOidcRevocationEndpointController(
        final OidcConfigurationContext configurationContext,
        final CasApi casApi,
        final Utils utils
    ) {
        super(configurationContext);
        this.casApi = casApi;
        this.utils = utils;
    }

    protected ModelAndView generateRevocationResponse(
        final String token,
        final String clientId,
        final HttpServletResponse response
    ) throws Exception {
        final var registryToken = FunctionUtils.doAndHandle(() -> {
            final var state = getConfigurationContext().getTicketRegistry().getTicket(token, OAuth20Token.class);
            return state == null || state.isExpired() ? null : state;
        });
        if (registryToken == null) {
            LOGGER.error("Provided token [{}] has not been found in the ticket registry", token);
        } else if (isRefreshToken(registryToken) || isAccessToken(registryToken)) {
            // Custom : Don't check clientId to allow revoke token to all services (SSO)
            revokeIamToken(registryToken);
            if (isRefreshToken(registryToken)) {
                revokeToken((OAuth20RefreshToken) registryToken);
            } else {
                revokeToken(registryToken.getId());
            }
        } else {
            LOGGER.error("Provided token [{}] is either not a refresh token or not an access token", token);
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
        }

        final var mv = new ModelAndView(new MappingJackson2JsonView());
        mv.setStatus(HttpStatus.OK);
        return mv;
    }

    /**
     * The access token identifier is the IAM auth token, which authenticates the API calls on its own: it is revoked
     * in IAM as well, the same way a CAS logout does. IAM ignores a token it no longer knows.
     */
    private void revokeIamToken(final OAuth20Token registryToken) {
        final var attributes = registryToken.getAuthentication().getPrincipal().getAttributes();
        final String authToken = (String) utils.getAttributeValue(attributes, AUTHTOKEN_ATTRIBUTE);
        if (StringUtils.isBlank(authToken)) {
            LOGGER.debug("Token [{}] carries no IAM token", registryToken.getId());
            return;
        }
        casApi.logout(
            authToken,
            (String) utils.getAttributeValue(attributes, SUPER_USER_ATTRIBUTE),
            (String) utils.getAttributeValue(attributes, SUPER_USER_CUSTOMER_ID_ATTRIBUTE)
        );
    }
}
