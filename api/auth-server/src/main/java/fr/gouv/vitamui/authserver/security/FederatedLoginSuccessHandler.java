/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.security;

import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.iam.common.dto.IdentityProviderDto;
import fr.gouv.vitamui.iam.common.dto.cas.JitProvisionRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

/**
 * Handles the successful OAuth2/OIDC callback from an external identity provider (via
 * {@code /login/oauth2/code/{registrationId}}). Replaces the transient {@link OAuth2AuthenticationToken}
 * that Spring puts in the {@code SecurityContext} with the vitam-ui-flavoured
 * {@link UsernamePasswordAuthenticationToken} carrying a {@link VitamuiPrincipal}. The rest of the SAS
 * chain (token generator, jwt customizer, OIDC discovery) then behaves identically to a local login.
 *
 * <p>The email seen by IAM is picked from the claim named by {@code IdentityProvider.mailAttribute}
 * (defaults to {@code email}), and the technical identifier is picked from
 * {@code IdentityProvider.identifierAttribute} (falls back to the {@code sub} claim). Both fields drive
 * the auto-provisioning done by IAM's {@code /cas/users/provisioning}.
 *
 * <p>After the auth is replaced, we redirect to the {@link SavedRequest} URL (typically the original
 * {@code /oauth2/authorize?…}) so SAS can continue emitting the {@code TOK-<UUID>} to the vitam-ui client
 * that initiated the flow.
 */
public class FederatedLoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FederatedLoginSuccessHandler.class);
    private static final String DEFAULT_MAIL_ATTRIBUTE = "email";

    private final IamClient iamClient;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolderStrategy =
        SecurityContextHolder.getContextHolderStrategy();
    private final RequestCache requestCache = new HttpSessionRequestCache();

    public FederatedLoginSuccessHandler(IamClient iamClient, SecurityContextRepository securityContextRepository) {
        this.iamClient = iamClient;
        this.securityContextRepository = securityContextRepository;
    }

    @Override
    public void onAuthenticationSuccess(
        HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication
    ) throws IOException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthAuth)) {
            LOGGER.warn(
                "Unexpected authentication type after OAuth2 callback: {}",
                authentication != null ? authentication.getClass().getName() : "null"
            );
            response.sendRedirect("/login?error=federation");
            return;
        }

        String registrationId = oauthAuth.getAuthorizedClientRegistrationId();
        OAuth2User oauthUser = oauthAuth.getPrincipal();

        IdentityProviderDto idp;
        try {
            idp = iamClient.getIdentityProvider(registrationId);
        } catch (IamClient.IdentityProviderNotFoundException e) {
            LOGGER.warn("IdP {} not found in IAM after callback — aborting", registrationId);
            response.sendRedirect("/login?error=idp_missing");
            return;
        }
        if (idp == null) {
            LOGGER.warn("IdP {} came back null after callback", registrationId);
            response.sendRedirect("/login?error=idp_missing");
            return;
        }

        String mailAttribute = idp.getMailAttribute() != null ? idp.getMailAttribute() : DEFAULT_MAIL_ATTRIBUTE;
        Object emailClaim = oauthUser.getAttribute(mailAttribute);
        String email = emailClaim != null ? String.valueOf(emailClaim) : oauthUser.getName();

        String identifierAttribute = idp.getIdentifierAttribute();
        String userIdentifier = null;
        if (identifierAttribute != null) {
            Object claim = oauthUser.getAttribute(identifierAttribute);
            if (claim != null) userIdentifier = String.valueOf(claim);
        }

        LOGGER.info(
            "IdP {} loaded: autoProvisioningEnabled={} defaultGroupId={} mailAttribute={} identifierAttribute={}",
            idp.getId(),
            idp.isAutoProvisioningEnabled(),
            idp.getDefaultGroupId(),
            idp.getMailAttribute(),
            idp.getIdentifierAttribute()
        );

        UserDto user = iamClient.resolveExternalUser(email, idp.getCustomerId(), idp.getId(), userIdentifier);
        if (user == null || user.getId() == null) {
            if (idp.isAutoProvisioningEnabled()) {
                LOGGER.info(
                    "User email={} customer={} unknown — attempting JIT provisioning via IdP {}",
                    email,
                    idp.getCustomerId(),
                    idp.getId()
                );
                String givenName = stringAttribute(oauthUser, "given_name");
                String familyName = stringAttribute(oauthUser, "family_name");
                String subject = stringAttribute(oauthUser, "sub");
                if (subject == null) subject = userIdentifier;
                if (subject == null) subject = oauthUser.getName();

                try {
                    user = iamClient.jitProvisionUser(
                        new JitProvisionRequestDto(email, idp.getCustomerId(), idp.getId(), subject, givenName, familyName)
                    );
                } catch (Exception e) {
                    LOGGER.warn(
                        "JIT provisioning failed for email={} customer={} idp={} : {}",
                        email,
                        idp.getCustomerId(),
                        idp.getId(),
                        e.getMessage()
                    );
                    response.sendRedirect("/login?error=jit_failed");
                    return;
                }
            } else {
                LOGGER.warn(
                    "IAM returned no user for email={} customer={} idp={} — user not registered and auto-provisioning is off",
                    email,
                    idp.getCustomerId(),
                    idp.getId()
                );
                response.sendRedirect("/login?error=user_not_registered");
                return;
            }
        }

        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            FactorGrantedAuthority.fromAuthority(FactorGrantedAuthority.SAML_RESPONSE_AUTHORITY.equals(
                idp.getProtocoleType()
            )
                ? FactorGrantedAuthority.SAML_RESPONSE_AUTHORITY
                : FactorGrantedAuthority.AUTHORIZATION_CODE_AUTHORITY)
        );
        UsernamePasswordAuthenticationToken newAuth = new UsernamePasswordAuthenticationToken(
            new VitamuiPrincipal(user),
            null,
            authorities
        );
        newAuth.setDetails(new CustomerIdAuthenticationDetails(idp.getCustomerId()));

        SecurityContext newContext = securityContextHolderStrategy.createEmptyContext();
        newContext.setAuthentication(newAuth);
        securityContextHolderStrategy.setContext(newContext);
        securityContextRepository.saveContext(newContext, request, response);

        LOGGER.info(
            "Federated authentication established for userId={} email={} via IdP {} ({})",
            user.getId(),
            email,
            idp.getName(),
            idp.getId()
        );

        SavedRequest saved = requestCache.getRequest(request, response);
        String redirectUrl = saved != null ? saved.getRedirectUrl() : "/";
        response.sendRedirect(redirectUrl);
    }

    private static String stringAttribute(OAuth2User user, String key) {
        Object v = user.getAttribute(key);
        return v != null ? String.valueOf(v) : null;
    }
}
