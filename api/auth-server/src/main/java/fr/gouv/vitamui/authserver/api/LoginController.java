/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.api;

import fr.gouv.vitamui.authserver.security.CustomerIdAuthenticationDetails;
import fr.gouv.vitamui.authserver.security.SubrogationConstants;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.iam.common.dto.cas.HrdEntryDto;
import fr.gouv.vitamui.iam.common.dto.cas.SubrogationValidateRequestDto;
import fr.gouv.vitamui.iam.common.dto.cas.SubrogationValidateResponseDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import fr.gouv.vitamui.authserver.security.IamClient;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.FactorGrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

/**
 * JSON endpoints consumed by the Angular login SPA served under {@code /login/}.
 *
 * <p>Two-step flow:
 * <ol>
 *   <li>{@link #resolve(HrdRequest)}: email &rarr; (customerId, providerType) via IAM's mini HRD.</li>
 *   <li>{@link #authenticate(AuthRequest, HttpServletRequest, HttpServletResponse)}: email + password + customerId
 *       &rarr; authenticate via {@link org.springframework.security.authentication.AuthenticationManager}, persist the
 *       {@link SecurityContext} in the session, then return the URL of the originally requested
 *       {@code /oauth2/authorize} so the SPA can {@code window.location.assign(...)} it.</li>
 * </ol>
 *
 * <p>Phase 1 supports only the {@code N=1 customer, internal IdP} happy path. Multi-customer selection and
 * external IdP redirect are deferred to Phase 2.
 */
