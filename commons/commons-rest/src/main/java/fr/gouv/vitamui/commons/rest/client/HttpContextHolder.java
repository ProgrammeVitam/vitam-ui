package fr.gouv.vitamui.commons.rest.client;

import java.util.Optional;

/**
 * Thread-local holder for HttpContext.
 * Allows context propagation across API calls when the security context is
 * empty.
 */
public class HttpContextHolder {

    private static final ThreadLocal<HttpContext> CONTEXT = new ThreadLocal<>();

    /**
     * Set the current HttpContext.
     */
    public static void set(HttpContext context) {
        CONTEXT.set(context);
    }

    /**
     * Get the current HttpContext.
     */
    public static Optional<HttpContext> get() {
        return Optional.ofNullable(CONTEXT.get());
    }

    /**
     * Clear the current HttpContext.
     */
    public static void clear() {
        CONTEXT.remove();
    }

    /**
     * Set the current HttpContext in a scope that will be cleared automatically.
     */
    public static HttpContextScope setInScope(HttpContext context) {
        return new HttpContextScope(context);
    }

    /**
     * AutoCloseable scope for HttpContext.
     */
    public static class HttpContextScope implements AutoCloseable {

        public HttpContextScope(HttpContext context) {
            set(context);
        }

        @Override
        public void close() {
            clear();
        }
    }
}
