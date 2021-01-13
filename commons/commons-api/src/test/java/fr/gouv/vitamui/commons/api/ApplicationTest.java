package fr.gouv.vitamui.commons.api;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import fr.gouv.vitamui.commons.api.application.AbstractVitamUIApplication;
import fr.gouv.vitamui.commons.api.exception.InternalServerException;
import fr.gouv.vitamui.commons.api.identity.ServerIdentityConfiguration;

@SpringBootApplication
@EnableAutoConfiguration
public class ApplicationTest extends AbstractVitamUIApplication {

    private static Logger logger = LoggerFactory.getLogger(ApplicationTest.class);

    public static void main(final String... args) {
        final ConfigurableApplicationContext ctx = SpringApplication.run(ApplicationTest.class, args);
        ctx.close();
    }

    @PostConstruct
    private void init() {
        logger.debug("Spring Boot - active profile: {}.", System.getProperty("spring.profiles.active"));
        try {
            logger.debug("Spring Boot - Module: {}.", getModuleName());
            logger.debug("Spring Boot - Logger Message preprend: {}.",
                    ServerIdentityConfiguration.getInstance().getLoggerMessagePrepend());
            logger.debug("Spring Boot : {}.", ServerIdentityConfiguration.getInstance());
        }
        catch (final InternalServerException | NullPointerException exception) {
            // do nothing
        }
    }
}
