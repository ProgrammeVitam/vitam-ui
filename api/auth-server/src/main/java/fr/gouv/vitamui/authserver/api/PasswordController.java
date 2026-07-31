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
import fr.gouv.vitamui.authserver.security.PasswordEndpointRateLimiter;
import fr.gouv.vitamui.authserver.security.PasswordResetMailer;
import fr.gouv.vitamui.authserver.security.PasswordResetService;
import fr.gouv.vitamui.authserver.security.PasswordResetTokenDocument;
import fr.gouv.vitamui.authserver.security.VitamuiPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import fr.gouv.vitamui.authserver.config.AuthServerProperties;
import fr.gouv.vitamui.commons.api.domain.UserDto;
import fr.gouv.vitamui.commons.api.enums.UserStatusEnum;
import fr.gouv.vitamui.commons.api.enums.UserTypeEnum;
import fr.gouv.vitamui.iam.common.dto.cas.PasswordPolicyDto;
import jakarta.mail.MessagingException;
import jakarta.validation.constraints.NotBlank;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
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
    private final PasswordResetService passwordResetService;
    private final PasswordResetMailer passwordResetMailer;
    private final AuthServerProperties properties;
    private final PasswordEndpointRateLimiter rateLimiter;
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
     *
     * <p>The {@code customerId} on the principal is trusted only as a hint: HRD-based login can
     * false-positive when several IdP patterns overlap on the same email domain, so we resolve the
     * authoritative (email, customerId) via {@code getUsersByEmail} and cross-check that the
     * principal's customer, when present, is one of the matches.
     */
    @PostMapping("/change")
    public ResponseEntity<Void> change(@RequestBody ChangePasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof VitamuiPrincipal principal)) {
            throw new AccessDeniedException("No authenticated user in context");
        }
        String email = principal.getUserDto().getEmail();
        if (email == null || email.isBlank()) {
            throw new AccessDeniedException("Authentication is missing email");
        }
        String principalCustomerIdCandidate = principal.getUserDto().getCustomerId();
        if (principalCustomerIdCandidate == null && auth.getDetails() instanceof CustomerIdAuthenticationDetails d) {
            principalCustomerIdCandidate = d.getCustomerId();
        }
        final String principalCustomerId = principalCustomerIdCandidate;

        // Resolve the actual account(s) for this email, independent of what HRD said at login time.
        List<UserDto> candidates = iamClient.getUsersByEmail(email).stream()
            .filter(u -> UserTypeEnum.NOMINATIVE.equals(u.getType()))
            .filter(u -> UserStatusEnum.ENABLED.equals(u.getStatus()))
            .toList();
        if (candidates.isEmpty()) {
            LOGGER.warn("Password change refused for authenticated email={} — no nominative enabled user in IAM", email);
            throw new AccessDeniedException("No matching account");
        }
        // Prefer the principal's customerId when it names a real match; otherwise the sole match; if
        // several candidates disagree with the principal, we refuse rather than change the wrong one.
        UserDto target = candidates.stream()
            .filter(u -> u.getCustomerId().equals(principalCustomerId))
            .findFirst()
            .orElseGet(() -> candidates.size() == 1 ? candidates.get(0) : null);
        if (target == null) {
            LOGGER.warn(
                "Password change refused for email={} — {} candidate customers, none matches principal customerId={}",
                email,
                candidates.size(),
                principalCustomerId
            );
            throw new AccessDeniedException("Ambiguous account");
        }

        LOGGER.info(
            "Password change attempt: email={} principalCustomerId={} resolvedCustomerId={} candidates={}",
            email,
            principalCustomerId,
            target.getCustomerId(),
            candidates.size()
        );

        // 1. Verify current password by attempting a login. IAM returns 401 on bad credentials and the
        //    client translates that into BadCredentialsClientException, which we map to 401 for the SPA.
        try {
            iamClient.login(email, request.getCurrentPassword(), target.getCustomerId());
        } catch (IamClient.BadCredentialsClientException e) {
            LOGGER.info("Password change refused for {} — bad current password", email);
            throw new BadCredentialsException("Current password is incorrect");
        }

        // 2. Apply the new password. IAM enforces the policy + history check server-side; any error
        //    is propagated (400/409) as a downstream client error and surfaced to the SPA.
        iamClient.changePassword(email, target.getCustomerId(), request.getNewPassword());
        LOGGER.info("Password changed for userId={} email={} customer={}", target.getId(), email, target.getCustomerId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Kick off a password reset for a given email. Always responds 200 with an opaque body — the SPA
     * doesn't learn whether the address exists or maps to multiple customers. Resolution is based on
     * actual user existence (via {@code getUsersByEmail}) rather than the HRD IdP-pattern lookup,
     * which can false-positive across customers when several tenants share an email domain in their
     * IdP patterns. Cases:
     *
     * <ul>
     *   <li>No nominative enabled user → silent (anti-enumeration).</li>
     *   <li>Exactly 1 nominative enabled user → issue a nonce and mail the link.</li>
     *   <li>Multiple nominative enabled users across customers → silent (POC: disambiguation not
     *       wired). Logged so ops can spot the case.</li>
     * </ul>
     */
    @PostMapping("/reset/request")
    public ResponseEntity<RequestResetResponse> requestReset(
        @RequestBody RequestResetRequest request,
        HttpServletRequest httpRequest
    ) {
        String email = request.getEmail();
        var denial = rateLimiter.tryAcquire(clientIp(httpRequest), email);
        if (denial.isPresent()) {
            return tooManyRequests(denial.get());
        }
        List<UserDto> candidates = iamClient.getUsersByEmail(email).stream()
            .filter(u -> UserTypeEnum.NOMINATIVE.equals(u.getType()))
            .filter(u -> UserStatusEnum.ENABLED.equals(u.getStatus()))
            .toList();
        if (candidates.isEmpty()) {
            LOGGER.info("Reset requested for email={} — no matching nominative enabled user, silent 200", email);
            return ResponseEntity.ok(RequestResetResponse.opaque());
        }
        if (candidates.size() > 1) {
            LOGGER.warn(
                "Reset requested for email={} matching {} users across customers — silent 200 (multi-org disambiguation not wired)",
                email,
                candidates.size()
            );
            return ResponseEntity.ok(RequestResetResponse.opaque());
        }
        UserDto user = candidates.get(0);

        String nonce = passwordResetService.issue(user.getEmail(), user.getCustomerId());
        String link = properties.getPasswordReset().getBaseUrl() + "/reset-password?token=" + nonce;
        try {
            passwordResetMailer.send(user.getEmail(), link, passwordResetService.getTtl().toMinutes());
        } catch (MessagingException e) {
            // The row is in Mongo but the email couldn't be sent — surface 500 so ops notice. The
            // nonce will still be cleaned up by TTL, no leak.
            LOGGER.error("Failed to send reset email to {}: {}", user.getEmail(), e.getMessage(), e);
            throw new RuntimeException("Failed to send reset email", e);
        }
        return ResponseEntity.ok(RequestResetResponse.opaque());
    }

    /**
     * Consume a reset nonce and apply the new password. The nonce proves control of the mailbox
     * associated with the account — no additional password check is needed here. Errors are mapped
     * to specific status codes so the SPA can show a targeted message.
     */
    @PostMapping("/reset")
    public ResponseEntity<Void> reset(@RequestBody ResetRequest request) {
        Optional<PasswordResetTokenDocument> resolved = passwordResetService.consume(request.getToken());
        if (resolved.isEmpty()) {
            LOGGER.info("Password reset refused — token invalid or expired");
            throw new BadCredentialsException("Reset token is invalid or expired");
        }
        PasswordResetTokenDocument doc = resolved.get();
        iamClient.changePassword(doc.getEmail(), doc.getCustomerId(), request.getNewPassword());
        LOGGER.info("Password reset via email token for email={} customer={}", doc.getEmail(), doc.getCustomerId());
        return ResponseEntity.noContent().build();
    }

    /**
     * Best-effort client IP. Trusts {@code X-Forwarded-For} when present (SAS in staging/prod runs
     * behind a reverse proxy) — the ingress must strip client-supplied values or an attacker could
     * spoof and bypass the per-IP bucket. Falls back to the socket remote address.
     */
    private static String clientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            int comma = xff.indexOf(',');
            return (comma > 0 ? xff.substring(0, comma) : xff).trim();
        }
        return request.getRemoteAddr();
    }

    private static ResponseEntity<RequestResetResponse> tooManyRequests(PasswordEndpointRateLimiter.Denial denial) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.TOO_MANY_REQUESTS)
            .header("Retry-After", String.valueOf(denial.retryAfterSeconds()))
            .body(RequestResetResponse.opaque());
    }

    @Data
    public static class ChangePasswordRequest {

        @NotBlank
        private String currentPassword;

        @NotBlank
        private String newPassword;
    }

    @Data
    public static class RequestResetRequest {

        @NotBlank
        private String email;
    }

    /**
     * Called by IAM right after {@code UserService.create} to hand a new user a link where they can
     * pick their initial password. Public endpoint (no auth) — anti-enumeration is enforced by the
     * always-opaque 200 body; the only reachable side-effect is delivering a welcome email to the
     * given address. Rate-limiting the endpoint is tracked as Phase 3 debt.
     */
    @PostMapping("/first-connection")
    public ResponseEntity<RequestResetResponse> firstConnection(
        @RequestBody FirstConnectionRequest request,
        HttpServletRequest httpRequest
    ) {
        String email = request.getEmail();
        String customerId = request.getCustomerId();
        var denial = rateLimiter.tryAcquire(clientIp(httpRequest), email);
        if (denial.isPresent()) {
            return tooManyRequests(denial.get());
        }
        long ttlHours = properties.getPasswordReset().getFirstConnectionTtlHours();
        String nonce = passwordResetService.issueWithTtl(email, customerId, java.time.Duration.ofHours(ttlHours));
        String link = properties.getPasswordReset().getBaseUrl() + "/reset-password?token=" + nonce;
        try {
            passwordResetMailer.sendWelcome(email, request.getFirstname(), link, ttlHours);
        } catch (MessagingException e) {
            LOGGER.error("Failed to send welcome email to {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Failed to send welcome email", e);
        }
        return ResponseEntity.ok(RequestResetResponse.opaque());
    }

    @Data
    public static class ResetRequest {

        @NotBlank
        private String token;

        @NotBlank
        private String newPassword;
    }

    @Data
    public static class FirstConnectionRequest {

        @NotBlank
        private String email;

        @NotBlank
        private String customerId;

        // Firstname is used to personalise the greeting — safe to be null/blank, we fall back to a
        // generic "Bonjour,".
        private String firstname;

        private String lastname;
        private String language;
    }

    @Data
    public static class RequestResetResponse {

        // Opaque field — same value regardless of what happened, to keep the endpoint non-enumerating.
        private final String message;

        public static RequestResetResponse opaque() {
            return new RequestResetResponse(
                "Si un compte existe pour cette adresse et est éligible à la réinitialisation par email, un lien a été envoyé."
            );
        }
    }
}
