package fr.gouv.vitamui.commons.api.utils;

import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

//TODO remove this class. Duplicate
public class ServerIdentityConfigurationBuilder {

    public static ServerIdentityConfiguration setup(String identityName, String identityRole, int identityServerId,
            int identitySiteId) {
        ServerIdentityConfiguration serverIdentityConfiguration = initializeServerIdentity();
        serverIdentityConfiguration.setIdentityName(identityName);
        serverIdentityConfiguration.setIdentityRole(identityRole);
        serverIdentityConfiguration.setIdentityServerId(identityServerId);
        serverIdentityConfiguration.setIdentitySiteId(identitySiteId);
        return serverIdentityConfiguration;
    }

    private static ServerIdentityConfiguration initializeServerIdentity() {
        try {
            final Constructor<ServerIdentityConfiguration> c = ServerIdentityConfiguration.class
                    .getDeclaredConstructor();
            c.setAccessible(true);
            final ServerIdentityConfiguration newServerIdentityConfiguration = c.newInstance();

            final Field field = ServerIdentityConfiguration.class.getDeclaredField("serverIdentityConfiguration");
            field.setAccessible(true);

            final Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

            field.set(null, newServerIdentityConfiguration);
            return newServerIdentityConfiguration;
        }
        catch (Exception exception) {
            throw new RuntimeException("Unable to instantiate ServerIdentityConfiguration in a test context.");
        }
    }

}
