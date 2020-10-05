package fr.gouv.vitamui.commons.test.extension;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import fr.gouv.vitamui.commons.test.utils.ServerIdentityConfigurationBuilder;

/**
 * Extension for mock ServerIdentityConfiguration
 */
public class ServerIdentityExtension implements BeforeAllCallback {

    private static boolean isConfigured = false;

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        if (!isConfigured) {
            ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
            isConfigured = true;
        }
    }
}

