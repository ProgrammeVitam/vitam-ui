package org.apereo.cas;

import org.apereo.cas.util.spring.boot.AbstractCasBanner;
import org.springframework.boot.Banner;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Copy/pasted from the original CAS class, removing the log4j2 stuffs and directly using a custom banner (avoid the search by reflection).
 */
public final class CasEmbeddedContainerUtils {
    /**
     * Property to dictate to the environment whether embedded container is running CAS.
     */
    public static final String EMBEDDED_CONTAINER_CONFIG_ACTIVE = "CasEmbeddedContainerConfigurationActive";

    private CasEmbeddedContainerUtils() {
    }

    /**
     * Gets runtime properties.
     *
     * @param embeddedContainerActive the embedded container active
     * @return the runtime properties
     */
    public static Map<String, Object> getRuntimeProperties(final Boolean embeddedContainerActive) {
        final Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(EMBEDDED_CONTAINER_CONFIG_ACTIVE, embeddedContainerActive);
        return properties;
    }

    /**
     * Gets cas banner instance.
     *
     * @return the cas banner instance
     */
    public static Banner getCasBannerInstance() {
        return new CustomCasBanner();
    }

    private static class CustomCasBanner extends AbstractCasBanner {

        @Override
        protected String getTitle() {
            return "(CAS VitamUI)";
        }
    }
}
