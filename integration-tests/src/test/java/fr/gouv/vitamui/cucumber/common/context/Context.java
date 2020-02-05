package fr.gouv.vitamui.cucumber.common.context;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableAutoConfiguration
public class Context {

    @Bean
    public TestContext securityContext() {
        return new TestContext();
    }

}
