/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */

package fr.gouv.vitamui.authserver.api;

import fr.gouv.vitamui.authserver.security.CustomerIdAuthenticationDetails;
import fr.gouv.vitamui.iam.common.dto.cas.HrdEntryDto;
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
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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

    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final RequestCache requestCache = new HttpSessionRequestCache();

    @PostMapping("/resolve")
    public ResponseEntity<?> resolve(@RequestBody HrdRequest request) {
        List<HrdEntryDto> entries = iamClient.resolveHrd(request.getEmail());
        LOGGER.info("HRD resolve email={} → {} matching entries: {}", request.getEmail(), entries.size(), entries);
        if (entries.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (entries.size() > 1) {
            LOGGER.warn(
                "HRD resolve email={} matched multiple entries — POC Phase 1 only supports N=1. Entries: {}",
                request.getEmail(),
                entries
            );
            return ResponseEntity.status(409).body(entries);
        }
        HrdEntryDto entry = entries.get(0);
        String providerType = entry.isInternal() ? "internal" : "external";
        return ResponseEntity.ok(new ResolveResponse(entry.getCustomerId(), entry.getIdentityProviderId(), providerType));
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

    @Data
    public static class HrdRequest {
        @NotBlank
        private String email;
    }

    @Data
    public static class ResolveResponse {
        private final String customerId;
        private final String identityProviderId;
        private final String providerType;
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
