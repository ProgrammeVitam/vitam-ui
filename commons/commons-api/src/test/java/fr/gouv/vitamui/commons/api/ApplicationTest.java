package fr.gouv.vitamui.commons.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class ApplicationTest {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationTest.class);

    @PostConstruct
    private void init() {
        logger.debug("Spring Boot - active profile: {}.", System.getProperty("spring.profiles.active"));
    }

    public static void main(final String... args) {
        final ConfigurableApplicationContext ctx = SpringApplication.run(ApplicationTest.class, args);
        ctx.close();
    }
}
