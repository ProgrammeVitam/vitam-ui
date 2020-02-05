package fr.gouv.vitamui.cucumber.front.context;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContextFrontConfiguration {

    @Bean
    public ContextFront contextFront() {
        return new ContextFront();
    }
}
