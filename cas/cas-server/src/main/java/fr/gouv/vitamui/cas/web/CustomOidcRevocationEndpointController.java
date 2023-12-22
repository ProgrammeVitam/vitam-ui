package fr.gouv.vitamui.cas.web;

import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import lombok.val;
import org.apereo.cas.oidc.OidcConfigurationContext;
import org.apereo.cas.oidc.web.controllers.token.OidcRevocationEndpointController;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.ticket.OAuth20Token;
import org.apereo.cas.ticket.accesstoken.OAuth20AccessToken;
import org.apereo.cas.ticket.refreshtoken.OAuth20RefreshToken;
import org.apereo.cas.util.function.FunctionUtils;
import org.jooq.lambda.Unchecked;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletResponse;

/**
 * Custom : Revoke token for all services without checking clientId : Global Logout
 */
public class CustomOidcRevocationEndpointController extends OidcRevocationEndpointController {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(CustomOidcRevocationEndpointController.class);

    public CustomOidcRevocationEndpointController(final OidcConfigurationContext configurationContext) {
        super(configurationContext);
    }

    protected ModelAndView generateRevocationResponse(final String token,
                                                      final String clientId,
                                                      final HttpServletResponse response) throws Exception {
        val registryToken = FunctionUtils.doAndHandle(() -> {
            val state = getConfigurationContext().getTicketRegistry().getTicket(token, OAuth20Token.class);
            return state == null || state.isExpired() ? null : state;
        });
        if (registryToken == null) {
            LOGGER.error("Provided token [{}] has not been found in the ticket registry", token);
        } else if (isRefreshToken(registryToken) || isAccessToken(registryToken)) {

            /*
            Custom : Don't check clientId to allow revoke token to all services (SSO)
            if (!StringUtils.equals(clientId, registryToken.getClientId())) {
                LOGGER.warn("Provided token [{}] has not been issued for the service [{}]", token, clientId);
                return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
            }
            */

            if (isRefreshToken(registryToken)) {
                revokeToken((OAuth20RefreshToken) registryToken);
            } else {
                revokeToken(registryToken.getId());
            }
        } else {
            LOGGER.error("Provided token [{}] is either not a refresh token or not an access token", token);
            return OAuth20Utils.writeError(response, OAuth20Constants.INVALID_REQUEST);
        }

        val mv = new ModelAndView(new MappingJackson2JsonView());
        mv.setStatus(HttpStatus.OK);
        return mv;
    }

    private void revokeToken(final OAuth20RefreshToken token) throws Exception {
        this.revokeToken(token.getId());
        token.getAccessTokens().forEach(Unchecked.consumer(this::revokeToken));
    }

    private boolean isRefreshToken(final OAuth20Token token) {
        return token instanceof OAuth20RefreshToken;
    }

    private boolean isAccessToken(final OAuth20Token token) {
        return token instanceof OAuth20AccessToken;
    }

}
