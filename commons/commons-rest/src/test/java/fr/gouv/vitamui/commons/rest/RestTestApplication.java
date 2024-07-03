package fr.gouv.vitamui.commons.rest;

import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;

/**
 * This class implements a SpringBoot Application
 */

@SpringBootApplication
@EnableAutoConfiguration
public class RestTestApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestTestApplication.class);
    private final ServerIdentityConfiguration serverIdentityConfiguration;

    @Autowired
    public RestTestApplication(final ServerIdentityConfiguration serverIdentityConfiguration) {
        this.serverIdentityConfiguration = serverIdentityConfiguration;
    }

    public static void main(final String... args) {
        final ConfigurableApplicationContext ctx = SpringApplication.run(RestTestApplication.class, args);
        ctx.close();
    }

    @PostConstruct
    private void init() {
        LOGGER.debug("Spring Boot - active profile: {}.", System.getProperty("spring.profiles.active"));
        try {
            LOGGER.debug("Spring Boot - Module: {}.", serverIdentityConfiguration.getIdentityRole());
            LOGGER.debug(
                "Spring Boot - Logger Message prepend: {}.",
                serverIdentityConfiguration.getLoggerMessagePrepend()
            );
            LOGGER.debug("Spring Boot : {}.", serverIdentityConfiguration);
        } catch (final InternalServerException | NullPointerException exception) {
            // do nothing
        }
    }
}
