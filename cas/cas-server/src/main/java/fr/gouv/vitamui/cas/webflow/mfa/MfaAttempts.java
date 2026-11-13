/*
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2022)
 *
 * contact.vitam@culture.gouv.fr
 *
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "https://cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitamui.cas.webflow.mfa;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.github.benmanes.caffeine.cache.Ticker;

import java.time.Duration;

/**
 * Counts the MFA codes submitted per principal until one of them completes the authentication.
 *
 * <p>Every submitted code is counted before it is checked, so concurrent submissions cannot get more than
 * {@code maxAttempts} codes checked. Once the limit is exceeded, the principal is blocked until no code has been
 * counted for {@code retention}; the codes submitted while blocked are not counted, so they do not extend the block.
 *
 * <p>The count lives in the memory of this CAS node: with several nodes, each one applies the limit on its own.
 */
public class MfaAttempts {

    private final int maxAttempts;

    private final Cache<String, Integer> attempts;

    public MfaAttempts(final int maxAttempts, final Duration retention) {
        this(maxAttempts, retention, Ticker.systemTicker());
    }

    MfaAttempts(final int maxAttempts, final Duration retention, final Ticker ticker) {
        this.maxAttempts = maxAttempts;
        final long retentionNanos = retention.toNanos();
        this.attempts = Caffeine.newBuilder()
            .ticker(ticker)
            .expireAfter(
                new Expiry<String, Integer>() {
                    @Override
                    public long expireAfterCreate(final String principalId, final Integer count, final long now) {
                        return retentionNanos;
                    }

                    @Override
                    public long expireAfterUpdate(
                        final String principalId,
                        final Integer count,
                        final long now,
                        final long currentDuration
                    ) {
                        return isBlocked(count) ? currentDuration : retentionNanos;
                    }

                    @Override
                    public long expireAfterRead(
                        final String principalId,
                        final Integer count,
                        final long now,
                        final long currentDuration
                    ) {
                        return currentDuration;
                    }
                }
            )
            .build();
    }

    /**
     * Counts a submitted code.
     *
     * @return {@code false} when the principal has exceeded the limit: the code must then be refused unchecked
     */
    public boolean tryAttempt(final String principalId) {
        final int count = attempts
            .asMap()
            .compute(principalId, (id, previous) -> previous == null ? 1 : Math.min(previous + 1, maxAttempts + 1));
        return !isBlocked(count);
    }

    /** Forgets the codes counted for the principal, once one of them has completed the authentication. */
    public void reset(final String principalId) {
        attempts.invalidate(principalId);
    }

    private boolean isBlocked(final int count) {
        return count > maxAttempts;
    }
}
