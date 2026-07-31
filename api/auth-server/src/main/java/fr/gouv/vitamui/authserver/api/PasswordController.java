/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.authserver.api;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.gouv.vitamui.authserver.security.CustomerIdAuthenticationDetails;
import fr.gouv.vitamui.authserver.security.IamClient;
import fr.gouv.vitamui.authserver.security.VitamuiPrincipal;
import fr.gouv.vitamui.iam.common.dto.cas.PasswordPolicyDto;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Password self-service endpoints for the vanilla mini-SPA under {@code /change-password}. Wraps IAM
 * (re-authentication + update) so the vanilla JS never sees IAM directly.
 */
@RestController
@RequestMapping("/api/password")
@RequiredArgsConstructor
public class PasswordController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordController.class);

    private final IamClient iamClient;
    // Config changes require a service redeploy — a 5-minute TTL is plenty and shields IAM from
    // a hot-reload burst if every open tab happens to open the change/reset screen at once.
    private final Cache<String, PasswordPolicyDto> policyCache = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(5))
        .maximumSize(1)
        .build();

    /**
     * Returns the password policy in a shape the SPA can render as bullet points. Publicly readable
     * (the same rules are surfaced on the login screen if password reset is engaged) and does not
     * leak user data — so no auth check.
     */
    @GetMapping("/policy")
    public ResponseEntity<PasswordPolicyDto> policy() {
        PasswordPolicyDto policy = policyCache.get("policy", k -> iamClient.getPasswordPolicy());
        return ResponseEntity.ok(policy);
    }

    /**
     * Change the currently authenticated user's password. Re-authenticates the caller via IAM's
     * password login (proves knowledge of the current password) before applying the new one — this
     * prevents a session hijack from turning into a permanent takeover.
     */
    @PostMapping("/change")
    public ResponseEntity<Void> change(@RequestBody ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof VitamuiPrincipal principal)) {
            throw new AccessDeniedException("No authenticated user in context");
        }
        String email = principal.getUserDto().getEmail();
        String customerId = principal.getUserDto().getCustomerId();
        // Fallback if the customer id was carried in the authentication details rather than on the DTO.
        if (customerId == null && auth.getDetails() instanceof CustomerIdAuthenticationDetails d) {
            customerId = d.getCustomerId();
        }
        if (email == null || customerId == null) {
            throw new AccessDeniedException("Authentication is missing email or customerId");
        }

        // 1. Verify current password by attempting a login. IAM returns 401 on bad credentials and the
        //    client translates that into BadCredentialsClientException, which we map to 401 for the SPA.
        try {
            iamClient.login(email, request.getCurrentPassword(), customerId);
        } catch (IamClient.BadCredentialsClientException e) {
            LOGGER.info("Password change refused for {} — bad current password", email);
            throw new BadCredentialsException("Current password is incorrect");
        }

        // 2. Apply the new password. IAM enforces the policy + history check server-side; any error
        //    is propagated (400/409) as a downstream client error and surfaced to the SPA.
        iamClient.changePassword(email, customerId, request.getNewPassword());
        LOGGER.info("Password changed for userId={} email={}", principal.getUserDto().getId(), email);
        return ResponseEntity.noContent().build();
    }

    @Data
    public static class ChangePasswordRequest {

        @NotBlank
        private String currentPassword;

        @NotBlank
        private String newPassword;
    }
}
