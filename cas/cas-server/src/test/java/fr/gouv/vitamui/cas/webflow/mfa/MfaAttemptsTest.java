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
