package fr.gouv.vitamui.commons.api;

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

@SpringBootApplication
@EnableAutoConfiguration
public class ApplicationTest {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationTest.class);
    private final ServerIdentityConfiguration serverIdentityConfiguration;

    @Autowired
    public ApplicationTest(final ServerIdentityConfiguration serverIdentityConfiguration) {
        this.serverIdentityConfiguration = serverIdentityConfiguration;
    }

    @PostConstruct
    private void init() {
        logger.debug("Spring Boot - active profile: {}.", System.getProperty("spring.profiles.active"));
        try {
            logger.debug("Spring Boot - Module: {}.", serverIdentityConfiguration.getIdentityRole());
            logger.debug(
                "Spring Boot - Logger Message prepend: {}.",
                serverIdentityConfiguration.getLoggerMessagePrepend()
            );
            logger.debug("Spring Boot : {}.", serverIdentityConfiguration);
        } catch (final InternalServerException | NullPointerException exception) {
            // do nothing
        }
    }

    public static void main(final String... args) {
        final ConfigurableApplicationContext ctx = SpringApplication.run(ApplicationTest.class, args);
        ctx.close();
    }
}
