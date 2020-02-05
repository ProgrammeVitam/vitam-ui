package fr.gouv.vitamui.commons.api.config;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ServerIdentityConfigurationForProfiles {

    @Bean
    @Profile({ "ServerIdentityConfiguration-test" })
    public ServerIdentityConfiguration serverIdentityConfiguration()
            throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        final ServerIdentityConfiguration serverIdentityConfiguration = initializeServerIdentity();
        serverIdentityConfiguration.setIdentityName("identityName");
        serverIdentityConfiguration.setIdentityRole("identityRole");
        serverIdentityConfiguration.setIdentityServerId(1);
        serverIdentityConfiguration.setIdentitySiteId(0);
        return serverIdentityConfiguration;
    }

    private ServerIdentityConfiguration initializeServerIdentity()
            throws NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchFieldException {
        final Constructor<ServerIdentityConfiguration> c = ServerIdentityConfiguration.class.getDeclaredConstructor();
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

}
