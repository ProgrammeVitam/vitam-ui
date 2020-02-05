package fr.gouv.vitamui.commons.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * This class starts a SpringBoot Application
 *
 *
 */

@SpringBootApplication
public class StartRestTestApplication {

    public static void main(final String[] args) {
        final SpringApplication app = new SpringApplication(RestTestApplication.class);
        app.run(args);
    }
}
