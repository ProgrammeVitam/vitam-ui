/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 * contact.vitam@culture.gouv.fr
 * This software is governed by the CeCILL 2.1 license.
 */
package fr.gouv.vitamui.authserver.security;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * In-memory rate limiter for the public password endpoints (welcome + forgot-password). Guards two
 * dimensions:
 *
 * <ul>
 *   <li><b>Per IP</b>: shrinks the useful blast radius of a single actor scripting the endpoint.</li>
 *   <li><b>Per email destinataire</b>: caps how many emails a real inbox can receive per hour,
 *       even from many different IPs — a well-distributed attack against one victim.</li>
 * </ul>
 *
 * <p>Both dimensions must have available tokens for a request to proceed. Buckets are stored in a
 * Caffeine cache with 1 h eviction — plenty for the sliding-window semantics without leaking memory
 * on a long-running process. Not fit for a multi-instance deployment (each replica gets its own
 * counters); when SAS scales horizontally, replace with a shared Redis-backed Bucket4j or move rate
 * limiting to the ingress (nginx / API gateway).
 */
@Component
public class PasswordEndpointRateLimiter {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordEndpointRateLimiter.class);

    // Tuned for a human filling a form, not a bot: an ip that fires 10 requests in a minute is
    // almost certainly abusing us; an email that receives 3 mails in an hour is our upper bound.
    private static final Bandwidth PER_IP_LIMIT = Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1)));
    private static final Bandwidth PER_EMAIL_LIMIT = Bandwidth.classic(3, Refill.intervally(3, Duration.ofHours(1)));

    private final Cache<String, Bucket> ipBuckets = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofHours(1))
        .maximumSize(10_000)
        .build();
    private final Cache<String, Bucket> emailBuckets = Caffeine.newBuilder()
        .expireAfterAccess(Duration.ofHours(1))
        .maximumSize(10_000)
        .build();

    /**
     * Consumes one token from both the IP bucket and the email bucket. Returns a description of the
     * first refusal (with the retry-after delay) when either dimension is exhausted, or empty when
     * the request is allowed.
     */
    public java.util.Optional<Denial> tryAcquire(String clientIp, String email) {
        Bucket ipBucket = ipBuckets.get(clientIp != null ? clientIp : "unknown", k -> Bucket.builder().addLimit(PER_IP_LIMIT).build());
        ConsumptionProbe ipProbe = ipBucket.tryConsumeAndReturnRemaining(1);
        if (!ipProbe.isConsumed()) {
            long retryAfterSeconds = Math.max(1L, ipProbe.getNanosToWaitForRefill() / 1_000_000_000L);
            LOGGER.warn("Rate-limit: IP {} exhausted its bucket, retry-after {}s", clientIp, retryAfterSeconds);
            return java.util.Optional.of(new Denial("ip", retryAfterSeconds));
        }

        if (email != null && !email.isBlank()) {
            String key = email.toLowerCase(java.util.Locale.ROOT);
            Bucket emailBucket = emailBuckets.get(key, k -> Bucket.builder().addLimit(PER_EMAIL_LIMIT).build());
            ConsumptionProbe emailProbe = emailBucket.tryConsumeAndReturnRemaining(1);
            if (!emailProbe.isConsumed()) {
                long retryAfterSeconds = Math.max(1L, emailProbe.getNanosToWaitForRefill() / 1_000_000_000L);
                LOGGER.warn("Rate-limit: email {} exhausted its bucket, retry-after {}s", email, retryAfterSeconds);
                return java.util.Optional.of(new Denial("email", retryAfterSeconds));
            }
        }
        return java.util.Optional.empty();
    }

    /** Which dimension refused, plus the {@code Retry-After} value in seconds. */
    public record Denial(String dimension, long retryAfterSeconds) {}
}
