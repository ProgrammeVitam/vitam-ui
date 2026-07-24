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
import org.springframework.security.saml2.provider.service.authentication.Saml2AuthenticatedPrincipal;
import org.springframework.security.saml2.provider.service.authentication.Saml2Authentication;
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
        ExternalIdentity identity = extractIdentity(authentication);
        if (identity == null) {
            LOGGER.warn(
                "Unexpected authentication type after federation callback: {}",
                authentication != null ? authentication.getClass().getName() : "null"
            );
            response.sendRedirect("/login?error=federation");
            return;
        }

        // SAML principal doesn't reliably expose the registrationId (null in Spring Security 7's
        // Saml2AssertionAuthentication); fall back to the trailing path segment of the callback URI
        // — Spring's Saml2WebSsoAuthenticationFilter matches exactly /login/saml2/sso/{id}.
        String registrationId = identity.registrationId();
        if (registrationId == null) {
            String uri = request.getRequestURI();
            int lastSlash = uri.lastIndexOf('/');
            if (lastSlash >= 0 && lastSlash < uri.length() - 1) {
                registrationId = uri.substring(lastSlash + 1);
            }
        }
        LOGGER.info(
            "Federated callback: auth={} registrationId={} name={}",
            authentication.getClass().getSimpleName(),
            registrationId,
            identity.name()
        );

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
        String emailClaim = identity.attribute(mailAttribute);
        String email = emailClaim != null ? emailClaim : identity.name();

        String identifierAttribute = idp.getIdentifierAttribute();
        String userIdentifier = null;
        if (identifierAttribute != null) {
            String claim = identity.attribute(identifierAttribute);
            if (claim != null) userIdentifier = claim;
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
                String givenName = firstNonNull(identity.attribute("given_name"), identity.attribute("givenName"));
                String familyName = firstNonNull(identity.attribute("family_name"), identity.attribute("familyName"));
                String subject = identity.attribute("sub");
                if (subject == null) subject = userIdentifier;
                if (subject == null) subject = identity.name();

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

        boolean isSaml = authentication instanceof Saml2Authentication;
        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            FactorGrantedAuthority.fromAuthority(
                isSaml
                    ? FactorGrantedAuthority.SAML_RESPONSE_AUTHORITY
                    : FactorGrantedAuthority.AUTHORIZATION_CODE_AUTHORITY
            )
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

    private static String firstNonNull(String a, String b) {
        return a != null ? a : b;
    }

    /**
     * Normalises the caller-provided {@link Authentication} into a protocol-agnostic view — either
     * OIDC ({@link OAuth2AuthenticationToken}) or SAML ({@link Saml2Authentication}) — so the rest of
     * this handler doesn't have to branch on the auth type.
     */
    private static ExternalIdentity extractIdentity(Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken oauth) {
            OAuth2User user = oauth.getPrincipal();
            return new ExternalIdentity(
                oauth.getAuthorizedClientRegistrationId(),
                name -> {
                    Object v = user.getAttribute(name);
                    return v != null ? String.valueOf(v) : null;
                },
                user.getName()
            );
        }
        if (authentication instanceof Saml2Authentication saml
            && saml.getPrincipal() instanceof Saml2AuthenticatedPrincipal samlPrincipal) {
            return new ExternalIdentity(
                samlPrincipal.getRelyingPartyRegistrationId(),
                name -> {
                    Object v = samlPrincipal.getFirstAttribute(name);
                    return v != null ? String.valueOf(v) : null;
                },
                samlPrincipal.getName()
            );
        }
        return null;
    }

    private record ExternalIdentity(
        String registrationId,
        java.util.function.Function<String, String> attributeFn,
        String name
    ) {
        String attribute(String name) {
            return attributeFn.apply(name);
        }
    }
}
