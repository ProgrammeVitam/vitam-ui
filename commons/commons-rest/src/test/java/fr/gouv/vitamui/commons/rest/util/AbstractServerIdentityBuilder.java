package fr.gouv.vitamui.commons.rest.util;

import org.junit.BeforeClass;

/**
 * @deprecated Do not use this class if you are using JUnit5
 * No longer need to redefine the server identity.
 * A JUnit Server Identity extension is globally registered {@link fr.gouv.vitamui.commons.test.extension.ServerIdentityExtension }) in the application.
 * It initializes a Server Identity before each test class.
 */
@Deprecated
public abstract class AbstractServerIdentityBuilder {

    @BeforeClass
    public static void beforeClass() {

        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

}

