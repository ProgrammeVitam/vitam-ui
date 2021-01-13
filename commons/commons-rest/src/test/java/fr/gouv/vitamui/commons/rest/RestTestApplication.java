package fr.gouv.vitamui.commons.rest;

import fr.gouv.vitamui.commons.api.application.AbstractVitamUIApplication;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import fr.gouv.vitamui.commons.api.logger.VitamUILogger;
import fr.gouv.vitamui.commons.api.logger.VitamUILoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;

/**
 * This class implements a SpringBoot Application
 *
 *
 */

@SpringBootApplication
public class RestTestApplication extends AbstractVitamUIApplication {

    private static final VitamUILogger LOGGER = VitamUILoggerFactory.getInstance(RestTestApplication.class);

    public static void main(final String... args) {
        final ConfigurableApplicationContext ctx = SpringApplication.run(RestTestApplication.class, args);
        ctx.close();
    }

    @PostConstruct
    private void init() {
        LOGGER.debug("Spring Boot - active profile: {}.", System.getProperty("spring.profiles.active"));
        try {
            LOGGER.debug("Spring Boot - Module: {}.", getModuleName());
            LOGGER.debug("Spring Boot - Logger Message preprend: {}.",
                    ServerIdentityConfiguration.getInstance().getLoggerMessagePrepend());
            LOGGER.debug("Spring Boot : {}.", ServerIdentityConfiguration.getInstance());
        }
        catch (final InternalServerException | NullPointerException exception) {
            // do nothing
        }
    }
}
