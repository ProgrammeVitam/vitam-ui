package org.apereo.cas;

import lombok.experimental.UtilityClass;
import lombok.val;
import org.apereo.cas.util.spring.boot.AbstractCasBanner;
import org.springframework.boot.Banner;

import java.util.HashMap;
import java.util.Map;

/**
 * Copy/pasted from the original CAS class, directly using a custom banner (avoid the search by reflection).
 */
@UtilityClass
public class CasEmbeddedContainerUtils {
    /**
     * Property to dictate to the environment whether embedded container is running CAS.
     */
    public static final String EMBEDDED_CONTAINER_CONFIG_ACTIVE = "CasEmbeddedContainerConfigurationActive";

    /**
     * Gets runtime properties.
     *
     * @param embeddedContainerActive the embedded container active
     * @return the runtime properties
     */
    public static Map<String, Object> getRuntimeProperties(final Boolean embeddedContainerActive) {
        val properties = new HashMap<String, Object>();
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
            return "   _______           _____  __      ___ _                  _    _ _______  \n" +
                "  / / ____|   /\\    / ____| \\ \\    / (_) |                | |  | |_   _\\ \\ \n" +
                " | | |       /  \\  | (___    \\ \\  / / _| |_ __ _ _ __ ___ | |  | | | |  | |\n" +
                " | | |      / /\\ \\  \\___ \\    \\ \\/ / | | __/ _` | '_ ` _ \\| |  | | | |  | |\n" +
                " | | |____ / ____ \\ ____) |    \\  /  | | || (_| | | | | | | |__| |_| |_ | |\n" +
                " | |\\_____/_/    \\_\\_____/      \\/   |_|\\__\\__,_|_| |_| |_|\\____/|_____|| |\n" +
                "  \\_\\                                                                  /_/ \n" +
                "                                                                           \n";
        }
    }
}
