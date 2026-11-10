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

import org.junit.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link MfaAttempts}.
 */
public class MfaAttemptsTest {

    private static final int MAX_ATTEMPTS = 3;

    private static final Duration RETENTION = Duration.ofMinutes(15);

    private final AtomicLong now = new AtomicLong();

    private final MfaAttempts mfaAttempts = new MfaAttempts(MAX_ATTEMPTS, RETENTION, now::get);

    @Test
    public void shouldRefuseTheAttemptsBeyondTheLimit() {
        for (int i = 0; i < MAX_ATTEMPTS; i++) {
            assertTrue(mfaAttempts.tryAttempt("user"));
        }

        assertFalse(mfaAttempts.tryAttempt("user"));
        assertTrue(mfaAttempts.tryAttempt("otherUser"));
    }

    @Test
    public void shouldAllowAttemptsAgainAfterAReset() {
        exhaust("user");

        mfaAttempts.reset("user");

        assertTrue(mfaAttempts.tryAttempt("user"));
    }

    @Test
    public void shouldAllowAttemptsAgainOnceTheRetentionHasElapsed() {
        exhaust("user");

        advance(RETENTION.plusSeconds(1));

        assertTrue(mfaAttempts.tryAttempt("user"));
    }

    @Test
    public void shouldNotExtendTheBlockWithTheAttemptsRefusedMeanwhile() {
        exhaust("user");

        advance(RETENTION.minusMinutes(1));
        assertFalse(mfaAttempts.tryAttempt("user"));
        advance(Duration.ofMinutes(2));

        assertTrue(mfaAttempts.tryAttempt("user"));
    }

    @Test
    public void shouldAllowNoMoreThanTheLimitWithConcurrentAttempts() throws InterruptedException {
        final int threads = 50;
        final var start = new CountDownLatch(1);
        final var allowed = new AtomicInteger();
        final var executor = Executors.newFixedThreadPool(threads);
        for (int i = 0; i < threads; i++) {
            executor.execute(() -> {
                try {
                    start.await();
                    if (mfaAttempts.tryAttempt("user")) {
                        allowed.incrementAndGet();
                    }
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        start.countDown();
        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(MAX_ATTEMPTS, allowed.get());
    }

    private void exhaust(final String principalId) {
        for (int i = 0; i <= MAX_ATTEMPTS; i++) {
            mfaAttempts.tryAttempt(principalId);
        }
        assertFalse(mfaAttempts.tryAttempt(principalId));
    }

    private void advance(final Duration duration) {
        now.addAndGet(duration.toNanos());
    }
}
