package fr.gouv.vitamui.commons.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;

/**
 * This class implements a SpringBoot Application
 */

@SpringBootApplication
public class RestTestApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestTestApplication.class);

    public static void main(final String... args) {
        final ConfigurableApplicationContext ctx = SpringApplication.run(RestTestApplication.class, args);
        ctx.close();
    }

    @PostConstruct
    private void init() {
        LOGGER.debug("Spring Boot - active profile: {}.", System.getProperty("spring.profiles.active"));
    }
}
