package fr.gouv.vitamui.commons.test.utils;

import org.junit.BeforeClass;

public abstract class AbstractServerIdentityBuilder {

    @BeforeClass
    public static void beforeClass() {

        ServerIdentityConfigurationBuilder.setup("identityName", "identityRole", 1, 0);
    }

}