@RestController
@RequestMapping("/api/login")
@RequiredArgsConstructor
public class LoginController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoginController.class);

    private final IamClient iamClient;
    private final AuthenticationManager authenticationManager;
    // Shared repository — same bean the two SecurityFilterChain read from, so authentications persisted here
    // are visible to /oauth2/authorize on subsequent requests (SSO across clients).
    private final SecurityContextRepository securityContextRepository;

    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private final RequestCache requestCache = new HttpSessionRequestCache();

    @PostMapping("/resolve")
    public ResponseEntity<ResolveResponse> resolve(@RequestBody HrdRequest request) {
        List<HrdEntryDto> entries = iamClient.resolveHrd(request.getEmail());
        LOGGER.info("HRD resolve email={} → {} matching entries: {}", request.getEmail(), entries.size(), entries);
        if (entries.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (entries.size() == 1) {
            HrdEntryDto entry = entries.get(0);
            return ResponseEntity.ok(ResolveResponse.single(entry));
        }
        return ResponseEntity.ok(ResolveResponse.multi(entries));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthResponse> authenticate(
        @RequestBody AuthRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken.unauthenticated(
            request.getEmail(),
            request.getPassword()
        );
        token.setDetails(new CustomerIdAuthenticationDetails(request.getCustomerId()));

        Authentication authenticated = authenticationManager.authenticate(token);

        SecurityContext newContext = securityContextHolderStrategy.createEmptyContext();
        newContext.setAuthentication(authenticated);
        securityContextHolderStrategy.setContext(newContext);
        securityContextRepository.saveContext(newContext, httpRequest, httpResponse);

        SavedRequest saved = requestCache.getRequest(httpRequest, httpResponse);
        String redirectUrl = saved != null ? saved.getRedirectUrl() : "/";
        return ResponseEntity.ok(new AuthResponse(redirectUrl));
    }

    @GetMapping("/csrf")
    public ResponseEntity<Void> csrf() {
        // Endpoint used by the SPA to fetch the CSRF cookie before POSTing /authenticate.
        return ResponseEntity.noContent().build();
    }

    /**
     * Returns the current login context inferred from the {@link SavedRequest} query parameters. If the
     * original {@code /oauth2/authorize} carried subrogation parameters (added by the portal after the modal
     * accepted flow), we surface them so the SPA can render the dedicated subrogation screen.
     */
    @GetMapping("/context")
    public ResponseEntity<LoginContextResponse> context(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        SavedRequest saved = requestCache.getRequest(httpRequest, httpResponse);
        if (saved == null) {
            return ResponseEntity.ok(LoginContextResponse.standard());
        }
        String surrogateEmail = firstParam(saved, SubrogationConstants.PARAM_SURROGATE_EMAIL);
        String surrogateCustomerId = firstParam(saved, SubrogationConstants.PARAM_SURROGATE_CUSTOMER_ID);
        String superUserEmail = firstParam(saved, SubrogationConstants.PARAM_SUPER_USER_EMAIL);
        String superUserCustomerId = firstParam(saved, SubrogationConstants.PARAM_SUPER_USER_CUSTOMER_ID);
        if (
            surrogateEmail == null || surrogateCustomerId == null || superUserEmail == null || superUserCustomerId == null
        ) {
            return ResponseEntity.ok(LoginContextResponse.standard());
        }
        return ResponseEntity.ok(
            LoginContextResponse.subrogation(superUserEmail, superUserCustomerId, surrogateEmail, surrogateCustomerId)
        );
    }

    @PostMapping("/authenticate-subrogation")
    public ResponseEntity<AuthResponse> authenticateSubrogation(
        @RequestBody SubrogationAuthRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse httpResponse
    ) {
        SavedRequest saved = requestCache.getRequest(httpRequest, httpResponse);
        if (saved == null) {
            throw new BadCredentialsException("No saved OAuth2 request in session");
        }
        String superUserEmail = required(saved, SubrogationConstants.PARAM_SUPER_USER_EMAIL);
        String superUserCustomerId = required(saved, SubrogationConstants.PARAM_SUPER_USER_CUSTOMER_ID);
        String surrogateEmail = required(saved, SubrogationConstants.PARAM_SURROGATE_EMAIL);
        String surrogateCustomerId = required(saved, SubrogationConstants.PARAM_SURROGATE_CUSTOMER_ID);

        // 1. Validate super-user password via IAM (throws BadCredentialsException on failure).
        UsernamePasswordAuthenticationToken superToken = UsernamePasswordAuthenticationToken.unauthenticated(
            superUserEmail,
            request.getPassword()
        );
        superToken.setDetails(new CustomerIdAuthenticationDetails(superUserCustomerId));
        authenticationManager.authenticate(superToken);

        // 2. Validate the ACCEPTED subrogation between super-user and surrogate.
        SubrogationValidateResponseDto validation;
        try {
            validation = iamClient.validateSubrogation(
                new SubrogationValidateRequestDto(
                    superUserEmail,
                    superUserCustomerId,
                    surrogateEmail,
                    surrogateCustomerId
                )
            );
        } catch (IamClient.SubrogationNotValidException e) {
            LOGGER.warn("Subrogation validation refused for superUser={} surrogate={}", superUserEmail, surrogateEmail);
            throw new BadCredentialsException("Subrogation not valid", e);
        }

        // 3. Resolve the surrogate UserDto by re-using the getUser IAM call — we already have the id from
        //    validation, but the resource-server chain needs email/customerId to be coherent. We build a
        //    minimal UserDto here; the actual profiles will be re-fetched by the resource servers on /users/me.
        UserDto surrogate = new UserDto();
        surrogate.setId(validation.getSurrogateUserId());
        surrogate.setEmail(surrogateEmail);
        surrogate.setCustomerId(surrogateCustomerId);

        List<GrantedAuthority> authorities = List.of(
            new SimpleGrantedAuthority("ROLE_USER"),
            new SimpleGrantedAuthority(SubrogationConstants.SUBROGATED_AUTHORITY),
            FactorGrantedAuthority.fromAuthority(FactorGrantedAuthority.PASSWORD_AUTHORITY)
        );
        UsernamePasswordAuthenticationToken authenticated = new UsernamePasswordAuthenticationToken(
            new fr.gouv.vitamui.authserver.security.VitamuiPrincipal(surrogate),
            null,
            authorities
        );
        authenticated.setDetails(new CustomerIdAuthenticationDetails(surrogateCustomerId));

        SecurityContext newContext = securityContextHolderStrategy.createEmptyContext();
        newContext.setAuthentication(authenticated);
        securityContextHolderStrategy.setContext(newContext);
        securityContextRepository.saveContext(newContext, httpRequest, httpResponse);

        String redirectUrl = saved.getRedirectUrl();
        LOGGER.info(
            "Subrogated authentication established: superUser={} surrogate={} surrogateUserId={}",
            superUserEmail,
            surrogateEmail,
            validation.getSurrogateUserId()
        );
        return ResponseEntity.ok(new AuthResponse(redirectUrl));
    }

    private static String firstParam(SavedRequest saved, String name) {
        String[] values = saved.getParameterValues(name);
        return values != null && values.length > 0 ? values[0] : null;
    }

    private static String required(SavedRequest saved, String name) {
        String value = firstParam(saved, name);
        if (value == null || value.isBlank()) {
            throw new BadCredentialsException("Missing saved request parameter: " + name);
        }
        return value;
    }

    @Data
    public static class SubrogationAuthRequest {
        @NotBlank
        private String password;
    }

    @Data
    public static class LoginContextResponse {

        private final String mode;
        private final String superUserEmail;
        private final String superUserCustomerId;
        private final String surrogateEmail;
        private final String surrogateCustomerId;

        public static LoginContextResponse standard() {
            return new LoginContextResponse("standard", null, null, null, null);
        }

        public static LoginContextResponse subrogation(
            String superUserEmail,
            String superUserCustomerId,
            String surrogateEmail,
            String surrogateCustomerId
        ) {
            return new LoginContextResponse(
                "subrogation",
                superUserEmail,
                superUserCustomerId,
                surrogateEmail,
                surrogateCustomerId
            );
        }
    }

    @Data
    public static class HrdRequest {
        @NotBlank
        private String email;
    }

    @Data
    public static class ResolveResponse {

        private final boolean needsCustomerSelection;
        private final String customerId;
        private final String customerName;
        private final String identityProviderId;
        private final String identityProviderName;
        private final String providerType;
        private final String protocoleType;
        private final List<HrdEntryDto> entries;

        public static ResolveResponse single(HrdEntryDto e) {
            return new ResolveResponse(
                false,
                e.getCustomerId(),
                e.getCustomerName(),
                e.getIdentityProviderId(),
                e.getIdentityProviderName(),
                e.isInternal() ? "internal" : "external",
                e.getProtocoleType(),
                null
            );
        }

        public static ResolveResponse multi(List<HrdEntryDto> entries) {
            return new ResolveResponse(true, null, null, null, null, null, null, entries);
        }
    }

    @Data
    public static class AuthRequest {
        @NotBlank
        private String email;
        @NotBlank
        private String password;
        @NotBlank
        private String customerId;
    }

    @Data
    public static class AuthResponse {
        private final String redirectUrl;
    }
}
